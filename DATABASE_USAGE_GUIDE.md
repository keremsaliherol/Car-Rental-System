# 🗄️ Car Rental System - Database Objects Usage Guide

Bu kılavuz, JavaFX projenizde SQL dosyasındaki **50 database nesnesini** (10 Views + 10 Stored Procedures + 10 Functions + 10 Indexes + 10 Triggers) nasıl kullanacağınızı gösterir.

## 📋 Database Objects Summary

### 📊 10 Views
1. `AvailableCarsView` - Müsait araçlar
2. `CustomerRentalHistory` - Müşteri kiralama geçmişi
3. `OverdueRentals` - Geciken kiralamalar
4. `ActiveReservations` - Aktif rezervasyonlar
5. `PaymentsSummary` - Ödeme özeti
6. `CarUsageStats` - Araç kullanım istatistikleri
7. `CustomerInvoiceSummary` - Müşteri fatura özeti
8. `EmployeeRentalCount` - Çalışan kiralama sayısı
9. `BranchIncome` - Şube geliri
10. `MaintenanceSchedule` - Bakım programı

### 🔧 10 Stored Procedures
1. `usp_AddRental` - Kiralama ekleme
2. `usp_GetAvailableCars` - Müsait araçları getirme
3. `usp_AddCustomer` - Müşteri ekleme
4. `usp_UpdateCarStatus` - Araç durumu güncelleme
5. `usp_GetCustomerRentals` - Müşteri kiralamalarını getirme
6. `usp_AddPayment` - Ödeme ekleme
7. `usp_GetOverdueRentals` - Geciken kiralamaları getirme
8. `usp_AddMaintenance` - Bakım ekleme
9. `usp_GetBranchStats` - Şube istatistikleri
10. `usp_CancelReservation` - Rezervasyon iptal etme

### ⚙️ 10 Functions
1. `fn_CalculateRentalDays` - Kiralama gün hesaplama
2. `fn_CustomerRentalCount` - Müşteri kiralama sayısı
3. `fn_GetCustomerAge` - Müşteri yaş hesaplama
4. `fn_GetTotalRentalAmount` - Toplam kiralama tutarı
5. `fn_GetBranchRentalCount` - Şube kiralama sayısı
6. `fn_GetEmployeeRentalCount` - Çalışan kiralama sayısı
7. `fn_GetCarAvailability` - Araç müsaitlik durumu
8. `fn_GetOutstandingBalance` - Bekleyen bakiye
9. `fn_GetCategoryName` - Kategori adı
10. `fn_GetCarStatusName` - Araç durum adı

### 📈 10 Indexes
1. `idx_Cars_PlateNumber` - Plaka numarası (UNIQUE)
2. `idx_Rentals_RentDate` - Kiralama tarihi
3. `idx_Customers_FullName` - Müşteri adı
4. `idx_Customers_Phone` - Telefon numarası
5. `idx_Reservations_Dates` - Rezervasyon tarihleri
6. `idx_Invoices_TotalAmount` - Fatura tutarı
7. `idx_Maintenance_CarID` - Bakım araç ID
8. `idx_Logs_PerformedAt` - Log tarihi
9. `idx_Branches_Location` - Şube lokasyonu
10. `idx_Cars_Brand_Model` - Araç marka-model

### 🔥 10 Triggers
1. `trg_PreventOverlappingReservations` - Çakışan rezervasyon engelleme
2. `trg_UpdateCarStatusOnReturn` - İade durumunda araç durumu güncelleme
3. `trg_LogCustomerInsert` - Müşteri ekleme logu
4. `trg_LogCarInsert` - Araç ekleme logu
5. `trg_UpdateMileageAfterReturn` - İade sonrası kilometre güncelleme
6. `trg_AutoCreateInvoice` - Otomatik fatura oluşturma
7. `trg_AutoPaymentStatus` - Otomatik ödeme durumu
8. `trg_MaintenanceLog` - Bakım logu
9. `trg_EmailNotification` - Email bildirimi
10. `trg_DeleteProtection` - Silme koruması

## 🚀 Kullanım Örnekleri

### 1. Views Kullanımı

```java
// DatabaseService'i başlat
DatabaseService dbService = new DatabaseService();

// 1. Müsait araçları getir
List<Map<String, Object>> availableCars = dbService.getAvailableCars();
System.out.println("Müsait araç sayısı: " + availableCars.size());

// 2. Müşteri kiralama geçmişi
List<Map<String, Object>> customerHistory = dbService.getCustomerRentalHistory(1);

// 3. Geciken kiralamalar
List<Map<String, Object>> overdueRentals = dbService.getOverdueRentals();
```

### 2. Stored Procedures Kullanımı

```java
// 1. Yeni kiralama ekle
boolean rentalAdded = dbService.addRental(
    1,                          // Customer ID
    1,                          // Car ID
    LocalDate.now(),            // Rent Date
    LocalDate.now().plusDays(7), // Return Date
    700.0                       // Total Amount
);

// 2. Yeni müşteri ekle
boolean customerAdded = dbService.addCustomer(
    1,                              // User ID
    "John Doe",                     // Full Name
    "123-456-7890",                 // Phone
    "123 Main St",                  // Address
    "DL123456789",                  // Driver License No
    LocalDate.of(1990, 1, 1)       // Date of Birth
);

// 3. Araç durumu güncelle
boolean statusUpdated = dbService.updateCarStatus(1, 2);
```

### 3. Functions Kullanımı

```java
// 1. Kiralama gün sayısını hesapla
int rentalDays = dbService.calculateRentalDays(
    LocalDate.now(), 
    LocalDate.now().plusDays(7)
);

// 2. Müşteri yaşını hesapla
int customerAge = dbService.getCustomerAge(LocalDate.of(1990, 1, 1));

// 3. Araç müsaitliğini kontrol et
boolean isCarAvailable = dbService.getCarAvailability(1);

// 4. Kategori adını getir
String categoryName = dbService.getCategoryName(1);
```

### 4. Index-Optimized Queries Kullanımı

```java
// 1. Plaka numarasına göre araç ara (idx_Cars_PlateNumber kullanır)
Map<String, Object> car = dbService.getCarByPlateNumber("34ABC123");

// 2. Tarihe göre kiralamalar (idx_Rentals_RentDate kullanır)
List<Map<String, Object>> rentals = dbService.getRentalsByDate(LocalDate.now());

// 3. Müşteri adına göre ara (idx_Customers_FullName kullanır)
List<Map<String, Object>> customers = dbService.getCustomersByName("John");

// 4. Telefona göre müşteri ara (idx_Customers_Phone kullanır)
Map<String, Object> customer = dbService.getCustomerByPhone("123-456-7890");
```

### 5. Triggers Test Etme

```java
// Trigger'ları test et (otomatik olarak çalışırlar)
dbService.demonstrateTriggers();
```

## 🎯 JavaFX Controller'da Kullanım

```java
@FXML
private void loadAvailableCars() {
    Task<List<Map<String, Object>>> task = new Task<>() {
        @Override
        protected List<Map<String, Object>> call() {
            return databaseService.getAvailableCars();
        }
    };
    
    task.setOnSucceeded(e -> {
        List<Map<String, Object>> cars = task.getValue();
        // TableView'e veri yükle
        updateCarTable(cars);
    });
    
    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
}

@FXML
private void addNewRental() {
    boolean success = databaseService.addRental(
        selectedCustomerId,
        selectedCarId,
        startDate,
        endDate,
        totalAmount
    );
    
    if (success) {
        showAlert("Başarılı", "Kiralama başarıyla eklendi!");
        refreshData();
    } else {
        showAlert("Hata", "Kiralama eklenirken hata oluştu!");
    }
}
```

## 🖥️ Test Uygulamasını Çalıştırma

1. **Database Test Application'ı çalıştırın:**
```bash
mvn clean javafx:run -Djavafx.mainClass="com.kerem.ordersystem.carrentalsystem.DatabaseTestApplication"
```

2. **Veya IDE'de:**
- `DatabaseTestApplication.java` dosyasını çalıştırın
- Test interface açılacak
- Her tab'da ilgili database nesnelerini test edebilirsiniz

## 📊 Performance Optimization

### Index Kullanımı
```java
// GOOD: Index kullanır
List<Map<String, Object>> fastQuery = dbService.getCarByPlateNumber("34ABC123");

// BAD: Index kullanmaz
// SELECT * FROM Cars WHERE UPPER(PlateNumber) LIKE '%ABC%'
```

### Async Operations
```java
// UI'yı dondurmamak için async kullanın
Task<List<Map<String, Object>>> task = new Task<>() {
    @Override
    protected List<Map<String, Object>> call() {
        return databaseService.getCustomerRentalHistory(customerId);
    }
};
```

## 🔧 Troubleshooting

### 1. Database Connection Issues
```java
// Connection test
DatabaseManager dbManager = DatabaseManager.getInstance();
if (dbManager.testConnection()) {
    System.out.println("✅ Database connection OK");
} else {
    System.out.println("❌ Database connection failed");
}
```

### 2. SQL Syntax Errors
- View/SP/Function isimleri doğru yazıldığından emin olun
- Parameter sayıları ve tipleri kontrol edin

### 3. Performance Issues
- Index'lerin aktif olduğunu kontrol edin
- Query execution plan'ları analiz edin

## 🎉 Sonuç

Bu sistem ile JavaFX projenizde **50 database nesnesini** tam olarak kullanabilirsiniz:

- ✅ **10 Views** - Veri görüntüleme
- ✅ **10 Stored Procedures** - Veri işleme
- ✅ **10 Functions** - Hesaplama ve dönüşüm
- ✅ **10 Indexes** - Performance optimizasyonu
- ✅ **10 Triggers** - Otomatik işlemler

Tüm database nesneleri `DatabaseService` sınıfı üzerinden kolayca erişilebilir ve JavaFX controller'larınızda kullanılabilir. 