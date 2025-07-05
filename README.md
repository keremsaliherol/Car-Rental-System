
### **Teknoloji Stack'i**
- **Frontend**: JavaFX 17.0.6 + FXML + CSS
- **Backend**: Java 22 + Maven
- **Database**: Microsoft SQL Server
- **Connection Pool**: HikariCP 5.1.0
- **Security**: BCrypt + Row Level Security
- **Email**: JavaMail API
- **Build Tool**: Maven 3.8+

## 🚀 Kurulum ve Çalıştırma

### **Ön Gereksinimler**
- ☕ Java 22 JDK
- 🗄️ Microsoft SQL Server 2019+
- 🔧 Maven 3.8+
- 💻 JavaFX Runtime (dahil)

### **1. Proje Klonlama**
```bash
git clone <repository-url>
cd CarRentalSystem
```

### **2. Veritabanı Kurulumu**
```sql
-- 1. Veritabanı oluştur
CREATE DATABASE CarRentalDB;
GO

-- 2. Ana tabloları oluştur
sqlcmd -S localhost -d CarRentalDB -i "CarRentalSystem_AllTables (1).sql"

-- 3. Güvenlik politikalarını uygula
sqlcmd -S localhost -d CarRentalDB -i "database/row_level_security_policies.sql"
```

### **3. Veritabanı Bağlantı Ayarları**
`src/main/resources/database.properties` dosyasını düzenleyin:
```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=CarRentalDB
db.username=your_username
db.password=your_password
```

### **4. Proje Derleme ve Çalıştırma**
```bash
# Bağımlılıkları yükle
mvn clean install

# Uygulamayı çalıştır
mvn javafx:run
```

**Alternatif Çalıştırma:**
```bash
# Direkt main sınıf ile
mvn clean javafx:run -Djavafx.mainClass="com.kerem.ordersystem.carrentalsystem.CarRentalApplication"
```

## 👥 Kullanıcı Rolleri ve Giriş Bilgileri

### **👑 Admin Kullanıcısı**
- **Kullanıcı Adı**: `admin`
- **Şifre**: `admin123`
- **Yetkiler**: Tam sistem yönetimi

### **👤 Müşteri Kullanıcısı**
- **Kayıt**: Yeni müşteriler kayıt olabilir
- **Yetkiler**: Araç kiralama ve profil yönetimi

## 🎯 Ana Özellikler

### **🔧 Admin Paneli İşlevleri**

#### 🚗 **Araç Yönetimi**
- ➕ Yeni araç ekleme
- ✏️ Araç bilgilerini güncelleme
- 🗑️ Araç silme
- 🔄 Araç durumu güncelleme
- 📊 Araç kullanım istatistikleri

#### 👥 **Müşteri Yönetimi**
- 📋 Müşteri listesi görüntüleme
- ➕ Yeni müşteri ekleme
- ✏️ Müşteri bilgilerini güncelleme
- 📈 Müşteri kiralama geçmişi

#### 📅 **Kiralama Yönetimi**
- 📋 Aktif kiralamalar
- 📊 Kiralama geçmişi
- ⚠️ Geciken kiralamalar
- 💰 Ödeme takibi

#### 📊 **Raporlar ve Analizler**
- 💵 Gelir raporları
- 📈 Performans analizi
- 🔍 Audit trail
- 📋 Sistem monitörü

### **👤 Müşteri Paneli İşlevleri**

#### 🔍 **Araç Arama ve Kiralama**
- 🚗 Müsait araçları görüntüleme
- 🔍 Araç filtreleme (marka, model, kategori)
- 📅 Tarih bazlı arama
- 💳 Online kiralama

#### 📋 **Kişisel Yönetim**
- 📊 Kiralama geçmişi
- ✏️ Profil düzenleme
- 🔐 Şifre değiştirme
- 📧 Email ayarları

## 🗄️ Veritabanı Yapısı

### **📊 Ana Tablolar (14 Tablo)**
```sql
-- Kullanıcı ve Yetki Yönetimi
Users, Roles, Customers, Employees

-- Araç Yönetimi  
Cars, CarCategories, CarStatuses

-- Kiralama İşlemleri
Rentals, Reservations, Payments, Invoices

-- Operasyon Yönetimi
Branches, MaintenanceRecords, Logs
```

### **🔍 50 Veritabanı Nesnesi**

#### **📋 10 Views (Görünümler)**
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

#### **⚙️ 10 Stored Procedures (Saklı Yordamlar)**
1. `usp_AddRental` - Kiralama ekleme
2. `usp_AddCustomer` - Müşteri ekleme
3. `usp_UpdateCarStatus` - Araç durumu güncelleme
4. `usp_GetAvailableCars` - Müsait araçları getirme
5. `usp_GetCustomerRentals` - Müşteri kiralamalarını getirme
6. `usp_AddPayment` - Ödeme ekleme
7. `usp_GetOverdueRentals` - Geciken kiralamaları getirme
8. `usp_AddMaintenance` - Bakım ekleme
9. `usp_GetBranchStats` - Şube istatistikleri
10. `usp_CancelReservation` - Rezervasyon iptal etme

#### **🔧 10 Functions (Fonksiyonlar)**
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

#### **⚡ 10 Indexes (İndeksler)**
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

#### **🎯 10 Triggers (Tetikleyiciler)**
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

## 🔐 Güvenlik Özellikleri

### **🛡️ Kimlik Doğrulama**
- **BCrypt Şifreleme**: Güvenli şifre hash'leme
- **Role-Based Access**: Rol tabanlı erişim kontrolü
- **Session Management**: Güvenli oturum yönetimi

### **🔒 Veritabanı Güvenliği**
- **Row Level Security (RLS)**: Satır düzeyinde güvenlik
- **Parameterized Queries**: SQL Injection koruması
- **Connection Pooling**: Güvenli bağlantı yönetimi

## 📧 Email Konfigürasyonu

Email bildirimlerini etkinleştirmek için `EmailService` sınıfını yapılandırın:

```java
// EmailService yapılandırması
private static final String SMTP_HOST = "smtp.gmail.com";
private static final String SMTP_PORT = "587";
private static final String EMAIL_USERNAME = "your-email@gmail.com";
private static final String EMAIL_PASSWORD = "your-app-password";
```

## 🧪 Test ve Geliştirme

### **Test Uygulaması Çalıştırma**
```bash
# Database test uygulamasını çalıştır
mvn clean javafx:run -Djavafx.mainClass="com.kerem.ordersystem.carrentalsystem.DatabaseTestApplication"
```

### **Demo Verileri**
Sistem otomatik olarak demo verileri oluşturur:
- 5 örnek araç
- 3 örnek müşteri
- 2 örnek kiralama

## 📂 Proje Yapısı Detayı
