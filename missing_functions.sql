-- Eksik Fonksiyonları Oluşturma Script'i
-- Bu script eksik olan veritabanı fonksiyonlarını oluşturur

USE CarRentalDB;
GO

-- 1. fn_CalculateRentalDays fonksiyonu
-- Eğer fonksiyon zaten varsa önce sil
IF OBJECT_ID('dbo.fn_CalculateRentalDays', 'FN') IS NOT NULL
    DROP FUNCTION dbo.fn_CalculateRentalDays;
GO

CREATE FUNCTION dbo.fn_CalculateRentalDays (@start DATE, @end DATE)
RETURNS INT
AS
BEGIN
    RETURN DATEDIFF(DAY, @start, @end);
END;
GO

-- 2. fn_GetOutstandingBalance fonksiyonu
-- Eğer fonksiyon zaten varsa önce sil
IF OBJECT_ID('dbo.fn_GetOutstandingBalance', 'FN') IS NOT NULL
    DROP FUNCTION dbo.fn_GetOutstandingBalance;
GO

CREATE FUNCTION dbo.fn_GetOutstandingBalance (@rentalId INT)
RETURNS DECIMAL(10,2)
AS
BEGIN
    DECLARE @invoice DECIMAL(10,2), @paid DECIMAL(10,2)
    SELECT @invoice = TotalAmount FROM Invoices WHERE RentalID = @rentalId
    SELECT @paid = SUM(Amount) FROM Payments WHERE RentalID = @rentalId
    RETURN ISNULL(@invoice,0) - ISNULL(@paid,0)
END;
GO

-- 3. Diğer eksik fonksiyonlar (ihtiyaç halinde)
-- fn_GetCustomerAge
IF OBJECT_ID('dbo.fn_GetCustomerAge', 'FN') IS NOT NULL
    DROP FUNCTION dbo.fn_GetCustomerAge;
GO

CREATE FUNCTION dbo.fn_GetCustomerAge (@dob DATE)
RETURNS INT
AS
BEGIN
    RETURN DATEDIFF(YEAR, @dob, GETDATE());
END;
GO

-- fn_GetTotalRentalAmount
IF OBJECT_ID('dbo.fn_GetTotalRentalAmount', 'FN') IS NOT NULL
    DROP FUNCTION dbo.fn_GetTotalRentalAmount;
GO

CREATE FUNCTION dbo.fn_GetTotalRentalAmount (@rentalId INT)
RETURNS DECIMAL(10,2)
AS
BEGIN
    DECLARE @amount DECIMAL(10,2)
    SELECT @amount = TotalAmount FROM Rentals WHERE RentalID = @rentalId
    RETURN ISNULL(@amount, 0)
END;
GO

-- fn_CustomerRentalCount
IF OBJECT_ID('dbo.fn_CustomerRentalCount', 'FN') IS NOT NULL
    DROP FUNCTION dbo.fn_CustomerRentalCount;
GO

CREATE FUNCTION dbo.fn_CustomerRentalCount (@customerID INT)
RETURNS INT
AS
BEGIN
    DECLARE @count INT
    SELECT @count = COUNT(*) FROM Rentals WHERE CustomerID = @customerID
    RETURN ISNULL(@count, 0)
END;
GO

-- fn_GetCategoryName
IF OBJECT_ID('dbo.fn_GetCategoryName', 'FN') IS NOT NULL
    DROP FUNCTION dbo.fn_GetCategoryName;
GO

CREATE FUNCTION dbo.fn_GetCategoryName (@catId INT)
RETURNS NVARCHAR(50)
AS
BEGIN
    DECLARE @name NVARCHAR(50)
    SELECT @name = CategoryName FROM CarCategories WHERE CategoryID = @catId
    RETURN ISNULL(@name, 'Unknown')
END;
GO

-- fn_GetCarStatusName
IF OBJECT_ID('dbo.fn_GetCarStatusName', 'FN') IS NOT NULL
    DROP FUNCTION dbo.fn_GetCarStatusName;
GO

CREATE FUNCTION dbo.fn_GetCarStatusName (@statusId INT)
RETURNS NVARCHAR(50)
AS
BEGIN
    DECLARE @name NVARCHAR(50)
    SELECT @name = StatusName FROM CarStatuses WHERE StatusID = @statusId
    RETURN ISNULL(@name, 'Unknown')
END;
GO

PRINT 'Tüm fonksiyonlar başarıyla oluşturuldu!'; 