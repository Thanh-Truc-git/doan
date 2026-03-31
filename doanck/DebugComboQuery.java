import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DebugComboQuery {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/doancuoiky";
        String user = "root";
        String password = "4102004";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            System.out.println("=== COMBO SCHEMA ===");
            try (ResultSet rs = statement.executeQuery("SHOW CREATE TABLE combo")) {
                while (rs.next()) {
                    System.out.println(rs.getString(2));
                }
            }

            System.out.println("=== COMBOS ===");
            try (ResultSet rs = statement.executeQuery(
                    "SELECT id, name, original_price, discounted_price, discount_percent, image, quantity, active " +
                            "FROM combo ORDER BY id DESC")) {
                while (rs.next()) {
                    System.out.printf(
                            "id=%d name=%s original=%s discounted=%s discountPercent=%s image=%s quantity=%s active=%s%n",
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("original_price"),
                            rs.getString("discounted_price"),
                            rs.getString("discount_percent"),
                            rs.getString("image"),
                            rs.getString("quantity"),
                            rs.getString("active"));
                }
            }

            System.out.println("=== INSERT TEST (ROLLBACK) ===");
            connection.setAutoCommit(false);
            try {
                statement.executeUpdate(
                        "INSERT INTO combo(name, description, original_price, discounted_price, discount_percent, image, quantity, active) " +
                                "VALUES ('DEBUG COMBO', 'debug', 100000, 80000, 20, 'debug.png', 10, true)");
                System.out.println("insert_test=SUCCESS");
                connection.rollback();
            } catch (Exception e) {
                System.out.println("insert_test=FAILED");
                e.printStackTrace(System.out);
                connection.rollback();
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
