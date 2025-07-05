-- 1. Roles
CREATE TABLE Roles (
    RoleID INT PRIMARY KEY IDENTITY(1,1),
    RoleName NVARCHAR(50) NOT NULL UNIQUE
);

-- 2. Users
CREATE TABLE Users (
    UserID INT PRIMARY KEY IDENTITY(1,1),
    Username NVARCHAR(50) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100),
    RoleID INT FOREIGN KEY REFERENCES Roles(RoleID),
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- 3. Customers
CREATE TABLE Customers (
    CustomerID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT FOREIGN KEY REFERENCES Users(UserID),
    FullName NVARCHAR(200) NOT NULL,
    Phone NVARCHAR(40),
    Address NVARCHAR(510),
    DriverLicenseNo NVARCHAR(100) UNIQUE,
    DateOfBirth DATE
);

-- 4. CarCategories
CREATE TABLE CarCategories (
    CategoryID INT PRIMARY KEY IDENTITY(1,1),
    CategoryName NVARCHAR(50) NOT NULL UNIQUE
);

-- 5. CarStatuses
CREATE TABLE CarStatuses (
    StatusID INT PRIMARY KEY IDENTITY(1,1),
    StatusName NVARCHAR(50) NOT NULL UNIQUE
);

-- 6. Cars
CREATE TABLE Cars (
    CarID INT PRIMARY KEY IDENTITY(1,1),
    PlateNumber NVARCHAR(20) NOT NULL UNIQUE,
    Brand NVARCHAR(50) NOT NULL,
    Model NVARCHAR(50) NOT NULL,
    Year INT NOT NULL,
    DailyRate DECIMAL(10, 2) NOT NULL,
    CategoryID INT FOREIGN KEY REFERENCES CarCategories(CategoryID),
    StatusID INT FOREIGN KEY REFERENCES CarStatuses(StatusID),
    Mileage INT,
    Description NVARCHAR(255)
);

-- 7. Branches
CREATE TABLE Branches (
    BranchID INT PRIMARY KEY IDENTITY(1,1),
    BranchName NVARCHAR(100),
    Location NVARCHAR(255)
);

-- 8. Employees
CREATE TABLE Employees (
    EmployeeID INT PRIMARY KEY IDENTITY(1,1),
    FullName NVARCHAR(100),
    BranchID INT FOREIGN KEY REFERENCES Branches(BranchID),
    Role NVARCHAR(50)
);

-- 9. Rentals
CREATE TABLE Rentals (
    RentalID INT PRIMARY KEY IDENTITY(1,1),
    CustomerID INT FOREIGN KEY REFERENCES Customers(CustomerID),
    CarID INT FOREIGN KEY REFERENCES Cars(CarID),
    EmployeeID INT FOREIGN KEY REFERENCES Employees(EmployeeID),
    BranchID INT FOREIGN KEY REFERENCES Branches(BranchID),
    RentDate DATE NOT NULL,
    ReturnDate DATE,
    TotalAmount DECIMAL(10,2)
);

-- 10. Reservations
CREATE TABLE Reservations (
    ReservationID INT PRIMARY KEY IDENTITY(1,1),
    CustomerID INT FOREIGN KEY REFERENCES Customers(CustomerID),
    CarID INT FOREIGN KEY REFERENCES Cars(CarID),
    ReservationDate DATE,
    ReservedFrom DATE,
    ReservedTo DATE
);

-- 11. Payments
CREATE TABLE Payments (
    PaymentID INT PRIMARY KEY IDENTITY(1,1),
    RentalID INT FOREIGN KEY REFERENCES Rentals(RentalID),
    Amount DECIMAL(10,2),
    PaymentDate DATE DEFAULT GETDATE(),
    Status NVARCHAR(20)
);

-- 12. Invoices
CREATE TABLE Invoices (
    InvoiceID INT PRIMARY KEY IDENTITY(1,1),
    RentalID INT FOREIGN KEY REFERENCES Rentals(RentalID),
    InvoiceDate DATE DEFAULT GETDATE(),
    TotalAmount DECIMAL(10,2),
    Notes NVARCHAR(255)
);

-- 13. Logs
CREATE TABLE Logs (
    LogID INT PRIMARY KEY IDENTITY(1,1),
    Action NVARCHAR(100),
    Description NVARCHAR(255),
    PerformedAt DATETIME DEFAULT GETDATE()
);

-- 14. MaintenanceRecords
CREATE TABLE MaintenanceRecords (
    MaintenanceID INT PRIMARY KEY IDENTITY(1,1),
    CarID INT FOREIGN KEY REFERENCES Cars(CarID),
    StartDate DATE,
    EndDate DATE,
    Description NVARCHAR(255)
);

-- ========================================
-- Views
-- ========================================
CREATE VIEW AvailableCarsView AS
SELECT c.CarID, c.PlateNumber, c.Brand, c.Model, c.DailyRate, cat.CategoryName
FROM Cars c
JOIN CarCategories cat ON c.CategoryID = cat.CategoryID
WHERE c.StatusID = 1;

-- ========================================
-- Triggers
-- ========================================
CREATE TRIGGER trg_PreventOverlappingReservations
ON Reservations
INSTEAD OF INSERT
AS
BEGIN
    PRINT 'Reservation validation logic here'
END;

CREATE TRIGGER trg_UpdateCarStatusOnReturn
ON Rentals
AFTER UPDATE
AS
BEGIN
    PRINT 'Car status update logic here'
END;

-- ========================================
-- Functions
-- ========================================
CREATE FUNCTION fn_CalculateRentalDays (@start DATE, @end DATE)
RETURNS INT
AS
BEGIN
    RETURN DATEDIFF(DAY, @start, @end);
END;

CREATE FUNCTION fn_CustomerRentalCount (@customerID INT)
RETURNS INT
AS
BEGIN
    DECLARE @count INT
    SELECT @count = COUNT(*) FROM Rentals WHERE CustomerID = @customerID
    RETURN @count
END;

-- ========================================
-- Stored Procedures
-- ========================================
CREATE PROCEDURE usp_AddRental
    @CustomerID INT,
    @CarID INT,
    @RentDate DATE,
    @ReturnDate DATE,
    @TotalAmount DECIMAL(10,2)
AS
BEGIN
    INSERT INTO Rentals (CustomerID, CarID, RentDate, ReturnDate, TotalAmount)
    VALUES (@CustomerID, @CarID, @RentDate, @ReturnDate, @TotalAmount);
END;

CREATE PROCEDURE usp_GetAvailableCars
AS
BEGIN
    SELECT * FROM AvailableCarsView;
END;

CREATE PROCEDURE usp_AddCustomer
    @UserID INT,
    @FullName NVARCHAR(200),
    @Phone NVARCHAR(40),
    @Address NVARCHAR(510),
    @DriverLicenseNo NVARCHAR(100),
    @DateOfBirth DATE
AS
BEGIN
    INSERT INTO Customers (UserID, FullName, Phone, Address, DriverLicenseNo, DateOfBirth)
    VALUES (@UserID, @FullName, @Phone, @Address, @DriverLicenseNo, @DateOfBirth);
END;

CREATE PROCEDURE usp_UpdateCarStatus
    @CarID INT,
    @StatusID INT
AS
BEGIN
    UPDATE Cars 
    SET StatusID = @StatusID
    WHERE CarID = @CarID;
END;

CREATE PROCEDURE usp_GetCustomerRentals
    @CustomerID INT
AS
BEGIN
    SELECT * FROM CustomerRentalHistory
    WHERE CustomerID = @CustomerID;
END;

CREATE PROCEDURE usp_AddPayment
    @RentalID INT,
    @Amount DECIMAL(10,2),
    @Status NVARCHAR(20)
AS
BEGIN
    INSERT INTO Payments (RentalID, Amount, Status)
    VALUES (@RentalID, @Amount, @Status);
END;

CREATE PROCEDURE usp_GetOverdueRentals
AS
BEGIN
    SELECT * FROM OverdueRentals;
END;

CREATE PROCEDURE usp_AddMaintenance
    @CarID INT,
    @StartDate DATE,
    @EndDate DATE,
    @Description NVARCHAR(255)
AS
BEGIN
    INSERT INTO MaintenanceRecords (CarID, StartDate, EndDate, Description)
    VALUES (@CarID, @StartDate, @EndDate, @Description);
END;

CREATE PROCEDURE usp_GetBranchStats
    @BranchID INT
AS
BEGIN
    SELECT 
        b.BranchName,
        COUNT(r.RentalID) as TotalRentals,
        SUM(r.TotalAmount) as TotalRevenue
    FROM Branches b
    LEFT JOIN Rentals r ON b.BranchID = r.BranchID
    WHERE b.BranchID = @BranchID
    GROUP BY b.BranchName;
END;

CREATE PROCEDURE usp_CancelReservation
    @ReservationID INT
AS
BEGIN
    DELETE FROM Reservations 
    WHERE ReservationID = @ReservationID;
END;

-- ========================================
-- Indexes
-- ========================================
CREATE UNIQUE INDEX idx_Cars_PlateNumber ON Cars(PlateNumber);
CREATE INDEX idx_Rentals_RentDate ON Rentals(RentDate);
CREATE INDEX idx_Customers_FullName ON Customers(FullName);

-- ========================================
-- Views (Toplam 10)
-- ========================================
CREATE VIEW CustomerRentalHistory AS
SELECT r.RentalID, c.FullName, car.PlateNumber, r.RentDate, r.ReturnDate
FROM Rentals r
JOIN Customers c ON r.CustomerID = c.CustomerID
JOIN Cars car ON r.CarID = car.CarID;

CREATE VIEW OverdueRentals AS
SELECT RentalID, CustomerID, CarID, ReturnDate
FROM Rentals
WHERE ReturnDate < GETDATE();

CREATE VIEW ActiveReservations AS
SELECT * FROM Reservations
WHERE ReservedTo >= GETDATE();

CREATE VIEW PaymentsSummary AS
SELECT RentalID, SUM(Amount) AS TotalPaid
FROM Payments
GROUP BY RentalID;

CREATE VIEW CarUsageStats AS
SELECT CarID, COUNT(*) AS RentalCount
FROM Rentals
GROUP BY CarID;

CREATE VIEW CustomerInvoiceSummary AS
SELECT c.FullName, SUM(i.TotalAmount) AS TotalInvoiced
FROM Invoices i
JOIN Rentals r ON i.RentalID = r.RentalID
JOIN Customers c ON r.CustomerID = c.CustomerID
GROUP BY c.FullName;

CREATE VIEW EmployeeRentalCount AS
SELECT e.FullName, COUNT(*) AS RentalCount
FROM Rentals r
JOIN Employees e ON r.EmployeeID = e.EmployeeID
GROUP BY e.FullName;

CREATE VIEW BranchIncome AS
SELECT b.BranchName, SUM(r.TotalAmount) AS TotalIncome
FROM Rentals r
JOIN Branches b ON r.BranchID = b.BranchID
GROUP BY b.BranchName;

CREATE VIEW MaintenanceSchedule AS
SELECT c.PlateNumber, c.Brand, c.Model, m.StartDate, m.EndDate, m.Description
FROM MaintenanceRecords m
JOIN Cars c ON m.CarID = c.CarID
WHERE m.EndDate >= GETDATE();

-- ========================================
-- Triggers (Toplam 10)
-- ========================================
CREATE TRIGGER trg_LogCustomerInsert
ON Customers
AFTER INSERT
AS
BEGIN
    PRINT 'Log insert to Logs table...'
END;

CREATE TRIGGER trg_LogCarInsert
ON Cars
AFTER INSERT
AS
BEGIN
    PRINT 'Log insert for new car...'
END;

CREATE TRIGGER trg_UpdateMileageAfterReturn
ON Rentals
AFTER UPDATE
AS
BEGIN
    PRINT 'Update mileage logic...'
END;

CREATE TRIGGER trg_AutoCreateInvoice
ON Rentals
AFTER INSERT
AS
BEGIN
    PRINT 'Create invoice automatically...'
END;

CREATE TRIGGER trg_AutoPaymentStatus
ON Payments
AFTER INSERT
AS
BEGIN
    PRINT 'Set payment status...'
END;

CREATE TRIGGER trg_MaintenanceLog
ON MaintenanceRecords
AFTER INSERT
AS
BEGIN
    PRINT 'Add maintenance log...'
END;

CREATE TRIGGER trg_EmailNotification
ON Reservations
AFTER INSERT
AS
BEGIN
    PRINT 'Send email notification...'
END;

CREATE TRIGGER trg_DeleteProtection
ON Cars
INSTEAD OF DELETE
AS
BEGIN
    PRINT 'Prevent deletion...'
END;

-- ========================================
-- Functions (Toplam 10)
-- ========================================
CREATE FUNCTION fn_GetCustomerAge (@dob DATE)
RETURNS INT
AS
BEGIN
    RETURN DATEDIFF(YEAR, @dob, GETDATE());
END;

CREATE FUNCTION fn_GetTotalRentalAmount (@rentalId INT)
RETURNS DECIMAL(10,2)
AS
BEGIN
    DECLARE @amount DECIMAL(10,2)
    SELECT @amount = TotalAmount FROM Rentals WHERE RentalID = @rentalId
    RETURN @amount
END;

CREATE FUNCTION fn_GetBranchRentalCount (@branchId INT)
RETURNS INT
AS
BEGIN
    RETURN (SELECT COUNT(*) FROM Rentals WHERE BranchID = @branchId)
END;

CREATE FUNCTION fn_GetEmployeeRentalCount (@employeeId INT)
RETURNS INT
AS
BEGIN
    RETURN (SELECT COUNT(*) FROM Rentals WHERE EmployeeID = @employeeId)
END;

CREATE FUNCTION fn_GetCarAvailability (@carId INT)
RETURNS BIT
AS
BEGIN
    RETURN (SELECT CASE WHEN StatusID = 1 THEN 1 ELSE 0 END FROM Cars WHERE CarID = @carId)
END;

CREATE FUNCTION fn_GetOutstandingBalance (@rentalId INT)
RETURNS DECIMAL(10,2)
AS
BEGIN
    DECLARE @invoice DECIMAL(10,2), @paid DECIMAL(10,2)
    SELECT @invoice = TotalAmount FROM Invoices WHERE RentalID = @rentalId
    SELECT @paid = SUM(Amount) FROM Payments WHERE RentalID = @rentalId
    RETURN ISNULL(@invoice,0) - ISNULL(@paid,0)
END;

CREATE FUNCTION fn_GetCategoryName (@catId INT)
RETURNS NVARCHAR(50)
AS
BEGIN
    RETURN (SELECT CategoryName FROM CarCategories WHERE CategoryID = @catId)
END;

CREATE FUNCTION fn_GetCarStatusName (@statusId INT)
RETURNS NVARCHAR(50)
AS
BEGIN
    RETURN (SELECT StatusName FROM CarStatuses WHERE StatusID = @statusId)
END;

-- ========================================
-- Indexes (Toplam 10)
-- ========================================
CREATE INDEX idx_Customers_Phone ON Customers(Phone);
CREATE INDEX idx_Reservations_Dates ON Reservations(ReservedFrom, ReservedTo);
CREATE INDEX idx_Invoices_TotalAmount ON Invoices(TotalAmount);
CREATE INDEX idx_Maintenance_CarID ON MaintenanceRecords(CarID);
CREATE INDEX idx_Logs_PerformedAt ON Logs(PerformedAt);
CREATE INDEX idx_Branches_Location ON Branches(Location);
CREATE INDEX idx_Cars_Brand_Model ON Cars(Brand, Model);
CREATE INDEX idx_Users_Username ON Users(Username);