-- ==============================================================================
-- e-Hotels Master Data Population Script
-- ==============================================================================

-- 1. HOTEL CHAINS
INSERT INTO Hotel_Chain (chainID, CentralOfficeAddress, ChainName, Number_Of_Hotels) VALUES
(1, '123 Marriott Way, Bethesda, MD', 'Marriott International', 8),
(2, '789 Hilton Blvd, McLean, VA', 'Hilton Worldwide', 8),
(3, '456 Hyatt Center, Chicago, IL', 'Hyatt Hotels', 8),
(4, '321 Wyndham Pl, Parsippany, NJ', 'Wyndham Hotels', 8),
(5, '654 IHG Street, Windsor, UK', 'InterContinental Hotels Group', 8);

-- 2. CHAIN CONTACT INFO
INSERT INTO Chain_Email (chainID, Email) VALUES
(1, 'contact@marriott.com'), (2, 'info@hilton.com'), (3, 'contact@hyatt.com'), 
(4, 'support@wyndham.com'), (5, 'hello@ihg.com');

INSERT INTO Chain_PhoneNumber (chainID, PhoneNumber) VALUES
(1, '1-800-555-0001'), (2, '1-800-555-0002'), (3, '1-800-555-0003'), 
(4, '1-800-555-0004'), (5, '1-800-555-0005');

-- 3. HOTELS 
-- Managers are NULL initially to prevent circular dependency errors.
INSERT INTO Hotel (hotel_ID, chainID, Email, Hotel_Address, Rating, Manager_National_ID) VALUES
(1, 1, 'h1@marriott.com', '100 Rideau St, Ottawa, ON', 5, NULL),
(2, 1, 'h2@marriott.com', '200 Bank St, Ottawa, ON', 4, NULL), 
(3, 1, 'h3@marriott.com', '300 Bay St, Toronto, ON', 3, NULL),
(4, 1, 'h4@marriott.com', '400 Yonge St, Toronto, ON', 5, NULL), 
(5, 1, 'h5@marriott.com', '500 Peel St, Montreal, QC', 4, NULL),
(6, 1, 'h6@marriott.com', '600 Guy St, Montreal, QC', 3, NULL), 
(7, 1, 'h7@marriott.com', '700 Burrard St, Vancouver, BC', 5, NULL),
(8, 1, 'h8@marriott.com', '800 Howe St, Vancouver, BC', 4, NULL), 
(9, 2, 'h9@hilton.com', '110 Centre St, Calgary, AB', 3, NULL),
(10, 2, 'h10@hilton.com', '120 Macleod Tr, Calgary, AB', 5, NULL),
(11, 2, 'h11@hilton.com', '130 Jasper Ave, Edmonton, AB', 4, NULL),
(12, 2, 'h12@hilton.com', '140 Whyte Ave, Edmonton, AB', 3, NULL),
(13, 2, 'h13@hilton.com', '150 Portage Ave, Winnipeg, MB', 5, NULL),
(14, 2, 'h14@hilton.com', '160 Main St, Winnipeg, MB', 4, NULL),
(15, 2, 'h15@hilton.com', '170 Water St, Halifax, NS', 3, NULL),
(16, 2, 'h16@hilton.com', '180 Hollis St, Halifax, NS', 5, NULL),
(17, 3, 'h17@hyatt.com', '210 Douglas St, Victoria, BC', 4, NULL),
(18, 3, 'h18@hyatt.com', '220 Fort St, Victoria, BC', 3, NULL),
(19, 3, 'h19@hyatt.com', '230 8th St, Saskatoon, SK', 5, NULL),
(20, 3, 'h20@hyatt.com', '240 Idylwyld Dr, Saskatoon, SK', 4, NULL),
(21, 3, 'h21@hyatt.com', '250 Albert St, Regina, SK', 3, NULL),
(22, 3, 'h22@hyatt.com', '260 Broad St, Regina, SK', 5, NULL),
(23, 3, 'h23@hyatt.com', '270 Grande Allee, Quebec, QC', 4, NULL),
(24, 3, 'h24@hyatt.com', '280 St Jean, Quebec, QC', 3, NULL),
(25, 4, 'h25@wyndham.com', '310 Water St, St Johns, NL', 5, NULL),
(26, 4, 'h26@wyndham.com', '320 Duckworth St, St Johns, NL', 4, NULL),
(27, 4, 'h27@wyndham.com', '330 Queen St, Charlottetown, PE', 3, NULL),
(28, 4, 'h28@wyndham.com', '340 Kent St, Charlottetown, PE', 5, NULL),
(29, 4, 'h29@wyndham.com', '350 King St, Fredericton, NB', 4, NULL),
(30, 4, 'h30@wyndham.com', '360 Queen St, Fredericton, NB', 3, NULL),
(31, 4, 'h31@wyndham.com', '370 Main St, Moncton, NB', 5, NULL),
(32, 4, 'h32@wyndham.com', '380 St George, Moncton, NB', 4, NULL),
(33, 5, 'h33@ihg.com', '410 Bernard Ave, Kelowna, BC', 3, NULL),
(34, 5, 'h34@ihg.com', '420 Water St, Kelowna, BC', 5, NULL),
(35, 5, 'h35@ihg.com', '430 Victoria St, Kamloops, BC', 4, NULL),
(36, 5, 'h36@ihg.com', '440 Seymour St, Kamloops, BC', 3, NULL),
(37, 5, 'h37@ihg.com', '450 Banff Ave, Banff, AB', 5, NULL),
(38, 5, 'h38@ihg.com', '460 Bear St, Banff, AB', 4, NULL),
(39, 5, 'h39@ihg.com', '470 Connaught Dr, Jasper, AB', 3, NULL),
(40, 5, 'h40@ihg.com', '480 Patricia St, Jasper, AB', 5, NULL);

-- 4. HOTEL PHONE NUMBERS
INSERT INTO Hotel_PhoneNumber (hotel_ID, PhoneNumber) VALUES
(1, '613-555-0101'), (2, '613-555-0102'), (3, '416-555-0103'), (4, '416-555-0104'),
(5, '514-555-0105'), (6, '514-555-0106'), (7, '604-555-0107'), (8, '604-555-0108'),
(9, '403-555-0109'), (10, '403-555-0110'), (11, '780-555-0111'), (12, '780-555-0112'),
(13, '204-555-0113'), (14, '204-555-0114'), (15, '902-555-0115'), (16, '902-555-0116'),
(17, '250-555-0117'), (18, '250-555-0118'), (19, '306-555-0119'), (20, '306-555-0120'),
(21, '306-555-0121'), (22, '306-555-0122'), (23, '418-555-0123'), (24, '418-555-0124'),
(25, '709-555-0125'), (26, '709-555-0126'), (27, '902-555-0127'), (28, '902-555-0128'),
(29, '506-555-0129'), (30, '506-555-0130'), (31, '506-555-0131'), (32, '506-555-0132'),
(33, '250-555-0133'), (34, '250-555-0134'), (35, '250-555-0135'), (36, '250-555-0136'),
(37, '403-555-0137'), (38, '403-555-0138'), (39, '780-555-0139'), (40, '780-555-0140');

-- 5. EMPLOYEES
INSERT INTO Employee (National_ID, hotel_ID, Employee_Name, Employee_Address) VALUES
('111-000-001', 1, 'Liam MacDonald', '12 Maple Ave, Ottawa, ON'),
('111-000-002', 2, 'Sophia Tremblay', '45 Elgin St, Ottawa, ON'),
('111-000-003', 3, 'Noah Smith', '88 Queen St W, Toronto, ON'),
('111-000-004', 4, 'Olivia Brown', '102 King St, Toronto, ON'),
('111-000-005', 5, 'William Roy', '55 St Catherine St, Montreal, QC'),
('111-000-006', 6, 'Emma Gagnon', '77 Rene Levesque Blvd, Montreal, QC'),
('111-000-007', 7, 'James Chen', '12 Robson St, Vancouver, BC'),
('111-000-008', 8, 'Charlotte Lee', '99 Granville St, Vancouver, BC'),
('111-000-009', 9, 'Benjamin White', '150 9th Ave SW, Calgary, AB'),
('111-000-010', 10, 'Amelia Taylor', '300 6th Ave SE, Calgary, AB'),
('111-000-011', 11, 'Lucas Anderson', '111 Jasper Ave, Edmonton, AB'),
('111-000-012', 12, 'Mia Thomas', '222 Whyte Ave, Edmonton, AB'),
('111-000-013', 13, 'Henry Jackson', '333 Portage Ave, Winnipeg, MB'),
('111-000-014', 14, 'Isabella Harris', '444 Main St, Winnipeg, MB'),
('111-000-015', 15, 'Alexander Martin', '555 Water St, Halifax, NS'),
('111-000-016', 16, 'Evelyn Thompson', '666 Hollis St, Halifax, NS'),
('111-000-017', 17, 'Michael Garcia', '777 Douglas St, Victoria, BC'),
('111-000-018', 18, 'Harper Martinez', '888 Fort St, Victoria, BC'),
('111-000-019', 19, 'Ethan Robinson', '999 8th St, Saskatoon, SK'),
('111-000-020', 20, 'Abigail Clark', '123 Idylwyld Dr, Saskatoon, SK'),
('111-000-021', 21, 'Daniel Rodriguez', '234 Albert St, Regina, SK'),
('111-000-022', 22, 'Emily Lewis', '345 Broad St, Regina, SK'),
('111-000-023', 23, 'Matthew Walker', '456 Grande Allee, Quebec, QC'),
('111-000-024', 24, 'Elizabeth Hall', '567 St Jean, Quebec, QC'),
('111-000-025', 25, 'Samuel Allen', '678 Water St, St Johns, NL'),
('111-000-026', 26, 'Sofia Young', '789 Duckworth St, St Johns, NL'),
('111-000-027', 27, 'David King', '890 Queen St, Charlottetown, PE'),
('111-000-028', 28, 'Avery Wright', '901 Kent St, Charlottetown, PE'),
('111-000-029', 29, 'Joseph Scott', '112 King St, Fredericton, NB'),
('111-000-030', 30, 'Ella Green', '223 Queen St, Fredericton, NB'),
('111-000-031', 31, 'Carter Baker', '334 Main St, Moncton, NB'),
('111-000-032', 32, 'Chloe Adams', '445 St George, Moncton, NB'),
('111-000-033', 33, 'Owen Nelson', '556 Bernard Ave, Kelowna, BC'),
('111-000-034', 34, 'Victoria Hill', '667 Water St, Kelowna, BC'),
('111-000-035', 35, 'Wyatt Ramirez', '778 Victoria St, Kamloops, BC'),
('111-000-036', 36, 'Grace Campbell', '889 Seymour St, Kamloops, BC'),
('111-000-037', 37, 'John Mitchell', '990 Banff Ave, Banff, AB'),
('111-000-038', 38, 'Zoey Roberts', '101 Bear St, Banff, AB'),
('111-000-039', 39, 'Luke Carter', '202 Connaught Dr, Jasper, AB'),
('111-000-040', 40, 'Penelope Phillips', '303 Patricia St, Jasper, AB'),
('222-000-001', 1, 'Sarah Jenkins', '14 Rideau St, Ottawa, ON'),
('222-000-002', 1, 'Mark Watney', '15 Rideau St, Ottawa, ON'),
('222-000-003', 2, 'Jessica Pearson', '88 Bank St, Ottawa, ON'),
('222-000-004', 3, 'Harvey Specter', '12 Bay St, Toronto, ON'),
('222-000-005', 3, 'Donna Paulsen', '14 Bay St, Toronto, ON'),
('222-000-006', 7, 'Rachel Zane', '55 Burrard St, Vancouver, BC'),
('222-000-007', 7, 'Mike Ross', '56 Burrard St, Vancouver, BC'),
('222-000-008', 9, 'Louis Litt', '22 Centre St, Calgary, AB'),
('222-000-009', 15, 'Katrina Bennett', '99 Water St, Halifax, NS'),
('222-000-010', 25, 'Alex Williams', '12 Duckworth St, St Johns, NL'),
('222-000-011', 37, 'Samantha Wheeler', '88 Banff Ave, Banff, AB'),
('222-000-012', 40, 'Robert Zane', '77 Patricia St, Jasper, AB');

-- 6. ASSIGN MANAGERS (Resolves Circular Dependency)
UPDATE Hotel SET Manager_National_ID = '111-000-001' WHERE hotel_ID = 1;
UPDATE Hotel SET Manager_National_ID = '111-000-002' WHERE hotel_ID = 2;
UPDATE Hotel SET Manager_National_ID = '111-000-003' WHERE hotel_ID = 3;
UPDATE Hotel SET Manager_National_ID = '111-000-004' WHERE hotel_ID = 4;
UPDATE Hotel SET Manager_National_ID = '111-000-005' WHERE hotel_ID = 5;
UPDATE Hotel SET Manager_National_ID = '111-000-006' WHERE hotel_ID = 6;
UPDATE Hotel SET Manager_National_ID = '111-000-007' WHERE hotel_ID = 7;
UPDATE Hotel SET Manager_National_ID = '111-000-008' WHERE hotel_ID = 8;
UPDATE Hotel SET Manager_National_ID = '111-000-009' WHERE hotel_ID = 9;
UPDATE Hotel SET Manager_National_ID = '111-000-010' WHERE hotel_ID = 10;
UPDATE Hotel SET Manager_National_ID = '111-000-011' WHERE hotel_ID = 11;
UPDATE Hotel SET Manager_National_ID = '111-000-012' WHERE hotel_ID = 12;
UPDATE Hotel SET Manager_National_ID = '111-000-013' WHERE hotel_ID = 13;
UPDATE Hotel SET Manager_National_ID = '111-000-014' WHERE hotel_ID = 14;
UPDATE Hotel SET Manager_National_ID = '111-000-015' WHERE hotel_ID = 15;
UPDATE Hotel SET Manager_National_ID = '111-000-016' WHERE hotel_ID = 16;
UPDATE Hotel SET Manager_National_ID = '111-000-017' WHERE hotel_ID = 17;
UPDATE Hotel SET Manager_National_ID = '111-000-018' WHERE hotel_ID = 18;
UPDATE Hotel SET Manager_National_ID = '111-000-019' WHERE hotel_ID = 19;
UPDATE Hotel SET Manager_National_ID = '111-000-020' WHERE hotel_ID = 20;
UPDATE Hotel SET Manager_National_ID = '111-000-021' WHERE hotel_ID = 21;
UPDATE Hotel SET Manager_National_ID = '111-000-022' WHERE hotel_ID = 22;
UPDATE Hotel SET Manager_National_ID = '111-000-023' WHERE hotel_ID = 23;
UPDATE Hotel SET Manager_National_ID = '111-000-024' WHERE hotel_ID = 24;
UPDATE Hotel SET Manager_National_ID = '111-000-025' WHERE hotel_ID = 25;
UPDATE Hotel SET Manager_National_ID = '111-000-026' WHERE hotel_ID = 26;
UPDATE Hotel SET Manager_National_ID = '111-000-027' WHERE hotel_ID = 27;
UPDATE Hotel SET Manager_National_ID = '111-000-028' WHERE hotel_ID = 28;
UPDATE Hotel SET Manager_National_ID = '111-000-029' WHERE hotel_ID = 29;
UPDATE Hotel SET Manager_National_ID = '111-000-030' WHERE hotel_ID = 30;
UPDATE Hotel SET Manager_National_ID = '111-000-031' WHERE hotel_ID = 31;
UPDATE Hotel SET Manager_National_ID = '111-000-032' WHERE hotel_ID = 32;
UPDATE Hotel SET Manager_National_ID = '111-000-033' WHERE hotel_ID = 33;
UPDATE Hotel SET Manager_National_ID = '111-000-034' WHERE hotel_ID = 34;
UPDATE Hotel SET Manager_National_ID = '111-000-035' WHERE hotel_ID = 35;
UPDATE Hotel SET Manager_National_ID = '111-000-036' WHERE hotel_ID = 36;
UPDATE Hotel SET Manager_National_ID = '111-000-037' WHERE hotel_ID = 37;
UPDATE Hotel SET Manager_National_ID = '111-000-038' WHERE hotel_ID = 38;
UPDATE Hotel SET Manager_National_ID = '111-000-039' WHERE hotel_ID = 39;
UPDATE Hotel SET Manager_National_ID = '111-000-040' WHERE hotel_ID = 40;

-- 7. EMPLOYEE ROLES
INSERT INTO Employee_Role (National_ID, Role_Name) VALUES
('111-000-001', 'General Manager'), ('111-000-002', 'General Manager'),
('111-000-003', 'General Manager'), ('111-000-004', 'General Manager'),
('111-000-005', 'General Manager'), ('111-000-006', 'General Manager'),
('111-000-007', 'General Manager'), ('111-000-008', 'General Manager'),
('111-000-009', 'General Manager'), ('111-000-010', 'General Manager'),
('111-000-011', 'General Manager'), ('111-000-012', 'General Manager'),
('111-000-013', 'General Manager'), ('111-000-014', 'General Manager'),
('111-000-015', 'General Manager'), ('111-000-016', 'General Manager'),
('111-000-017', 'General Manager'), ('111-000-018', 'General Manager'),
('111-000-019', 'General Manager'), ('111-000-020', 'General Manager'),
('111-000-021', 'General Manager'), ('111-000-022', 'General Manager'),
('111-000-023', 'General Manager'), ('111-000-024', 'General Manager'),
('111-000-025', 'General Manager'), ('111-000-026', 'General Manager'),
('111-000-027', 'General Manager'), ('111-000-028', 'General Manager'),
('111-000-029', 'General Manager'), ('111-000-030', 'General Manager'),
('111-000-031', 'General Manager'), ('111-000-032', 'General Manager'),
('111-000-033', 'General Manager'), ('111-000-034', 'General Manager'),
('111-000-035', 'General Manager'), ('111-000-036', 'General Manager'),
('111-000-037', 'General Manager'), ('111-000-038', 'General Manager'),
('111-000-039', 'General Manager'), ('111-000-040', 'General Manager'),
('222-000-001', 'Front Desk Agent'), ('222-000-002', 'Housekeeping Supervisor'),
('222-000-003', 'Concierge'), ('222-000-004', 'Front Desk Agent'),
('222-000-005', 'Event Coordinator'), ('222-000-006', 'Front Desk Agent'),
('222-000-007', 'Valet'), ('222-000-008', 'Front Desk Agent'),
('222-000-009', 'Housekeeping Staff'), ('222-000-010', 'Front Desk Agent'),
('222-000-011', 'Concierge'), ('222-000-012', 'Maintenance Technician');

-- 8. HOTEL ROOMS
INSERT INTO Hotel_Room (roomID, hotel_ID, roomNumber, Price, Room_Status, Extendable, Room_View, Capacity, Problems_Damages) VALUES
-- Hotel 1-5
(1, 1, '101', 100, 'Available', FALSE, 'City', 1, NULL), (2, 1, '102', 150, 'Available', TRUE, 'City', 2, NULL), (3, 1, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (4, 1, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (5, 1, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(6, 2, '101', 100, 'Available', FALSE, 'City', 1, NULL), (7, 2, '102', 150, 'Available', TRUE, 'City', 2, NULL), (8, 2, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (9, 2, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (10, 2, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(11, 3, '101', 100, 'Available', FALSE, 'City', 1, NULL), (12, 3, '102', 150, 'Available', TRUE, 'City', 2, NULL), (13, 3, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (14, 3, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (15, 3, '105', 350, 'Damaged', FALSE, 'Penthouse', 5, 'Broken AC'),
(16, 4, '101', 100, 'Available', FALSE, 'City', 1, NULL), (17, 4, '102', 150, 'Available', TRUE, 'City', 2, NULL), (18, 4, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (19, 4, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (20, 4, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(21, 5, '101', 100, 'Available', FALSE, 'City', 1, NULL), (22, 5, '102', 150, 'Available', TRUE, 'City', 2, NULL), (23, 5, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (24, 5, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (25, 5, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
-- Hotel 6-10
(26, 6, '101', 100, 'Available', FALSE, 'City', 1, NULL), (27, 6, '102', 150, 'Available', TRUE, 'City', 2, NULL), (28, 6, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (29, 6, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (30, 6, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(31, 7, '101', 100, 'Available', FALSE, 'City', 1, NULL), (32, 7, '102', 150, 'Available', TRUE, 'City', 2, NULL), (33, 7, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (34, 7, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (35, 7, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(36, 8, '101', 100, 'Available', FALSE, 'City', 1, NULL), (37, 8, '102', 150, 'Available', TRUE, 'City', 2, NULL), (38, 8, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (39, 8, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (40, 8, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(41, 9, '101', 100, 'Available', FALSE, 'City', 1, NULL), (42, 9, '102', 150, 'Available', TRUE, 'City', 2, NULL), (43, 9, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (44, 9, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (45, 9, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(46, 10, '101', 100, 'Available', FALSE, 'City', 1, NULL), (47, 10, '102', 150, 'Available', TRUE, 'City', 2, NULL), (48, 10, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (49, 10, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (50, 10, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
-- Hotel 11-15
(51, 11, '101', 100, 'Available', FALSE, 'City', 1, NULL), (52, 11, '102', 150, 'Available', TRUE, 'City', 2, NULL), (53, 11, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (54, 11, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (55, 11, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(56, 12, '101', 100, 'Available', FALSE, 'City', 1, NULL), (57, 12, '102', 150, 'Available', TRUE, 'City', 2, NULL), (58, 12, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (59, 12, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (60, 12, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(61, 13, '101', 100, 'Available', FALSE, 'City', 1, NULL), (62, 13, '102', 150, 'Available', TRUE, 'City', 2, NULL), (63, 13, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (64, 13, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (65, 13, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(66, 14, '101', 100, 'Available', FALSE, 'City', 1, NULL), (67, 14, '102', 150, 'Available', TRUE, 'City', 2, NULL), (68, 14, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (69, 14, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (70, 14, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(71, 15, '101', 100, 'Available', FALSE, 'City', 1, NULL), (72, 15, '102', 150, 'Available', TRUE, 'City', 2, NULL), (73, 15, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (74, 15, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (75, 15, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
-- Hotel 16-20
(76, 16, '101', 100, 'Available', FALSE, 'City', 1, NULL), (77, 16, '102', 150, 'Available', TRUE, 'City', 2, NULL), (78, 16, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (79, 16, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (80, 16, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(81, 17, '101', 100, 'Available', FALSE, 'City', 1, NULL), (82, 17, '102', 150, 'Available', TRUE, 'City', 2, NULL), (83, 17, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (84, 17, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (85, 17, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(86, 18, '101', 100, 'Available', FALSE, 'City', 1, NULL), (87, 18, '102', 150, 'Available', TRUE, 'City', 2, NULL), (88, 18, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (89, 18, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (90, 18, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(91, 19, '101', 100, 'Available', FALSE, 'City', 1, NULL), (92, 19, '102', 150, 'Available', TRUE, 'City', 2, NULL), (93, 19, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (94, 19, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (95, 19, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(96, 20, '101', 100, 'Available', FALSE, 'City', 1, NULL), (97, 20, '102', 150, 'Available', TRUE, 'City', 2, NULL), (98, 20, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (99, 20, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (100, 20, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
-- Hotel 21-25
(101, 21, '101', 100, 'Available', FALSE, 'City', 1, NULL), (102, 21, '102', 150, 'Available', TRUE, 'City', 2, NULL), (103, 21, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (104, 21, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (105, 21, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(106, 22, '101', 100, 'Available', FALSE, 'City', 1, NULL), (107, 22, '102', 150, 'Available', TRUE, 'City', 2, NULL), (108, 22, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (109, 22, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (110, 22, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(111, 23, '101', 100, 'Available', FALSE, 'City', 1, NULL), (112, 23, '102', 150, 'Available', TRUE, 'City', 2, NULL), (113, 23, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (114, 23, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (115, 23, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(116, 24, '101', 100, 'Available', FALSE, 'City', 1, NULL), (117, 24, '102', 150, 'Available', TRUE, 'City', 2, NULL), (118, 24, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (119, 24, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (120, 24, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(121, 25, '101', 100, 'Available', FALSE, 'City', 1, NULL), (122, 25, '102', 150, 'Available', TRUE, 'City', 2, NULL), (123, 25, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (124, 25, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (125, 25, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
-- Hotel 26-30
(126, 26, '101', 100, 'Available', FALSE, 'City', 1, NULL), (127, 26, '102', 150, 'Available', TRUE, 'City', 2, NULL), (128, 26, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (129, 26, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (130, 26, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(131, 27, '101', 100, 'Available', FALSE, 'City', 1, NULL), (132, 27, '102', 150, 'Available', TRUE, 'City', 2, NULL), (133, 27, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (134, 27, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (135, 27, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(136, 28, '101', 100, 'Available', FALSE, 'City', 1, NULL), (137, 28, '102', 150, 'Available', TRUE, 'City', 2, NULL), (138, 28, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (139, 28, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (140, 28, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(141, 29, '101', 100, 'Available', FALSE, 'City', 1, NULL), (142, 29, '102', 150, 'Available', TRUE, 'City', 2, NULL), (143, 29, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (144, 29, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (145, 29, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(146, 30, '101', 100, 'Available', FALSE, 'City', 1, NULL), (147, 30, '102', 150, 'Available', TRUE, 'City', 2, NULL), (148, 30, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (149, 30, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (150, 30, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
-- Hotel 31-35
(151, 31, '101', 100, 'Available', FALSE, 'City', 1, NULL), (152, 31, '102', 150, 'Available', TRUE, 'City', 2, NULL), (153, 31, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (154, 31, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (155, 31, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(156, 32, '101', 100, 'Available', FALSE, 'City', 1, NULL), (157, 32, '102', 150, 'Available', TRUE, 'City', 2, NULL), (158, 32, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (159, 32, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (160, 32, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(161, 33, '101', 100, 'Available', FALSE, 'City', 1, NULL), (162, 33, '102', 150, 'Available', TRUE, 'City', 2, NULL), (163, 33, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (164, 33, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (165, 33, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(166, 34, '101', 100, 'Available', FALSE, 'City', 1, NULL), (167, 34, '102', 150, 'Available', TRUE, 'City', 2, NULL), (168, 34, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (169, 34, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (170, 34, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(171, 35, '101', 100, 'Available', FALSE, 'City', 1, NULL), (172, 35, '102', 150, 'Available', TRUE, 'City', 2, NULL), (173, 35, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (174, 35, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (175, 35, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
-- Hotel 36-40
(176, 36, '101', 100, 'Available', FALSE, 'City', 1, NULL), (177, 36, '102', 150, 'Available', TRUE, 'City', 2, NULL), (178, 36, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (179, 36, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (180, 36, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(181, 37, '101', 100, 'Available', FALSE, 'City', 1, NULL), (182, 37, '102', 150, 'Available', TRUE, 'City', 2, NULL), (183, 37, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (184, 37, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (185, 37, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(186, 38, '101', 100, 'Available', FALSE, 'City', 1, NULL), (187, 38, '102', 150, 'Available', TRUE, 'City', 2, NULL), (188, 38, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (189, 38, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (190, 38, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(191, 39, '101', 100, 'Available', FALSE, 'City', 1, NULL), (192, 39, '102', 150, 'Available', TRUE, 'City', 2, NULL), (193, 39, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (194, 39, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (195, 39, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL),
(196, 40, '101', 100, 'Available', FALSE, 'City', 1, NULL), (197, 40, '102', 150, 'Available', TRUE, 'City', 2, NULL), (198, 40, '103', 200, 'Available', TRUE, 'Ocean', 3, NULL), (199, 40, '104', 250, 'Available', FALSE, 'Ocean', 4, NULL), (200, 40, '105', 350, 'Available', FALSE, 'Penthouse', 5, NULL);

-- 9. HOTEL ROOM AMENITIES
INSERT INTO Hotel_Room_Amenity (roomID, Amenity) VALUES
(1, 'WiFi'), (1, 'TV'), (2, 'WiFi'), (2, 'Mini Fridge'), (5, 'Mini Bar'), (5, 'Jacuzzi'), 
(6, 'WiFi'), (10, 'Sauna'), (15, 'Balcony'), (100, 'Balcony'), (100, 'Jacuzzi'), 
(200, 'Jacuzzi'), (200, 'Private Pool');

-- 10. CUSTOMERS
INSERT INTO Customer (custID, ID_type, DateOfRegistration, Customer_Name, Customer_Address) VALUES
('DL-ON-987654', 'driving licence', '2026-01-15', 'John Doe', '123 Maple Dr, Toronto, ON'),
('DL-ON-492494', 'driving licence', '2026-03-31', 'John Pork', '800 King Edward Rd, Ottawa, ON'),
('234-567-890', 'SIN', '2026-02-20', 'Jane Roe', '456 Oak St, Montreal, QC'),
('987-65-4321', 'SSN', '2026-03-01', 'Sam Smith', '789 Pine Ln, Buffalo, NY');

-- 11. BOOKINGS
INSERT INTO Booking (bookingID, custID, roomID, startDate, endDate, bookingTime) VALUES
(1001, 'DL-ON-987654', 1, '2026-05-10', '2026-05-15', '2026-04-01 10:30:00'),
(1002, '234-567-890', 2, '2026-06-01', '2026-06-05', '2026-04-01 11:45:00');

-- 12. RENTING
INSERT INTO Renting (rentingID, bookingID, custID, roomID, National_ID, checkInDate, checkOutDate, paymentAmount) VALUES
(5001, 1001, 'DL-ON-987654', 1, '111-000-001', '2026-05-10', '2026-05-15', 500.00);

-- 13. ARCHIVE
INSERT INTO Archive (archiveID, chainID, hotel_ID, recordType, customerName, customerIDType, roomNumber, startDate, endDate, paymentAmount) VALUES
(9001, 1, 1, 'Renting', 'Old Customer', 'driving licence', '101', '2025-12-01', '2025-12-05', 500.00);
