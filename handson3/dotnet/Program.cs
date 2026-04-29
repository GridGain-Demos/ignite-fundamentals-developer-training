using Apache.Ignite.Core;
using Apache.Ignite.Core.Binary;
using Apache.Ignite.Core.Cache.Query;
using Apache.Ignite.Core.Client;
using Apache.Ignite.Core.Client.Cache;

namespace GridGain.Training.Fundamentals;

/// <summary>
/// Demonstrates connecting to a GridGain 8 cluster and working with data
/// using the .NET thin-client API: SQL queries, SQL DML, and key-value
/// access with BinaryObject.
/// </summary>
public class Program
{
    public static void Main(string[] args)
    {
        var address = Environment.GetEnvironmentVariable("IGNITE_ADDRESS") ?? "localhost:10800";

        var cfg = new IgniteClientConfiguration
        {
            Endpoints = new[] { address }
        };

        using var client = Ignition.StartClient(cfg);
        Console.WriteLine("Connected to the cluster");

        QueryExistingTable(client);
        InsertWithSqlDml(client);
        KeyValueWithBinaryObject(client);
        VerifyResults(client);
    }

    /// <summary>
    /// Block 1 — SQL SELECT: query pre-loaded Chinook data.
    /// </summary>
    private static void QueryExistingTable(IIgniteClient client)
    {
        Console.WriteLine("\n--- Querying Album table using SQL ---");

        var cache = client.GetCache<object, object>("Artist");

        var rows = cache.Query(
            new SqlFieldsQuery("SELECT AlbumId, Title, ArtistId FROM Album LIMIT 10")
        ).GetAll();

        foreach (var row in rows)
        {
            Console.WriteLine($"Album: {row[1]}");
        }
    }

    /// <summary>
    /// Block 2 — SQL DML: insert data using parameterized SQL statements.
    /// </summary>
    private static void InsertWithSqlDml(IIgniteClient client)
    {
        Console.WriteLine("\n--- Inserting data using SQL DML ---");

        var cache = client.GetCache<object, object>("Artist");

        cache.Query(new SqlFieldsQuery(
            "INSERT INTO Artist (ArtistId, Name) VALUES (?, ?)", 276, "New Discovery Band")
        ).GetAll();
        Console.WriteLine("Added artist using SQL INSERT");

        cache.Query(new SqlFieldsQuery(
            "INSERT INTO Album (AlbumId, Title, ArtistId, ReleaseYear) VALUES (?, ?, ?, ?)",
            348, "First Light", 276, 2023)
        ).GetAll();
        Console.WriteLine("Added album using SQL INSERT");
    }

    /// <summary>
    /// Block 3 — Key-Value API: put and get data using BinaryObject.
    /// BinaryObject provides schema-less access without requiring POCO
    /// classes on the server.
    /// </summary>
    private static void KeyValueWithBinaryObject(IIgniteClient client)
    {
        Console.WriteLine("\n--- Using Key-Value API with BinaryObject ---");

        var cache = client.GetCache<int, IBinaryObject>("Artist")
            .WithKeepBinary<int, IBinaryObject>();

        var val = client.GetBinary().GetBuilder("Artist")
            .SetField("NAME", "New Order")
            .Build();
        cache.Put(277, val);
        Console.WriteLine("Added artist 277 using Key-Value put");

        var result = cache.Get(277);
        Console.WriteLine($"Retrieved artist 277: {result.GetField<string>("NAME")}");
    }

    /// <summary>
    /// Block 4 — Verify: read back all new data with a SQL JOIN.
    /// </summary>
    private static void VerifyResults(IIgniteClient client)
    {
        Console.WriteLine("\n--- Verifying with SQL JOIN ---");

        var cache = client.GetCache<object, object>("Artist");

        var results = cache.Query(new SqlFieldsQuery(
            "SELECT a.Title, ar.Name FROM Album a " +
            "JOIN Artist ar ON a.ArtistId = ar.ArtistId " +
            "WHERE ar.ArtistId IN (276, 277)")
        ).GetAll();

        foreach (var row in results)
        {
            Console.WriteLine($"Album: '{row[0]}' by '{row[1]}'");
        }
    }
}
