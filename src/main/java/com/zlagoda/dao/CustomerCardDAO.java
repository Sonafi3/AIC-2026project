package com.zlagoda.dao;

import com.zlagoda.model.CustomerCard;
import com.zlagoda.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerCardDAO {

    public List<CustomerCard> getAllCustomerCards() {
        List<CustomerCard> cards = new ArrayList<>();
        String query = "SELECT * FROM Customer_Card ORDER BY cust_surname ASC";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                cards.add(new CustomerCard(
                        rs.getString("card_number"),
                        rs.getString("cust_surname"),
                        rs.getString("cust_name"),
                        rs.getString("cust_patronymic"),
                        rs.getString("phone_number"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip_code"),
                        rs.getInt("percent")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    public boolean addCustomerCard(CustomerCard card) {
        String query = "INSERT INTO Customer_Card (card_number, cust_surname, cust_name, cust_patronymic, phone_number, city, street, zip_code, percent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, card.getCardNumber());
            pstmt.setString(2, card.getCustSurname());
            pstmt.setString(3, card.getCustName());
            pstmt.setString(4, card.getCustPatronymic() == null || card.getCustPatronymic().isEmpty() ? null
                    : card.getCustPatronymic());
            pstmt.setString(5, card.getPhoneNumber());
            pstmt.setString(6, card.getCity());
            pstmt.setString(7, card.getStreet());
            pstmt.setString(8, card.getZipCode());
            pstmt.setInt(9, card.getPercent());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при додаванні карти клієнта: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCustomerCard(CustomerCard card) {
        String query = "UPDATE Customer_Card SET cust_surname=?, cust_name=?, cust_patronymic=?, phone_number=?, city=?, street=?, zip_code=?, percent=? WHERE card_number=?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, card.getCustSurname());
            pstmt.setString(2, card.getCustName());
            pstmt.setString(3, card.getCustPatronymic() == null || card.getCustPatronymic().isEmpty() ? null
                    : card.getCustPatronymic());
            pstmt.setString(4, card.getPhoneNumber());
            pstmt.setString(5, card.getCity());
            pstmt.setString(6, card.getStreet());
            pstmt.setString(7, card.getZipCode());
            pstmt.setInt(8, card.getPercent());
            pstmt.setString(9, card.getCardNumber());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні карти клієнта: " + e.getMessage());
            return false;
        }
    }

    // Видалення карти клієнта (з безпечним збереженням історії чеків)
    public boolean deleteCustomerCard(String cardNumber) {
        // Анулювання (видалення) дисконтної картки клієнта
        // UPDATE + DELETE
        // Щоб не порушувати цілісність даних,
        // запит спочатку ставить card_number = NULL в усіх чеках, де фігурувала
        // ця картка (історія покупок при цьому зберігається), а потім видаляє
        // саму картку з таблиці Customer_Card.
        String updateReceiptsQuery = "UPDATE Receipt SET card_number = NULL WHERE card_number = ?";
        String deleteCardQuery = "DELETE FROM Customer_Card WHERE card_number = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Початок транзакції
            try (PreparedStatement pstmtReceipts = conn.prepareStatement(updateReceiptsQuery);
                    PreparedStatement pstmtCard = conn.prepareStatement(deleteCardQuery)) {

                // 1. Відв'язуємо карту від старих чеків (щоб не втратити фінансову історію)
                pstmtReceipts.setString(1, cardNumber);
                pstmtReceipts.executeUpdate();

                // 2. Видаляємо саму карту з бази
                pstmtCard.setString(1, cardNumber);
                int rows = pstmtCard.executeUpdate();

                conn.commit(); // Підтверджуємо зміни
                return rows > 0;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Помилка транзакції при видаленні карти: " + ex.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Помилка з'єднання: " + e.getMessage());
            return false;
        }
    }
}