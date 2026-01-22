package org.gridgain.training.fundamentals;

import org.apache.ignite.catalog.annotations.*;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.table.KeyValueView;
import org.apache.ignite.table.RecordView;
import org.apache.ignite.table.Tuple;

/**
 * This example demonstrates connecting to an GridGain 9 cluster
 * and working with data using different table view patterns.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // Create an array of connection addresses for fault tolerance
        String[] addresses = {
                "localhost:10800",
                "localhost:10801",
                "localhost:10802"
        };

        // Connect to the Ignite cluster using the client builder pattern
        try (IgniteClient client = IgniteClient.builder()
                .addresses(addresses)
                .build()) {

            System.out.println("Connected to the cluster: " + client.connections());

            // Demonstrate querying existing data using SQL API
            queryExistingTable(client);

            // Demonstrate different ways to interact with tables
            populateTableWithDifferentViews(client);

            // Query the new table using SQL API
            queryNewTable(client);
        }
    }

    /**
     * Queries the pre-created Person table using SQL
     */
    private static void queryExistingTable(IgniteClient client) {
        System.out.println("\n--- Querying Album table ---");
        try (var rs = client.sql().execute(null, "SELECT * FROM Album LIMIT 10")) {
                rs.forEachRemaining(row -> System.out.println("Album: " + row.stringValue("title")));
        }
    }

    /**
     * Demonstrates different ways to interact with tables
     */
    private static void populateTableWithDifferentViews(IgniteClient client) throws Exception {
        System.out.println("\n--- Populating Artist and Album tables using different views ---");

        // 1. Using RecordView with Tuples
        try (RecordView<Tuple> recordView = client.tables().table("Artist").recordView()) {
            recordView.upsert(null, Tuple.create().set("artistId", 276).set("name", "New Discovery Band"));
            System.out.println("Added record using RecordView with Tuple");
        }

        // 2. Using RecordView with POJOs
        try (RecordView<Album> pojoView = client.tables().table("Album").recordView(Album.class)) {
            pojoView.upsert(null, new Album(348, "First Light", 276, 2023));
            System.out.println("Added record using RecordView with POJO");
        }

        // 3. Using KeyValueView with Tuples
        try (KeyValueView<Tuple, Tuple> keyValueView = client.tables().table("Artist").keyValueView()) {
            keyValueView.put(null, Tuple.create().set("artistId", 277), Tuple.create().set("name", "New Order"));
            System.out.println("Added record using KeyValueView with Tuples");
        }

        // 4. Using KeyValueView with Native Types
        try (KeyValueView<AlbumKey, AlbumValue> keyValuePojoView = client.tables().table("Album").keyValueView(AlbumKey.class, AlbumValue.class)) {
            keyValuePojoView.put(null, new AlbumKey(349, 277), new AlbumValue("Technique", 1989));
            System.out.println("Added record using KeyValueView with Native Types");
        }
    }

    /**
     * Queries the newly created Person2 table using SQL
     */
    private static void queryNewTable(IgniteClient client) {
        System.out.println("\n--- Querying Album table ---");
        try (var rs = client.sql().execute(null, "SELECT * FROM Album WHERE artistId = ?", 277)) {
                rs.forEachRemaining(row -> System.out.println("Album: " + row.stringValue("title")));
        }
    }

    /**
     * POJO class representing an Album
     */
    @Table(zone = @Zone(value = "Chinook", replicas = 2, storageProfiles = "default"),
            colocateBy = {@ColumnRef("artistId")})
    public static class Album {
        // Default constructor required for serialization
        public Album() { }

        public Album(Integer albumId, String title, Integer artistId, Integer releaseYear) {
            this.albumId = albumId;
            this.title = title;
            this.artistId = artistId;
            this.releaseYear = releaseYear;
        }

        @Id
        private Integer albumId;
        @Column(length = 25)
        private String title;
        @Id
        private Integer artistId;
        private Integer releaseYear;
    }

    public static class AlbumKey {
        private Integer albumId;
        private Integer artistId;

        public AlbumKey() {
        }

        public AlbumKey(Integer albumId, Integer artistId) {
            this.albumId = albumId;
            this.artistId = artistId;
        }
    }

    public static class AlbumValue {
        private String title;
        private Integer releaseYear;

        public AlbumValue() {
        }

        public AlbumValue(String title, Integer releaseYear) {
            this.title = title;
            this.releaseYear = releaseYear;
        }
    }
}
