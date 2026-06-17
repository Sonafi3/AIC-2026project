package com.zlagoda.dao;

import com.zlagoda.model.Receipt;
import com.zlagoda.model.Sale;
import com.zlagoda.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReceiptDAO {

    public boolean addReceiptWithSales(Receipt receipt, List<Sale> sales) {
        // Оформлення покупки (створення чека та запис проданих товарів)
        // Алгоритм дій:
        // 1. Створюється "шапка" чека в таблиці Receipt.
        // 2. У циклі записуються всі куплені товари в таблицю Sale.
        // 3. Для кожного проданого товару одразу робиться UPDATE таблиці
        // Store_Product (віднімається продана кількість від залишку на полиці).
        // Якщо хоча б на одному етапі стається збій (наприклад не вистачає товару),
        // спрацьовує ROLLBACK, і чек повністю скасовується.
        String insertReceiptQuery = "INSERT INTO Receipt (check_number, id_employee, card_number, print_date, sum_total, vat) VALUES (?, ?, ?, ?, ?, ?)";
        String insertSaleQuery = "INSERT INTO Sale (UPC, check_number, product_number, selling_price) VALUES (?, ?, ?, ?)";
        String updateStoreProductQuery = "UPDATE Store_Product SET products_number = products_number - ? WHERE UPC = ?";

        Connection conn = null;

        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtReceipt = conn.prepareStatement(insertReceiptQuery)) {
                pstmtReceipt.setString(1, receipt.getCheckNumber());
                pstmtReceipt.setString(2, receipt.getIdEmployee());

                if (receipt.getCardNumber() != null && !receipt.getCardNumber().isEmpty()) {
                    pstmtReceipt.setString(3, receipt.getCardNumber());
                } else {
                    pstmtReceipt.setNull(3, Types.VARCHAR);
                }

                pstmtReceipt.setTimestamp(4, receipt.getPrintDate());
                pstmtReceipt.setBigDecimal(5, receipt.getSumTotal());
                pstmtReceipt.setBigDecimal(6, receipt.getVat());
                pstmtReceipt.executeUpdate();
            }

            try (PreparedStatement pstmtSale = conn.prepareStatement(insertSaleQuery);
                    PreparedStatement pstmtUpdate = conn.prepareStatement(updateStoreProductQuery)) {

                for (Sale sale : sales) {
                    pstmtSale.setString(1, sale.getUpc());
                    pstmtSale.setString(2, receipt.getCheckNumber());
                    pstmtSale.setInt(3, sale.getProductNumber());
                    pstmtSale.setBigDecimal(4, sale.getSellingPrice());
                    pstmtSale.addBatch();

                    pstmtUpdate.setInt(1, sale.getProductNumber());
                    pstmtUpdate.setString(2, sale.getUpc());
                    pstmtUpdate.addBatch();
                }

                pstmtSale.executeBatch();
                pstmtUpdate.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("Помилка транзакції. Відкат змін...");
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<com.zlagoda.model.Receipt> getReceiptsByCashierAndPeriod(String idEmployee, java.sql.Timestamp start,
            java.sql.Timestamp end) {
        List<com.zlagoda.model.Receipt> receipts = new ArrayList<>();
        // Фільтрація чеків конкретного касира за обраний час
        // Використовується касиром у вкладці "Історія чеків" для
        // перегляду своєї зміни. Відбирає лише ті чеки, які пробив поточний
        // співробітник (id_employee = ?) у межах визначених дат (BETWEEN ? AND ?).
        String sql = "SELECT * FROM Receipt WHERE id_employee = ? AND print_date >= ? AND print_date <= ? ORDER BY print_date DESC";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idEmployee);
            pstmt.setTimestamp(2, start);
            pstmt.setTimestamp(3, end);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    receipts.add(new com.zlagoda.model.Receipt(
                            rs.getString("check_number"),
                            rs.getString("id_employee"),
                            rs.getString("card_number"),
                            rs.getTimestamp("print_date"),
                            rs.getBigDecimal("sum_total"),
                            rs.getBigDecimal("vat")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receipts;
    }

    public List<String[]> getSalesDetailsByCheck(String checkNumber) {
        List<String[]> details = new ArrayList<>();
        // Перегляд детального складу конкретного чека
        // Щоб менеджер побачив не просто штрих-коди, а нормальні
        // назви товарів, запит об'єднує таблиці Sale (продажі), Store_Product
        // (партія на касі) та Product (довідник з іменами)
        String sql = "SELECT p.product_name, s.product_number, s.selling_price, (s.product_number * s.selling_price) AS row_total "
                +
                "FROM Sale s " +
                "INNER JOIN Store_Product sp ON s.UPC = sp.UPC " +
                "INNER JOIN Product p ON sp.id_product = p.id_product " +
                "WHERE s.check_number = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, checkNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    details.add(new String[] {
                            rs.getString("product_name"),
                            String.valueOf(rs.getInt("product_number")),
                            rs.getBigDecimal("selling_price").toString() + " грн",
                            rs.getBigDecimal("row_total").toString() + " грн"
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    // Отримати взагалі всі чеки (для менеджера)
    public List<com.zlagoda.model.Receipt> getAllReceipts() {
        List<com.zlagoda.model.Receipt> receipts = new ArrayList<>();
        // Отримання історії всіх чеків супермаркету
        // SELECT із сортуванням за датою
        // Сортує чеки від найновіших до найстаріших (DESC) за часом друку
        String sql = "SELECT * FROM Receipt ORDER BY print_date DESC";

        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                receipts.add(new com.zlagoda.model.Receipt(
                        rs.getString("check_number"),
                        rs.getString("id_employee"),
                        rs.getString("card_number"),
                        rs.getTimestamp("print_date"),
                        rs.getBigDecimal("sum_total"),
                        rs.getBigDecimal("vat")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receipts;
    }

    // Видалення чека за номером (для Менеджера)
    public boolean deleteReceipt(String checkNumber) {
        // Фізичний DELETE
        // Видаляє запис із таблиці Receipt. На рівні бази даних для
        // таблиці Sale (Продані товари) налаштовано ON DELETE CASCADE,
        // тому всі товари, що належали цьому чеку, видаляються автоматично.
        String sql = "DELETE FROM Receipt WHERE check_number = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, checkNumber);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Помилка при видаленні чека: " + e.getMessage());
            return false;
        }
    }
}