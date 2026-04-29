-- Chinook database schema for GridGain 8 Community Edition.
--
-- Each table specifies a cache mode in its WITH clause:
--   - Partitioned caches: WITH "template=partitioned,backups=1"
--   - Replicated caches:  WITH "template=replicated"
--   - Colocation:         WITH "affinityKey=<column>"
--   - CACHE_NAME set on each table for clean cache-API access from Java/.NET.

-- Drop all tables (in reverse order of dependencies)
DROP TABLE IF EXISTS PlaylistTrack;
DROP TABLE IF EXISTS Playlist;
DROP TABLE IF EXISTS InvoiceLine;
DROP TABLE IF EXISTS Invoice;
DROP TABLE IF EXISTS Track;
DROP TABLE IF EXISTS MediaType;
DROP TABLE IF EXISTS Genre;
DROP TABLE IF EXISTS Employee;
DROP TABLE IF EXISTS Customer;
DROP TABLE IF EXISTS Album;
DROP TABLE IF EXISTS Artist;

-- Artist: partitioned, single-column PK.
-- VALUE_TYPE=Artist makes KV-written BinaryObjects visible in SQL queries.
-- Without it, cache.put() succeeds but new rows don't appear in SELECT.
CREATE TABLE Artist
(
    ArtistId INT NOT NULL,
    Name VARCHAR(120),
    PRIMARY KEY (ArtistId)
) WITH "template=partitioned,backups=1,CACHE_NAME=Artist,VALUE_TYPE=Artist";

-- Album: partitioned, colocated with Artist by ArtistId.
CREATE TABLE Album
(
    AlbumId INT NOT NULL,
    Title VARCHAR(160) NOT NULL,
    ArtistId INT NOT NULL,
    ReleaseYear INT,
    PRIMARY KEY (AlbumId, ArtistId)
) WITH "template=partitioned,backups=1,affinityKey=ArtistId,CACHE_NAME=Album";

-- Genre: replicated (small lookup table, needed on every node for joins).
CREATE TABLE Genre
(
    GenreId INT NOT NULL,
    Name VARCHAR(120),
    PRIMARY KEY (GenreId)
) WITH "template=replicated,CACHE_NAME=Genre";

-- MediaType: replicated (small lookup table).
CREATE TABLE MediaType
(
    MediaTypeId INT NOT NULL,
    Name VARCHAR(120),
    PRIMARY KEY (MediaTypeId)
) WITH "template=replicated,CACHE_NAME=MediaType";

-- Customer: partitioned.
CREATE TABLE Customer
(
    CustomerId INT NOT NULL,
    FirstName VARCHAR(40) NOT NULL,
    LastName VARCHAR(20) NOT NULL,
    Company VARCHAR(80),
    Address VARCHAR(70),
    City VARCHAR(40),
    State VARCHAR(40),
    Country VARCHAR(40),
    PostalCode VARCHAR(10),
    Phone VARCHAR(24),
    Fax VARCHAR(24),
    Email VARCHAR(60) NOT NULL,
    SupportRepId INT,
    PRIMARY KEY (CustomerId)
) WITH "template=partitioned,backups=1,CACHE_NAME=Customer";

-- Employee: partitioned.
CREATE TABLE Employee
(
    EmployeeId INT NOT NULL,
    LastName VARCHAR(20) NOT NULL,
    FirstName VARCHAR(20) NOT NULL,
    Title VARCHAR(30),
    ReportsTo INT,
    BirthDate DATE,
    HireDate DATE,
    Address VARCHAR(70),
    City VARCHAR(40),
    State VARCHAR(40),
    Country VARCHAR(40),
    PostalCode VARCHAR(10),
    Phone VARCHAR(24),
    Fax VARCHAR(24),
    Email VARCHAR(60),
    PRIMARY KEY (EmployeeId)
) WITH "template=partitioned,backups=1,CACHE_NAME=Employee";

-- Invoice: partitioned, colocated with Customer by CustomerId.
CREATE TABLE Invoice
(
    InvoiceId INT NOT NULL,
    CustomerId INT NOT NULL,
    InvoiceDate DATE NOT NULL,
    BillingAddress VARCHAR(70),
    BillingCity VARCHAR(40),
    BillingState VARCHAR(40),
    BillingCountry VARCHAR(40),
    BillingPostalCode VARCHAR(10),
    Total NUMERIC(10,2) NOT NULL,
    PRIMARY KEY (InvoiceId, CustomerId)
) WITH "template=partitioned,backups=1,affinityKey=CustomerId,CACHE_NAME=Invoice";

-- InvoiceLine: partitioned, colocated with Invoice by InvoiceId.
CREATE TABLE InvoiceLine
(
    InvoiceLineId INT NOT NULL,
    InvoiceId INT NOT NULL,
    TrackId INT NOT NULL,
    UnitPrice NUMERIC(10,2) NOT NULL,
    Quantity INT NOT NULL,
    PRIMARY KEY (InvoiceLineId, InvoiceId)
) WITH "template=partitioned,backups=1,affinityKey=InvoiceId,CACHE_NAME=InvoiceLine";

-- Playlist: partitioned.
CREATE TABLE Playlist
(
    PlaylistId INT NOT NULL,
    Name VARCHAR(120),
    PRIMARY KEY (PlaylistId)
) WITH "template=partitioned,backups=1,CACHE_NAME=Playlist";

-- PlaylistTrack: partitioned.
-- GG8 requires at least one non-PK column; _val is a placeholder.
CREATE TABLE PlaylistTrack
(
    PlaylistId INT NOT NULL,
    TrackId INT NOT NULL,
    Active TINYINT DEFAULT 1,
    PRIMARY KEY (PlaylistId, TrackId)
) WITH "template=partitioned,backups=1,CACHE_NAME=PlaylistTrack";

-- Track: partitioned, colocated with Album by AlbumId.
CREATE TABLE Track
(
    TrackId INT NOT NULL,
    Name VARCHAR(200) NOT NULL,
    AlbumId INT NOT NULL,
    MediaTypeId INT NOT NULL,
    GenreId INT,
    Composer VARCHAR(220),
    Milliseconds INT NOT NULL,
    Bytes INT,
    UnitPrice NUMERIC(10,2) NOT NULL,
    PRIMARY KEY (TrackId, AlbumId)
) WITH "template=partitioned,backups=1,affinityKey=AlbumId,CACHE_NAME=Track";
