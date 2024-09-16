package Test;


import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class InventoryController {

    private Connection connect() throws SQLException {
        // Connection details (adjust based on your database)
        String url = "jdbc:mysql://localhost:3306/pos_db";  // Your DB URL
        String user = "root";                               // Your DB username
        String password = "pass";                               // Your DB password
        return DriverManager.getConnection(url, user, password);
    }

    public DefaultTableModel getInventoryData() {
        String[] columnNames = {"Item ID", "Item Name", "Stock Quantity"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        try (Connection conn = connect()) {
            String query = "SELECT menu_items.item_id, menu_items.item_name, inventory.quantity " +
                           "FROM menu_items " +
                           "JOIN inventory ON menu_items.item_id = inventory.item_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int itemId = rs.getInt("item_id");
                String itemName = rs.getString("item_name");
                int quantity = rs.getInt("quantity");
                model.addRow(new Object[]{itemId, itemName, quantity});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    public void updateInventory(int itemId, int quantity) {
        String query = "UPDATE inventory SET quantity = ?, last_updated = NOW() WHERE item_id = ?";
        
        try (Connection conn = connect()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
