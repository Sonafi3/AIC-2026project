package com.zlagoda.dao;

import com.zlagoda.model.Product;
import com.zlagoda.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        // Повертає список усіх товарів супермаркету, відсортованих
        // за назвою в алфавітному порядку (ASC)
        String query = "SELECT * FROM Product ORDER BY product_name ASC";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id_product"),
                        rs.getInt("category_number"),
                        rs.getString("product_name"),
                        rs.getString("producer"),
                        rs.getString("characteristics")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean addProduct(Product product) {
        // Додаємо запис у таблицю Product
        String query = "INSERT INTO Product (id_product, category_number, product_name, producer, characteristics) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, product.getIdProduct());
            pstmt.setInt(2, product.getCategoryNumber());
            pstmt.setString(3, product.getProductName());
            pstmt.setString(4, product.getProducer());

            // Характеристики необов'язкові
            if (product.getCharacteristics() == null || product.getCharacteristics().isEmpty()) {
                pstmt.setNull(5, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(5, product.getCharacteristics());
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка додавання: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProduct(Product product) {
        // UPDATE за первинним ключем (id_product)
        // Оновлює назву, виробника, категорію та характеристики товару.
        // Первинний ключ (ID) не змінюється
        String query = "UPDATE Product SET category_number = ?, product_name = ?, producer = ?, characteristics = ? WHERE id_product = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, product.getCategoryNumber());
            pstmt.setString(2, product.getProductName());
            pstmt.setString(3, product.getProducer());

            if (product.getCharacteristics() == null || product.getCharacteristics().isEmpty()) {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(4, product.getCharacteristics());
            }
            pstmt.setInt(5, product.getIdProduct());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка оновлення: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteProduct(int idProduct) {
        String query = "DELETE FROM Product WHERE id_product = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idProduct);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}