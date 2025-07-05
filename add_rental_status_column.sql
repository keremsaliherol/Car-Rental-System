-- Rentals tablosuna Status sütunu ekleme script'i
USE CarRentalDB;
GO

-- Rentals tablosuna Status sütunu ekle
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Rentals') AND name = 'Status')
BEGIN
    ALTER TABLE Rentals 
    ADD Status NVARCHAR(20) DEFAULT 'Active';
    
    PRINT 'Status sütunu Rentals tablosuna eklendi.';
END
ELSE
BEGIN
    PRINT 'Status sütunu zaten mevcut.';
END
GO

-- Mevcut kayıtları güncelle
UPDATE Rentals 
SET Status = CASE 
    WHEN ReturnDate IS NULL OR ReturnDate > CAST(GETDATE() AS DATE) THEN 'Active'
    WHEN ReturnDate <= CAST(GETDATE() AS DATE) THEN 'Completed'
    ELSE 'Active'
END
WHERE Status IS NULL OR Status = '';

PRINT 'Mevcut kiralama kayıtları güncellendi.';
GO

-- Status değerleri için check constraint ekle (opsiyonel)
IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_Rentals_Status')
BEGIN
    ALTER TABLE Rentals
    ADD CONSTRAINT CK_Rentals_Status 
    CHECK (Status IN ('Active', 'Completed', 'Cancelled'));
    
    PRINT 'Status için check constraint eklendi.';
END
GO

PRINT 'Rentals tablosu Status sütunu ile güncellendi!'; 