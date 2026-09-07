# Hands-on #3: Using the Client API

This guide walks you through creating an application that connects to a GridGain 8 cluster using the thin-client API. The demo is available in both **Java** and **.NET** — pick the language for your session.

## Prerequisites

* Completed hands-on #1 and #2: running cluster with Chinook schema and data loaded
* **Java path:** JDK 17 and Maven — or use the Docker sidecar (no local install needed)
* **.NET path:** .NET 8 SDK — or use the Docker sidecar (no local install needed)
* (Optionally an IDE such as IntelliJ or Visual Studio)

## What the Demo Covers

The application demonstrates four patterns for working with a GridGain 8 cluster via the thin client:

1. **Connecting** to the cluster
2. **SQL SELECT** — querying existing data with `SqlFieldsQuery`
3. **SQL DML** — inserting data with parameterized SQL statements
4. **Key-Value API** — putting and getting data using `BinaryObject` (schema-less access without requiring POJO classes on the server)
5. **Verification** — reading back data with a SQL JOIN

The app deletes the rows it creates before it starts, so you can run it as many times as you like — and run the Java and .NET versions back to back.

## Java

### Review the Code

Open `java/src/main/java/org/gridgain/training/fundamentals/Main.java` and examine the four blocks:

* **`resetDemoData`** — deletes the rows created below, so the demo can be re-run
* **`queryExistingTable`** — runs a `SqlFieldsQuery` SELECT against the Album table
* **`insertWithSqlDml`** — inserts new Artist and Album rows using parameterized SQL
* **`keyValueWithBinaryObject`** — uses the cache key-value API with `BinaryObject` to put and get an Artist
* **`verifyResults`** — runs a SQL JOIN to confirm the data is consistent

### Build and Run

#### Option A: Local Maven

From the repository root:

```bash
mvn -f handson3/java/pom.xml compile exec:exec
```

#### Option B: Docker Maven Sidecar

```bash
docker compose -f docker/docker-compose.yaml run --rm app mvn -f handson3/java/pom.xml compile exec:exec
```

The sidecar connects to the cluster over the Docker network (address `node1:10800` is set via the `IGNITE_ADDRESS` environment variable).

### Expected Output

```text
Connected to the cluster

--- Querying Album table using SQL ---
Album: For Those About To Rock We Salute You
Album: Balls to the Wall
Album: Restless and Wild
...

--- Inserting data using SQL DML ---
Added artist using SQL INSERT
Added album using SQL INSERT

--- Using Key-Value API with BinaryObject ---
Added artist 277 using Key-Value put
Retrieved artist 277: New Order

--- Verifying with SQL JOIN ---
Album: 'First Light' by 'New Discovery Band'
```

## .NET

### Review the Code

Open `dotnet/Program.cs` and examine the same four blocks, translated to C#:

* **`ResetDemoData`** — deletes the rows created below, so the demo can be re-run
* **`QueryExistingTable`** — `SqlFieldsQuery` SELECT
* **`InsertWithSqlDml`** — parameterized SQL INSERT
* **`KeyValueWithBinaryObject`** — cache `Put`/`Get` with `IBinaryObject`
* **`VerifyResults`** — SQL JOIN verification

### Build and Run

#### Option A: Local .NET SDK

From the repository root:

```bash
dotnet run --project handson3/dotnet/dotnet.csproj
```

#### Option B: Docker .NET Sidecar

```bash
docker compose -f docker/docker-compose.yaml run --rm app-dotnet dotnet run --project handson3/dotnet/dotnet.csproj
```

### Expected Output

Same as the Java output above — both versions produce identical results.

The .NET client also prints one extra line before `Connected to the cluster`:

```text
[Warn] [] BinaryConfiguration.UnwrapNullablePrimitiveTypes is not enabled. ...
```

That warning is harmless — it concerns a legacy binary-format compatibility setting and does not affect this demo.

## Understanding GG8 Thin-Client Patterns

### SQL via SqlFieldsQuery

The primary way to work with data in GG8. You obtain a cache handle and call `.query()` (Java) or `.Query()` (.NET) with a `SqlFieldsQuery`.

**Java:**
```java
ClientCache<?, ?> cache = client.cache("Artist");
List<List<?>> rows = cache.query(new SqlFieldsQuery("SELECT * FROM Album WHERE ArtistId = ?").setArgs(22)).getAll();
```

**.NET:**
```csharp
var cache = client.GetCache<object, object>("Artist");
var rows = cache.Query(new SqlFieldsQuery("SELECT * FROM Album WHERE ArtistId = ?", 22)).GetAll();
```

### Key-Value API with BinaryObject

For direct cache access without SQL. `BinaryObject` lets you read and write fields without needing server-side POJO/POCO classes.

**Java:**
```java
ClientCache<Integer, BinaryObject> cache = client.<Integer, BinaryObject>cache("Artist").withKeepBinary();
BinaryObject val = client.binary().builder("Artist").setField("NAME", "My Band").build();
cache.put(999, val);
```

**.NET:**
```csharp
var cache = client.GetCache<int, IBinaryObject>("Artist").WithKeepBinary<int, IBinaryObject>();
var val = client.GetBinary().GetBuilder("Artist").SetField("NAME", "My Band").Build();
cache.Put(999, val);
```

## Cleaning Up

To stop the cluster when you're done with all exercises:

```bash
docker compose -f docker/docker-compose.yaml down
```

## Troubleshooting

* **Connection refused:** Verify containers are running with `docker compose -f docker/docker-compose.yaml ps`
* **Cache not found:** Make sure you loaded the schema and data in hands-on #2
* **Wrong address:** Local runs connect to `localhost:10800`; Docker sidecar runs use `node1:10800` (set automatically via `IGNITE_ADDRESS`)
* **`Duplicate key during INSERT [key=276]`:** Demo rows left behind by an earlier run. The app clears them at startup, so just run it again. On an older checkout, clear them by hand in sqlline: `DELETE FROM Album WHERE AlbumId = 348;` then `DELETE FROM Artist WHERE ArtistId IN (276, 277);`

## Next Steps

Now that you've explored the basics of connecting to GridGain and interacting with data:

* Try more complex SQL queries and joins
* Explore affinity colocation by querying colocated tables
* Investigate GridGain's distributed computing capabilities (compute grid, services)
* Look into persistence for durable storage

For more information, consult the [GridGain documentation](https://www.gridgain.com/docs/latest).
