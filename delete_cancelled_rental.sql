-- İptal edilmiş kiralama kaydını silme script'i
USE CarRentalDB;
GO

PRINT 'İptal edilmiş kiralama kaydı siliniyor...';

-- Deneme aracının iptal edilmiş kiralama kaydını sil
DELETE FROM Rentals 
WHERE CarID = 9 AND Status = 'Cancelled';

PRINT 'İptal edilmiş kiralama kaydı silindi.';

-- Kontrol et
SELECT 
    'Kalan Kiralama Kayıtları' as Durum,
    COUNT(*) as Sayi
FROM Rentals 
WHERE CarID = 9;

PRINT 'Artık deneme aracı silinebilir!';
GO 