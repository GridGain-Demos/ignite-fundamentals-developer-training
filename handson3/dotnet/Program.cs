using System;
using System.Threading.Tasks;
using Apache.Ignite;
using Apache.Ignite.Sql;
using Apache.Ignite.Table;
using Apache.Ignite.Transactions;

namespace GridGain.Training.Fundamentals
{
    /// <summary>
    /// This example demonstrates connecting to a GridGain 9 cluster
    /// and working with data using different table view patterns.
    /// </summary>
    public class Program
    {
        public static async Task Main(string[] args)
        {
            var addresses = new[]
            {
                "localhost:10800",
                "localhost:10801",
                "localhost:10802"
            };

            using var client = await IgniteClient.StartAsync(new IgniteClientConfiguration
            {
                Endpoints = {
                "localhost:10800",
                "localhost:10801",
                "localhost:10802"
            }
            });

            //Console.WriteLine($"Connected to the cluster: {string.Join(", ", client.Connections)}");

            await QueryExistingTable(client);
            await PopulateTableWithDifferentViews(client);
        }

        /// <summary>
        /// Queries the pre-created Album table using SQL
        /// </summary>
        private static async Task QueryExistingTable(IIgniteClient client)
        {
            Console.WriteLine("\n--- Querying Album table ---");

            await using var resultSet = await client.Sql.ExecuteAsync(
                transaction: null,
                statement: "SELECT * FROM Album LIMIT 10");

            await foreach (var row in resultSet)
            {
                Console.WriteLine($"Album: {row["title"]}");
            }
        }

        /// <summary>
        /// Demonstrates different ways to interact with tables
        /// </summary>
        private static async Task PopulateTableWithDifferentViews(IIgniteClient client)
        {
            Console.WriteLine("\n--- Populating Artist and Album tables using different views ---");

            // 1. Using RecordView with Tuples
            var artistTable = await client.Tables.GetTableAsync("Artist");
            var artistRecordView = artistTable!.RecordBinaryView;

            await artistRecordView.UpsertAsync(
                transaction: null,
                new IgniteTuple{
                    ["artistId"] = 276,
                    ["name"] = "New Discovery Band"
                    }
                );

            Console.WriteLine("Added record using RecordView with Tuple");

            // 2. Using RecordView with POCO
            var albumTable = await client.Tables.GetTableAsync("Album");
            var albumPojoView = albumTable!.GetRecordView<Album>();

            await albumPojoView.UpsertAsync(
                null,
                new Album
                {
                    AlbumId = 348,
                    Title = "First Light",
                    ArtistId = 276,
                    ReleaseYear = 2023
                });

            Console.WriteLine("Added record using RecordView with POCO");

            // 3. Using KeyValueView with Tuples
            var artistKvView = artistTable.KeyValueBinaryView;

            await artistKvView.PutAsync(
                null,
                new IgniteTuple { ["artistId"] = 277 },
                new IgniteTuple { ["name"] = "New Order"}
            );

            Console.WriteLine("Added record using KeyValueView with Tuples");

            // 4. Using KeyValueView with Native Types
            var albumKvPojoView = albumTable.GetKeyValueView<AlbumKey, AlbumValue>();

            await albumKvPojoView.PutAsync(
                null,
                new AlbumKey { AlbumId = 349, ArtistId = 277 },
                new AlbumValue { Title = "Technique", ReleaseYear = 1989 });

            Console.WriteLine("Added record using KeyValueView with Native Types");
        }
    }

    /// <summary>
    /// POCO class representing an Album
    /// </summary>
    public class Album
    {
        public int AlbumId { get; set; }
        public string? Title { get; set; }
        public int ArtistId { get; set; }
        public int? ReleaseYear { get; set; }
    }

    public class AlbumKey
    {
        public int AlbumId { get; set; }
        public int ArtistId { get; set; }
    }

    public class AlbumValue
    {
        public string? Title { get; set; }
        public int? ReleaseYear { get; set; }
    }
}

