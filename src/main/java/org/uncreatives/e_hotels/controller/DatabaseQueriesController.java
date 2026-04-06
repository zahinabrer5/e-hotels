package org.uncreatives.e_hotels.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/queries")
public class DatabaseQueriesController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseQueriesController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Room count and average price per hotel (aggregation)
    @GetMapping("/hotel-stats")
    public List<Map<String, Object>> getHotelStats() {
        String sql = """
            SELECT
                c.ChainName,
                h.Hotel_Address,
                COUNT(r.roomID) AS Total_Rooms,
                ROUND(AVG(r.Price), 2) AS Average_Room_Price
            FROM Hotel_Chain c
            JOIN Hotel h ON c.chainID = h.chainID
            JOIN Hotel_Room r ON h.hotel_ID = r.hotel_ID
            GROUP BY c.ChainName, h.Hotel_Address, h.hotel_ID
            ORDER BY Average_Room_Price DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // Hotels with above-average ratings (nested query)
    @GetMapping("/top-rated-hotels")
    public List<Map<String, Object>> getTopRatedHotels() {
        String sql = """
            SELECT
                hotel_ID,
                Hotel_Address,
                Email,
                Rating
            FROM Hotel
            WHERE Rating > (
                SELECT AVG(Rating) FROM Hotel
            )
            ORDER BY Rating DESC
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // Find the top-spending customer (nesting & aggregation)
    @GetMapping("/top-spending-customer")
    public List<Map<String, Object>> getTopSpendingCustomer() {
        String sql = """
            SELECT
                c.Customer_Name,
                SUM(r.paymentAmount) AS Total_Spent
            FROM Customer c
            JOIN Renting r ON c.custID = r.custID
            GROUP BY c.custID, c.Customer_Name
            HAVING SUM(r.paymentAmount) = (
                SELECT MAX(total_amount)
                FROM (
                    SELECT SUM(paymentAmount) AS total_amount
                    FROM Renting
                    GROUP BY custID
                ) AS CustomerTotals
            )
            """;
        return jdbcTemplate.queryForList(sql);
    }

    // Find available rooms for a given hotel and date range (subquery)
    @GetMapping("/available-rooms")
    public List<Map<String, Object>> getAvailableRooms(
            @RequestParam(defaultValue = "1") int hotelId,
            @RequestParam(defaultValue = "2026-05-01") String startDate,
            @RequestParam(defaultValue = "2026-05-10") String endDate) {

        // A room is available if it doesn't overlap with any existing booking
        // Overlap logic: booking.startDate < searchEndDate AND booking.endDate > searchStartDate
        String sql = """
            SELECT
                r.roomID,
                r.roomNumber,
                r.Price,
                r.Capacity,
                r.Room_View
            FROM Hotel_Room r
            WHERE r.hotel_ID = ?
              AND r.roomID NOT IN (
                  SELECT b.roomID
                  FROM Booking b
                  WHERE b.startDate < CAST(? AS date)
                    AND b.endDate > CAST(? AS date)
              )
            """;

        return jdbcTemplate.queryForList(sql, hotelId, endDate, startDate);
    }
}
