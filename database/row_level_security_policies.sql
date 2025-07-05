-- =====================================
-- ROW-LEVEL SECURITY POLICIES 
-- Car Rental System
-- =====================================

USE CarRentalDB;
GO

-- Enable Row-Level Security on relevant tables
-- 1. RENTALS Table - Users can only see their own rentals
-- 2. CUSTOMERS Table - Users can only see their own customer data
-- 3. ACTIVITIES Table - Users can only see activities related to their data

-- =====================================
-- 1. RENTALS TABLE RLS
-- =====================================

-- Create security policy function for Rentals
CREATE OR ALTER FUNCTION Security.fn_securitypredicate_Rentals(
    @UserId INT,
    @CustomerID INT
)
RETURNS TABLE
WITH SCHEMABINDING
AS
RETURN (
    SELECT 1 AS fn_securitypredicate_result
    WHERE 
        IS_ROLEMEMBER('db_owner') = 1
        OR IS_ROLEMEMBER('CarRentalAdmin') = 1
        OR EXISTS (
            SELECT 1
            FROM dbo.Customers c
            WHERE c.UserID = @UserId
              AND c.CustomerID = @CustomerID
        )
        OR USER_NAME() IN ('sa', 'CarRentalService')
);
GO

-- Enable RLS on Rentals table
ALTER TABLE Rentals ENABLE ROW LEVEL SECURITY;
GO

-- Create security policy for Rentals
CREATE SECURITY POLICY RentalAccessPolicy
ADD FILTER PREDICATE Security.fn_securitypredicate_Rentals(USER_ID(), CustomerID) ON dbo.Rentals,
ADD BLOCK PREDICATE Security.fn_securitypredicate_Rentals(USER_ID(), CustomerID) ON dbo.Rentals AFTER INSERT,
ADD BLOCK PREDICATE Security.fn_securitypredicate_Rentals(USER_ID(), CustomerID) ON dbo.Rentals AFTER UPDATE
WITH (STATE = ON);
GO


-- =====================================
-- 2. CUSTOMERS TABLE RLS
-- =====================================

-- Create security policy function for Customers
CREATE OR ALTER FUNCTION Security.fn_securitypredicate_Customers(
    @UserId INT,
    @RowUserId INT
)
RETURNS TABLE
WITH SCHEMABINDING
AS
RETURN (
    SELECT 1 AS fn_securitypredicate_result
    WHERE 
        IS_ROLEMEMBER('db_owner') = 1
        OR IS_ROLEMEMBER('CarRentalAdmin') = 1
        OR @UserId = @RowUserId
        OR USER_NAME() IN ('sa', 'CarRentalService')
);
GO


-- Enable RLS on Customers table
ALTER TABLE Customers ENABLE ROW LEVEL SECURITY;
GO

-- Create security policy for Customers
CREATE SECURITY POLICY CustomerAccessPolicy
ADD FILTER PREDICATE Security.fn_securitypredicate_Customers(USER_ID(), UserID) ON dbo.Customers,
ADD BLOCK PREDICATE Security.fn_securitypredicate_Customers(USER_ID(), UserID) ON dbo.Customers AFTER INSERT,
ADD BLOCK PREDICATE Security.fn_securitypredicate_Customers(USER_ID(), UserID) ON dbo.Customers AFTER UPDATE
WITH (STATE = ON);
GO

-- =====================================
-- 3. ACTIVITIES TABLE RLS (AUDIT LOG)
-- =====================================

-- Create security policy function for Activities
CREATE OR ALTER FUNCTION Security.fn_securitypredicate_Activities(
    @UserId INT,
    @Description NVARCHAR(1000)
)
RETURNS TABLE
WITH SCHEMABINDING
AS
RETURN (
    SELECT 1 AS fn_securitypredicate_result
    WHERE
        IS_ROLEMEMBER('db_owner') = 1
        OR IS_ROLEMEMBER('CarRentalAdmin') = 1
        OR EXISTS (
            SELECT 1
            FROM dbo.Customers c
            WHERE c.UserID = @UserId
              AND (
                  @Description LIKE '%' + c.FullName + '%'
                  OR @Description LIKE '%' + c.Phone + '%'
                  OR @Description LIKE '%Customer ID: ' + CAST(c.CustomerID AS NVARCHAR) + '%'
              )
        )
        OR USER_NAME() IN ('sa', 'CarRentalService')
);
GO


-- Create security policy for Activities
CREATE SECURITY POLICY ActivityAccessPolicy
ADD FILTER PREDICATE Security.fn_securitypredicate_Activities(USER_ID(), Description)
ON dbo.Activities
WITH (STATE = ON);
GO


-- =====================================
-- 4. USER MANAGEMENT FOR RLS
-- =====================================

-- Create Security schema if not exists
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'Security')
BEGIN
    EXEC('CREATE SCHEMA Security');
END
GO

-- Create role for Car Rental Admins
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'CarRentalAdmin' AND type = 'R')
BEGIN
    CREATE ROLE CarRentalAdmin;
END
GO

-- Grant necessary permissions to CarRentalAdmin role
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.Rentals TO CarRentalAdmin;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.Customers TO CarRentalAdmin;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.Cars TO CarRentalAdmin;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.Activities TO CarRentalAdmin;
GRANT SELECT ON dbo.Users TO CarRentalAdmin;
GRANT SELECT ON dbo.Roles TO CarRentalAdmin;
GO

-- Create role for Car Rental Users (customers)
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'CarRentalUser' AND type = 'R')
BEGIN
    CREATE ROLE CarRentalUser;
END
GO

-- Grant limited permissions to CarRentalUser role
GRANT SELECT ON dbo.Rentals TO CarRentalUser;
GRANT SELECT ON dbo.Customers TO CarRentalUser;
GRANT SELECT ON dbo.Cars TO CarRentalUser;
GRANT SELECT ON dbo.Activities TO CarRentalUser;
GO

-- =====================================
-- 5. TESTING RLS POLICIES
-- =====================================

-- Create test users for RLS validation
/*
-- Create test admin user
CREATE LOGIN CarRentalAdminTest WITH PASSWORD = 'AdminTest123!';
CREATE USER CarRentalAdminTest FOR LOGIN CarRentalAdminTest;
ALTER ROLE CarRentalAdmin ADD MEMBER CarRentalAdminTest;

-- Create test regular user
CREATE LOGIN CarRentalUserTest WITH PASSWORD = 'UserTest123!';
CREATE USER CarRentalUserTest FOR LOGIN CarRentalUserTest;
ALTER ROLE CarRentalUser ADD MEMBER CarRentalUserTest;

-- Test queries to validate RLS
-- As Admin: Should see all records
EXECUTE AS USER = 'CarRentalAdminTest';
SELECT COUNT(*) AS AdminCanSeeRentals FROM Rentals;
SELECT COUNT(*) AS AdminCanSeeCustomers FROM Customers;
REVERT;

-- As User: Should see only their own records
EXECUTE AS USER = 'CarRentalUserTest';
SELECT COUNT(*) AS UserCanSeeRentals FROM Rentals;
SELECT COUNT(*) AS UserCanSeeCustomers FROM Customers;
REVERT;
*/

-- =====================================
-- 6. RLS MONITORING AND MAINTENANCE
-- =====================================

-- View to monitor RLS policies
CREATE OR ALTER VIEW Security.vw_RLS_PolicyStatus
AS
SELECT 
    p.name AS PolicyName,
    p.is_enabled AS IsEnabled,
    p.is_schema_bound AS IsSchemaBinding,
    t.name AS TableName,
    pp.predicate_definition AS PredicateDefinition,
    pp.predicate_type_desc AS PredicateType
FROM sys.security_policies p
INNER JOIN sys.security_predicates pp ON p.object_id = pp.object_id
INNER JOIN sys.tables t ON pp.target_object_id = t.object_id;
GO

-- Grant SELECT permission to admin role
GRANT SELECT ON Security.vw_RLS_PolicyStatus TO CarRentalAdmin;
GO

-- Procedure to disable RLS for maintenance
CREATE OR ALTER PROCEDURE Security.sp_DisableRLS
AS
BEGIN
    SET NOCOUNT ON;
    
    ALTER SECURITY POLICY RentalAccessPolicy WITH (STATE = OFF);
    ALTER SECURITY POLICY CustomerAccessPolicy WITH (STATE = OFF);
    ALTER SECURITY POLICY ActivityAccessPolicy WITH (STATE = OFF);
    
    PRINT 'Row-Level Security policies disabled for maintenance.';
END
GO

-- Procedure to enable RLS after maintenance
CREATE OR ALTER PROCEDURE Security.sp_EnableRLS
AS
BEGIN
    SET NOCOUNT ON;
    
    ALTER SECURITY POLICY RentalAccessPolicy WITH (STATE = ON);
    ALTER SECURITY POLICY CustomerAccessPolicy WITH (STATE = ON);
    ALTER SECURITY POLICY ActivityAccessPolicy WITH (STATE = ON);
    
    PRINT 'Row-Level Security policies enabled.';
END
GO

-- Grant execute permissions to admin
GRANT EXECUTE ON Security.sp_DisableRLS TO CarRentalAdmin;
GRANT EXECUTE ON Security.sp_EnableRLS TO CarRentalAdmin;
GO

-- =====================================
-- 7. DOCUMENTATION AND NOTES
-- =====================================

/*
Row-Level Security Implementation Summary:

1. RENTALS TABLE:
   - Users can only see rentals associated with their customer records
   - Admins can see all rentals
   - Prevents data leakage between customers

2. CUSTOMERS TABLE:
   - Users can only see their own customer information
   - Admins can see all customer records
   - Ensures privacy of personal information

3. ACTIVITIES TABLE:
   - Users can only see activities related to their actions
   - Admins can see all system activities
   - Maintains audit trail security

4. SECURITY ROLES:
   - CarRentalAdmin: Full access to all records
   - CarRentalUser: Limited access based on ownership

5. MAINTENANCE:
   - Use Security.sp_DisableRLS / Security.sp_EnableRLS for maintenance
   - Monitor with Security.vw_RLS_PolicyStatus view

6. TESTING:
   - Uncomment test section to create test users
   - Validate RLS functionality before production deployment

IMPORTANT NOTES:
- RLS policies are enforced at the database level
- Application connections should use individual user credentials
- Service accounts (sa, CarRentalService) bypass RLS for system operations
- Regular monitoring is recommended to ensure policies remain effective
*/

PRINT 'Row-Level Security policies created successfully!';
PRINT 'Please review and test the policies before enabling in production.';
GO 
-- ROW-LEVEL SECURITY POLICIES 
-- Car Rental System
-- =====================================

USE CarRentalDB;
GO

-- Enable Row-Level Security on relevant tables
-- 1. RENTALS Table - Users can only see their own rentals
-- 2. CUSTOMERS Table - Users can only see their own customer data
-- 3. ACTIVITIES Table - Users can only see activities related to their data

-- =====================================
-- 1. RENTALS TABLE RLS
-- =====================================

-- Create security policy function for Rentals
CREATE OR ALTER FUNCTION Security.fn_securitypredicate_Rentals(
    @UserId INT,
    @CustomerID INT
)
RETURNS TABLE
WITH SCHEMABINDING
AS
RETURN (
    SELECT 1 AS fn_securitypredicate_result
    WHERE 
        IS_ROLEMEMBER('db_owner') = 1
        OR IS_ROLEMEMBER('CarRentalAdmin') = 1
        OR EXISTS (
            SELECT 1
            FROM dbo.Customers c
            WHERE c.UserID = @UserId
              AND c.CustomerID = @CustomerID
        )
        OR USER_NAME() IN ('sa', 'CarRentalService')
);
GO

-- Enable RLS on Rentals table
ALTER TABLE Rentals ENABLE ROW LEVEL SECURITY;
GO

-- Create security policy for Rentals
CREATE SECURITY POLICY RentalAccessPolicy
ADD FILTER PREDICATE Security.fn_securitypredicate_Rentals(USER_ID(), CustomerID) ON dbo.Rentals,
ADD BLOCK PREDICATE Security.fn_securitypredicate_Rentals(USER_ID(), CustomerID) ON dbo.Rentals AFTER INSERT,
ADD BLOCK PREDICATE Security.fn_securitypredicate_Rentals(USER_ID(), CustomerID) ON dbo.Rentals AFTER UPDATE
WITH (STATE = ON);
GO


-- =====================================
-- 2. CUSTOMERS TABLE RLS
-- =====================================

-- Create security policy function for Customers
CREATE OR ALTER FUNCTION Security.fn_securitypredicate_Customers(
    @UserId INT,
    @RowUserId INT
)
RETURNS TABLE
WITH SCHEMABINDING
AS
RETURN (
    SELECT 1 AS fn_securitypredicate_result
    WHERE 
        IS_ROLEMEMBER('db_owner') = 1
        OR IS_ROLEMEMBER('CarRentalAdmin') = 1
        OR @UserId = @RowUserId
        OR USER_NAME() IN ('sa', 'CarRentalService')
);
GO


-- Enable RLS on Customers table
ALTER TABLE Customers ENABLE ROW LEVEL SECURITY;
GO

-- Create security policy for Customers
CREATE SECURITY POLICY CustomerAccessPolicy
ADD FILTER PREDICATE Security.fn_securitypredicate_Customers(USER_ID(), UserID) ON dbo.Customers,
ADD BLOCK PREDICATE Security.fn_securitypredicate_Customers(USER_ID(), UserID) ON dbo.Customers AFTER INSERT,
ADD BLOCK PREDICATE Security.fn_securitypredicate_Customers(USER_ID(), UserID) ON dbo.Customers AFTER UPDATE
WITH (STATE = ON);
GO

-- =====================================
-- 3. ACTIVITIES TABLE RLS (AUDIT LOG)
-- =====================================

-- Create security policy function for Activities
CREATE OR ALTER FUNCTION Security.fn_securitypredicate_Activities(
    @UserId INT,
    @Description NVARCHAR(1000)
)
RETURNS TABLE
WITH SCHEMABINDING
AS
RETURN (
    SELECT 1 AS fn_securitypredicate_result
    WHERE
        IS_ROLEMEMBER('db_owner') = 1
        OR IS_ROLEMEMBER('CarRentalAdmin') = 1
        OR EXISTS (
            SELECT 1
            FROM dbo.Customers c
            WHERE c.UserID = @UserId
              AND (
                  @Description LIKE '%' + c.FullName + '%'
                  OR @Description LIKE '%' + c.Phone + '%'
                  OR @Description LIKE '%Customer ID: ' + CAST(c.CustomerID AS NVARCHAR) + '%'
              )
        )
        OR USER_NAME() IN ('sa', 'CarRentalService')
);
GO


-- Create security policy for Activities
CREATE SECURITY POLICY ActivityAccessPolicy
ADD FILTER PREDICATE Security.fn_securitypredicate_Activities(USER_ID(), Description)
ON dbo.Activities
WITH (STATE = ON);
GO


-- =====================================
-- 4. USER MANAGEMENT FOR RLS
-- =====================================

-- Create Security schema if not exists
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'Security')
BEGIN
    EXEC('CREATE SCHEMA Security');
END
GO

-- Create role for Car Rental Admins
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'CarRentalAdmin' AND type = 'R')
BEGIN
    CREATE ROLE CarRentalAdmin;
END
GO

-- Grant necessary permissions to CarRentalAdmin role
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.Rentals TO CarRentalAdmin;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.Customers TO CarRentalAdmin;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.Cars TO CarRentalAdmin;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.Activities TO CarRentalAdmin;
GRANT SELECT ON dbo.Users TO CarRentalAdmin;
GRANT SELECT ON dbo.Roles TO CarRentalAdmin;
GO

-- Create role for Car Rental Users (customers)
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'CarRentalUser' AND type = 'R')
BEGIN
    CREATE ROLE CarRentalUser;
END
GO

-- Grant limited permissions to CarRentalUser role
GRANT SELECT ON dbo.Rentals TO CarRentalUser;
GRANT SELECT ON dbo.Customers TO CarRentalUser;
GRANT SELECT ON dbo.Cars TO CarRentalUser;
GRANT SELECT ON dbo.Activities TO CarRentalUser;
GO

-- =====================================
-- 5. TESTING RLS POLICIES
-- =====================================

-- Create test users for RLS validation
/*
-- Create test admin user
CREATE LOGIN CarRentalAdminTest WITH PASSWORD = 'AdminTest123!';
CREATE USER CarRentalAdminTest FOR LOGIN CarRentalAdminTest;
ALTER ROLE CarRentalAdmin ADD MEMBER CarRentalAdminTest;

-- Create test regular user
CREATE LOGIN CarRentalUserTest WITH PASSWORD = 'UserTest123!';
CREATE USER CarRentalUserTest FOR LOGIN CarRentalUserTest;
ALTER ROLE CarRentalUser ADD MEMBER CarRentalUserTest;

-- Test queries to validate RLS
-- As Admin: Should see all records
EXECUTE AS USER = 'CarRentalAdminTest';
SELECT COUNT(*) AS AdminCanSeeRentals FROM Rentals;
SELECT COUNT(*) AS AdminCanSeeCustomers FROM Customers;
REVERT;

-- As User: Should see only their own records
EXECUTE AS USER = 'CarRentalUserTest';
SELECT COUNT(*) AS UserCanSeeRentals FROM Rentals;
SELECT COUNT(*) AS UserCanSeeCustomers FROM Customers;
REVERT;
*/

-- =====================================
-- 6. RLS MONITORING AND MAINTENANCE
-- =====================================

-- View to monitor RLS policies
CREATE OR ALTER VIEW Security.vw_RLS_PolicyStatus
AS
SELECT 
    p.name AS PolicyName,
    p.is_enabled AS IsEnabled,
    p.is_schema_bound AS IsSchemaBinding,
    t.name AS TableName,
    pp.predicate_definition AS PredicateDefinition,
    pp.predicate_type_desc AS PredicateType
FROM sys.security_policies p
INNER JOIN sys.security_predicates pp ON p.object_id = pp.object_id
INNER JOIN sys.tables t ON pp.target_object_id = t.object_id;
GO

-- Grant SELECT permission to admin role
GRANT SELECT ON Security.vw_RLS_PolicyStatus TO CarRentalAdmin;
GO

-- Procedure to disable RLS for maintenance
CREATE OR ALTER PROCEDURE Security.sp_DisableRLS
AS
BEGIN
    SET NOCOUNT ON;
    
    ALTER SECURITY POLICY RentalAccessPolicy WITH (STATE = OFF);
    ALTER SECURITY POLICY CustomerAccessPolicy WITH (STATE = OFF);
    ALTER SECURITY POLICY ActivityAccessPolicy WITH (STATE = OFF);
    
    PRINT 'Row-Level Security policies disabled for maintenance.';
END
GO

-- Procedure to enable RLS after maintenance
CREATE OR ALTER PROCEDURE Security.sp_EnableRLS
AS
BEGIN
    SET NOCOUNT ON;
    
    ALTER SECURITY POLICY RentalAccessPolicy WITH (STATE = ON);
    ALTER SECURITY POLICY CustomerAccessPolicy WITH (STATE = ON);
    ALTER SECURITY POLICY ActivityAccessPolicy WITH (STATE = ON);
    
    PRINT 'Row-Level Security policies enabled.';
END
GO

-- Grant execute permissions to admin
GRANT EXECUTE ON Security.sp_DisableRLS TO CarRentalAdmin;
GRANT EXECUTE ON Security.sp_EnableRLS TO CarRentalAdmin;
GO

-- =====================================
-- 7. DOCUMENTATION AND NOTES
-- =====================================

/*
Row-Level Security Implementation Summary:

1. RENTALS TABLE:
   - Users can only see rentals associated with their customer records
   - Admins can see all rentals
   - Prevents data leakage between customers

2. CUSTOMERS TABLE:
   - Users can only see their own customer information
   - Admins can see all customer records
   - Ensures privacy of personal information

3. ACTIVITIES TABLE:
   - Users can only see activities related to their actions
   - Admins can see all system activities
   - Maintains audit trail security

4. SECURITY ROLES:
   - CarRentalAdmin: Full access to all records
   - CarRentalUser: Limited access based on ownership

5. MAINTENANCE:
   - Use Security.sp_DisableRLS / Security.sp_EnableRLS for maintenance
   - Monitor with Security.vw_RLS_PolicyStatus view

6. TESTING:
   - Uncomment test section to create test users
   - Validate RLS functionality before production deployment

IMPORTANT NOTES:
- RLS policies are enforced at the database level
- Application connections should use individual user credentials
- Service accounts (sa, CarRentalService) bypass RLS for system operations
- Regular monitoring is recommended to ensure policies remain effective
*/

PRINT 'Row-Level Security policies created successfully!';
PRINT 'Please review and test the policies before enabling in production.';
GO 