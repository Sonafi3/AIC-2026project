package com.zlagoda.dao;

import com.zlagoda.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public List<String[]> getCategorySalesByPeriod(Timestamp startDate, Timestamp endDate) {
        List<String[]> results = new ArrayList<>();
        // Запит №1 для звіту: Продажі за категоріями за обраний період
        // Об'єднує 5 таблиць (Category -> Product -> Store_Product -> Sale -> Receipt),
        // фільтрує чеки за датою через BETWEEN за допомогою
        // параметрів користувача та групує результати за назвою категорії.
        // Функція SUM() підраховує точну кількість проданих одиниць у кожній групі.
        String sql = "SELECT c.category_name, SUM(s.product_number) AS total_sold " +
                "FROM Category c " +
                "INNER JOIN Product p ON c.category_number = p.category_number " +
                "INNER JOIN Store_Product sp ON p.id_product = sp.id_product " +
                "INNER JOIN Sale s ON sp.UPC = s.UPC " +
                "INNER JOIN Receipt r ON s.check_number = r.check_number " +
                "WHERE r.print_date >= ? AND r.print_date <= ? " +
                "GROUP BY c.category_name " +
                "ORDER BY total_sold DESC";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, startDate);
            pstmt.setTimestamp(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(new String[] {
                        rs.getString("category_name"),
                        String.valueOf(rs.getInt("total_sold"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<String[]> getCustomersWhoBoughtAllCategories() {
        List<String[]> results = new ArrayList<>();
        String sql = "SELECT cc.card_number, cc.cust_surname, cc.cust_name " +
                "FROM Customer_Card cc " +
                "WHERE NOT EXISTS (" +
                "    SELECT c.category_number FROM Category c " +
                "    WHERE NOT EXISTS (" +
                "        SELECT r.check_number " +
                "        FROM Receipt r " +
                "        INNER JOIN Sale s ON r.check_number = s.check_number " +
                "        INNER JOIN Store_Product sp ON s.UPC = sp.UPC " +
                "        INNER JOIN Product p ON sp.id_product = p.id_product " +
                "        WHERE r.card_number = cc.card_number " +
                "        AND p.category_number = c.category_number" +
                "    )" +
                ")";

        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(new String[] {
                        rs.getString("card_number"),
                        rs.getString("cust_surname") + " " + rs.getString("cust_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // Товари, які купували абсолютно всі VIP-клієнти (власники карток)
    public List<String[]> getProductsBoughtByAllVipCustomers() {
        List<String[]> data = new ArrayList<>();
        // Запит №2 для звіту: Пошук якірних товарів, які купили ВСІ VIP-клієнти
        String query = "SELECT p.id_product, p.product_name " +
                "FROM Product p " +
                "WHERE NOT EXISTS ( " +
                "    SELECT cc.card_number " +
                "    FROM Customer_Card cc " +
                "    WHERE NOT EXISTS ( " +
                "        SELECT 1 " +
                "        FROM Receipt r " +
                "        INNER JOIN Sale s ON r.check_number = s.check_number " +
                "        INNER JOIN Store_Product sp ON s.UPC = sp.UPC " +
                "        WHERE sp.id_product = p.id_product " +
                "          AND r.card_number = cc.card_number " +
                "    ) " +
                ")";

        try (Connection conn = com.zlagoda.util.Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                data.add(new String[] {
                        String.valueOf(rs.getInt("id_product")),
                        rs.getString("product_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}