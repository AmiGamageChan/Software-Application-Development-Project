package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class SQL {

    private static Connection connection;

    private static void setupCon() {
        try {
            if (connection == null) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/culinary_resort_db", "root", "password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResultSet executeSearch(String query) {
        try {
            setupCon();
            return (ResultSet) connection.createStatement().executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer executeIUD(String query) {
        try {
            setupCon();
            return connection.createStatement().executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
