package com.zlagoda.ui;

import com.zlagoda.dao.CategoryDAO;
import com.zlagoda.dao.CustomerCardDAO;
import com.zlagoda.dao.ProductDAO;
import com.zlagoda.dao.ReceiptDAO;
import com.zlagoda.dao.StoreProductDAO;
import com.zlagoda.model.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CashierFrame extends JFrame {

    private Employee loggedInCashier;
    private JTabbedPane tabbedPane;

    private List<Sale> cartSales = new ArrayList<>();
    private DefaultTableModel cartTableModel;
    private JLabel totalSumLabel;
    private JLabel vatLabel;
    private JComboBox<String> customerCardCombo;
    private JComboBox<String> storeProductCombo;

    private CustomerCardDAO cardDAO = new CustomerCardDAO();
    private ReceiptDAO receiptDAO = new ReceiptDAO();

    public CashierFrame(Employee cashier) {
        this.loggedInCashier = cashier;

        setTitle("ZLAGODA - Робоче місце касира (" + cashier.getEmplSurname() + " " + cashier.getEmplName() + ")");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception ex) {
            System.out.println("Іконку не знайдено.");
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel(
                "Касовий апарат. Касир: " + cashier.getEmplSurname() + " " + cashier.getEmplName(),
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

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Каса (Створення чека)", createCheckoutPanel());
        tabbedPane.addTab("Довідник товарів", createProductSearchPanel()); // НОВА ВКЛАДКА ПОШУКУ ТОВАРІВ
        tabbedPane.addTab("Постійні клієнти", createCustomerPanel());
        tabbedPane.addTab("Історія чеків", createReceiptHistoryPanel());
        tabbedPane.addTab("Мій профіль", createProfilePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- Вкладка "Довідник товарів" (Живий пошук та фільтрація) ---
    private JPanel createProductSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        StoreProductDAO spDAO = new StoreProductDAO();
        ProductDAO pDAO = new ProductDAO();
        CategoryDAO cDAO = new CategoryDAO();

        // Верхня панель з фільтрами
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JTextField searchField = new JTextField(15);

        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.addItem("Всі категорії");
        List<Category> categories = cDAO.getAllCategories();
        for (Category c : categories) {
            categoryCombo.addItem(c.getCategoryName());
        }

        JComboBox<String> promoCombo = new JComboBox<>(
                new String[] { "Всі товари", "Тільки акційні", "Тільки не акційні" });

        filterPanel.add(new JLabel("Пошук (Назва/UPC):"));
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Категорія:"));
        filterPanel.add(categoryCombo);
        filterPanel.add(new JLabel("Тип:"));
        filterPanel.add(promoCombo);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Таблиця
        String[] columnNames = { "UPC", "Назва", "Категорія", "Ціна (грн)", "Залишок", "Акція" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Завантаження даних
        Map<Integer, Product> productMap = pDAO.getAllProducts().stream()
                .collect(Collectors.toMap(Product::getIdProduct, p -> p));
        Map<Integer, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryNumber, Category::getCategoryName));

        for (StoreProduct sp : spDAO.getAllStoreProducts()) {
            Product p = productMap.get(sp.getIdProduct());
            String catName = (p != null) ? categoryMap.getOrDefault(p.getCategoryNumber(), "Невідомо") : "Невідомо";
            String pName = (p != null) ? p.getProductName() : "Невідомо";

            tableModel.addRow(new Object[] {
                    sp.getUpc(), pName, catName, sp.getSellingPrice(), sp.getProductsNumber(),
                    sp.isPromotionalProduct() ? "Так" : "Ні"
            });
        }

        // Налаштування фільтрації та сортування (TableRowSorter)
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // За замовчуванням сортуємо за колонкою "Назва" (індекс 1)
        sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));

        Runnable applyFilters = () -> {
            List<RowFilter<Object, Object>> filters = new ArrayList<>();

            // 1. Пошук за назвою або UPC (Колонки 0 і 1)
            String text = searchField.getText().trim();
            if (!text.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + text, 0, 1));
            }

            // 2. Фільтр за категорією (Колонка 2)
            String cat = (String) categoryCombo.getSelectedItem();
            if (!"Всі категорії".equals(cat)) {
                // Використовуємо Pattern.quote щоб уникнути помилок з спецсимволами в назвах
                filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(cat) + "$", 2));
            }

            // 3. Фільтр за акцією (Колонка 5)
            String promo = (String) promoCombo.getSelectedItem();
            if ("Тільки акційні".equals(promo)) {
                filters.add(RowFilter.regexFilter("^Так$", 5));
            } else if ("Тільки не акційні".equals(promo)) {
                filters.add(RowFilter.regexFilter("^Ні$", 5));
            }

            sorter.setRowFilter(RowFilter.andFilter(filters));
        };

        // Слухачі подій для миттєвого оновлення
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilters.run();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilters.run();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilters.run();
            }
        });
        categoryCombo.addActionListener(e -> applyFilters.run());
        promoCombo.addActionListener(e -> applyFilters.run());

        return panel;
    }

    private JPanel createCheckoutPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // --- ЛІВА ПАНЕЛЬ (Пошук та додавання) ---
        JPanel leftPanel = new JPanel(new GridLayout(8, 1, 5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Додати товар"));
        leftPanel.setPreferredSize(new Dimension(350, 0));

        JTextField searchField = new JTextField();
        storeProductCombo = new JComboBox<>();
        refreshStoreProductsCombo(""); // Оновлений виклик з порожнім фільтром

        // Живий пошук: реагує на кожну введену літеру
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshStoreProductsCombo(searchField.getText());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshStoreProductsCombo(searchField.getText());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshStoreProductsCombo(searchField.getText());
            }
        });

        JTextField qtyField = new JTextField("1");
        JButton addToCartButton = new JButton("Додати в кошик");

        leftPanel.add(new JLabel("Пошук (Назва або UPC):"));
        leftPanel.add(searchField);
        leftPanel.add(new JLabel("Оберіть товар:"));
        leftPanel.add(storeProductCombo);
        leftPanel.add(new JLabel("Кількість:"));
        leftPanel.add(qtyField);
        leftPanel.add(new JLabel(""));
        leftPanel.add(addToCartButton);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // --- ЦЕНТРАЛЬНА ПАНЕЛЬ ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Поточний чек"));

        String[] columnNames = { "UPC", "Назва", "Ціна", "К-сть", "Сума" };
        cartTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable cartTable = new JTable(cartTableModel);
        centerPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // --- НИЖНЯ ПАНЕЛЬ (Підрахунок ПДВ та суми) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

        customerCardCombo = new JComboBox<>();
        refreshCustomerCardCombo();
        customerCardCombo.addActionListener(e -> updateTotalSum());

        JPanel sumsPanel = new JPanel(new GridLayout(2, 1));
        totalSumLabel = new JLabel("До сплати: 0.00 грн", SwingConstants.RIGHT);
        totalSumLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalSumLabel.setForeground(Color.RED);

        vatLabel = new JLabel("в т.ч. ПДВ (20%): 0.00 грн", SwingConstants.RIGHT);
        vatLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        vatLabel.setForeground(Color.GRAY);

        sumsPanel.add(totalSumLabel);
        sumsPanel.add(vatLabel);

        JButton checkoutButton = new JButton("ОФОРМИТИ ЧЕК");
        checkoutButton.setBackground(new Color(34, 139, 34));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 14));

        bottomPanel.add(new JLabel("Картка клієнта:"));
        bottomPanel.add(customerCardCombo);
        bottomPanel.add(sumsPanel);
        bottomPanel.add(checkoutButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        addToCartButton.addActionListener(e -> {
            if (storeProductCombo.getSelectedItem() == null)
                return;
            String selectedItem = (String) storeProductCombo.getSelectedItem();
            try {
                String[] parts = selectedItem.split(" - ");
                String upc = parts[0];
                String name = parts[1];
                String priceStr = parts[2].split(" грн")[0].trim();
                BigDecimal price = new BigDecimal(priceStr);

                String qtyStr = selectedItem.substring(selectedItem.lastIndexOf("Залишок: ") + 9,
                        selectedItem.lastIndexOf(")"));
                int maxQty = Integer.parseInt(qtyStr.trim());
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0 || qty > maxQty) {
                    JOptionPane.showMessageDialog(this, "Невірна кількість! Доступно: " + maxQty, "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BigDecimal rowSum = price.multiply(new BigDecimal(qty));
                cartTableModel.addRow(new Object[] { upc, name, price, qty, rowSum });
                cartSales.add(new Sale(upc, "", qty, price));

                updateTotalSum();
                qtyField.setText("1");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка формату кількості!", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        checkoutButton.addActionListener(e -> processCheckout());

        return mainPanel;
    }

    private void refreshStoreProductsCombo(String filter) {
        if (storeProductCombo == null)
            return;
        storeProductCombo.removeAllItems();
        StoreProductDAO spDAO = new StoreProductDAO();
        ProductDAO pDAO = new ProductDAO();

        Map<Integer, String> productNames = pDAO.getAllProducts().stream()
                .collect(Collectors.toMap(Product::getIdProduct, Product::getProductName));

        String lowerFilter = filter.toLowerCase();

        for (StoreProduct sp : spDAO.getAllStoreProducts()) {
            if (sp.getProductsNumber() > 0) {
                String name = productNames.getOrDefault(sp.getIdProduct(), "Невідомо");
                if (sp.isPromotionalProduct())
                    name = "[АКЦІЯ] " + name;

                String priceStr = sp.getSellingPrice().setScale(2, RoundingMode.HALF_UP).toString();
                String itemText = sp.getUpc() + " - " + name + " - " + priceStr + " грн (Залишок: "
                        + sp.getProductsNumber() + ")";

                // Якщо поле пошуку порожнє АБО товар містить введені букви
                if (filter.isEmpty() || itemText.toLowerCase().contains(lowerFilter)) {
                    storeProductCombo.addItem(itemText);
                }
            }
        }
    }

    private void refreshCustomerCardCombo() {
        if (customerCardCombo == null)
            return;
        customerCardCombo.removeAllItems();
        customerCardCombo.addItem("Без картки");
        for (CustomerCard card : cardDAO.getAllCustomerCards()) {
            customerCardCombo
                    .addItem(card.getCardNumber() + " - " + card.getCustSurname() + " (" + card.getPercent() + "%)");
        }
    }

    private void updateTotalSum() {
        BigDecimal total = BigDecimal.ZERO;
        for (Sale sale : cartSales) {
            total = total.add(sale.getSellingPrice().multiply(new BigDecimal(sale.getProductNumber())));
        }

        String selectedCard = (String) customerCardCombo.getSelectedItem();
        if (selectedCard != null && !selectedCard.equals("Без картки")) {
            int percentStart = selectedCard.indexOf("(") + 1;
            int percentEnd = selectedCard.indexOf("%");
            int percent = Integer.parseInt(selectedCard.substring(percentStart, percentEnd));

            BigDecimal discountMultiplier = BigDecimal.ONE
                    .subtract(new BigDecimal(percent).divide(new BigDecimal(100)));
            total = total.multiply(discountMultiplier);
        }

        total = total.setScale(4, RoundingMode.HALF_UP);
        totalSumLabel.setText("До сплати: " + total.toString() + " грн");

        // Вираховуємо ПДВ (20% від загальної суми) та виводимо на екран
        BigDecimal vat = total.multiply(new BigDecimal("0.2")).setScale(4, RoundingMode.HALF_UP);
        if (vatLabel != null) {
            vatLabel.setText("в т.ч. ПДВ (20%): " + vat.toString() + " грн");
        }
    }

    private void processCheckout() {
        if (cartSales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Кошик порожній!", "Увага", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String checkNumber = "CH" + (System.currentTimeMillis() % 10000000);
        String cardNumber = null;
        String selectedCard = (String) customerCardCombo.getSelectedItem();
        if (selectedCard != null && !selectedCard.equals("Без картки")) {
            cardNumber = selectedCard.split(" - ")[0];
        }

        BigDecimal totalSum = BigDecimal.ZERO;
        for (Sale sale : cartSales) {
            sale.setCheckNumber(checkNumber);
            totalSum = totalSum.add(sale.getSellingPrice().multiply(new BigDecimal(sale.getProductNumber())));
        }

        if (cardNumber != null) {
            int percentStart = selectedCard.indexOf("(") + 1;
            int percentEnd = selectedCard.indexOf("%");
            int percent = Integer.parseInt(selectedCard.substring(percentStart, percentEnd));
            BigDecimal discountMultiplier = BigDecimal.ONE
                    .subtract(new BigDecimal(percent).divide(new BigDecimal(100)));
            totalSum = totalSum.multiply(discountMultiplier);
        }

        totalSum = totalSum.setScale(4, RoundingMode.HALF_UP);
        BigDecimal vat = totalSum.multiply(new BigDecimal("0.2")).setScale(4, RoundingMode.HALF_UP);

        Receipt receipt = new Receipt(checkNumber, loggedInCashier.getIdEmployee(), cardNumber,
                new Timestamp(System.currentTimeMillis()), totalSum, vat);

        if (receiptDAO.addReceiptWithSales(receipt, cartSales)) {
            JOptionPane.showMessageDialog(this, "Чек успішно створено!\nСума: " + totalSum + " грн", "Успіх",
                    JOptionPane.INFORMATION_MESSAGE);
            cartSales.clear();
            cartTableModel.setRowCount(0);
            updateTotalSum();
            refreshStoreProductsCombo("");
            customerCardCombo.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(this, "Помилка збереження чека!", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Вкладка "Постійні клієнти" ---
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout());

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
            for (CustomerCard card : cardDAO.getAllCustomerCards()) {
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
        JButton addButton = new JButton("Реєстрація нової карти");
        JButton editButton = new JButton("Редагувати дані");

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
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
            JTextField percentField = new JTextField("5", 5);

            JPanel inputPanel = new JPanel(new GridLayout(9, 2, 5, 5));
            inputPanel.add(new JLabel("Номер карти:"));
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

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Нова картка клієнта",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int percent = Integer.parseInt(percentField.getText().trim());
                    CustomerCard newCard = new CustomerCard(
                            cardField.getText().trim(), surnameField.getText().trim(), nameField.getText().trim(),
                            patField.getText().trim().isEmpty() ? null : patField.getText().trim(),
                            phoneField.getText().trim(), cityField.getText().trim(), streetField.getText().trim(),
                            zipField.getText().trim(), percent);
                    if (cardDAO.addCustomerCard(newCard)) {
                        loadCards.run();
                        refreshCustomerCardCombo();
                        JOptionPane.showMessageDialog(this, "Картку зареєстровано!");
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
                JOptionPane.showMessageDialog(this, "Оберіть клієнта для редагування!");
                return;
            }

            String cardNum = (String) tableModel.getValueAt(selectedRow, 0);
            CustomerCard currentCard = cardDAO.getAllCustomerCards().stream()
                    .filter(c -> c.getCardNumber().equals(cardNum)).findFirst().orElse(null);

            if (currentCard == null)
                return;

            JTextField surnameField = new JTextField(currentCard.getCustSurname(), 15);
            JTextField nameField = new JTextField(currentCard.getCustName(), 15);
            JTextField patField = new JTextField(15);
            JTextField phoneField = new JTextField(currentCard.getPhoneNumber(), 13);
            JTextField cityField = new JTextField(currentCard.getCity(), 15);
            JTextField streetField = new JTextField(currentCard.getStreet(), 15);
            JTextField zipField = new JTextField(currentCard.getZipCode(), 9);
            JTextField percentField = new JTextField(String.valueOf(currentCard.getPercent()), 5);

            JPanel inputPanel = new JPanel(new GridLayout(9, 2, 5, 5));
            inputPanel.add(new JLabel("Номер карти:"));
            inputPanel.add(new JLabel(cardNum));
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

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Редагування клієнта",
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
                        refreshCustomerCardCombo();
                        JOptionPane.showMessageDialog(this, "Дані оновлено!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Помилка оновлення!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Невірний формат!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private JPanel createReceiptHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton todayButton = new JButton("Чеки за сьогодні");

        com.toedter.calendar.JDateChooser startChooser = new com.toedter.calendar.JDateChooser(new java.util.Date());
        startChooser.setDateFormatString("yyyy-MM-dd");
        com.toedter.calendar.JDateChooser endChooser = new com.toedter.calendar.JDateChooser(new java.util.Date());
        endChooser.setDateFormatString("yyyy-MM-dd");

        JButton periodButton = new JButton("Пошук за період");

        filterPanel.add(todayButton);
        filterPanel.add(new JLabel("  |  Період з:"));
        filterPanel.add(startChooser);
        filterPanel.add(new JLabel("по:"));
        filterPanel.add(endChooser);
        filterPanel.add(periodButton);
        panel.add(filterPanel, BorderLayout.NORTH);

        String[] columnNames = { "Номер чека", "Дата та час", "Картка клієнта", "Загальна сума (грн)", "ПДВ (грн)" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton detailsButton = new JButton("Переглянути товари в чеку");
        bottomPanel.add(detailsButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        java.util.function.BiConsumer<Timestamp, Timestamp> loadReceipts = (start, end) -> {
            tableModel.setRowCount(0);
            List<Receipt> receipts = receiptDAO.getReceiptsByCashierAndPeriod(loggedInCashier.getIdEmployee(), start,
                    end);
            for (Receipt r : receipts) {
                tableModel.addRow(new Object[] {
                        r.getCheckNumber(),
                        r.getPrintDate().toString(),
                        r.getCardNumber() == null ? "Немає" : r.getCardNumber(),
                        r.getSumTotal(),
                        r.getVat()
                });
            }
        };

        todayButton.addActionListener(e -> {
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            loadReceipts.accept(Timestamp.valueOf(today + " 00:00:00"), Timestamp.valueOf(today + " 23:59:59"));
        });

        periodButton.addActionListener(e -> {
            try {
                if (startChooser.getDate() == null || endChooser.getDate() == null) {
                    JOptionPane.showMessageDialog(this, "Оберіть дати у календарі!", "Увага",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Timestamp start = Timestamp.valueOf(sdf.format(startChooser.getDate()) + " 00:00:00");
                Timestamp end = Timestamp.valueOf(sdf.format(endChooser.getDate()) + " 23:59:59");
                loadReceipts.accept(start, end);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка при зчитуванні дат!", "Помилка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        detailsButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Будь ласка, оберіть чек у таблиці!");
                return;
            }
            String checkNum = (String) tableModel.getValueAt(selectedRow, 0);
            List<String[]> details = receiptDAO.getSalesDetailsByCheck(checkNum);

            String[] detColumns = { "Назва товару", "Кількість", "Ціна продажу", "Разом" };
            DefaultTableModel detModel = new DefaultTableModel(detColumns, 0);
            for (String[] rowData : details)
                detModel.addRow(rowData);

            JTable detTable = new JTable(detModel);
            JScrollPane detScroll = new JScrollPane(detTable);
            detScroll.setPreferredSize(new Dimension(500, 250));

            JOptionPane.showMessageDialog(this, detScroll, "Склад чека " + checkNum, JOptionPane.PLAIN_MESSAGE);
        });

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                " Особиста картка співробітника ",
                0, 0,
                new Font("Arial", Font.BOLD, 14)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addProfileRow(cardPanel, gbc, "ID Працівника:", loggedInCashier.getIdEmployee(), row++);

        String fullPIB = loggedInCashier.getEmplSurname() + " " +
                loggedInCashier.getEmplName() + " " +
                (loggedInCashier.getEmplPatronymic() != null ? loggedInCashier.getEmplPatronymic() : "");
        addProfileRow(cardPanel, gbc, "Повне ПІБ:", fullPIB, row++);
        addProfileRow(cardPanel, gbc, "Посада:", loggedInCashier.getEmplRole(), row++);

        String salaryStr = (loggedInCashier.getSalary() != null) ? loggedInCashier.getSalary().toString() + " грн"
                : "Не вказано";
        addProfileRow(cardPanel, gbc, "Ставка заробітної плати:", salaryStr, row++);

        String dobStr = (loggedInCashier.getDateOfBirth() != null) ? loggedInCashier.getDateOfBirth().toString()
                : "Не вказано";
        addProfileRow(cardPanel, gbc, "Дата народження:", dobStr, row++);

        String dosStr = (loggedInCashier.getDateOfStart() != null) ? loggedInCashier.getDateOfStart().toString()
                : "Не вказано";
        addProfileRow(cardPanel, gbc, "Дата початку контракту:", dosStr, row++);

        addProfileRow(cardPanel, gbc, "Контактний телефон:",
                loggedInCashier.getPhoneNumber() != null ? loggedInCashier.getPhoneNumber() : "Не вказано", row++);
        addProfileRow(cardPanel, gbc, "Населений пункт (Місто):",
                loggedInCashier.getCity() != null ? loggedInCashier.getCity() : "Не вказано", row++);
        addProfileRow(cardPanel, gbc, "Адреса проживання (Вулиця):",
                loggedInCashier.getStreet() != null ? loggedInCashier.getStreet() : "Не вказано", row++);
        addProfileRow(cardPanel, gbc, "Поштовий індекс:",
                loggedInCashier.getZipCode() != null ? loggedInCashier.getZipCode() : "Не вказано", row++);

        mainPanel.add(new JScrollPane(cardPanel), BorderLayout.CENTER);

        // ДОДАЄМО КНОПКУ РЕДАГУВАННЯ
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editButton = new JButton("✏️ Редагувати мої контактні дані");
        bottomPanel.add(editButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        editButton.addActionListener(e -> {
            JTextField phoneField = new JTextField(loggedInCashier.getPhoneNumber());
            JTextField cityField = new JTextField(loggedInCashier.getCity());
            JTextField streetField = new JTextField(loggedInCashier.getStreet());
            JTextField zipField = new JTextField(loggedInCashier.getZipCode());

            JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
            inputPanel.add(new JLabel("Телефон:"));
            inputPanel.add(phoneField);
            inputPanel.add(new JLabel("Місто:"));
            inputPanel.add(cityField);
            inputPanel.add(new JLabel("Вулиця:"));
            inputPanel.add(streetField);
            inputPanel.add(new JLabel("Індекс:"));
            inputPanel.add(zipField);

            if (JOptionPane.showConfirmDialog(this, inputPanel, "Оновлення контактних даних",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                loggedInCashier.setPhoneNumber(phoneField.getText().trim());
                loggedInCashier.setCity(cityField.getText().trim());
                loggedInCashier.setStreet(streetField.getText().trim());
                loggedInCashier.setZipCode(zipField.getText().trim());

                com.zlagoda.dao.EmployeeDAO empDAO = new com.zlagoda.dao.EmployeeDAO();
                if (empDAO.updateEmployee(loggedInCashier)) {
                    JOptionPane.showMessageDialog(this,
                            "Дані успішно оновлено! Щоб побачити зміни на екрані, вийдіть і зайдіть в акаунт.");
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка при оновленні бази даних!", "Помилка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return mainPanel;
    }

    private void addProfileRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(val, gbc);
    }
}