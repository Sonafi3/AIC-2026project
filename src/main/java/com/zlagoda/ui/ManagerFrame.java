package com.zlagoda.ui;

import com.zlagoda.dao.ReportDAO;
import com.zlagoda.dao.CategoryDAO;
import com.zlagoda.dao.ProductDAO;
import com.zlagoda.model.Category;
import com.zlagoda.model.Employee;
import com.zlagoda.model.Product;
import com.zlagoda.model.Receipt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerFrame extends JFrame {

    private Employee loggedInManager;
    private JTabbedPane tabbedPane;

    public ManagerFrame(Employee manager) {
        this.loggedInManager = manager;

        setTitle("ZLAGODA - Панель Менеджера (" + manager.getEmplSurname() + " " + manager.getEmplName() + ")");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Панель керування",
                SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton logoutButton = new JButton("Вийти з акаунту");
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });

        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.out.println("Іконку не знайдено, використовується стандартна.");
        }

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Категорії", createCategoryPanel());
        tabbedPane.addTab("Товари", createProductPanel());
        tabbedPane.addTab("Товари в магазині", createStoreProductPanel());
        tabbedPane.addTab("Працівники", createEmployeePanel());
        tabbedPane.addTab("Клієнти", createCustomerPanel());
        tabbedPane.addTab("Чеки", createReceiptPanel());
        tabbedPane.addTab("Аналітика та Звіти", createReportsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        com.zlagoda.dao.CategoryDAO categoryDAO = new com.zlagoda.dao.CategoryDAO();

        String[] columnNames = { "Номер категорії", "Назва категорії" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadCategories = () -> {
            tableModel.setRowCount(0);
            for (Category cat : categoryDAO.getAllCategories()) {
                tableModel.addRow(new Object[] { cat.getCategoryNumber(), cat.getCategoryName() });
            }
        };
        loadCategories.run();

        JPanel bottomPanel = new JPanel();
        JButton addButton = new JButton("Додати");
        JButton editButton = new JButton("Редагувати");
        JButton deleteButton = new JButton("Видалити");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Дія: Додати
        addButton.addActionListener(e -> {
            JTextField idField = new JTextField(5);
            JTextField nameField = new JTextField(15);
            JPanel inputPanel = new JPanel();
            inputPanel.add(new JLabel("Номер:"));
            inputPanel.add(idField);
            inputPanel.add(new JLabel("Назва:"));
            inputPanel.add(nameField);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Нова категорія",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int id = Integer.parseInt(idField.getText().trim());
                    String name = nameField.getText().trim();
                    if (!name.isEmpty() && categoryDAO.addCategory(new Category(id, name))) {
                        loadCategories.run();
                        JOptionPane.showMessageDialog(this, "Успішно додано!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Помилка додавання!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Номер має бути числом!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Дія: Редагувати
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть категорію для редагування!");
                return;
            }

            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String oldName = (String) tableModel.getValueAt(selectedRow, 1);

            JTextField nameField = new JTextField(oldName, 15);
            JPanel inputPanel = new JPanel();
            inputPanel.add(new JLabel("Нова назва:"));
            inputPanel.add(nameField);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Редагування категорії ID: " + id,
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                if (!newName.isEmpty() && categoryDAO.updateCategory(new Category(id, newName))) {
                    loadCategories.run();
                    JOptionPane.showMessageDialog(this, "Категорію оновлено!");
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка оновлення!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Дія: Видалити
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть категорію для видалення!");
                return;
            }

            int id = (int) tableModel.getValueAt(selectedRow, 0);
            if (JOptionPane.showConfirmDialog(this, "Точно видалити категорію ID: " + id + "?", "Підтвердження",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (categoryDAO.deleteCategory(id)) {
                    loadCategories.run();
                    JOptionPane.showMessageDialog(this, "Категорію видалено!");
                } else {
                    JOptionPane.showMessageDialog(this, "Не можна видалити категорію, до якої прив'язані товари!",
                            "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    // --- Вкладка "Товари" ---
    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        com.zlagoda.dao.ProductDAO productDAO = new com.zlagoda.dao.ProductDAO();
        com.zlagoda.dao.CategoryDAO categoryDAO = new com.zlagoda.dao.CategoryDAO();

        String[] columnNames = { "ID Товару", "Назва", "Виробник", "Категорія", "Характеристики" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadProducts = () -> {
            tableModel.setRowCount(0);
            java.util.List<Category> categories = categoryDAO.getAllCategories();
            java.util.Map<Integer, String> catMap = new java.util.HashMap<>();
            for (Category c : categories)
                catMap.put(c.getCategoryNumber(), c.getCategoryName());

            for (Product p : productDAO.getAllProducts()) {
                tableModel.addRow(new Object[] {
                        p.getIdProduct(), p.getProductName(), p.getProducer(),
                        catMap.getOrDefault(p.getCategoryNumber(), "Невідомо"),
                        p.getCharacteristics() == null ? "" : p.getCharacteristics()
                });
            }
        };
        loadProducts.run();

        JPanel bottomPanel = new JPanel();
        JButton addButton = new JButton("Додати товар");
        JButton editButton = new JButton("Редагувати");
        JButton deleteButton = new JButton("Видалити");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        JButton printButton = new JButton("🖨 Друк");

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(printButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        printButton.addActionListener(e -> printReport(table, "Довідник Товарів"));

        addButton.addActionListener(e -> {
            JTextField idField = new JTextField(10);
            JTextField nameField = new JTextField(15);
            JTextField prodField = new JTextField(15);
            JTextField charField = new JTextField(20);

            JComboBox<String> categoryCombo = new JComboBox<>();
            for (Category c : categoryDAO.getAllCategories())
                categoryCombo.addItem(c.getCategoryNumber() + " - " + c.getCategoryName());

            JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
            inputPanel.add(new JLabel("ID Товару (число):"));
            inputPanel.add(idField);
            inputPanel.add(new JLabel("Назва товару:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("Виробник:"));
            inputPanel.add(prodField);
            inputPanel.add(new JLabel("Категорія:"));
            inputPanel.add(categoryCombo);
            inputPanel.add(new JLabel("Характеристики (необов'язково):"));
            inputPanel.add(charField);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Новий товар",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int id = Integer.parseInt(idField.getText().trim());
                    String name = nameField.getText().trim();
                    String prod = prodField.getText().trim();
                    String chars = charField.getText().trim();
                    int catId = Integer.parseInt(((String) categoryCombo.getSelectedItem()).split(" - ")[0]);

                    if (name.isEmpty() || prod.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Назва та виробник є обов'язковими!", "Помилка",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (productDAO.addProduct(new Product(id, catId, name, prod, chars))) {
                        loadProducts.run();
                    } else {
                        JOptionPane.showMessageDialog(this, "Помилка! Можливо, товар з таким ID вже існує.", "Помилка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "ID має бути числом!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1)
                return;

            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String currentName = (String) tableModel.getValueAt(selectedRow, 1);
            String currentProd = (String) tableModel.getValueAt(selectedRow, 2);
            String currentCategoryName = (String) tableModel.getValueAt(selectedRow, 3);
            String currentChars = (String) tableModel.getValueAt(selectedRow, 4);

            JTextField nameField = new JTextField(currentName, 15);
            JTextField prodField = new JTextField(currentProd, 15);
            JTextField charField = new JTextField(currentChars, 20);

            JComboBox<String> categoryCombo = new JComboBox<>();
            for (Category c : categoryDAO.getAllCategories()) {
                String item = c.getCategoryNumber() + " - " + c.getCategoryName();
                categoryCombo.addItem(item);
                if (c.getCategoryName().equals(currentCategoryName))
                    categoryCombo.setSelectedItem(item);
            }

            JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
            inputPanel.add(new JLabel("Назва товару:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("Виробник:"));
            inputPanel.add(prodField);
            inputPanel.add(new JLabel("Категорія:"));
            inputPanel.add(categoryCombo);
            inputPanel.add(new JLabel("Характеристики:"));
            inputPanel.add(charField);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Редагування товару",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                String newProd = prodField.getText().trim();
                String newChars = charField.getText().trim();
                int newCatId = Integer.parseInt(((String) categoryCombo.getSelectedItem()).split(" - ")[0]);

                if (newName.isEmpty() || newProd.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Назва та виробник обов'язкові!");
                    return;
                }

                if (productDAO.updateProduct(new Product(id, newCatId, newName, newProd, newChars))) {
                    loadProducts.run();
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1)
                return;
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            if (JOptionPane.showConfirmDialog(this, "Точно видалити?", "Підтвердження",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (productDAO.deleteProduct(id))
                    loadProducts.run();
                else
                    JOptionPane.showMessageDialog(this, "Неможливо видалити товар! Спочатку зніміть його з продажу.",
                            "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createStoreProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        com.zlagoda.dao.StoreProductDAO storeProductDAO = new com.zlagoda.dao.StoreProductDAO();
        com.zlagoda.dao.ProductDAO productDAO = new com.zlagoda.dao.ProductDAO();

        String[] columnNames = { "UPC", "UPC акційного", "Товар", "Ціна (грн)", "Кількість", "Акція" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadStoreProducts = () -> {
            tableModel.setRowCount(0);
            List<Product> products = productDAO.getAllProducts();
            Map<Integer, String> productMap = new HashMap<>();
            for (Product p : products) {
                productMap.put(p.getIdProduct(), p.getProductName());
            }

            for (com.zlagoda.model.StoreProduct sp : storeProductDAO.getAllStoreProducts()) {
                String prodName = productMap.getOrDefault(sp.getIdProduct(), "Невідомо");
                tableModel.addRow(new Object[] {
                        sp.getUpc(),
                        sp.getUpcProm() == null ? "-" : sp.getUpcProm(),
                        prodName,
                        sp.getSellingPrice(),
                        sp.getProductsNumber(),
                        sp.isPromotionalProduct() ? "Так" : "Ні"
                });
            }
        };
        loadStoreProducts.run();

        JPanel bottomPanel = new JPanel();
        JButton addButton = new JButton("Додати на полицю");
        JButton editButton = new JButton("Редагувати");
        JButton deleteButton = new JButton("Зняти з продажу");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Дія: Додати товар на полицю
        addButton.addActionListener(e -> {
            JTextField upcField = new JTextField(12);
            JTextField upcPromField = new JTextField(12);
            JTextField priceField = new JTextField(8);
            JTextField countField = new JTextField(5);
            JCheckBox promoCheck = new JCheckBox("Акційний товар");

            // ДРУЖНІЙ ІНТЕРФЕЙС: Якщо товар акційний, йому не потрібен UPC іншого акційного
            promoCheck.addActionListener(ev -> {
                if (promoCheck.isSelected()) {
                    upcPromField.setText("");
                    upcPromField.setEnabled(false);
                    upcPromField.setToolTipText("Акційний товар не може посилатися на інший акційний товар");
                } else {
                    upcPromField.setEnabled(true);
                    upcPromField.setToolTipText(null);
                }
            });

            JComboBox<String> productCombo = new JComboBox<>();
            for (Product p : productDAO.getAllProducts()) {
                productCombo.addItem(p.getIdProduct() + " - " + p.getProductName());
            }

            JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
            inputPanel.add(new JLabel("UPC штрих-код:"));
            inputPanel.add(upcField);
            inputPanel.add(new JLabel("UPC акційного (якщо є):"));
            inputPanel.add(upcPromField);
            inputPanel.add(new JLabel("Оберіть товар:"));
            inputPanel.add(productCombo);
            inputPanel.add(new JLabel("Ціна продажу:"));
            inputPanel.add(priceField);
            inputPanel.add(new JLabel("Кількість одиниць:"));
            inputPanel.add(countField);
            inputPanel.add(new JLabel("Статус:"));
            inputPanel.add(promoCheck);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Виставити товар",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    String upc = upcField.getText().trim();
                    String upcProm = upcPromField.getText().trim().isEmpty() ? null : upcPromField.getText().trim();
                    java.math.BigDecimal price = new java.math.BigDecimal(priceField.getText().trim());
                    int count = Integer.parseInt(countField.getText().trim());
                    boolean isPromo = promoCheck.isSelected();

                    if (upc.isEmpty() || productCombo.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(this, "UPC та Товар є обов'язковими!", "Помилка",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int prodId = Integer.parseInt(((String) productCombo.getSelectedItem()).split(" - ")[0]);

                    com.zlagoda.model.StoreProduct newSP = new com.zlagoda.model.StoreProduct(upc, upcProm, prodId,
                            price, count, isPromo);
                    if (storeProductDAO.addStoreProduct(newSP)) {
                        loadStoreProducts.run();
                        JOptionPane.showMessageDialog(this, "Товар виставлено на продаж!");
                    } else {
                        // РОЗШИРЕНЕ ПОВІДОМЛЕННЯ ПРО ПОМИЛКУ
                        JOptionPane.showMessageDialog(this,
                                "Помилка бази даних!\nМожливі причини:\n1. Товар з таким UPC вже існує.\n2. Ви вказали 'UPC акційного', якого ще немає в базі (спочатку створіть акційний товар!).",
                                "Помилка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Невірний формат ціни або кількості!", "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Дія: Редагувати
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть партію товару для редагування!");
                return;
            }

            String upc = (String) tableModel.getValueAt(selectedRow, 0);
            String currentUpcProm = (String) tableModel.getValueAt(selectedRow, 1);
            String currentProductName = (String) tableModel.getValueAt(selectedRow, 2);
            java.math.BigDecimal currentPrice = (java.math.BigDecimal) tableModel.getValueAt(selectedRow, 3);
            int currentCount = (int) tableModel.getValueAt(selectedRow, 4);
            boolean currentPromo = tableModel.getValueAt(selectedRow, 5).equals("Так");

            JTextField upcPromField = new JTextField(currentUpcProm.equals("-") ? "" : currentUpcProm, 12);
            JTextField priceField = new JTextField(currentPrice.toString(), 8);
            JTextField countField = new JTextField(String.valueOf(currentCount), 5);
            JCheckBox promoCheck = new JCheckBox("Акційний товар?", currentPromo);

            // Блокування поля при редагуванні
            upcPromField.setEnabled(!currentPromo);
            promoCheck.addActionListener(ev -> {
                if (promoCheck.isSelected()) {
                    upcPromField.setText("");
                    upcPromField.setEnabled(false);
                } else {
                    upcPromField.setEnabled(true);
                }
            });

            JComboBox<String> productCombo = new JComboBox<>();
            for (Product p : productDAO.getAllProducts()) {
                String item = p.getIdProduct() + " - " + p.getProductName();
                productCombo.addItem(item);
                if (p.getProductName().equals(currentProductName)) {
                    productCombo.setSelectedItem(item);
                }
            }

            JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
            inputPanel.add(new JLabel("UPC штрих-код:"));
            inputPanel.add(new JLabel("<html><b>" + upc + "</b> (Не змінюється)</html>"));
            inputPanel.add(new JLabel("UPC акційного:"));
            inputPanel.add(upcPromField);
            inputPanel.add(new JLabel("Оберіть товар:"));
            inputPanel.add(productCombo);
            inputPanel.add(new JLabel("Нова ціна:"));
            inputPanel.add(priceField);
            inputPanel.add(new JLabel("Кількість одиниць:"));
            inputPanel.add(countField);
            inputPanel.add(new JLabel("Статус:"));
            inputPanel.add(promoCheck);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Редагування товару UPC: " + upc,
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    String upcProm = upcPromField.getText().trim().isEmpty() ? null : upcPromField.getText().trim();
                    java.math.BigDecimal price = new java.math.BigDecimal(priceField.getText().trim());
                    int count = Integer.parseInt(countField.getText().trim());
                    boolean isPromo = promoCheck.isSelected();
                    int prodId = Integer.parseInt(((String) productCombo.getSelectedItem()).split(" - ")[0]);

                    if (storeProductDAO.updateStoreProduct(
                            new com.zlagoda.model.StoreProduct(upc, upcProm, prodId, price, count, isPromo))) {
                        loadStoreProducts.run();
                        JOptionPane.showMessageDialog(this, "Партію товару успішно оновлено!");
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Помилка оновлення! Переконайтеся, що 'UPC акційного' дійсно існує в базі.", "Помилка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Невірний формат ціни або кількості!", "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть партію товару для видалення!");
                return;
            }

            String upc = (String) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 2);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Ви впевнені, що хочете повністю зняти з продажу (видалити):\n" + name + " (UPC: " + upc + ")?",
                    "Підтвердження", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (storeProductDAO.deleteStoreProduct(upc)) {
                    loadStoreProducts.run();
                    JOptionPane.showMessageDialog(this,
                            "Товар успішно знято з продажу!\n(Якщо він був у старих чеках, його залишок змінено на 0).",
                            "Успіх", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка при знятті з продажу.", "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private void printReport(JTable table, String reportTitle) {
        try {
            // Формуємо верхній та нижній колонтитули
            java.text.MessageFormat header = new java.text.MessageFormat(
                    "Міні-супермаркет ZLAGODA | Звіт: " + reportTitle);
            java.text.MessageFormat footer = new java.text.MessageFormat("Сторінка {0} | Згенеровано: "
                    + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date()));

            // Виклик системного вікна друку (попередній перегляд), таблиця на всю ширину
            // сторінки (FIT_WIDTH)
            boolean complete = table.print(JTable.PrintMode.FIT_WIDTH, header, footer, true, null, true, null);

            if (complete) {
                JOptionPane.showMessageDialog(this, "Друк успішно завершено!", "Друк", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.awt.print.PrinterException pe) {
            JOptionPane.showMessageDialog(this, "Помилка при створенні звіту: " + pe.getMessage(), "Помилка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Вкладка "Аналітика та Звіти" (Твої складні запити) ---
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        com.zlagoda.dao.ReportDAO reportDAO = new com.zlagoda.dao.ReportDAO();

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        JButton categorySalesButton = new JButton("Продажі за категоріями (Період)");
        JButton anchorProductsButton = new JButton("Якірні товари (Куплені всіма VIP)");

        JButton printButton = new JButton("🖨 Друк");
        printButton.setBackground(new Color(0, 123, 255));
        printButton.setForeground(Color.WHITE);

        JButton downloadButton = new JButton("💾 Завантажити CSV");
        downloadButton.setBackground(new Color(40, 167, 69)); // Зелений колір
        downloadButton.setForeground(Color.WHITE);

        controlPanel.add(categorySalesButton);
        controlPanel.add(anchorProductsButton);
        controlPanel.add(printButton);
        controlPanel.add(downloadButton); // Додаємо на панель
        panel.add(controlPanel, BorderLayout.NORTH);

        // Таблиця для виводу результатів
        DefaultTableModel resultTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable resultTable = new JTable(resultTableModel);
        panel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        // Змінна для збереження назви поточного звіту (для колонтитулів при друці)
        final String[] currentReportTitle = { "Порожній звіт" };

        // Логіка 1-го запиту (Продажі за категоріями)
        // Логіка 1-го запиту (Продажі за категоріями)
        categorySalesButton.addActionListener(e -> {
            com.toedter.calendar.JDateChooser startChooser = new com.toedter.calendar.JDateChooser(
                    new java.util.Date());
            startChooser.setDateFormatString("yyyy-MM-dd");
            com.toedter.calendar.JDateChooser endChooser = new com.toedter.calendar.JDateChooser(new java.util.Date());
            endChooser.setDateFormatString("yyyy-MM-dd");

            JPanel datePanel = new JPanel();
            datePanel.add(new JLabel("Початок:"));
            datePanel.add(startChooser);
            datePanel.add(new JLabel("Кінець:"));
            datePanel.add(endChooser);

            if (JOptionPane.showConfirmDialog(this, datePanel, "Оберіть період",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    if (startChooser.getDate() == null || endChooser.getDate() == null)
                        return;

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    String startStr = sdf.format(startChooser.getDate());
                    String endStr = sdf.format(endChooser.getDate());

                    java.sql.Timestamp start = java.sql.Timestamp.valueOf(startStr + " 00:00:00");
                    java.sql.Timestamp end = java.sql.Timestamp.valueOf(endStr + " 23:59:59");

                    java.util.List<String[]> data = reportDAO.getCategorySalesByPeriod(start, end);

                    resultTableModel.setColumnIdentifiers(new String[] { "Назва категорії", "Продано одиниць товару" });
                    resultTableModel.setRowCount(0);
                    for (String[] row : data)
                        resultTableModel.addRow(row);

                    currentReportTitle[0] = "Продажі за категоріями (" + startStr + " - " + endStr + ")";
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка зчитування дат!", "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Логіка 2-го запиту (VIP-клієнти)
        anchorProductsButton.addActionListener(e -> {
            java.util.List<String[]> data = reportDAO.getProductsBoughtByAllVipCustomers();

            resultTableModel.setColumnIdentifiers(new String[] { "ID Товару", "Назва товару" });
            resultTableModel.setRowCount(0);

            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Немає товарів, які б купили абсолютно всі VIP-клієнти.");
            } else {
                for (String[] row : data)
                    resultTableModel.addRow(row);
            }
            currentReportTitle[0] = "Якірні товари VIP-клієнтів (Куплені всіма власниками карток)";
        });

        // Дія: Друк поточного звіту
        printButton.addActionListener(e -> {
            if (resultTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Таблиця порожня! Спочатку згенеруйте звіт (натисніть одну з кнопок).", "Увага",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            printReport(resultTable, currentReportTitle[0]); // Виклик нашого універсального методу друку
        });

        // Дія: Завантажити поточний звіт
        downloadButton.addActionListener(e -> {
            if (resultTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Таблиця порожня! Спочатку згенеруйте звіт.", "Увага",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            exportTableToCSV(resultTable, currentReportTitle[0]);
        });

        return panel;
    }

    // --- Вкладка "Працівники" (З календарями та повним CRUD) ---
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        com.zlagoda.dao.EmployeeDAO employeeDAO = new com.zlagoda.dao.EmployeeDAO();

        String[] columnNames = { "ID", "ПІБ", "Посада", "Телефон", "Зарплата", "Початок роботи" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadData = () -> {
            tableModel.setRowCount(0);
            for (Employee emp : employeeDAO.getAllEmployees()) {
                String pib = emp.getEmplSurname() + " " + emp.getEmplName();
                tableModel.addRow(new Object[] { emp.getIdEmployee(), pib, emp.getEmplRole(), emp.getPhoneNumber(),
                        emp.getSalary(), emp.getDateOfStart() });
            }
        };
        loadData.run();

        JPanel bottomPanel = new JPanel();
        JButton addButton = new JButton("Найняти");
        JButton editButton = new JButton("Редагувати дані");
        JButton deleteButton = new JButton("Звільнити");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        JButton printButton = new JButton("🖨 Друк звіту");

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(printButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        printButton.addActionListener(e -> printReport(table, "Список працівників"));

        // Додавання (Найм) працівника
        addButton.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField surnameField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField patField = new JTextField();
            JComboBox<String> roleCombo = new JComboBox<>(new String[] { "Касир", "Менеджер" });
            JTextField salaryField = new JTextField("15000.00");

            com.toedter.calendar.JDateChooser dobChooser = new com.toedter.calendar.JDateChooser();
            com.toedter.calendar.JDateChooser startChooser = new com.toedter.calendar.JDateChooser(
                    new java.util.Date()); // Сьогодні

            JTextField phoneField = new JTextField("+380");
            JTextField cityField = new JTextField("Київ");
            JTextField streetField = new JTextField();
            JTextField zipField = new JTextField();
            JPasswordField passField = new JPasswordField();

            JPanel inputPanel = new JPanel(new GridLayout(13, 2, 5, 5));
            inputPanel.add(new JLabel("ID (напр. CAS002):"));
            inputPanel.add(idField);
            inputPanel.add(new JLabel("Прізвище:"));
            inputPanel.add(surnameField);
            inputPanel.add(new JLabel("Ім'я:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("По батькові:"));
            inputPanel.add(patField);
            inputPanel.add(new JLabel("Посада:"));
            inputPanel.add(roleCombo);
            inputPanel.add(new JLabel("Зарплата (грн):"));
            inputPanel.add(salaryField);
            inputPanel.add(new JLabel("Дата народження:"));
            inputPanel.add(dobChooser);
            inputPanel.add(new JLabel("Дата початку роботи:"));
            inputPanel.add(startChooser);
            inputPanel.add(new JLabel("Телефон:"));
            inputPanel.add(phoneField);
            inputPanel.add(new JLabel("Місто:"));
            inputPanel.add(cityField);
            inputPanel.add(new JLabel("Вулиця:"));
            inputPanel.add(streetField);
            inputPanel.add(new JLabel("Індекс:"));
            inputPanel.add(zipField);
            inputPanel.add(new JLabel("Пароль:"));
            inputPanel.add(passField);

            JScrollPane scrollPane = new JScrollPane(inputPanel);
            scrollPane.setPreferredSize(new Dimension(400, 400));

            if (JOptionPane.showConfirmDialog(this, scrollPane, "Анкета нового працівника",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    if (dobChooser.getDate() == null || startChooser.getDate() == null) {
                        JOptionPane.showMessageDialog(this, "Оберіть дати в календарі!", "Помилка",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String hashedPass = com.zlagoda.util.PasswordUtil.hashPassword(new String(passField.getPassword()));

                    Employee newEmp = new Employee(
                            idField.getText().trim(), surnameField.getText().trim(), nameField.getText().trim(),
                            patField.getText().trim(), (String) roleCombo.getSelectedItem(),
                            new java.math.BigDecimal(salaryField.getText().trim()),
                            new java.sql.Date(dobChooser.getDate().getTime()),
                            new java.sql.Date(startChooser.getDate().getTime()),
                            phoneField.getText().trim(), cityField.getText().trim(),
                            streetField.getText().trim(), zipField.getText().trim(), hashedPass);

                    if (employeeDAO.addEmployee(newEmp)) {
                        JOptionPane.showMessageDialog(this, "Працівника успішно найнято!");
                        loadData.run();
                    } else {
                        JOptionPane.showMessageDialog(this, "Помилка бази даних! Можливо такий ID вже існує.",
                                "Помилка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Перевірте правильність введених даних (особливо зарплату).",
                            "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Редагування працівника (Пароль тут не міняється з міркувань безпеки, лише
        // анкета)
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть працівника у таблиці!");
                return;
            }

            String empId = (String) tableModel.getValueAt(selectedRow, 0);
            Employee currentEmp = employeeDAO.getAllEmployees().stream()
                    .filter(emp -> emp.getIdEmployee().equals(empId)).findFirst().orElse(null);

            if (currentEmp == null)
                return;

            JTextField surnameField = new JTextField(currentEmp.getEmplSurname());
            JTextField nameField = new JTextField(currentEmp.getEmplName());
            JTextField patField = new JTextField(
                    currentEmp.getEmplPatronymic() != null ? currentEmp.getEmplPatronymic() : "");
            JComboBox<String> roleCombo = new JComboBox<>(new String[] { "Касир", "Менеджер" });
            roleCombo.setSelectedItem(currentEmp.getEmplRole());
            JTextField salaryField = new JTextField(currentEmp.getSalary().toString());

            com.toedter.calendar.JDateChooser dobChooser = new com.toedter.calendar.JDateChooser(
                    currentEmp.getDateOfBirth());
            com.toedter.calendar.JDateChooser startChooser = new com.toedter.calendar.JDateChooser(
                    currentEmp.getDateOfStart());

            JTextField phoneField = new JTextField(currentEmp.getPhoneNumber());
            JTextField cityField = new JTextField(currentEmp.getCity());
            JTextField streetField = new JTextField(currentEmp.getStreet());
            JTextField zipField = new JTextField(currentEmp.getZipCode());

            JPanel inputPanel = new JPanel(new GridLayout(12, 2, 5, 5));
            inputPanel.add(new JLabel("ID:"));
            inputPanel.add(new JLabel("<html><b>" + empId + "</b></html>"));
            inputPanel.add(new JLabel("Прізвище:"));
            inputPanel.add(surnameField);
            inputPanel.add(new JLabel("Ім'я:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("По батькові:"));
            inputPanel.add(patField);
            inputPanel.add(new JLabel("Посада:"));
            inputPanel.add(roleCombo);
            inputPanel.add(new JLabel("Зарплата (грн):"));
            inputPanel.add(salaryField);
            inputPanel.add(new JLabel("Дата народження:"));
            inputPanel.add(dobChooser);
            inputPanel.add(new JLabel("Дата початку роботи:"));
            inputPanel.add(startChooser);
            inputPanel.add(new JLabel("Телефон:"));
            inputPanel.add(phoneField);
            inputPanel.add(new JLabel("Місто:"));
            inputPanel.add(cityField);
            inputPanel.add(new JLabel("Вулиця:"));
            inputPanel.add(streetField);
            inputPanel.add(new JLabel("Індекс:"));
            inputPanel.add(zipField);

            JScrollPane scrollPane = new JScrollPane(inputPanel);
            scrollPane.setPreferredSize(new Dimension(400, 380));

            if (JOptionPane.showConfirmDialog(this, scrollPane, "Редагування даних працівника",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    currentEmp.setEmplSurname(surnameField.getText().trim());
                    currentEmp.setEmplName(nameField.getText().trim());
                    currentEmp.setEmplPatronymic(patField.getText().trim());
                    currentEmp.setEmplRole((String) roleCombo.getSelectedItem());
                    currentEmp.setSalary(new java.math.BigDecimal(salaryField.getText().trim()));
                    currentEmp.setDateOfBirth(new java.sql.Date(dobChooser.getDate().getTime()));
                    currentEmp.setDateOfStart(new java.sql.Date(startChooser.getDate().getTime()));
                    currentEmp.setPhoneNumber(phoneField.getText().trim());
                    currentEmp.setCity(cityField.getText().trim());
                    currentEmp.setStreet(streetField.getText().trim());
                    currentEmp.setZipCode(zipField.getText().trim());

                    if (employeeDAO.updateEmployee(currentEmp)) {
                        JOptionPane.showMessageDialog(this, "Дані працівника оновлено!");
                        loadData.run();
                    } else {
                        JOptionPane.showMessageDialog(this, "Помилка оновлення!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Перевірте формат введених даних.", "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Видалення
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть працівника у таблиці!");
                return;
            }
            String empId = (String) tableModel.getValueAt(selectedRow, 0);
            if (empId.equals(loggedInManager.getIdEmployee())) {
                JOptionPane.showMessageDialog(this, "Ви не можете звільнити самі себе!", "Увага",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Точно звільнити працівника " + empId + "?", "Підтвердження",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (employeeDAO.deleteEmployee(empId)) {
                    JOptionPane.showMessageDialog(this, "Працівника успішно звільнено!");
                    loadData.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка! Можливо касир має прив'язані чеки.", "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        com.zlagoda.dao.CustomerCardDAO cardDAO = new com.zlagoda.dao.CustomerCardDAO();

        String[] columnNames = { "Номер карти", "ПІБ", "Телефон", "Місто", "Вулиця", "Індекс", "Знижка (%)" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadCards = () -> {
            tableModel.setRowCount(0);
            for (com.zlagoda.model.CustomerCard card : cardDAO.getAllCustomerCards()) {
                String pib = card.getCustSurname() + " " + card.getCustName() + " "
                        + (card.getCustPatronymic() != null ? card.getCustPatronymic() : "");
                tableModel.addRow(new Object[] {
                        card.getCardNumber(), pib.trim(), card.getPhoneNumber(),
                        card.getCity(), card.getStreet(), card.getZipCode(), card.getPercent()
                });
            }
        };
        loadCards.run();

        JPanel bottomPanel = new JPanel();
        JButton addButton = new JButton("Додати клієнта");
        JButton editButton = new JButton("Редагувати");
        JButton deleteButton = new JButton("Видалити");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            JTextField cardField = new JTextField(13);
            JTextField surnameField = new JTextField(15);
            JTextField nameField = new JTextField(15);
            JTextField patField = new JTextField(15);
            JTextField phoneField = new JTextField(13);
            JTextField cityField = new JTextField(15);
            JTextField streetField = new JTextField(15);
            JTextField zipField = new JTextField(9);
            JTextField percentField = new JTextField("5", 5); // Знижка за замовчуванням 5%

            JPanel inputPanel = new JPanel(new GridLayout(9, 2, 5, 5));
            inputPanel.add(new JLabel("Номер карти (напр. CARD001):"));
            inputPanel.add(cardField);
            inputPanel.add(new JLabel("Прізвище:"));
            inputPanel.add(surnameField);
            inputPanel.add(new JLabel("Ім'я:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("По батькові:"));
            inputPanel.add(patField);
            inputPanel.add(new JLabel("Телефон:"));
            inputPanel.add(phoneField);
            inputPanel.add(new JLabel("Місто:"));
            inputPanel.add(cityField);
            inputPanel.add(new JLabel("Вулиця:"));
            inputPanel.add(streetField);
            inputPanel.add(new JLabel("Індекс:"));
            inputPanel.add(zipField);
            inputPanel.add(new JLabel("Відсоток знижки (%):"));
            inputPanel.add(percentField);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Нова карта клієнта",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int percent = Integer.parseInt(percentField.getText().trim());
                    com.zlagoda.model.CustomerCard newCard = new com.zlagoda.model.CustomerCard(
                            cardField.getText().trim(), surnameField.getText().trim(), nameField.getText().trim(),
                            patField.getText().trim().isEmpty() ? null : patField.getText().trim(),
                            phoneField.getText().trim(), cityField.getText().trim(), streetField.getText().trim(),
                            zipField.getText().trim(), percent);
                    if (cardDAO.addCustomerCard(newCard)) {
                        loadCards.run();
                        JOptionPane.showMessageDialog(this, "Клієнта додано!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Помилка при збереженні!", "Помилка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Невірний формат даних!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть клієнта у таблиці!");
                return;
            }

            String cardNum = (String) tableModel.getValueAt(selectedRow, 0);

            com.zlagoda.model.CustomerCard currentCard = cardDAO.getAllCustomerCards().stream()
                    .filter(c -> c.getCardNumber().equals(cardNum)).findFirst().orElse(null);

            if (currentCard == null)
                return;

            JTextField surnameField = new JTextField(currentCard.getCustSurname(), 15);
            JTextField nameField = new JTextField(currentCard.getCustName(), 15);
            JTextField patField = new JTextField(
                    currentCard.getCustPatronymic() == null ? "" : currentCard.getCustPatronymic(), 15);
            JTextField phoneField = new JTextField(currentCard.getPhoneNumber(), 13);
            JTextField cityField = new JTextField(currentCard.getCity(), 15);
            JTextField streetField = new JTextField(currentCard.getStreet(), 15);
            JTextField zipField = new JTextField(currentCard.getZipCode(), 9);
            JTextField percentField = new JTextField(String.valueOf(currentCard.getPercent()), 5);

            JPanel inputPanel = new JPanel(new GridLayout(9, 2, 5, 5));
            inputPanel.add(new JLabel("Номер карти:"));
            inputPanel.add(new JLabel("<html><b>" + cardNum + "</b></html>"));
            inputPanel.add(new JLabel("Прізвище:"));
            inputPanel.add(surnameField);
            inputPanel.add(new JLabel("Ім'я:"));
            inputPanel.add(nameField);
            inputPanel.add(new JLabel("По батькові:"));
            inputPanel.add(patField);
            inputPanel.add(new JLabel("Телефон:"));
            inputPanel.add(phoneField);
            inputPanel.add(new JLabel("Місто:"));
            inputPanel.add(cityField);
            inputPanel.add(new JLabel("Вулиця:"));
            inputPanel.add(streetField);
            inputPanel.add(new JLabel("Індекс:"));
            inputPanel.add(zipField);
            inputPanel.add(new JLabel("Відсоток знижки (%):"));
            inputPanel.add(percentField);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Редагування карти клієнта",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int percent = Integer.parseInt(percentField.getText().trim());
                    currentCard.setCustSurname(surnameField.getText().trim());
                    currentCard.setCustName(nameField.getText().trim());
                    currentCard
                            .setCustPatronymic(patField.getText().trim().isEmpty() ? null : patField.getText().trim());
                    currentCard.setPhoneNumber(phoneField.getText().trim());
                    currentCard.setCity(cityField.getText().trim());
                    currentCard.setStreet(streetField.getText().trim());
                    currentCard.setZipCode(zipField.getText().trim());
                    currentCard.setPercent(percent);

                    if (cardDAO.updateCustomerCard(currentCard)) {
                        loadCards.run();
                        JOptionPane.showMessageDialog(this, "Дані клієнта оновлено!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Помилка при оновленні!", "Помилка",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Невірний формат даних!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть клієнта у таблиці!");
                return;
            }

            String cardNum = (String) tableModel.getValueAt(selectedRow, 0);
            if (JOptionPane.showConfirmDialog(this, "Точно видалити карту: " + cardNum + "?", "Підтвердження",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (cardDAO.deleteCustomerCard(cardNum)) {
                    loadCards.run();
                    JOptionPane.showMessageDialog(this,
                            "Карту клієнта видалено!\n(Історія покупок за цією картою збережена).", "Успіх",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка при видаленні карти.", "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    // --- Вкладка "Чеки" (Управління історією транзакцій для Менеджера) ---
    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        com.zlagoda.dao.ReceiptDAO receiptDAO = new com.zlagoda.dao.ReceiptDAO();

        String[] columnNames = { "Номер чека", "Касир (ID)", "Картка клієнта", "Дата", "Сума (грн)", "ПДВ (грн)" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadReceipts = () -> {
            tableModel.setRowCount(0);
            for (Receipt r : receiptDAO.getAllReceipts()) {
                tableModel.addRow(new Object[] {
                        r.getCheckNumber(), r.getIdEmployee(),
                        r.getCardNumber() == null ? "Немає" : r.getCardNumber(),
                        r.getPrintDate().toString(), r.getSumTotal(), r.getVat()
                });
            }
        };
        loadReceipts.run();

        JPanel bottomPanel = new JPanel();
        JButton detailsButton = new JButton("Переглянути товари");
        JButton deleteButton = new JButton("Видалити чек");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        JButton printButton = new JButton("🖨 Друк звіту");

        bottomPanel.add(detailsButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(printButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Дія: Перегляд товарів
        detailsButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть чек у таблиці!");
                return;
            }
            String checkNum = (String) tableModel.getValueAt(selectedRow, 0);
            java.util.List<String[]> details = receiptDAO.getSalesDetailsByCheck(checkNum);

            String[] detColumns = { "Назва товару", "Кількість", "Ціна продажу", "Разом" };
            DefaultTableModel detModel = new DefaultTableModel(detColumns, 0);
            for (String[] rowData : details)
                detModel.addRow(rowData);

            JTable detTable = new JTable(detModel);
            JScrollPane detScroll = new JScrollPane(detTable);
            detScroll.setPreferredSize(new Dimension(500, 250));
            JOptionPane.showMessageDialog(this, detScroll, "Склад чека " + checkNum, JOptionPane.PLAIN_MESSAGE);
        });

        // Дія: Видалення чека (Доступно лише Менеджеру)
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть чек для видалення!");
                return;
            }
            String checkNum = (String) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Точно видалити чек " + checkNum
                            + "?\nУВАГА: Ця дія автоматично видалить інформацію про всі продані товари в цьому чеку!",
                    "Підтвердження видалення", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (receiptDAO.deleteReceipt(checkNum)) {
                    JOptionPane.showMessageDialog(this, "Чек успішно видалено!");
                    loadReceipts.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка при видаленні чека!", "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        printButton.addActionListener(e -> printReport(table, "Історія всіх чеків міні-супермаркету"));

        return panel;
    }

    // --- МЕТОД ДЛЯ ЗАВАНТАЖЕННЯ ЗВІТУ (ЕКСПОРТ У CSV ДЛЯ EXCEL) ---
    private void exportTableToCSV(JTable table, String defaultTitle) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Зберегти звіт");
        // Очищаємо назву від зайвих символів для імені файлу
        String safeTitle = defaultTitle.replaceAll("[^a-zA-Z0-9а-яА-ЯіІїЇєЄ]", "_");
        fileChooser.setSelectedFile(new java.io.File(safeTitle + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(fileChooser.getSelectedFile()), "UTF-8"))) {

                // Додаємо BOM (Byte Order Mark), щоб Excel правильно читав кирилицю (українську
                // мову)
                writer.write('\ufeff');

                javax.swing.table.TableModel model = table.getModel();
                int colCount = model.getColumnCount();

                // Записуємо заголовки колонок
                for (int i = 0; i < colCount; i++) {
                    writer.print("\"" + model.getColumnName(i) + "\"");
                    if (i < colCount - 1)
                        writer.print(";");
                }
                writer.println();

                // Записуємо дані з таблиці
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object value = model.getValueAt(i, j);
                        String strValue = (value != null) ? value.toString().replace("\"", "\"\"") : "";
                        writer.print("\"" + strValue + "\"");
                        if (j < colCount - 1)
                            writer.print(";");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(this, "Звіт успішно збережено на комп'ютер!", "Успіх",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка при збереженні файлу: " + ex.getMessage(), "Помилка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}