package org.gridgain.training.fundamentals;

import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import java.util.List;

/**
 * Demonstrates connecting to a GridGain 8 cluster and working with data
 * using the thin-client API: SQL queries, SQL DML, and key-value access
 * with BinaryObject.
 */
public class Main {
    public static void main(String[] args) {
        String address = System.getenv().getOrDefault("IGNITE_ADDRESS", "localhost:10800");

        ClientConfiguration cfg = new ClientConfiguration().setAddresses(address);

        try (IgniteClient client = Ignition.startClient(cfg)) {
            System.out.println("Connected to the cluster");

            resetDemoData(client);
            queryExistingTable(client);
            insertWithSqlDml(client);
            keyValueWithBinaryObject(client);
            verifyResults(client);
        }
    }

    /**
     * Removes the rows this demo creates so it can be run repeatedly.
     * Without this, the INSERTs below fail on a second run with
     * "Duplicate key during INSERT". Artist 277 is created by the
     * key-value block; it is visible to SQL, so DELETE removes it too.
     */
    private static void resetDemoData(IgniteClient client) {
        ClientCache<?, ?> cache = client.cache("Artist");

        cache.query(new SqlFieldsQuery("DELETE FROM Album WHERE AlbumId = ?").setArgs(348)).getAll();
        cache.query(new SqlFieldsQuery("DELETE FROM Artist WHERE ArtistId = ?").setArgs(276)).getAll();
        cache.query(new SqlFieldsQuery("DELETE FROM Artist WHERE ArtistId = ?").setArgs(277)).getAll();
    }

    /**
     * Block 1 — SQL SELECT: query pre-loaded Chinook data.
     */
    private static void queryExistingTable(IgniteClient client) {
        System.out.println("\n--- Querying Album table using SQL ---");

        ClientCache<?, ?> cache = client.cache("Artist");

        List<List<?>> rows = cache.query(
                new SqlFieldsQuery("SELECT AlbumId, Title, ArtistId FROM Album ORDER BY AlbumId LIMIT 10")
        ).getAll();

        for (List<?> row : rows) {
            System.out.println("Album: " + row.get(1));
        }
    }

    /**
     * Block 2 — SQL DML: insert data using parameterized SQL statements.
     */
    private static void insertWithSqlDml(IgniteClient client) {
        System.out.println("\n--- Inserting data using SQL DML ---");

        ClientCache<?, ?> cache = client.cache("Artist");

        cache.query(new SqlFieldsQuery(
                "INSERT INTO Artist (ArtistId, Name) VALUES (?, ?)")
                .setArgs(276, "New Discovery Band")
        ).getAll();
        System.out.println("Added artist using SQL INSERT");

        cache.query(new SqlFieldsQuery(
                "INSERT INTO Album (AlbumId, Title, ArtistId, ReleaseYear) VALUES (?, ?, ?, ?)")
                .setArgs(348, "First Light", 276, 2023)
        ).getAll();
        System.out.println("Added album using SQL INSERT");
    }

    /**
     * Block 3 — Key-Value API: put and get data using BinaryObject.
     * BinaryObject provides schema-less access without requiring POJO
     * classes on the server.
     */
    private static void keyValueWithBinaryObject(IgniteClient client) {
        System.out.println("\n--- Using Key-Value API with BinaryObject ---");

        ClientCache<Integer, BinaryObject> cache =
                client.<Integer, BinaryObject>cache("Artist").withKeepBinary();

        BinaryObject val = client.binary().builder("Artist")
                .setField("NAME", "New Order")
                .build();
        cache.put(277, val);
        System.out.println("Added artist 277 using Key-Value put");

        BinaryObject result = cache.get(277);
        if (result != null) {
            System.out.println("Retrieved artist 277: " + result.field("NAME"));
        }
    }

    /**
     * Block 4 — Verify: read back all new data with a SQL JOIN.
     */
    private static void verifyResults(IgniteClient client) {
        System.out.println("\n--- Verifying with SQL JOIN ---");

        ClientCache<?, ?> cache = client.cache("Artist");

        List<List<?>> results = cache.query(new SqlFieldsQuery(
                "SELECT a.Title, ar.Name FROM Album a " +
                        "JOIN Artist ar ON a.ArtistId = ar.ArtistId " +
                        "WHERE ar.ArtistId IN (276, 277)")
        ).getAll();

        for (List<?> row : results) {
            System.out.println("Album: '" + row.get(0) + "' by '" + row.get(1) + "'");
        }
    }
}
