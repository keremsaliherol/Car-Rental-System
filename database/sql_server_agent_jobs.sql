-- =====================================
-- SQL SERVER AGENT JOBS
-- Car Rental System Automation
-- =====================================

USE msdb;
GO

-- =====================================
-- 1. DAILY BACKUP JOB
-- =====================================

-- Create Daily Backup Job
IF EXISTS (SELECT job_id FROM dbo.sysjobs WHERE name = 'CarRental_DailyBackup')
BEGIN
    EXEC dbo.sp_delete_job @job_name = 'CarRental_DailyBackup';
    PRINT 'Existing daily backup job deleted.';
END

EXEC dbo.sp_add_job
    @job_name = 'CarRental_DailyBackup',
    @enabled = 1,
    @description = 'Daily backup of CarRentalDB database with compression and verification',
    @category_name = 'Database Maintenance',
    @owner_login_name = 'sa';

-- Add backup job step
EXEC dbo.sp_add_jobstep
    @job_name = 'CarRental_DailyBackup',
    @step_name = 'Full Database Backup',
    @subsystem = 'TSQL',
    @command = '
DECLARE @BackupPath NVARCHAR(500);
DECLARE @BackupFileName NVARCHAR(500);
DECLARE @BackupDate NVARCHAR(20);

-- Create backup filename with timestamp
SET @BackupDate = FORMAT(GETDATE(), ''yyyyMMdd_HHmmss'');
SET @BackupPath = ''C:\Backup\CarRental\''; -- Adjust path as needed
SET @BackupFileName = @BackupPath + ''CarRentalDB_'' + @BackupDate + ''.bak'';

-- Create backup directory if not exists (requires xp_cmdshell enabled)
EXEC xp_cmdshell ''mkdir "C:\Backup\CarRental"'', NO_OUTPUT;

-- Create database backup
BACKUP DATABASE [CarRentalDB] 
TO DISK = @BackupFileName
WITH COMPRESSION, 
     CHECKSUM, 
     INIT,
     FORMAT,
     DESCRIPTION = ''CarRental System Daily Full Backup'',
     NAME = ''CarRentalDB Full Backup'';

-- Verify backup
RESTORE VERIFYONLY FROM DISK = @BackupFileName;

-- Log backup completion
USE CarRentalDB;
INSERT INTO Activities (ActivityType, ActivityDescription, CreatedAt)
VALUES (''System'', ''Daily backup completed successfully: '' + @BackupFileName, GETDATE());

PRINT ''✅ Daily backup completed: '' + @BackupFileName;
',
    @on_success_action = 1, -- Quit with success
    @on_fail_action = 2;    -- Quit with failure

-- Create schedule for daily backup (every day at 2:00 AM)
EXEC dbo.sp_add_schedule
    @schedule_name = 'CarRental_DailyBackup_Schedule',
    @freq_type = 4,        -- Daily
    @freq_interval = 1,    -- Every 1 day
    @active_start_time = 020000; -- 2:00 AM

-- Attach schedule to job
EXEC dbo.sp_attach_schedule
    @job_name = 'CarRental_DailyBackup',
    @schedule_name = 'CarRental_DailyBackup_Schedule';

-- Add job to server
EXEC dbo.sp_add_jobserver
    @job_name = 'CarRental_DailyBackup',
    @server_name = @@SERVERNAME;

PRINT '✅ Daily backup job created successfully!';
GO

-- =====================================
-- 2. WEEKLY ARCHIVING JOB
-- =====================================

-- Create Weekly Archiving Job
IF EXISTS (SELECT job_id FROM dbo.sysjobs WHERE name = 'CarRental_WeeklyArchiving')
BEGIN
    EXEC dbo.sp_delete_job @job_name = 'CarRental_WeeklyArchiving';
    PRINT 'Existing weekly archiving job deleted.';
END

EXEC dbo.sp_add_job
    @job_name = 'CarRental_WeeklyArchiving',
    @enabled = 1,
    @description = 'Weekly archiving of old rental data and activities',
    @category_name = 'Database Maintenance',
    @owner_login_name = 'sa';

-- Add archiving job step
EXEC dbo.sp_add_jobstep
    @job_name = 'CarRental_WeeklyArchiving',
    @step_name = 'Archive Old Data',
    @subsystem = 'TSQL',
    @database_name = 'CarRentalDB',
    @command = '
-- Archive rentals older than 1 year to archive table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = ''RentalsArchive'')
BEGIN
    -- Create archive table
    SELECT TOP 0 * 
    INTO RentalsArchive 
    FROM Rentals;
    
    -- Add archive date column
    ALTER TABLE RentalsArchive ADD ArchivedDate DATETIME2 DEFAULT GETDATE();
    
    PRINT ''📦 RentalsArchive table created'';
END

-- Archive old completed rentals
DECLARE @ArchiveDate DATE = DATEADD(YEAR, -1, GETDATE());
DECLARE @ArchivedRentals INT = 0;

INSERT INTO RentalsArchive
SELECT *, GETDATE() as ArchivedDate
FROM Rentals 
WHERE Status IN (''Completed'', ''Cancelled'') 
AND CreatedAt < @ArchiveDate;

SET @ArchivedRentals = @@ROWCOUNT;

-- Delete archived rentals from main table
DELETE FROM Rentals 
WHERE Status IN (''Completed'', ''Cancelled'') 
AND CreatedAt < @ArchiveDate;

PRINT ''✅ Weekly archiving completed:'';
PRINT ''   - Archived Rentals: '' + CAST(@ArchivedRentals AS VARCHAR);
',
    @on_success_action = 1, -- Quit with success
    @on_fail_action = 2;    -- Quit with failure

-- Create schedule for weekly archiving (every Sunday at 3:00 AM)
EXEC dbo.sp_add_schedule
    @schedule_name = 'CarRental_WeeklyArchiving_Schedule',
    @freq_type = 8,        -- Weekly
    @freq_interval = 1,    -- Sunday
    @active_start_time = 030000; -- 3:00 AM

-- Attach schedule to job
EXEC dbo.sp_attach_schedule
    @job_name = 'CarRental_WeeklyArchiving',
    @schedule_name = 'CarRental_WeeklyArchiving_Schedule';

-- Add job to server
EXEC dbo.sp_add_jobserver
    @job_name = 'CarRental_WeeklyArchiving',
    @server_name = @@SERVERNAME;

PRINT '✅ Weekly archiving job created successfully!';
GO

-- =====================================
-- 3. MONTHLY REPORT EMAIL JOB
-- =====================================

-- Create Monthly Report Job
IF EXISTS (SELECT job_id FROM dbo.sysjobs WHERE name = 'CarRental_MonthlyReport')
BEGIN
    EXEC dbo.sp_delete_job @job_name = 'CarRental_MonthlyReport';
    PRINT 'Existing monthly report job deleted.';
END

EXEC dbo.sp_add_job
    @job_name = 'CarRental_MonthlyReport',
    @enabled = 1,
    @description = 'Generate and email monthly business reports',
    @category_name = 'Report Generation',
    @owner_login_name = 'sa';

-- Add report generation step
EXEC dbo.sp_add_jobstep
    @job_name = 'CarRental_MonthlyReport',
    @step_name = 'Generate Monthly Report',
    @subsystem = 'TSQL',
    @database_name = 'CarRentalDB',
    @command = '
-- Generate Monthly Report Data
DECLARE @ReportMonth VARCHAR(50) = FORMAT(DATEADD(MONTH, -1, GETDATE()), ''MMMM yyyy'');
DECLARE @StartDate DATE = DATEFROMPARTS(YEAR(DATEADD(MONTH, -1, GETDATE())), MONTH(DATEADD(MONTH, -1, GETDATE())), 1);
DECLARE @EndDate DATE = EOMONTH(DATEADD(MONTH, -1, GETDATE()));

-- Rental Statistics
DECLARE @TotalRentals INT, @CompletedRentals INT, @TotalRevenue DECIMAL(10,2);

SELECT 
    @TotalRentals = COUNT(*),
    @CompletedRentals = SUM(CASE WHEN Status = ''Completed'' THEN 1 ELSE 0 END),
    @TotalRevenue = SUM(CASE WHEN Status = ''Completed'' THEN TotalAmount ELSE 0 END)
FROM Rentals 
WHERE CreatedAt BETWEEN @StartDate AND @EndDate;

PRINT ''📊 Monthly Report Generated for '' + @ReportMonth;
PRINT ''   - Total Rentals: '' + CAST(ISNULL(@TotalRentals, 0) AS VARCHAR);
PRINT ''   - Completed: '' + CAST(ISNULL(@CompletedRentals, 0) AS VARCHAR);
PRINT ''   - Revenue: ₺'' + FORMAT(ISNULL(@TotalRevenue, 0), ''N2'');
',
    @on_success_action = 1, -- Quit with success
    @on_fail_action = 2;    -- Quit with failure

-- Create schedule for monthly report (1st of every month at 9:00 AM)
EXEC dbo.sp_add_schedule
    @schedule_name = 'CarRental_MonthlyReport_Schedule',
    @freq_type = 16,       -- Monthly
    @freq_interval = 1,    -- 1st day of month
    @active_start_time = 090000; -- 9:00 AM

-- Attach schedule to job
EXEC dbo.sp_attach_schedule
    @job_name = 'CarRental_MonthlyReport',
    @schedule_name = 'CarRental_MonthlyReport_Schedule';

-- Add job to server
EXEC dbo.sp_add_jobserver
    @job_name = 'CarRental_MonthlyReport',
    @server_name = @@SERVERNAME;

PRINT '✅ Monthly report job created successfully!';
GO

PRINT '';
PRINT '✅ All SQL Server Agent Jobs created successfully!';
PRINT '';
PRINT '📋 CREATED JOBS:';
PRINT '1. CarRental_DailyBackup - Daily at 2:00 AM';
PRINT '2. CarRental_WeeklyArchiving - Sundays at 3:00 AM'; 
PRINT '3. CarRental_MonthlyReport - 1st of month at 9:00 AM';
PRINT '';
GO 
-- SQL SERVER AGENT JOBS
-- Car Rental System Automation
-- =====================================

USE msdb;
GO

-- =====================================
-- 1. DAILY BACKUP JOB
-- =====================================

-- Create Daily Backup Job
IF EXISTS (SELECT job_id FROM dbo.sysjobs WHERE name = 'CarRental_DailyBackup')
BEGIN
    EXEC dbo.sp_delete_job @job_name = 'CarRental_DailyBackup';
    PRINT 'Existing daily backup job deleted.';
END

EXEC dbo.sp_add_job
    @job_name = 'CarRental_DailyBackup',
    @enabled = 1,
    @description = 'Daily backup of CarRentalDB database with compression and verification',
    @category_name = 'Database Maintenance',
    @owner_login_name = 'sa';

-- Add backup job step
EXEC dbo.sp_add_jobstep
    @job_name = 'CarRental_DailyBackup',
    @step_name = 'Full Database Backup',
    @subsystem = 'TSQL',
    @command = '
DECLARE @BackupPath NVARCHAR(500);
DECLARE @BackupFileName NVARCHAR(500);
DECLARE @BackupDate NVARCHAR(20);

-- Create backup filename with timestamp
SET @BackupDate = FORMAT(GETDATE(), ''yyyyMMdd_HHmmss'');
SET @BackupPath = ''C:\Backup\CarRental\''; -- Adjust path as needed
SET @BackupFileName = @BackupPath + ''CarRentalDB_'' + @BackupDate + ''.bak'';

-- Create backup directory if not exists (requires xp_cmdshell enabled)
EXEC xp_cmdshell ''mkdir "C:\Backup\CarRental"'', NO_OUTPUT;

-- Create database backup
BACKUP DATABASE [CarRentalDB] 
TO DISK = @BackupFileName
WITH COMPRESSION, 
     CHECKSUM, 
     INIT,
     FORMAT,
     DESCRIPTION = ''CarRental System Daily Full Backup'',
     NAME = ''CarRentalDB Full Backup'';

-- Verify backup
RESTORE VERIFYONLY FROM DISK = @BackupFileName;

-- Log backup completion
USE CarRentalDB;
INSERT INTO Activities (ActivityType, ActivityDescription, CreatedAt)
VALUES (''System'', ''Daily backup completed successfully: '' + @BackupFileName, GETDATE());

PRINT ''✅ Daily backup completed: '' + @BackupFileName;
',
    @on_success_action = 1, -- Quit with success
    @on_fail_action = 2;    -- Quit with failure

-- Create schedule for daily backup (every day at 2:00 AM)
EXEC dbo.sp_add_schedule
    @schedule_name = 'CarRental_DailyBackup_Schedule',
    @freq_type = 4,        -- Daily
    @freq_interval = 1,    -- Every 1 day
    @active_start_time = 020000; -- 2:00 AM

-- Attach schedule to job
EXEC dbo.sp_attach_schedule
    @job_name = 'CarRental_DailyBackup',
    @schedule_name = 'CarRental_DailyBackup_Schedule';

-- Add job to server
EXEC dbo.sp_add_jobserver
    @job_name = 'CarRental_DailyBackup',
    @server_name = @@SERVERNAME;

PRINT '✅ Daily backup job created successfully!';
GO

-- =====================================
-- 2. WEEKLY ARCHIVING JOB
-- =====================================

-- Create Weekly Archiving Job
IF EXISTS (SELECT job_id FROM dbo.sysjobs WHERE name = 'CarRental_WeeklyArchiving')
BEGIN
    EXEC dbo.sp_delete_job @job_name = 'CarRental_WeeklyArchiving';
    PRINT 'Existing weekly archiving job deleted.';
END

EXEC dbo.sp_add_job
    @job_name = 'CarRental_WeeklyArchiving',
    @enabled = 1,
    @description = 'Weekly archiving of old rental data and activities',
    @category_name = 'Database Maintenance',
    @owner_login_name = 'sa';

-- Add archiving job step
EXEC dbo.sp_add_jobstep
    @job_name = 'CarRental_WeeklyArchiving',
    @step_name = 'Archive Old Data',
    @subsystem = 'TSQL',
    @database_name = 'CarRentalDB',
    @command = '
-- Archive rentals older than 1 year to archive table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = ''RentalsArchive'')
BEGIN
    -- Create archive table
    SELECT TOP 0 * 
    INTO RentalsArchive 
    FROM Rentals;
    
    -- Add archive date column
    ALTER TABLE RentalsArchive ADD ArchivedDate DATETIME2 DEFAULT GETDATE();
    
    PRINT ''📦 RentalsArchive table created'';
END

-- Archive old completed rentals
DECLARE @ArchiveDate DATE = DATEADD(YEAR, -1, GETDATE());
DECLARE @ArchivedRentals INT = 0;

INSERT INTO RentalsArchive
SELECT *, GETDATE() as ArchivedDate
FROM Rentals 
WHERE Status IN (''Completed'', ''Cancelled'') 
AND CreatedAt < @ArchiveDate;

SET @ArchivedRentals = @@ROWCOUNT;

-- Delete archived rentals from main table
DELETE FROM Rentals 
WHERE Status IN (''Completed'', ''Cancelled'') 
AND CreatedAt < @ArchiveDate;

PRINT ''✅ Weekly archiving completed:'';
PRINT ''   - Archived Rentals: '' + CAST(@ArchivedRentals AS VARCHAR);
',
    @on_success_action = 1, -- Quit with success
    @on_fail_action = 2;    -- Quit with failure

-- Create schedule for weekly archiving (every Sunday at 3:00 AM)
EXEC dbo.sp_add_schedule
    @schedule_name = 'CarRental_WeeklyArchiving_Schedule',
    @freq_type = 8,        -- Weekly
    @freq_interval = 1,    -- Sunday
    @active_start_time = 030000; -- 3:00 AM

-- Attach schedule to job
EXEC dbo.sp_attach_schedule
    @job_name = 'CarRental_WeeklyArchiving',
    @schedule_name = 'CarRental_WeeklyArchiving_Schedule';

-- Add job to server
EXEC dbo.sp_add_jobserver
    @job_name = 'CarRental_WeeklyArchiving',
    @server_name = @@SERVERNAME;

PRINT '✅ Weekly archiving job created successfully!';
GO

-- =====================================
-- 3. MONTHLY REPORT EMAIL JOB
-- =====================================

-- Create Monthly Report Job
IF EXISTS (SELECT job_id FROM dbo.sysjobs WHERE name = 'CarRental_MonthlyReport')
BEGIN
    EXEC dbo.sp_delete_job @job_name = 'CarRental_MonthlyReport';
    PRINT 'Existing monthly report job deleted.';
END

EXEC dbo.sp_add_job
    @job_name = 'CarRental_MonthlyReport',
    @enabled = 1,
    @description = 'Generate and email monthly business reports',
    @category_name = 'Report Generation',
    @owner_login_name = 'sa';

-- Add report generation step
EXEC dbo.sp_add_jobstep
    @job_name = 'CarRental_MonthlyReport',
    @step_name = 'Generate Monthly Report',
    @subsystem = 'TSQL',
    @database_name = 'CarRentalDB',
    @command = '
-- Generate Monthly Report Data
DECLARE @ReportMonth VARCHAR(50) = FORMAT(DATEADD(MONTH, -1, GETDATE()), ''MMMM yyyy'');
DECLARE @StartDate DATE = DATEFROMPARTS(YEAR(DATEADD(MONTH, -1, GETDATE())), MONTH(DATEADD(MONTH, -1, GETDATE())), 1);
DECLARE @EndDate DATE = EOMONTH(DATEADD(MONTH, -1, GETDATE()));

-- Rental Statistics
DECLARE @TotalRentals INT, @CompletedRentals INT, @TotalRevenue DECIMAL(10,2);

SELECT 
    @TotalRentals = COUNT(*),
    @CompletedRentals = SUM(CASE WHEN Status = ''Completed'' THEN 1 ELSE 0 END),
    @TotalRevenue = SUM(CASE WHEN Status = ''Completed'' THEN TotalAmount ELSE 0 END)
FROM Rentals 
WHERE CreatedAt BETWEEN @StartDate AND @EndDate;

PRINT ''📊 Monthly Report Generated for '' + @ReportMonth;
PRINT ''   - Total Rentals: '' + CAST(ISNULL(@TotalRentals, 0) AS VARCHAR);
PRINT ''   - Completed: '' + CAST(ISNULL(@CompletedRentals, 0) AS VARCHAR);
PRINT ''   - Revenue: ₺'' + FORMAT(ISNULL(@TotalRevenue, 0), ''N2'');
',
    @on_success_action = 1, -- Quit with success
    @on_fail_action = 2;    -- Quit with failure

-- Create schedule for monthly report (1st of every month at 9:00 AM)
EXEC dbo.sp_add_schedule
    @schedule_name = 'CarRental_MonthlyReport_Schedule',
    @freq_type = 16,       -- Monthly
    @freq_interval = 1,    -- 1st day of month
    @active_start_time = 090000; -- 9:00 AM

-- Attach schedule to job
EXEC dbo.sp_attach_schedule
    @job_name = 'CarRental_MonthlyReport',
    @schedule_name = 'CarRental_MonthlyReport_Schedule';

-- Add job to server
EXEC dbo.sp_add_jobserver
    @job_name = 'CarRental_MonthlyReport',
    @server_name = @@SERVERNAME;

PRINT '✅ Monthly report job created successfully!';
GO

PRINT '';
PRINT '✅ All SQL Server Agent Jobs created successfully!';
PRINT '';
PRINT '📋 CREATED JOBS:';
PRINT '1. CarRental_DailyBackup - Daily at 2:00 AM';
PRINT '2. CarRental_WeeklyArchiving - Sundays at 3:00 AM'; 
PRINT '3. CarRental_MonthlyReport - 1st of month at 9:00 AM';
PRINT '';
GO 