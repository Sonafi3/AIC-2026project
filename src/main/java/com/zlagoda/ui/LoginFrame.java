package com.zlagoda.ui;

import com.zlagoda.dao.EmployeeDAO;
import com.zlagoda.model.Employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private EmployeeDAO employeeDAO;

    public LoginFrame() {
        employeeDAO = new EmployeeDAO();

        setTitle("ZLAGODA - Авторизація");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception ex) {
            System.out.println("Іконку не знайдено, використовується стандартна.");
        }

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        inputPanel.add(new JLabel("ID Працівника (Логін):"));
        loginField = new JTextField();
        inputPanel.add(loginField);

        inputPanel.add(new JLabel("Пароль:"));
        passwordField = new JPasswordField();
        inputPanel.add(passwordField);

        loginButton = new JButton("Увійти");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));

        loginButton.addActionListener(this::handleLogin);

        add(inputPanel, BorderLayout.CENTER);
        add(loginButton, BorderLayout.SOUTH);
    }

    private void handleLogin(ActionEvent e) {
        String idEmployee = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (idEmployee.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Будь ласка, заповніть всі поля!", "Помилка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Employee loggedInUser = employeeDAO.authenticate(idEmployee, password);

        if (loggedInUser != null) {
            this.dispose();

            if ("Менеджер".equalsIgnoreCase(loggedInUser.getEmplRole())) {
                new ManagerFrame(loggedInUser).setVisible(true);
            } else if ("Касир".equalsIgnoreCase(loggedInUser.getEmplRole())) {
                new CashierFrame(loggedInUser).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Невідома роль працівника!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Невірний логін або пароль!", "Помилка доступу",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}