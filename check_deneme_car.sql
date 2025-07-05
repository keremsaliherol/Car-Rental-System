-- Deneme aracının durumunu kontrol etme script'i
USE CarRentalDB;
GO

PRINT 'Deneme aracının durumu kontrol ediliyor...';

-- 1. Deneme aracını bul
SELECT 
    CarID, 
    PlateNumber, 
    Brand, 
    Model, 
    StatusID
FROM Cars 
WHERE Brand LIKE '%deneme%' OR Model LIKE '%deneme%' OR PlateNumber LIKE '%deneme%';

-- 2. Bu araca ait tüm kiralama kayıtlarını göster
DECLARE @CarID INT;
SELECT @CarID = CarID FROM Cars WHERE Brand LIKE '%deneme%' OR Model LIKE '%deneme%' OR PlateNumber LIKE '%deneme%';

IF @CarID IS NOT NULL
BEGIN
    PRINT 'Deneme aracının ID: ' + CAST(@CarID AS VARCHAR);
    
    SELECT 
        RentalID,
        CustomerID,
        RentDate,
        ReturnDate,
        ISNULL(Status, 'NULL') as Status,
        TotalAmount
    FROM Rentals 
    WHERE CarID = @CarID;
    
    -- Aktif kiralama sayısı
    SELECT 
        'Aktif Kiralamalar' as Tip,
        COUNT(*) as Sayi
    FROM Rentals 
    WHERE CarID = @CarID AND ISNULL(Status, 'Active') = 'Active'
    
    UNION ALL
    
    -- Tamamlanan kiralama sayısı
    SELECT 
        'Tamamlanan Kiralamalar' as Tip,
        COUNT(*) as Sayi
    FROM Rentals 
    WHERE CarID = @CarID AND Status = 'Completed'
    
    UNION ALL
    
    -- İptal edilen kiralama sayısı
    SELECT 
        'İptal Edilen Kiralamalar' as Tip,
        COUNT(*) as Sayi
    FROM Rentals 
    WHERE CarID = @CarID AND Status = 'Cancelled'
    
    UNION ALL
    
    -- Toplam kiralama sayısı
    SELECT 
        'TOPLAM Kiralamalar' as Tip,
        COUNT(*) as Sayi
    FROM Rentals 
    WHERE CarID = @CarID;
END
ELSE
BEGIN
    PRINT 'Deneme aracı bulunamadı!';
END

GO 