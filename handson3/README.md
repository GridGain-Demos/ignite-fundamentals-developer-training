# Hands-on #3: Using Java API

This guide walks you through creating a Java application that connects to an Apache Ignite 3 cluster, demonstrating key patterns for working with data using Ignite's Java API.

## Prerequisites

* JDK 11 or later
* Maven
* Docker and Docker Compose
* (Optionally an IDE, such as IntelliJ)

## Setting Up Your Java Project

## Building Your Java Application

Now, let's create a Java application that connects to our Ignite cluster and performs various data operations.

### Main Application Class

Review the `Main.java` file, paying special attention to the following:

* Connecting to the cluster
* Running SQL
* Using the key-value API
* Using the record API

## Running the Application

To run your application:

1. Make sure your Ignite cluster is up and running
2. Compile and run your Java application:

   ```bash
   mvn compile exec:java -Dexec.mainClass="org.gridgain.training.fundamentals.Main"
   ```

## Expected Output

You should see output similar to this:

```text
Connected to the cluster: [ClientClusterNode [id=f82f73ba-8e3d-4342-91f3-3ee9cd01632b, name=node1, address=localhost:10800, nodeMetadata=null]]

--- Querying Album table ---
Album: King For A Day Fool For A Lifetime
Album: Na Pista
Album: The Best Of R.E.M.: The IRS Years
Album: Cássia Eller - Coleção Sem Limite [Disc 2]
Album: Cássia Eller - Sem Limite [Disc 1]
Album: Wagner: Favourite Overtures
Album: Greatest Hits II
Album: Album Of The Year
Album: Alcohol Fueled Brewtality Live! [Disc 1]
Album: New Adventures In Hi-Fi

--- Populating Artist and Album tables using different views ---
Added record using RecordView with Tuple
Added record using RecordView with POJO
Added record using KeyValueView with Tuples
Added record using KeyValueView with Native Types

--- Querying Album table ---
Album: Technique
```

## Understanding Table Views in GridGain 9

Ignite 3 and GridGain 9 provides multiple view patterns for interacting with tables:

### RecordView Pattern

RecordView treats tables as a collection of records, perfect for operations that work with entire rows:

```java
// Get RecordView for Tuple objects (schema-less)
RecordView<Tuple> recordView = table.recordView();
recordView.upsert(null, Tuple.create().set("id", 2).set("name", "Jane"));

// Get RecordView for mapped POJO objects (type-safe)
RecordView<Artist> pojoView = table.recordView(Artist.class);
pojoView.upsert(null, new Artist(3, "Beatles"));
```

### KeyValueView Pattern

KeyValueView treats tables as a key-value store, ideal for simple lookups:

```java
// Get KeyValueView for Tuple objects
KeyValueView<Tuple, Tuple> keyValueView = table.keyValueView();
keyValueView.put(null, Tuple.create().set("id", 4), Tuple.create().set("name", "Jill"));

// Get KeyValueView for native Java types
KeyValueView<Integer, String> keyValuePojoView = table.keyValueView(Integer.class, String.class);
keyValuePojoView.put(null, 5, "Joe");
```

## Cleaning Up

To stop your Ignite cluster when you're done:

```bash
docker compose down
```

## Troubleshooting

If you encounter connection issues:

* Verify your Docker containers are running with `docker compose ps`
* Check if the exposed ports match those in your client configuration
* Ensure that the `localhost` interface can access the Docker container network

## Next Steps

Now that you've explored the basics of connecting to Ignite and interacting with data:

* Try implementing transactions
* Experiment with more complex schemas and data types
* Explore data partitioning strategies
* Investigate Ignite's distributed computing capabilities

For more information, consult the [Apache Ignite 3 documentation](https://ignite.apache.org/docs/3.0.0/index).
