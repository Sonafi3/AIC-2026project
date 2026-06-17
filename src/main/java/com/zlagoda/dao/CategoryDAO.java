package com.zlagoda.dao;

import com.zlagoda.model.Category;
import com.zlagoda.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public boolean addCategory(Category category) {
        String query = "INSERT INTO Category (category_number, category_name) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, category.getCategoryNumber());
            pstmt.setString(2, category.getCategoryName());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Запит з сортуванням за назвою згідно з вимогами
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        // Отримання списку всіх категорій
        // Витягує всі категорії товарів з бази даних
        String query = "SELECT * FROM Category ORDER BY category_name ASC";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_number"),
                        rs.getString("category_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public boolean updateCategory(Category category) {
        String query = "UPDATE Category SET category_name = ? WHERE category_number = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, category.getCategoryName());
            pstmt.setInt(2, category.getCategoryNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні категорії: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCategory(int categoryNumber) {
        String query = "DELETE FROM Category WHERE category_number = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, categoryNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні категорії: " + e.getMessage());
            return false;
        }
    }
}