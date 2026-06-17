package com.zlagoda.dao;

import com.zlagoda.model.StoreProduct;
import com.zlagoda.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreProductDAO {

    public boolean addStoreProduct(StoreProduct sp) {
        String query = "INSERT INTO Store_Product (UPC, UPC_prom, id_product, selling_price, products_number, promotional_product) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, sp.getUpc());
            if (sp.getUpcProm() != null && !sp.getUpcProm().trim().isEmpty()) {
                pstmt.setString(2, sp.getUpcProm());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }
            pstmt.setInt(3, sp.getIdProduct());
            pstmt.setBigDecimal(4, sp.getSellingPrice());
            pstmt.setInt(5, sp.getProductsNumber());
            pstmt.setBoolean(6, sp.isPromotionalProduct());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<StoreProduct> getAllStoreProducts() {
        List<StoreProduct> storeProducts = new ArrayList<>();
        String query = "SELECT * FROM Store_Product ORDER BY products_number ASC";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                storeProducts.add(new StoreProduct(
                        rs.getString("UPC"),
                        rs.getString("UPC_prom"),
                        rs.getInt("id_product"),
                        rs.getBigDecimal("selling_price"),
                        rs.getInt("products_number"),
                        rs.getBoolean("promotional_product")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storeProducts;
    }

    public boolean updateStoreProduct(com.zlagoda.model.StoreProduct storeProduct) {
        String query = "UPDATE Store_Product SET UPC_prom = ?, id_product = ?, selling_price = ?, products_number = ?, promotional_product = ? WHERE UPC = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (storeProduct.getUpcProm() == null || storeProduct.getUpcProm().isEmpty()) {
                pstmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(1, storeProduct.getUpcProm());
            }
            pstmt.setInt(2, storeProduct.getIdProduct());
            pstmt.setBigDecimal(3, storeProduct.getSellingPrice());
            pstmt.setInt(4, storeProduct.getProductsNumber());
            pstmt.setBoolean(5, storeProduct.isPromotionalProduct());
            pstmt.setString(6, storeProduct.getUpc());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні товару в магазині: " + e.getMessage());
            return false;
        }
    }

    // Видалення товару з магазину (або зняття з продажу, якщо він є у чеках)
    public boolean deleteStoreProduct(String upc) {
        // Зняття товару з продажу (видалення з полиць магазину)
        // Soft Delete на основі логічної перевірки SELECT
        // Спочатку через SELECT COUNT(*) перевіряється, чи продавався
        // цей товар (чи є він у чеках). Якщо ТАК — виконується UPDATE (кількість
        // стає 0), щоб товар зник з каси, але зберігся у старих чеках покупців.
        // Якщо НІ (товар новий і не продавався) — виконується фізичний DELETE.
        String checkSaleQuery = "SELECT COUNT(*) FROM Sale WHERE UPC = ?";
        String updateQuantityQuery = "UPDATE Store_Product SET products_number = 0 WHERE UPC = ?";
        String deleteQuery = "DELETE FROM Store_Product WHERE UPC = ?";

        try (Connection conn = Database.getConnection()) {
            // 1. Перевіряємо, чи товар вже продавався (чи є він у чеках)
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSaleQuery)) {
                checkStmt.setString(1, upc);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Товар Є в чеках! Фізично видаляти не можна.
                    // М'яке видалення: ставимо залишок 0 (він зникне з каси)
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuantityQuery)) {
                        updateStmt.setString(1, upc);
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }

            // 2. Якщо товару немає в жодному чеку - безпечно видаляємо його повністю
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setString(1, upc);
                return deleteStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Помилка при знятті товару з продажу: " + e.getMessage());
            return false;
        }
    }
}