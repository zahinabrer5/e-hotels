-- ==============================================================================
-- Build e-Hotels Database Tables (Part 2a)
-- ==============================================================================

CREATE TABLE Hotel_Chain (
    chainID INT PRIMARY KEY,
    CentralOfficeAddress VARCHAR(255) NOT NULL,
    ChainName VARCHAR(100) NOT NULL,
    Number_Of_Hotels INT DEFAULT 0
);

CREATE TABLE Chain_Email (
    chainID INT,
    Email VARCHAR(100),
    PRIMARY KEY (chainID, Email),
    FOREIGN KEY (chainID) REFERENCES Hotel_Chain(chainID) ON DELETE CASCADE
);

CREATE TABLE Chain_PhoneNumber (
    chainID INT,
    PhoneNumber VARCHAR(20),
    PRIMARY KEY (chainID, PhoneNumber),
    FOREIGN KEY (chainID) REFERENCES Hotel_Chain(chainID) ON DELETE CASCADE
);

-- Note: Manager_National_ID foreign key is added later via ALTER TABLE
-- to avoid circular dependency errors with the Employee table during creation
CREATE TABLE Hotel (
    hotel_ID INT PRIMARY KEY,
    chainID INT NOT NULL,
    Email VARCHAR(100) NOT NULL,
    Hotel_Address VARCHAR(255) NOT NULL,
    Rating INT CHECK (Rating BETWEEN 1 AND 5),
    Manager_National_ID VARCHAR(15),
    FOREIGN KEY (chainID) REFERENCES Hotel_Chain(chainID) ON DELETE CASCADE
);

CREATE TABLE Hotel_PhoneNumber (
    hotel_ID INT,
    PhoneNumber VARCHAR(20),
    PRIMARY KEY (hotel_ID, PhoneNumber),
    FOREIGN KEY (hotel_ID) REFERENCES Hotel(hotel_ID) ON DELETE CASCADE
);

CREATE TABLE Employee (
    National_ID VARCHAR(15) PRIMARY KEY,
    hotel_ID INT NOT NULL,
    Employee_Name VARCHAR(100) NOT NULL,
    Employee_Address VARCHAR(255),
    FOREIGN KEY (hotel_ID) REFERENCES Hotel(hotel_ID) ON DELETE CASCADE
);

ALTER TABLE Hotel
ADD CONSTRAINT fk_hotel_manager
FOREIGN KEY (Manager_National_ID) REFERENCES Employee(National_ID) ON DELETE SET NULL;

CREATE TABLE Employee_Role (
    National_ID VARCHAR(15),
    Role_Name VARCHAR(50),
    PRIMARY KEY (National_ID, Role_Name),
    FOREIGN KEY (National_ID) REFERENCES Employee(National_ID) ON DELETE CASCADE
);

CREATE TABLE Hotel_Room (
    roomID INT PRIMARY KEY,
    hotel_ID INT NOT NULL,
    roomNumber VARCHAR(10) NOT NULL,
    Price DECIMAL(10, 2) CHECK (Price > 0),
    Room_Status VARCHAR(50),
    Extendable BOOLEAN,
    Room_View VARCHAR(100),
    Capacity INT, -- 1 for single, 2 for double, to allow for numeric aggregation
    Problems_Damages TEXT, -- Tracks problems or damages in the room
    UNIQUE (hotel_ID, roomNumber), -- Ensures room numbers are unique per hotel
    FOREIGN KEY (hotel_ID) REFERENCES Hotel(hotel_ID) ON DELETE CASCADE
);

CREATE TABLE Hotel_Room_Amenity (
    roomID INT,
    Amenity VARCHAR(100),
    PRIMARY KEY (roomID, Amenity),
    FOREIGN KEY (roomID) REFERENCES Hotel_Room(roomID) ON DELETE CASCADE
);

CREATE TABLE Customer (
    custID VARCHAR(20) PRIMARY KEY,
    ID_type VARCHAR(20) CHECK (ID_type IN ('SSN', 'SIN', 'driving licence')),
    DateOfRegistration DATE,
    Customer_Name VARCHAR(100) NOT NULL,
    Customer_Address VARCHAR(255)
);

CREATE TABLE Booking (
    bookingID INT PRIMARY KEY,
    custID VARCHAR(20),
    roomID INT,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    bookingTime TIMESTAMP,
    CHECK (startDate < endDate),
    FOREIGN KEY (custID) REFERENCES Customer(custID) ON DELETE SET NULL,
    FOREIGN KEY (roomID) REFERENCES Hotel_Room(roomID) ON DELETE SET NULL
);

CREATE TABLE Renting (
    rentingID INT PRIMARY KEY,
    bookingID INT, -- Reference to the original booking for convenience
    custID VARCHAR(20),
    roomID INT,
    National_ID VARCHAR(15),
    checkInDate DATE NOT NULL,
    checkOutDate DATE NOT NULL,
    paymentAmount DECIMAL(10, 2) CHECK (paymentAmount >= 0),
    CHECK (checkInDate < checkOutDate),
    FOREIGN KEY (bookingID) REFERENCES Booking(bookingID) ON DELETE SET NULL,
    FOREIGN KEY (custID) REFERENCES Customer(custID) ON DELETE SET NULL,
    FOREIGN KEY (roomID) REFERENCES Hotel_Room(roomID) ON DELETE SET NULL,
    FOREIGN KEY (National_ID) REFERENCES Employee(National_ID) ON DELETE SET NULL
);

CREATE TABLE Archive (
    archiveID INT PRIMARY KEY,
    chainID INT,
    hotel_ID INT,
    recordType VARCHAR(20) CHECK (recordType IN ('Booking', 'Renting')),
    customerName VARCHAR(100) NOT NULL,
    customerIDType VARCHAR(20), -- Tracks if it was SSN, SIN, or DL
    roomNumber VARCHAR(10) NOT NULL,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    paymentAmount DECIMAL(10, 2), -- Only populated if recordType is 'Renting'
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Tracking when it was archived
    FOREIGN KEY (chainID) REFERENCES Hotel_Chain(chainID) ON DELETE SET NULL,
    FOREIGN KEY (hotel_ID) REFERENCES Hotel(hotel_ID) ON DELETE SET NULL
);


-- ==============================================================================
-- Triggers (Part 2d)
-- ==============================================================================

-- Trigger 1: Automatically maintain the Number_Of_Hotels count in Hotel_Chain
CREATE OR REPLACE FUNCTION update_hotel_chain_count_func()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE Hotel_Chain
        SET Number_Of_Hotels = Number_Of_Hotels + 1
        WHERE chainID = NEW.chainID;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE Hotel_Chain
        SET Number_Of_Hotels = Number_Of_Hotels - 1
        WHERE chainID = OLD.chainID;
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' AND NEW.chainID IS DISTINCT FROM OLD.chainID THEN
        UPDATE Hotel_Chain
        SET Number_Of_Hotels = Number_Of_Hotels - 1
        WHERE chainID = OLD.chainID;

        UPDATE Hotel_Chain
        SET Number_Of_Hotels = Number_Of_Hotels + 1
        WHERE chainID = NEW.chainID;
        RETURN NEW;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_hotel_chain_count_trigger
AFTER INSERT OR DELETE OR UPDATE ON Hotel
FOR EACH ROW EXECUTE FUNCTION update_hotel_chain_count_func();


-- Trigger 2: Prevent double bookings for the same room with overlapping dates
CREATE OR REPLACE FUNCTION prevent_double_booking_func()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM Booking
        WHERE roomID = NEW.roomID
          -- Skip checking against the same booking record if updating
          AND bookingID != COALESCE(NEW.bookingID, -1)
          AND (NEW.startDate < endDate AND NEW.endDate > startDate)
    ) THEN
        RAISE EXCEPTION 'Room % is already booked for the requested dates!', NEW.roomID;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_double_booking_trigger
BEFORE INSERT OR UPDATE ON Booking
FOR EACH ROW EXECUTE FUNCTION prevent_double_booking_func();


-- ==============================================================================
-- Indexes (Part 2e)
-- ==============================================================================

-- Index 1: Booking dates and room
CREATE INDEX idx_booking_room_dates ON Booking (roomID, startDate, endDate);
-- This will significantly improve performance as the database increases in size.
-- Specifically, it helps reduce the amount of time needed to check for overlaps
-- in prevent_double_booking_func.
-- Without this index, the database would eventually have to scan the entire
-- Booking table whenever the user tries to book a room.

-- Index 2: Find Hotel Rooms by Capacity and Price
CREATE INDEX idx_room_search ON Hotel_Room (hotel_ID, Capacity, Price);
-- The most common kind of query would look like "Find single rooms (Capacity=1)
-- in hotel X with price less than $150."
-- This index will significantly reduce the amount of time taken for these kinds
-- of queries.

-- Index 3: Find Renting by Customer
CREATE INDEX idx_customer_renting ON Renting (custID, checkInDate);
-- This index will reduce the time required for generating things like invoices
-- or user dashboards, which require finding the rentings issued by a certain
-- customer.


-- ==============================================================================
-- Views (Part 2f)
-- ==============================================================================

-- View 1: Number of available rooms per area
-- Assumption: Use Hotel_Address as the "area" mentioned in the description for 2f
CREATE VIEW Available_Rooms_Per_Area AS
SELECT
    h.Hotel_Address AS Area,
    COUNT(r.roomID) AS Available_Rooms
FROM Hotel_Room r
JOIN Hotel h ON r.hotel_ID = h.hotel_ID
WHERE r.Room_Status = 'Available'
GROUP BY h.Hotel_Address;

-- View 2: Aggregated capacity of all the rooms of a specific hotel
CREATE VIEW Hotel_Capacity AS
SELECT
    h.hotel_ID,
    h.Hotel_Address,
    COALESCE(SUM(r.Capacity), 0) AS Total_Capacity
FROM Hotel h
LEFT JOIN Hotel_Room r ON h.hotel_ID = r.hotel_ID
GROUP BY h.hotel_ID, h.Hotel_Address;
