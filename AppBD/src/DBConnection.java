import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Modifică aceste date după setările tale MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/navalwarsbd?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Eugenia.18"; // pune parola ta


    // Obține conexiunea
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Test conexiune
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            System.out.println("Conexiunea la MySQL a reușit!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
