package Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class InventoryManagementGUI extends JFrame {

    private JTable inventoryTable;
    private JTextField itemIDField;
    private JTextField quantityField;
    private JButton updateButton;
    private InventoryController controller;

    public InventoryManagementGUI() {
        setTitle("Inventory Management");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        controller = new InventoryController();

        // Create components
        inventoryTable = new JTable();  // You will populate this with data
        itemIDField = new JTextField(10);
        quantityField = new JTextField(10);
        updateButton = new JButton("Update Stock");

        // Panel for updating inventory
        JPanel updatePanel = new JPanel();
        updatePanel.add(new JLabel("Item ID:"));
        updatePanel.add(itemIDField);
        updatePanel.add(new JLabel("Quantity:"));
        updatePanel.add(quantityField);
        updatePanel.add(updateButton);

        // Adding components to the main frame
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        add(updatePanel, BorderLayout.SOUTH);

        // Button Action
        updateButton.addActionListener(e -> updateStock());

        // Load initial data
        loadInventoryData();
    }

    private void loadInventoryData() {
        // Method to load the data from the database and display it in the JTable
        inventoryTable.setModel(controller.getInventoryData());
    }

    private void updateStock() {
        try {
            int itemId = Integer.parseInt(itemIDField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            controller.updateInventory(itemId, quantity);
            loadInventoryData();  // Reload the updated data
            JOptionPane.showMessageDialog(this, "Inventory updated successfully!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InventoryManagementGUI gui = new InventoryManagementGUI();
            gui.setVisible(true);
        });
    }
}
