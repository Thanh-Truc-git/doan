import java.sql.*;
public class DebugSeatState {
  public static void main(String[] args) throws Exception {
    try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/doancuoiky","root","4102004");
         Statement s = c.createStatement()) {
      System.out.println("=== MOVIE 21 SHOWTIMES ===");
      try (ResultSet rs = s.executeQuery("SELECT s.id, s.room_id, s.start_time, m.title FROM showtime s LEFT JOIN movie m ON m.id=s.movie_id WHERE s.movie_id=21")) {
        while (rs.next()) {
          System.out.printf("showtimeId=%s roomId=%s start=%s title=%s%n", rs.getString("id"), rs.getString("room_id"), rs.getString("start_time"), rs.getString("title"));
        }
      }
      System.out.println("=== ROOM 4? SEATS ===");
      try (ResultSet rs = s.executeQuery("SELECT room_id, COUNT(*) cnt, MIN(seat_number) min_seat, MAX(seat_number) max_seat FROM seat GROUP BY room_id ORDER BY room_id")) {
        while (rs.next()) {
          System.out.printf("roomId=%s cnt=%s min=%s max=%s%n", rs.getString("room_id"), rs.getString("cnt"), rs.getString("min_seat"), rs.getString("max_seat"));
        }
      }
      System.out.println("=== TICKETS SHOWTIME 4 ===");
      try (ResultSet rs = s.executeQuery("SELECT id, seat_number, user_id, showtime_id, status FROM ticket WHERE showtime_id=4 ORDER BY id")) {
        while (rs.next()) {
          System.out.printf("ticketId=%s seat=%s userId=%s showtimeId=%s status=%s%n", rs.getString("id"), rs.getString("seat_number"), rs.getString("user_id"), rs.getString("showtime_id"), rs.getString("status"));
        }
      }
    }
  }
}
