-- Araç durumları ile kiralama durumlarını senkronize etme script'i
USE CarRentalDB;
GO

PRINT 'Araç durumları ve kiralama durumları senkronize ediliyor...';

-- 1. Aktif kiralamalar için araç durumunu "Kiralanan" (2) yap
UPDATE Cars 
SET StatusID = 2 
WHERE CarID IN (
    SELECT DISTINCT r.CarID 
    FROM Rentals r 
    WHERE ISNULL(r.Status, 'Active') = 'Active'
);

PRINT 'Aktif kiralamalar için araç durumları güncellendi.';

-- 2. Tamamlanan/iptal edilen kiralamalar için araç durumunu "Müsait" (1) yap
-- Ancak başka aktif kiralamada olmayan araçlar için
UPDATE Cars 
SET StatusID = 1 
WHERE CarID NOT IN (
    SELECT DISTINCT r.CarID 
    FROM Rentals r 
    WHERE ISNULL(r.Status, 'Active') = 'Active'
) 
AND StatusID = 2;

PRINT 'Tamamlanan kiralamalar için araç durumları güncellendi.';

-- 3. Mevcut durumları kontrol et ve rapor ver
SELECT 
    'Aktif Kiralamalar' as Kategori,
    COUNT(*) as Sayi
FROM Rentals 
WHERE ISNULL(Status, 'Active') = 'Active'

UNION ALL

SELECT 
    'Kiralanan Araçlar' as Kategori,
    COUNT(*) as Sayi
FROM Cars 
WHERE StatusID = 2

UNION ALL

SELECT 
    'Müsait Araçlar' as Kategori,
    COUNT(*) as Sayi
FROM Cars 
WHERE StatusID = 1 OR StatusID IS NULL

UNION ALL

SELECT 
    'Bakımdaki Araçlar' as Kategori,
    COUNT(*) as Sayi
FROM Cars 
WHERE StatusID = 3;

PRINT 'Senkronizasyon tamamlandı!';
GO 