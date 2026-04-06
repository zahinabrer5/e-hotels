package org.uncreatives.e_hotels.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/management")
public class AppManagementController {

    private final JdbcTemplate jdbcTemplate;

    public AppManagementController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Room search endpoint
    @GetMapping("/search-rooms")
    public List<Map<String, Object>> searchRooms(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String chainName,
            @RequestParam(required = false) Integer categoryRating,
            @RequestParam(required = false) Integer minTotalRooms,
            @RequestParam(required = false) Double maxPrice) {

        StringBuilder sql = new StringBuilder("""
            SELECT 
                r.roomID, r.roomNumber, r.Price, r.Capacity, h.Hotel_Address, hc.ChainName, h.Rating,
                (SELECT COUNT(*) FROM Hotel_Room hr WHERE hr.hotel_ID = h.hotel_ID) AS Total_Rooms
            FROM Hotel_Room r
            JOIN Hotel h ON r.hotel_ID = h.hotel_ID
            JOIN Hotel_Chain hc ON h.chainID = hc.chainID
            WHERE 1=1
        """);

        List<Object> args = new ArrayList<>();

        if (capacity != null) {
            sql.append(" AND r.Capacity = ?");
            args.add(capacity);
        }
        if (area != null && !area.isEmpty()) {
            sql.append(" AND h.Hotel_Address ILIKE ?");
            args.add("%" + area + "%");
        }
        if (chainName != null && !chainName.isEmpty()) {
            sql.append(" AND hc.ChainName ILIKE ?");
            args.add("%" + chainName + "%");
        }
        if (categoryRating != null) {
            sql.append(" AND h.Rating = ?");
            args.add(categoryRating);
        }
        if (maxPrice != null) {
            sql.append(" AND r.Price <= ?");
            args.add(maxPrice);
        }
        if (startDate != null && endDate != null) {
            // Room is available if it doesn't overlap with any existing booking
            sql.append("""
               AND r.roomID NOT IN (
                   SELECT b.roomID FROM Booking b 
                   WHERE b.startDate < ?::date AND b.endDate > ?::date
               )
            """);
            args.add(endDate);
            args.add(startDate);
        }

        if (minTotalRooms != null) {
            sql.append(" AND (SELECT COUNT(*) FROM Hotel_Room hr WHERE hr.hotel_ID = h.hotel_ID) >= ?");
            args.add(minTotalRooms);
        }

        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    // View endpoints
    @GetMapping("/views/available-rooms-per-area")
    public List<Map<String, Object>> getAvailableRoomsPerArea() {
        return jdbcTemplate.queryForList("SELECT * FROM Available_Rooms_Per_Area");
    }

    @GetMapping("/views/hotel-capacity")
    public List<Map<String, Object>> getHotelCapacity() {
        return jdbcTemplate.queryForList("SELECT * FROM Hotel_Capacity");
    }

    // Customer performs a booking
    @PostMapping("/book")
    @Operation(summary = "Create a Room Booking", description = "Customer makes a booking for a room")
    public String createBooking(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Customer Booking", value = "{\n  \"custId\": \"222-000-001\",\n  \"roomId\": 1,\n  \"startDate\": \"2026-05-01\",\n  \"endDate\": \"2026-05-15\"\n}")
            }))
            @RequestBody Map<String, Object> payload) {
        String custId = (String) payload.get("custId");
        Integer roomId = (Integer) payload.get("roomId");
        String startDate = (String) payload.get("startDate");
        String endDate = (String) payload.get("endDate");
        
        String query = "INSERT INTO Booking (bookingID, custID, roomID, startDate, endDate, bookingTime) " +
                       "VALUES (nextval('booking_seq_auth'), ?, ?, ?::date, ?::date, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(query, custId, roomId, startDate, endDate);
        return "Booking created successfully!";
    }

    // Employee coverts a booking to renting (or creates direct renting)
    @PostMapping("/rent")
    @Operation(summary = "Execute a Renting/Check-In", description = "Employee checks in a customer by converting an existing booking to a renting or directly creating a new renting")
    public String executeRenting(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Direct Renting (No previous booking)", value = "{\n  \"bookingId\": null,\n  \"custId\": \"222-000-001\",\n  \"roomId\": 1,\n  \"employeeId\": \"111-000-001\",\n  \"checkInDate\": \"2026-05-01\",\n  \"checkOutDate\": \"2026-05-15\",\n  \"paymentAmount\": 1500.00\n}"),
                    @ExampleObject(name = "Convert Booking to Renting", value = "{\n  \"bookingId\": 10,\n  \"custId\": \"222-000-001\",\n  \"roomId\": 1,\n  \"employeeId\": \"111-000-001\",\n  \"checkInDate\": \"2026-05-01\",\n  \"checkOutDate\": \"2026-05-15\",\n  \"paymentAmount\": 1500.00\n}")
            }))
            @RequestBody Map<String, Object> payload) {
        Integer bookingId = (Integer) payload.get("bookingId"); // nullable for direct rentings
        String custId = (String) payload.get("custId");
        Integer roomId = (Integer) payload.get("roomId");
        String employeeId = (String) payload.get("employeeId");
        String checkIn = (String) payload.get("checkInDate");
        String checkOut = (String) payload.get("checkOutDate");
        Double paymentAmount = Double.valueOf(payload.get("paymentAmount").toString());

        String query = "INSERT INTO Renting (rentingID, bookingID, custID, roomID, National_ID, checkInDate, checkOutDate, paymentAmount) " +
                "VALUES (nextval('renting_seq_auth'), ?, ?, ?, ?, ?::date, ?::date, ?)";
        
        jdbcTemplate.update(query, bookingId, custId, roomId, employeeId, checkIn, checkOut, paymentAmount);

        // Update room status for direct renting
        jdbcTemplate.update("UPDATE Hotel_Room SET Room_Status = 'Occupied' WHERE roomID = ?", roomId);

        return "Renting processed and payment received successfully!";
    }

    // CRUD endpoints for Customers, Employees, Hotels, Rooms

    // Customers
    @GetMapping("/customers")
    public List<Map<String, Object>> getAllCustomers() {
        return jdbcTemplate.queryForList("SELECT * FROM Customer");
    }

    @GetMapping("/customers/{custID}")
    public Map<String, Object> getCustomer(@PathVariable String custID) {
        return jdbcTemplate.queryForMap("SELECT * FROM Customer WHERE custID = ?", custID);
    }

    @PostMapping("/customers")
    @Operation(summary = "Create a New Customer", description = "Insert a new customer into the database")
    public String insertCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Customer Payload", value = "{\n  \"custID\": \"333-000-001\",\n  \"ID_type\": \"driving licence\",\n  \"DateOfRegistration\": \"2026-04-06\",\n  \"Customer_Name\": \"Jane Doe\",\n  \"Customer_Address\": \"123 Fake St, Ottawa, ON\"\n}")
            }))
            @RequestBody Map<String, Object> payload) {
        String query = "INSERT INTO Customer (custID, ID_type, DateOfRegistration, Customer_Name, Customer_Address) VALUES (?, ?, ?::date, ?, ?)";
        jdbcTemplate.update(query, payload.get("custID"), payload.get("ID_type"), payload.get("DateOfRegistration"), payload.get("Customer_Name"), payload.get("Customer_Address"));
        return "Customer created successfully";
    }

    @PutMapping("/customers/{custID}")
    @Operation(summary = "Update an Existing Customer", description = "Update customer details using their custID")
    public String updateCustomer(
            @PathVariable String custID, 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Update Customer", value = "{\n  \"ID_type\": \"driving licence\",\n  \"DateOfRegistration\": \"2026-04-06\",\n  \"Customer_Name\": \"Jane Doe\",\n  \"Customer_Address\": \"123 Fake St, Ottawa, ON\"\n}")
            }))
            @RequestBody Map<String, Object> payload) {
        String query = "UPDATE Customer SET ID_type = ?, DateOfRegistration = ?::date, Customer_Name = ?, Customer_Address = ? WHERE custID = ?";
        jdbcTemplate.update(query, payload.get("ID_type"), payload.get("DateOfRegistration"), payload.get("Customer_Name"), payload.get("Customer_Address"), custID);
        return "Customer updated successfully";
    }

    @DeleteMapping("/customers/{custID}")
    public String deleteCustomer(@PathVariable String custID) {
        jdbcTemplate.update("DELETE FROM Customer WHERE custID = ?", custID);
        return "Customer deleted successfully";
    }

    // Employees
    @GetMapping("/employees")
    public List<Map<String, Object>> getAllEmployees() {
        return jdbcTemplate.queryForList("SELECT * FROM Employee");
    }

    @GetMapping("/employees/{nationalID}")
    public Map<String, Object> getEmployee(@PathVariable String nationalID) {
        return jdbcTemplate.queryForMap("SELECT * FROM Employee WHERE National_ID = ?", nationalID);
    }

    @PostMapping("/employees")
    @Operation(summary = "Create an Employee", description = "Register a new hotel employee")
    public String insertEmployee(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Employee Payload", value = "{\n  \"National_ID\": \"123-456-789\",\n  \"hotel_ID\": 1,\n  \"Employee_Name\": \"John Wick\",\n  \"Employee_Address\": \"Continental, NY\"\n}")
            }))
            @RequestBody Map<String, Object> payload) {
        String query = "INSERT INTO Employee (National_ID, hotel_ID, Employee_Name, Employee_Address) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(query, payload.get("National_ID"), payload.get("hotel_ID"), payload.get("Employee_Name"), payload.get("Employee_Address"));
        return "Employee created successfully";
    }

    @PutMapping("/employees/{nationalID}")
    @Operation(summary = "Update an Employee", description = "Update an existing employee details")
    public String updateEmployee(
            @PathVariable String nationalID, 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Update Employee", value = "{\n  \"hotel_ID\": 1,\n  \"Employee_Name\": \"John Wick\",\n  \"Employee_Address\": \"Continental, NY\"\n}")
            }))
            @RequestBody Map<String, Object> payload) {
        String query = "UPDATE Employee SET hotel_ID = ?, Employee_Name = ?, Employee_Address = ? WHERE National_ID = ?";
        jdbcTemplate.update(query, payload.get("hotel_ID"), payload.get("Employee_Name"), payload.get("Employee_Address"), nationalID);
        return "Employee updated successfully";
    }

    @DeleteMapping("/employees/{nationalID}")
    public String deleteEmployee(@PathVariable String nationalID) {
        jdbcTemplate.update("DELETE FROM Employee WHERE National_ID = ?", nationalID);
        return "Employee deleted successfully";
    }

    // Hotels
    @GetMapping("/hotels")
    public List<Map<String, Object>> getAllHotels() {
        return jdbcTemplate.queryForList("SELECT * FROM Hotel");
    }

    @GetMapping("/hotels/{hotelID}")
    public Map<String, Object> getHotel(@PathVariable Integer hotelID) {
        return jdbcTemplate.queryForMap("SELECT * FROM Hotel WHERE hotel_ID = ?", hotelID);
    }

    @PostMapping("/hotels")
    public String insertHotel(@RequestBody Map<String, Object> payload) {
        String query = "INSERT INTO Hotel (hotel_ID, chainID, Email, Hotel_Address, Rating, Manager_National_ID) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(query, payload.get("hotel_ID"), payload.get("chainID"), payload.get("Email"), payload.get("Hotel_Address"), payload.get("Rating"), payload.get("Manager_National_ID"));
        return "Hotel created successfully";
    }

    @PutMapping("/hotels/{hotelID}")
    public String updateHotel(@PathVariable Integer hotelID, @RequestBody Map<String, Object> payload) {
        String query = "UPDATE Hotel SET chainID = ?, Email = ?, Hotel_Address = ?, Rating = ?, Manager_National_ID = ? WHERE hotel_ID = ?";
        jdbcTemplate.update(query, payload.get("chainID"), payload.get("Email"), payload.get("Hotel_Address"), payload.get("Rating"), payload.get("Manager_National_ID"), hotelID);
        return "Hotel updated successfully";
    }

    @DeleteMapping("/hotels/{hotelID}")
    public String deleteHotel(@PathVariable Integer hotelID) {
        jdbcTemplate.update("DELETE FROM Hotel WHERE hotel_ID = ?", hotelID);
        return "Hotel deleted successfully";
    }

    // Rooms
    @GetMapping("/rooms")
    public List<Map<String, Object>> getAllRooms() {
        return jdbcTemplate.queryForList("SELECT * FROM Hotel_Room");
    }

    @GetMapping("/rooms/{roomID}")
    public Map<String, Object> getRoom(@PathVariable Integer roomID) {
        return jdbcTemplate.queryForMap("SELECT * FROM Hotel_Room WHERE roomID = ?", roomID);
    }

    @PostMapping("/rooms")
    public String insertRoom(@RequestBody Map<String, Object> payload) {
        String query = "INSERT INTO Hotel_Room (roomID, hotel_ID, roomNumber, Price, Room_Status, Extendable, Room_View, Capacity, Problems_Damages) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(query, payload.get("roomID"), payload.get("hotel_ID"), payload.get("roomNumber"), payload.get("Price"), payload.get("Room_Status"), payload.get("Extendable"), payload.get("Room_View"), payload.get("Capacity"), payload.get("Problems_Damages"));
        return "Room created successfully";
    }

    @PutMapping("/rooms/{roomID}")
    public String updateRoom(@PathVariable Integer roomID, @RequestBody Map<String, Object> payload) {
        String query = "UPDATE Hotel_Room SET hotel_ID = ?, roomNumber = ?, Price = ?, Room_Status = ?, Extendable = ?, Room_View = ?, Capacity = ?, Problems_Damages = ? WHERE roomID = ?";
        jdbcTemplate.update(query, payload.get("hotel_ID"), payload.get("roomNumber"), payload.get("Price"), payload.get("Room_Status"), payload.get("Extendable"), payload.get("Room_View"), payload.get("Capacity"), payload.get("Problems_Damages"), roomID);
        return "Room updated successfully";
    }

    @DeleteMapping("/rooms/{roomID}")
    public String deleteRoom(@PathVariable Integer roomID) {
        jdbcTemplate.update("DELETE FROM Hotel_Room WHERE roomID = ?", roomID);
        return "Room deleted successfully";
    }
}
