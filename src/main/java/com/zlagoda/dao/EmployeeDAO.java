package com.zlagoda.dao;

import com.zlagoda.model.Employee;
import com.zlagoda.util.Database;
import com.zlagoda.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public Employee authenticate(String idEmployee, String password) {
        String query = "SELECT * FROM Employee WHERE id_employee = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, idEmployee);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (com.zlagoda.util.PasswordUtil.checkPassword(password, storedHash)) {

                        return new Employee(
                                rs.getString("id_employee"),
                                rs.getString("empl_surname"),
                                rs.getString("empl_name"),
                                rs.getString("empl_patronymic"),
                                rs.getString("empl_role"),
                                rs.getBigDecimal("salary"),
                                rs.getDate("date_of_birth"),
                                rs.getDate("date_of_start"),
                                rs.getString("phone_number"),
                                rs.getString("city"),
                                rs.getString("street"),
                                rs.getString("zip_code"),
                                storedHash);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка авторизації: " + e.getMessage());
        }
        return null;
    }

    public boolean addEmployee(Employee emp) {
        String query = "INSERT INTO Employee (id_employee, empl_surname, empl_name, empl_patronymic, " +
                "empl_role, salary, date_of_birth, date_of_start, phone_number, city, street, zip_code, password_hash) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, emp.getIdEmployee());
            pstmt.setString(2, emp.getEmplSurname());
            pstmt.setString(3, emp.getEmplName());
            pstmt.setString(4, emp.getEmplPatronymic());
            pstmt.setString(5, emp.getEmplRole());
            pstmt.setBigDecimal(6, emp.getSalary());
            pstmt.setDate(7, emp.getDateOfBirth());
            pstmt.setDate(8, emp.getDateOfStart());
            pstmt.setString(9, emp.getPhoneNumber());
            pstmt.setString(10, emp.getCity());
            pstmt.setString(11, emp.getStreet());
            pstmt.setString(12, emp.getZipCode());
            pstmt.setString(13, emp.getPasswordHash()); // Тут вже має бути захешований пароль

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Помилка при додаванні працівника: " + e.getMessage());
            return false;
        }
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM Employee ORDER BY empl_surname ASC";

        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Employee emp = new Employee(
                        rs.getString("id_employee"),
                        rs.getString("empl_surname"),
                        rs.getString("empl_name"),
                        rs.getString("empl_patronymic"),
                        rs.getString("empl_role"),
                        rs.getBigDecimal("salary"),
                        rs.getDate("date_of_birth"),
                        rs.getDate("date_of_start"),
                        rs.getString("phone_number"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip_code"),
                        rs.getString("password_hash"));
                employees.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    // Звільнення працівника зі збереженням історії його чеків (переприв'язка до
    // SYS000)
    public boolean deleteEmployee(String idEmployee) {
        // Захист: системного працівника видаляти не можна
        if ("SYS000".equals(idEmployee)) {
            System.err.println("Спроба видалити системний акаунт заборонена.");
            return false;
        }

        // Замість DELETE тепер робим UPDATE для чеків
        String reassignReceiptsQuery = "UPDATE Receipt SET id_employee = 'SYS000' WHERE id_employee = ?";
        String deleteEmployeeQuery = "DELETE FROM Employee WHERE id_employee = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // Початок транзакції

            try (PreparedStatement pstmtReceipts = conn.prepareStatement(reassignReceiptsQuery);
                    PreparedStatement pstmtEmployee = conn.prepareStatement(deleteEmployeeQuery)) {

                // 1. Переприв'язуємо всі чеки цього касира на системний акаунт 'SYS000'
                pstmtReceipts.setString(1, idEmployee);
                pstmtReceipts.executeUpdate();

                // 2. Тепер безпечно звільняємо (видаляємо) самого працівника
                pstmtEmployee.setString(1, idEmployee);
                int rows = pstmtEmployee.executeUpdate();

                conn.commit(); // Підтверджуємо транзакцію
                return rows > 0;

            } catch (SQLException ex) {
                conn.rollback(); // Відкат у разі помилки
                System.err.println("Помилка транзакції при звільненні: " + ex.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Помилка з'єднання: " + e.getMessage());
            return false;
        }
    }

    // Редагування (оновлення) даних працівника
    public boolean updateEmployee(Employee emp) {
        // UPDATE за первинним ключем (id_employee)
        // Використовується менеджером для зміни анкети підлеглого
        // або касиром для самостійного оновлення контактів (телефон, адреса)
        String query = "UPDATE Employee SET empl_surname=?, empl_name=?, empl_patronymic=?, empl_role=?, salary=?, date_of_birth=?, date_of_start=?, phone_number=?, city=?, street=?, zip_code=? WHERE id_employee=?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, emp.getEmplSurname());
            pstmt.setString(2, emp.getEmplName());
            pstmt.setString(3, emp.getEmplPatronymic() == null || emp.getEmplPatronymic().isEmpty() ? null
                    : emp.getEmplPatronymic());
            pstmt.setString(4, emp.getEmplRole());
            pstmt.setBigDecimal(5, emp.getSalary());
            pstmt.setDate(6, emp.getDateOfBirth());
            pstmt.setDate(7, emp.getDateOfStart());
            pstmt.setString(8, emp.getPhoneNumber());
            pstmt.setString(9, emp.getCity());
            pstmt.setString(10, emp.getStreet());
            pstmt.setString(11, emp.getZipCode());
            pstmt.setString(12, emp.getIdEmployee());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні працівника: " + e.getMessage());
            return false;
        }
    }
}