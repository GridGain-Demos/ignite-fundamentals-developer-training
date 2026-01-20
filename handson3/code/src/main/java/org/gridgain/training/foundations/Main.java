package org.gridgain.training.foundations;

import org.apache.ignite.catalog.ColumnType;
import org.apache.ignite.catalog.definitions.ColumnDefinition;
import org.apache.ignite.catalog.definitions.TableDefinition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.table.KeyValueView;
import org.apache.ignite.table.RecordView;
import org.apache.ignite.table.Table;
import org.apache.ignite.table.Tuple;

/**
 * This example demonstrates connecting to an GridGain 9 cluster
 * and working with data using different table view patterns.
 */
public class Main {
    public static void main(String[] args) {
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

            // Create a new table using Java API
            Table table = createTable(client);

            // Demonstrate different ways to interact with tables
            populateTableWithDifferentViews(table);

            // Query the new table using SQL API
            queryNewTable(client);
        }
    }

    /**
     * Queries the pre-created Person table using SQL
     */
    private static void queryExistingTable(IgniteClient client) {
        System.out.println("\n--- Querying Person table ---");
        client.sql().execute(null, "SELECT * FROM Person")
                .forEachRemaining(row -> System.out.println("Person: " + row.stringValue("name")));
    }

    /**
     * Creates a new table using the Java API
     */
    private static Table createTable(IgniteClient client) {
        System.out.println("\n--- Creating Person2 table ---");
        return client.catalog().createTable(
                TableDefinition.builder("Person2")
                        .ifNotExists()
                        .columns(
                                ColumnDefinition.column("ID", ColumnType.INT32),
                                ColumnDefinition.column("NAME", ColumnType.VARCHAR))
                        .primaryKey("ID")
                        .build());
    }

    /**
     * Demonstrates different ways to interact with tables
     */
    private static void populateTableWithDifferentViews(Table table) {
        System.out.println("\n--- Populating Person2 table using different views ---");

        // 1. Using RecordView with Tuples
        RecordView<Tuple> recordView = table.recordView();
        recordView.upsert(null, Tuple.create().set("id", 2).set("name", "Jane"));
        System.out.println("Added record using RecordView with Tuple");

        // 2. Using RecordView with POJOs
        RecordView<Person> pojoView = table.recordView(Person.class);
        pojoView.upsert(null, new Person(3, "Jack"));
        System.out.println("Added record using RecordView with POJO");

        // 3. Using KeyValueView with Tuples
        KeyValueView<Tuple, Tuple> keyValueView = table.keyValueView();
        keyValueView.put(null, Tuple.create().set("id", 4), Tuple.create().set("name", "Jill"));
        System.out.println("Added record using KeyValueView with Tuples");

        // 4. Using KeyValueView with Native Types
        KeyValueView<Integer, String> keyValuePojoView = table.keyValueView(Integer.class, String.class);
        keyValuePojoView.put(null, 5, "Joe");
        System.out.println("Added record using KeyValueView with Native Types");
    }

    /**
     * Queries the newly created Person2 table using SQL
     */
    private static void queryNewTable(IgniteClient client) {
        System.out.println("\n--- Querying Person2 table ---");
        client.sql().execute(null, "SELECT * FROM Person2")
                .forEachRemaining(row -> System.out.println("Person2: " + row.stringValue("name")));
    }

    /**
     * POJO class representing a Person
     */
    public static class Person {
        // Default constructor required for serialization
        public Person() { }

        public Person(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        Integer id;
        String name;
    }
}
