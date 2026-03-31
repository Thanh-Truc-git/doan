import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DebugTicketQuery {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/doancuoiky";
        String user = "root";
        String password = "4102004";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {

            System.out.println("=== USERS ===");
            try (ResultSet rs = statement.executeQuery(
                    "SELECT id, username, email, role FROM `user` ORDER BY id DESC LIMIT 10")) {
                while (rs.next()) {
                    System.out.printf(
                            "id=%d username=%s email=%s role=%s%n",
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("role"));
                }
            }

            System.out.println("=== TICKETS ===");
            try (ResultSet rs = statement.executeQuery(
                    "SELECT t.id, t.seat_number, t.user_id, u.username, t.showtime_id, t.ticket_code, t.status " +
                    "FROM ticket t LEFT JOIN `user` u ON u.id = t.user_id " +
                    "ORDER BY t.id DESC LIMIT 20")) {
                while (rs.next()) {
                    System.out.printf(
                            "id=%d seat=%s userId=%s username=%s showtimeId=%s code=%s status=%s%n",
                            rs.getLong("id"),
                            rs.getString("seat_number"),
                            rs.getString("user_id"),
                            rs.getString("username"),
                            rs.getString("showtime_id"),
                            rs.getString("ticket_code"),
                            rs.getString("status"));
                }
            }

            System.out.println("=== PENDING ORDERS ===");
            try (ResultSet rs = statement.executeQuery(
                    "SELECT id, txn_ref, username, seats, showtime_id, movie_id, processed, created_at " +
                    "FROM pending_ticket_order ORDER BY id DESC LIMIT 20")) {
                while (rs.next()) {
                    System.out.printf(
                            "id=%d txnRef=%s username=%s seats=%s showtimeId=%s movieId=%s processed=%s createdAt=%s%n",
                            rs.getLong("id"),
                            rs.getString("txn_ref"),
                            rs.getString("username"),
                            rs.getString("seats"),
                            rs.getString("showtime_id"),
                            rs.getString("movie_id"),
                            rs.getString("processed"),
                            rs.getString("created_at"));
                }
            }

            System.out.println("=== SHOWTIMES ===");
            try (ResultSet rs = statement.executeQuery(
                    "SELECT s.id, m.title, r.name, s.start_time " +
                    "FROM showtime s " +
                    "LEFT JOIN movie m ON m.id = s.movie_id " +
                    "LEFT JOIN room r ON r.id = s.room_id " +
                    "ORDER BY s.id DESC LIMIT 20")) {
                while (rs.next()) {
                    System.out.printf(
                            "id=%d movie=%s room=%s start=%s%n",
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("name"),
                            rs.getString("start_time"));
                }
            }

            System.out.println("=== TICKET SCHEMA ===");
            try (ResultSet rs = statement.executeQuery("SHOW CREATE TABLE ticket")) {
                while (rs.next()) {
                    System.out.println(rs.getString(2));
                }
            }

            System.out.println("=== INSERT TEST (ROLLBACK) ===");
            connection.setAutoCommit(false);
            try {
                statement.executeUpdate(
                        "INSERT INTO ticket(seat_number, qr_code, status, ticket_code, user_id, showtime_id, booking_id) " +
                        "VALUES ('DEBUG-SEAT', NULL, 'BOOKED', 'DEBUG-CODE', 2, NULL, NULL)");
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
