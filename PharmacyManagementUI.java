import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

class Medicine {
    int id;
    String name;
    int quantity;
    double price;

    public Medicine(int id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%-5d,%-20s,%-10d,%-10.2f", id, name, quantity, price);
    }
}

public class PharmacyManagementUI {
    private static final String CSV_FILE = "medicine_data.csv";
    private static List<Medicine> medicines = new ArrayList<>();
    private static final Random random = new Random();

    private JFrame frame;
    private JTextArea displayArea;
    private JTextField nameField, quantityField, priceField;
    private JTextField sellNameField, sellQuantityField;

    public PharmacyManagementUI() {
        loadMedicines();
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Pharmacy Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setBackground(Color.BLACK);
        displayArea.setForeground(Color.GREEN);
        displayArea.setFont(new Font("Consolas", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        buttonPanel.setBackground(Color.BLACK);

        JButton addButton = createStyledButton("Add Medicine");
        JButton sellButton = createStyledButton("Sell Medicine");
        JButton displayButton = createStyledButton("Display Stock");
        JButton exitButton = createStyledButton("Exit");

        addButton.addActionListener(_ -> addMedicine());
        sellButton.addActionListener(_ -> sellMedicine());
        displayButton.addActionListener(_ -> displayStock());
        exitButton.addActionListener(_ -> {
            saveMedicines();
            frame.dispose();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(displayButton);
        buttonPanel.add(exitButton);

        JLabel creditLabel = new JLabel("<html><div style='text-align: center;'>"
                + "By<br>"
                + "~ Yukta Aggarwal<br>"
                + "~ Riya Acharya<br>"
                + "~ Kshitij Yagnik"
                + "</div></html>");
        creditLabel.setFont(new Font("Arial", Font.BOLD, 14));
        creditLabel.setForeground(Color.WHITE);
        creditLabel.setHorizontalAlignment(SwingConstants.CENTER);
        creditLabel.setOpaque(true);
        creditLabel.setBackground(Color.BLACK);
        creditLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buttonPanel.add(creditLabel);

        frame.add(buttonPanel, BorderLayout.WEST);
        frame.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        return button;
    }

    private void addMedicine() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Medicine Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        panel.add(quantityField);

        panel.add(new JLabel("Price:"));
        priceField = new JTextField();
        panel.add(priceField);

        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Medicine", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());

                if (name.isEmpty() || quantity <= 0 || price <= 0) {
                    JOptionPane.showMessageDialog(frame, "Please fill all fields with valid values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (Medicine med : medicines) {
                    if (med.name.equalsIgnoreCase(name)) {
                        JOptionPane.showMessageDialog(frame, "Error: Medicine name already exists!", "Duplicate Entry", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                int id = getNextUniqueId();
                medicines.add(new Medicine(id, name, quantity, price));
                saveMedicines();
                displayArea.append("Medicine added successfully!\n");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input! Please enter valid numbers.");
            }
        }
    }

    private void sellMedicine() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Medicine Name:"));
        sellNameField = new JTextField();
        panel.add(sellNameField);

        panel.add(new JLabel("Quantity:"));
        sellQuantityField = new JTextField();
        panel.add(sellQuantityField);

        SwingUtilities.invokeLater(() -> sellNameField.requestFocusInWindow());

        int result = JOptionPane.showConfirmDialog(frame, panel, "Sell Medicine", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = sellNameField.getText().trim();
                int quantity = Integer.parseInt(sellQuantityField.getText().trim());

                if (name.isEmpty() || quantity <= 0) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean medicineFound = false;
                for (Medicine med : medicines) {
                    if (med.name.equalsIgnoreCase(name)) {
                        medicineFound = true;
                        if (med.quantity >= quantity) {
                            med.quantity -= quantity;
                            generateBill(med, quantity);
                            saveMedicines();
                            displayArea.append("Sold " + quantity + " of " + med.name + "\n");
                            return;
                        } else {
                            displayArea.append("Not enough stock! Ordering...\n");
                            orderMedicine(name);
                            return;
                        }
                    }
                }

                if (!medicineFound) {
                    displayArea.append("Medicine not found! Ordering...\n");
                    orderMedicine(name);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input! Please enter valid numbers.");
            }
        }
    }

    private void orderMedicine(String medicineName) {
        try {
            String quantityStr = JOptionPane.showInputDialog(frame, "Enter Quantity to Order:");
            if (quantityStr == null) {
                JOptionPane.showMessageDialog(frame, "Order cancelled.");
                return;
            }

            int quantity = Integer.parseInt(quantityStr.trim());

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(frame, "Quantity must be positive.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog loadingDialog = new JDialog(frame, "Ordering Medicine...", false);
            loadingDialog.setSize(300, 150);
            loadingDialog.setLayout(new BorderLayout());
            JLabel loadingLabel = new JLabel("Ordering, please wait...", SwingConstants.CENTER);
            loadingDialog.add(loadingLabel, BorderLayout.CENTER);
            loadingDialog.setLocationRelativeTo(frame);
            loadingDialog.setVisible(true);

            javax.swing.Timer timer = new javax.swing.Timer(5000, _ -> {
                loadingDialog.dispose();
                Medicine existing = null;
                for (Medicine med : medicines) {
                    if (med.name.equalsIgnoreCase(medicineName)) {
                        existing = med;
                        break;
                    }
                }

                if (existing != null) {
                    existing.quantity += quantity;
                } else {
                    double price = 50 + (random.nextDouble() * 450);
                    int id = getNextUniqueId();
                    medicines.add(new Medicine(id, medicineName, quantity, Math.round(price * 100.0) / 100.0));
                }

                saveMedicines();
                displayStock();
                JOptionPane.showMessageDialog(frame, "Medicine is now in stock!", "Success", JOptionPane.INFORMATION_MESSAGE);
            });

            timer.setRepeats(false);
            timer.start();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid input! Please enter valid numbers.");
        }
    }

    private void displayStock() {
        displayArea.setText(""); 
        displayArea.append("************ STOCK ************\n");
        if (medicines.isEmpty()) {
            displayArea.append("No medicines in stock.\n");
        } else {
            for (Medicine med : medicines) {
                displayArea.append(String.format("ID: %-5d Name: %-20s Qty: %-5d Price: ₹%-10.2f\n", med.id, med.name, med.quantity, med.price));
            }
        }
        displayArea.append("*******************************\n");
    }

    private void generateBill(Medicine med, int quantity) {
        double total = med.price * quantity;
        JOptionPane.showMessageDialog(frame, "Bill:\n" +
                "Medicine: " + med.name + "\n" +
                "Quantity: " + quantity + "\n" +
                "Price per unit: ₹" + med.price + "\n" +
                "Total: ₹" + total, "Bill", JOptionPane.INFORMATION_MESSAGE);
    }

    private int getNextUniqueId() {
        int maxId = 0;
        for (Medicine med : medicines) {
            if (med.id > maxId) {
                maxId = med.id;
            }
        }
        return maxId + 1;
    }

    private void loadMedicines() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    int quantity = Integer.parseInt(parts[2].trim());
                    double price = Double.parseDouble(parts[3].trim());
                    medicines.add(new Medicine(id, name, quantity, price));
                }
            }
        } catch (IOException | NumberFormatException e) {
            // File might not exist yet, or bad data - ignore
        }
    }

    private void saveMedicines() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
            for (Medicine med : medicines) {
                writer.write(med.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving medicines!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PharmacyManagementUI::new);
    }
}
