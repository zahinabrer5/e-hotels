package org.uncreatives.e_hotels.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@CrossOrigin("*")
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
                       "VALUES (COALESCE((SELECT MAX(bookingID) FROM Booking), 0) + 1, ?, ?, ?::date, ?::date, CURRENT_TIMESTAMP)";
                       // ^ use COALESCE to get generate the next ID
        jdbcTemplate.update(query, custId, roomId, startDate, endDate);
        return "Booking created successfully!";
    }

    // Employee coverts a booking to renting (or creates direct renting)
    @Transactional
    @PostMapping("/rent")
    @Operation(summary = "Execute a Renting/Check-In", description = "Employee checks in a customer by converting an existing booking to a renting or directly creating a new renting")
    public String executeRenting(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Direct Renting (No previous booking)", value = "{\n  \"bookingId\": null,\n  \"custId\": \"222-000-001\",\n  \"roomId\": 1,\n  \"employeeId\": \"111-000-001\",\n  \"checkInDate\": \"2026-05-01\",\n  \"checkOutDate\": \"2026-05-15\",\n  \"paymentAmount\": 1500.00\n}"),
                    @ExampleObject(name = "Convert Booking to Renting", value = "{\n  \"bookingId\": 10,\n  \"custId\": \"222-000-001\",\n  \"roomId\": 1,\n  \"employeeId\": \"111-000-001\",\n  \"checkInDate\": \"2026-05-01\",\n  \"checkOutDate\": \"2026-05-15\",\n  \"paymentAmount\": 1500.00\n}")
            }))
            @RequestBody Map<String, Object> payload) {
        Integer bookingId = toInteger(readFirstValue(payload, "bookingId", "bookingID", "booking_id"), "bookingId", false);
        String custId = readOptionalString(payload, "custId", "custID", "customerId", "customerID");
        Integer roomId = toInteger(readFirstValue(payload, "roomId", "roomID", "room_id"), "roomId", false);
        String employeeId = readRequiredString(payload, "employeeId", "employeeID", "employee_id");
        String checkIn = readRequiredString(payload, "checkInDate", "checkinDate", "check_in_date");
        String checkOut = readRequiredString(payload, "checkOutDate", "checkoutDate", "check_out_date");
        Double paymentAmount = toDouble(readFirstValue(payload, "paymentAmount", "payment_amount"), "paymentAmount", true);

        if (paymentAmount < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentAmount cannot be negative");
        }

        if (bookingId != null) {
            Map<String, Object> bookingRow;
            try {
                bookingRow = jdbcTemplate.queryForMap(
                        "SELECT custID, roomID FROM Booking WHERE bookingID = ?",
                        bookingId
                );
            } catch (EmptyResultDataAccessException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookingId does not reference an existing booking", ex);
            }

            String bookingCustId = readRequiredString(bookingRow, "custid", "custID");
            Integer bookingRoomId = toInteger(readFirstValue(bookingRow, "roomid", "roomID"), "roomId", true);

            if (custId == null || custId.isBlank() || !bookingCustId.equals(custId)) {
                custId = bookingCustId;
            }
            if (roomId == null || !bookingRoomId.equals(roomId)) {
                roomId = bookingRoomId;
            }

            Integer convertedCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM Renting WHERE bookingID = ?",
                    Integer.class,
                    bookingId
            );

            if (convertedCount != null && convertedCount > 0) {
                jdbcTemplate.update("DELETE FROM Booking WHERE bookingID = ?", bookingId);
                return "Booking already had a renting entry. Booking removed from pending list.";
            }
        }

        if (custId == null || custId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing custId");
        }
        if (roomId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing roomId");
        }

        String query = "INSERT INTO Renting (rentingID, bookingID, custID, roomID, National_ID, checkInDate, checkOutDate, paymentAmount) " +
                "VALUES (COALESCE((SELECT MAX(rentingID) FROM Renting), 0) + 1, ?, ?, ?, ?, ?::date, ?::date, ?)";
                // ^ use COALESCE to get generate the next ID

        jdbcTemplate.update(query, bookingId, custId, roomId, employeeId, checkIn, checkOut, paymentAmount);

        if (bookingId != null) {
            jdbcTemplate.update("DELETE FROM Booking WHERE bookingID = ?", bookingId);
        }

        // Update room status after successful check-in.
        jdbcTemplate.update("UPDATE Hotel_Room SET Room_Status = 'Occupied' WHERE roomID = ?", roomId);

        return bookingId != null
                ? "Renting processed and booking converted successfully!"
                : "Renting processed and payment received successfully!";
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
        String custId = readRequiredString(payload, "custID", "custId", "customerID", "customerId");
        String idType = readRequiredString(payload, "ID_type", "idType", "id_type");
        String dateOfRegistration = readRequiredString(payload, "DateOfRegistration", "dateOfRegistration", "date_of_registration");
        String customerName = readRequiredString(payload, "Customer_Name", "customerName", "customer_name");
        String customerAddress = readRequiredString(payload, "Customer_Address", "customerAddress", "customer_address");

        Integer customerCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Customer WHERE custID = ?",
            Integer.class,
            custId
        );

        if (customerCount != null && customerCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer with this custID already exists");
        }

        String query = "INSERT INTO Customer (custID, ID_type, DateOfRegistration, Customer_Name, Customer_Address) VALUES (?, ?, ?::date, ?, ?)";
        jdbcTemplate.update(query, custId, idType, dateOfRegistration, customerName, customerAddress);
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
        String nationalId = readRequiredString(payload, "National_ID", "nationalID", "national_id");
        Integer hotelId = toInteger(readFirstValue(payload, "hotel_ID", "hotelId", "hotelid"), "hotel_ID", true);
        String employeeName = readRequiredString(payload, "Employee_Name", "employeeName", "employee_name");
        String employeeAddress = readOptionalString(payload, "Employee_Address", "employeeAddress", "employee_address");

        Integer hotelCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Hotel WHERE hotel_ID = ?",
            Integer.class,
            hotelId
        );

        if (hotelCount == null || hotelCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hotel_ID does not reference an existing hotel");
        }

        Integer employeeCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Employee WHERE National_ID = ?",
            Integer.class,
            nationalId
        );

        if (employeeCount != null && employeeCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee with this National_ID already exists");
        }

        String query = "INSERT INTO Employee (National_ID, hotel_ID, Employee_Name, Employee_Address) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(query, nationalId, hotelId, employeeName, employeeAddress);
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
        Integer hotelId = toInteger(readFirstValue(payload, "hotel_ID", "hotelId", "hotelid"), "hotel_ID", true);
        String employeeName = readRequiredString(payload, "Employee_Name", "employeeName", "employee_name");
        String employeeAddress = readOptionalString(payload, "Employee_Address", "employeeAddress", "employee_address");

        Integer hotelCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Hotel WHERE hotel_ID = ?",
            Integer.class,
            hotelId
        );

        if (hotelCount == null || hotelCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hotel_ID does not reference an existing hotel");
        }

        String query = "UPDATE Employee SET hotel_ID = ?, Employee_Name = ?, Employee_Address = ? WHERE National_ID = ?";
        jdbcTemplate.update(query, hotelId, employeeName, employeeAddress, nationalID);
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
        Integer hotelId = toInteger(readFirstValue(payload, "hotel_ID", "hotelId", "hotelID", "hotel_id", "hotelid"), "hotel_ID", true);
        Integer chainId = toInteger(readFirstValue(payload, "chainID", "chainId", "chainid", "chain_id"), "chainID", true);
        String address = readRequiredString(payload, "Hotel_Address", "Address", "hotelAddress", "address", "hotel_address");
        Integer rating = toInteger(readFirstValue(payload, "Rating", "Category_Rating", "categoryRating", "rating"), "Rating", true);
        String email = readOptionalString(payload, "Email", "email");
        String managerNationalId = readOptionalString(payload, "Manager_National_ID", "managerNationalId", "managerId", "manager_national_id");

        if (email == null || email.isBlank()) {
            email = "hotel-" + hotelId + "@ehotels.local";
        }
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        Integer hotelCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Hotel WHERE hotel_ID = ?",
                Integer.class,
                hotelId
        );

        if (hotelCount != null && hotelCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hotel with this hotel_ID already exists");
        }

        Integer chainCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Hotel_Chain WHERE chainID = ?",
                Integer.class,
                chainId
        );

        if (chainCount == null || chainCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chainID does not reference an existing hotel chain");
        }

        managerNationalId = resolveManagerNationalIdOrNull(managerNationalId);

        String query = "INSERT INTO Hotel (hotel_ID, chainID, Email, Hotel_Address, Rating, Manager_National_ID) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(query, hotelId, chainId, email, address, rating, managerNationalId);
        return "Hotel created successfully";
    }

    @PutMapping("/hotels/{hotelID}")
    public String updateHotel(@PathVariable Integer hotelID, @RequestBody Map<String, Object> payload) {
        Map<String, Object> existingHotel = jdbcTemplate.queryForMap("SELECT * FROM Hotel WHERE hotel_ID = ?", hotelID);

        Integer chainId = toInteger(
                readFirstValue(payload, "chainID", "chainId", "chainid", "chain_id"),
                "chainID",
                false
        );
        String email = readOptionalString(payload, "Email", "email");
        String address = readOptionalString(payload, "Hotel_Address", "Address", "hotelAddress", "address", "hotel_address");
        Integer rating = toInteger(readFirstValue(payload, "Rating", "Category_Rating", "categoryRating", "rating"), "Rating", false);
        String managerNationalId = readOptionalString(payload, "Manager_National_ID", "managerNationalId", "managerId", "manager_national_id");

        if (chainId == null) {
            chainId = toInteger(readFirstValue(existingHotel, "chain_id", "chainid"), "chainID", true);
        }
        if (email == null || email.isBlank()) {
            email = readRequiredString(existingHotel, "email");
        }
        if (address == null || address.isBlank()) {
            address = readRequiredString(existingHotel, "hotel_address");
        }
        if (rating == null) {
            rating = toInteger(readFirstValue(existingHotel, "rating"), "Rating", true);
        }
        if (managerNationalId == null || managerNationalId.isBlank()) {
            managerNationalId = readOptionalString(existingHotel, "manager_national_id");
        }

        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        Integer chainCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Hotel_Chain WHERE chainID = ?",
                Integer.class,
                chainId
        );

        if (chainCount == null || chainCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "chainID does not reference an existing hotel chain");
        }

        managerNationalId = resolveManagerNationalIdOrNull(managerNationalId);

        String query = "UPDATE Hotel SET chainID = ?, Email = ?, Hotel_Address = ?, Rating = ?, Manager_National_ID = ? WHERE hotel_ID = ?";
        int updatedRows = jdbcTemplate.update(query, chainId, email, address, rating, managerNationalId, hotelID);
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found");
        }
        return "Hotel updated successfully";
    }

    @DeleteMapping("/hotels/{hotelID}")
    public String deleteHotel(@PathVariable Integer hotelID) {
        int deletedRows = jdbcTemplate.update("DELETE FROM Hotel WHERE hotel_ID = ?", hotelID);
        if (deletedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found");
        }
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
        Integer requestedRoomId = toInteger(readFirstValue(payload, "roomID", "roomId", "room_id", "roomid"), "roomID", false);
        Integer roomId = requestedRoomId != null ? requestedRoomId : nextRoomId();
        Integer hotelId = toInteger(readFirstValue(payload, "hotel_ID", "hotelId", "hotelID", "hotel_id", "hotelid"), "hotel_ID", true);
        String roomNumber = readOptionalString(payload, "roomNumber", "room_number", "roomnumber");
        boolean roomNumberDerivedFromRoomId = roomNumber == null || roomNumber.isBlank();
        Double price = toDouble(readFirstValue(payload, "Price", "price"), "Price", true);
        String roomStatus = readOptionalString(payload, "Room_Status", "roomStatus", "room_status", "roomstatus");
        Boolean extendable = toBoolean(readFirstValue(payload, "Extendable", "extendable"), "Extendable", false);
        String roomView = readOptionalString(payload, "Room_View", "View_Type", "roomView", "viewType", "room_view", "roomview");
        Integer capacity = toInteger(readFirstValue(payload, "Capacity", "capacity"), "Capacity", false);
        String problemsDamages = readOptionalString(payload, "Problems_Damages", "problemsDamages", "problems_damages");

        if (roomNumberDerivedFromRoomId) {
            roomNumber = String.valueOf(roomId);
        }
        if (roomStatus == null || roomStatus.isBlank()) {
            roomStatus = "Available";
        }
        if (extendable == null) {
            extendable = Boolean.FALSE;
        }

        Integer hotelCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Hotel WHERE hotel_ID = ?",
                Integer.class,
                hotelId
        );

        if (hotelCount == null || hotelCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hotel_ID does not reference an existing hotel");
        }

        Integer roomCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Hotel_Room WHERE roomID = ?",
                Integer.class,
                roomId
        );

        boolean generatedRoomId = requestedRoomId == null;
        boolean generatedRoomNumber = roomNumberDerivedFromRoomId;

        if (roomCount != null && roomCount > 0) {
            roomId = nextRoomId();
            generatedRoomId = true;
            if (roomNumberDerivedFromRoomId) {
                roomNumber = String.valueOf(roomId);
                generatedRoomNumber = true;
            }
        }

        Integer roomNumberCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Hotel_Room WHERE hotel_ID = ? AND roomNumber = ?",
                Integer.class,
                hotelId,
                roomNumber
        );

        if (roomNumberCount != null && roomNumberCount > 0) {
            roomNumber = nextAvailableRoomNumber(hotelId, roomNumber);
            generatedRoomNumber = true;
        }

        String query = "INSERT INTO Hotel_Room (roomID, hotel_ID, roomNumber, Price, Room_Status, Extendable, Room_View, Capacity, Problems_Damages) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(query, roomId, hotelId, roomNumber, price, roomStatus, extendable, roomView, capacity, problemsDamages);
        } catch (DataIntegrityViolationException ex) {
            throw toRoomConstraintException(ex, roomId, hotelId, roomNumber);
        }

        if (generatedRoomId && generatedRoomNumber) {
            return "Room created successfully with generated roomID " + roomId + " and roomNumber " + roomNumber;
        }
        if (generatedRoomId) {
            return "Room created successfully with generated roomID " + roomId;
        }
        if (generatedRoomNumber) {
            return "Room created successfully with generated roomNumber " + roomNumber;
        }

        return "Room created successfully";
    }

    @PutMapping("/rooms/{roomID}")
    public String updateRoom(@PathVariable Integer roomID, @RequestBody Map<String, Object> payload) {
        Map<String, Object> existingRoom = jdbcTemplate.queryForMap("SELECT * FROM Hotel_Room WHERE roomID = ?", roomID);

        Integer hotelId = toInteger(readFirstValue(payload, "hotel_ID", "hotelId", "hotelID", "hotel_id", "hotelid"), "hotel_ID", false);
        String roomNumber = readOptionalString(payload, "roomNumber", "room_number", "roomnumber");
        Double price = toDouble(readFirstValue(payload, "Price", "price"), "Price", false);
        String roomStatus = readOptionalString(payload, "Room_Status", "roomStatus", "room_status", "roomstatus");
        Boolean extendable = toBoolean(readFirstValue(payload, "Extendable", "extendable"), "Extendable", false);
        String roomView = readOptionalString(payload, "Room_View", "View_Type", "roomView", "viewType", "room_view", "roomview");
        Integer capacity = toInteger(readFirstValue(payload, "Capacity", "capacity"), "Capacity", false);
        String problemsDamages = readOptionalString(payload, "Problems_Damages", "problemsDamages", "problems_damages");

        if (hotelId == null) {
            hotelId = toInteger(readFirstValue(existingRoom, "hotel_id", "hotelid"), "hotel_ID", true);
        }
        if (roomNumber == null || roomNumber.isBlank()) {
            roomNumber = readRequiredString(existingRoom, "roomnumber");
        }
        if (price == null) {
            price = toDouble(readFirstValue(existingRoom, "price"), "Price", true);
        }
        if (roomStatus == null || roomStatus.isBlank()) {
            roomStatus = readOptionalString(existingRoom, "room_status");
        }
        if (extendable == null) {
            extendable = toBoolean(readFirstValue(existingRoom, "extendable"), "Extendable", false);
            if (extendable == null) {
                extendable = Boolean.FALSE;
            }
        }
        if (roomView == null || roomView.isBlank()) {
            roomView = readOptionalString(existingRoom, "room_view");
        }
        if (capacity == null) {
            capacity = toInteger(readFirstValue(existingRoom, "capacity"), "Capacity", false);
        }
        if (problemsDamages == null) {
            problemsDamages = readOptionalString(existingRoom, "problems_damages");
        }

        Integer hotelCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Hotel WHERE hotel_ID = ?",
                Integer.class,
                hotelId
        );

        if (hotelCount == null || hotelCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hotel_ID does not reference an existing hotel");
        }

        Integer roomNumberConflictCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Hotel_Room WHERE hotel_ID = ? AND roomNumber = ? AND roomID <> ?",
                Integer.class,
                hotelId,
                roomNumber,
                roomID
        );

        if (roomNumberConflictCount != null && roomNumberConflictCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room number " + roomNumber + " already exists for hotel_ID " + hotelId
            );
        }

        String query = "UPDATE Hotel_Room SET hotel_ID = ?, roomNumber = ?, Price = ?, Room_Status = ?, Extendable = ?, Room_View = ?, Capacity = ?, Problems_Damages = ? WHERE roomID = ?";
        try {
            int updatedRows = jdbcTemplate.update(query, hotelId, roomNumber, price, roomStatus, extendable, roomView, capacity, problemsDamages, roomID);
            if (updatedRows == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
            }
        } catch (DataIntegrityViolationException ex) {
            throw toRoomConstraintException(ex, roomID, hotelId, roomNumber);
        }
        return "Room updated successfully";
    }

    @DeleteMapping("/rooms/{roomID}")
    public String deleteRoom(@PathVariable Integer roomID) {
        int deletedRows = jdbcTemplate.update("DELETE FROM Hotel_Room WHERE roomID = ?", roomID);
        if (deletedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        return "Room deleted successfully";
    }

    // Bookings
    @GetMapping("/bookings")
    public List<Map<String, Object>> getAllBookings() {
        String query = """
            SELECT
                b.bookingID AS "bookingId",
                b.custID AS "custId",
                b.roomID AS "roomId",
                b.startDate AS "startDate",
                b.endDate AS "endDate",
                b.bookingTime AS "bookingTime",
                EXISTS (
                    SELECT 1
                    FROM Renting r
                    WHERE r.bookingID = b.bookingID
                ) AS "alreadyConverted"
            FROM Booking b
            ORDER BY "alreadyConverted", b.startDate, b.bookingID
            """;

        return jdbcTemplate.queryForList(query);
    }

    @GetMapping("/bookings/{bookingID}")
    public Map<String, Object> getBooking(@PathVariable Integer bookingID) {
        String query = """
            SELECT
                b.bookingID AS "bookingId",
                b.custID AS "custId",
                b.roomID AS "roomId",
                b.startDate AS "startDate",
                b.endDate AS "endDate",
                b.bookingTime AS "bookingTime"
            FROM Booking b
            WHERE b.bookingID = ?
            """;

        return jdbcTemplate.queryForMap(query, bookingID);
    }

    @GetMapping("/rentings")
    public List<Map<String, Object>> getAllRentings() {
        String query = """
            SELECT
                r.rentingID AS "rentingId",
                r.bookingID AS "bookingId",
                r.custID AS "custId",
                r.roomID AS "roomId",
                r.National_ID AS "employeeId",
                r.checkInDate AS "checkInDate",
                r.checkOutDate AS "checkOutDate",
                r.paymentAmount AS "paymentAmount"
            FROM Renting r
            ORDER BY r.checkInDate DESC, r.rentingID DESC
            """;

        return jdbcTemplate.queryForList(query);
    }

    @GetMapping("/rentings/{rentingID}")
    public Map<String, Object> getRenting(@PathVariable Integer rentingID) {
        String query = """
            SELECT
                r.rentingID AS "rentingId",
                r.bookingID AS "bookingId",
                r.custID AS "custId",
                r.roomID AS "roomId",
                r.National_ID AS "employeeId",
                r.checkInDate AS "checkInDate",
                r.checkOutDate AS "checkOutDate",
                r.paymentAmount AS "paymentAmount"
            FROM Renting r
            WHERE r.rentingID = ?
            """;

        return jdbcTemplate.queryForMap(query, rentingID);
    }

    // @PostMapping("/bookings")
    // public String insertBooking(@RequestBody Map<String, Object> payload) {
    //     String query = "INSERT INTO Booking (bookingID, custID, roomID, startDate, endDate, bookingTime) " +
    //                    "VALUES (COALESCE((SELECT MAX(bookingID) FROM Booking), 0) + 1, ?, ?, ?::date, ?::date, CURRENT_TIMESTAMP)";
    //     jdbcTemplate.update(query, payload.get("custID"), payload.get("roomID"), payload.get("startDate"), payload.get("endDate"));
    //     return "Booking created successfully";
    // }

    // @PutMapping("/bookings/{bookingID}")
    // public String updateBooking(@PathVariable Integer bookingID, @RequestBody Map<String, Object> payload) {
    //     String query = "UPDATE Booking SET custID = ?, roomID = ?, startDate = ?::date, endDate = ?::date WHERE bookingID = ?";
    //     jdbcTemplate.update(query, payload.get("custID"), payload.get("roomID"), payload.get("startDate"), payload.get("endDate"), bookingID);
    //     return "Booking updated successfully";
    // }

    // @DeleteMapping("/bookings/{bookingID}")
    // public String deleteBooking(@PathVariable Integer bookingID) {
    //     jdbcTemplate.update("DELETE FROM Booking WHERE bookingID = ?", bookingID);
    //     return "Booking deleted successfully";
    // }

    private ResponseStatusException toRoomConstraintException(
            DataIntegrityViolationException ex,
            Integer roomId,
            Integer hotelId,
            String roomNumber
    ) {
        Throwable rootCause = ex.getMostSpecificCause();
        String rootMessage = rootCause != null ? rootCause.getMessage() : ex.getMessage();
        String normalized = rootMessage == null ? "" : rootMessage.toLowerCase();

        if (normalized.contains("hotel_room_hotel_id_roomnumber_key")) {
            return new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room number " + roomNumber + " already exists for hotel_ID " + hotelId,
                    ex
            );
        }

        if (normalized.contains("hotel_room_pkey")) {
            return new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room with roomID " + roomId + " already exists",
                    ex
            );
        }

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Room payload violates database constraints",
                ex
        );
    }

    private String resolveManagerNationalIdOrNull(String managerNationalId) {
        if (managerNationalId == null || managerNationalId.isBlank()) {
            return null;
        }

        Integer managerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Employee WHERE National_ID = ?",
                Integer.class,
                managerNationalId
        );

        if (managerCount == null || managerCount == 0) {
            return null;
        }

        return managerNationalId;
    }

    private Integer nextRoomId() {
        Integer maxRoomId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(roomID), 0) FROM Hotel_Room",
                Integer.class
        );

        return (maxRoomId == null ? 0 : maxRoomId) + 1;
    }

    private String nextAvailableRoomNumber(Integer hotelId, String requestedRoomNumber) {
        String preferred = requestedRoomNumber == null ? "" : requestedRoomNumber.trim();
        if (preferred.isEmpty()) {
            preferred = "1";
        }

        if (preferred.matches("\\d+")) {
            int candidate = Math.max(Integer.parseInt(preferred), 1);
            while (roomNumberExists(hotelId, String.valueOf(candidate))) {
                candidate++;
            }
            return String.valueOf(candidate);
        }

        if (!roomNumberExists(hotelId, preferred)) {
            return preferred;
        }

        int suffix = 2;
        String candidate = preferred + "-" + suffix;
        while (roomNumberExists(hotelId, candidate)) {
            suffix++;
            candidate = preferred + "-" + suffix;
        }
        return candidate;
    }

    private boolean roomNumberExists(Integer hotelId, String roomNumber) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Hotel_Room WHERE hotel_ID = ? AND roomNumber = ?",
                Integer.class,
                hotelId,
                roomNumber
        );

        return count != null && count > 0;
    }

    private Integer toInteger(Object value, String fieldName, boolean required) {
        if (value == null) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
            }
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
            }
            return null;
        }

        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName);
        }
    }

    private Double toDouble(Object value, String fieldName, boolean required) {
        if (value == null) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
            }
            return null;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
            }
            return null;
        }

        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName);
        }
    }

    private Boolean toBoolean(Object value, String fieldName, boolean required) {
        if (value == null) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
            }
            return null;
        }

        if (value instanceof Boolean bool) {
            return bool;
        }

        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
            }
            return null;
        }

        if ("true".equalsIgnoreCase(raw) || "1".equals(raw)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(raw) || "0".equals(raw)) {
            return Boolean.FALSE;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName);
    }

    private Object readFirstValue(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            if (payload.containsKey(key)) {
                Object value = payload.get(key);
                if (value != null && !String.valueOf(value).trim().isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    private String readRequiredString(Map<String, Object> payload, String... keys) {
        String fieldName = keys.length > 0 ? keys[0] : "field";
        Object value = readFirstValue(payload, keys);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
        }

        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
        }

        return raw;
    }

    private String readOptionalString(Map<String, Object> payload, String... keys) {
        Object value = readFirstValue(payload, keys);
        if (value == null) {
            return null;
        }
        return String.valueOf(value).trim();
    }
}
