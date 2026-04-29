# Apache Ignite Fundamentals Developer Training

New to Apache Ignite? This 90-minute hands-on training webinar is the fastest way for Java and .NET developers to get started with GridGain 8 and distributed in-memory computing. In this session, you'll learn how to spin up a cluster, connect a client application, and work with distributed data using SQL and the key-value API.

We'll cover the core building blocks of GridGain — caches, partitioning, replication, and affinity colocation — and show how they translate into code. By the end of the session, you'll understand how GridGain distributes data, why it delivers high performance at scale, and how to start building your own applications with confidence.

## What You'll Learn

* How GridGain 8 is structured and where it fits in modern architectures
* How to start a GridGain cluster using Docker
* How to connect using the thin-client API (Java or .NET)
* How to create tables, insert data, and run queries using SQL and the key-value API
* How GridGain distributes data (partitioning, replication, colocation) and why it matters

## Who Should Attend

Java or .NET developers, architects, and engineers evaluating Apache Ignite or getting started with GridGain for distributed data and high-performance applications.

## Join Us!

Check the [complete schedule](https://www.gridgain.com/products/services/training/true-ignite-fundamentals-training-java) and register for an upcoming session.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Layout](#project-layout)
- [1. Clone the Project](#1-clone-the-project)
- [2. Hands-on #1 — Start the Cluster](#2-hands-on-1--start-the-cluster)
- [3. Hands-on #2 — SQL](#3-hands-on-2--sql)
- [4. Affinity Colocation — Why It Matters](#4-affinity-colocation--why-it-matters)
- [5. Hands-on #3 — Build and Run the Client App](#5-hands-on-3--build-and-run-the-client-app)
- [6. Shutdown](#6-shutdown)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Git
- Docker Desktop
- Your favorite IDE (IntelliJ, Visual Studio, VS Code, or a plain editor)

JDK 17 / Maven and .NET 8 SDK are optional — the `app` and `app-dotnet` sidecar services provide them. Install locally only if you prefer the standalone workflow.

**Linux only:** the GridGain container image runs as UID 10000. If nodes fail to start on Linux, run `chown -R 10000:10000 docker/data/` and retry.

---

## Project Layout

Three GridGain nodes (`node1`, `node2`, `node3`) run on an isolated Docker bridge network. Only `node1` publishes port `10800` to the host — that is the thin-client address your apps connect to. Two optional sidecar services (`app` for Maven / JDK 17, `app-dotnet` for .NET 8 SDK) let you build and run without local toolchains.

```
docker/
  docker-compose.yaml     ← cluster topology + sidecar definitions
  config/                 ← training-node-config.xml + ignite-log4j2.xml,
  │                          bind-mounted read-only into every server node
  data/
  │  node1/log/           ← node1 log files (also via `docker compose logs node1`)
  │  node2/log/
  │  node3/log/
handson1/
  README.md               ← Hands-on #1: cluster setup and verification
handson2/
  README.md               ← Hands-on #2: SQL (Chinook schema, queries, colocation)
  sql/
    schema.sql             ← Chinook DDL with cache templates and affinity keys
    data.sql               ← Chinook sample data (275 artists, 3503 tracks, …)
handson3/
  README.md               ← Hands-on #3: thin-client API (Java and .NET)
  java/                   ← Maven project: Main.java demo
  dotnet/                 ← .NET 8 project: Program.cs demo
```

---

## 1. Clone the Project

```bash
git clone -b gg8_docker https://github.com/GridGain-Demos/ignite-fundamentals-developer-training.git
cd ignite-fundamentals-developer-training
```

---

## 2. Hands-on #1 — Start the Cluster

Full instructions: [`handson1/README.md`](handson1/README.md)

Quick start from the repository root:

```bash
docker compose -f docker/docker-compose.yaml up -d
```

Verify all three nodes joined:

```bash
docker compose -f docker/docker-compose.yaml logs node1 | grep "Topology snapshot" | tail -1
```

Expect `servers=3` in the output.

---

## 3. Hands-on #2 — SQL

Full instructions: [`handson2/README.md`](handson2/README.md)

Quick start — copy the SQL files into the container:

```bash
docker cp handson2/sql/. "$(docker compose -f docker/docker-compose.yaml ps -q node1)":/opt/gridgain/work/sql/
```

Load the schema:

```bash
docker compose -f docker/docker-compose.yaml exec node1 /opt/gridgain/bin/sqlline.sh -u jdbc:ignite:thin://node1:10800 -f /opt/gridgain/work/sql/schema.sql
```

Load the data:

```bash
docker compose -f docker/docker-compose.yaml exec node1 /opt/gridgain/bin/sqlline.sh -u jdbc:ignite:thin://node1:10800 -f /opt/gridgain/work/sql/data.sql
```

Verify:

```bash
docker compose -f docker/docker-compose.yaml exec node1 /opt/gridgain/bin/sqlline.sh -u jdbc:ignite:thin://node1:10800 -e "SELECT 'Artist', COUNT(*) FROM Artist UNION ALL SELECT 'Track', COUNT(*) FROM Track;"
```

Expect **275** artists and **3503** tracks.

---

## 4. Affinity Colocation — Why It Matters

GridGain distributes data across nodes by partitioning. By default, rows from different tables land on whichever node owns their partition — so an Artist and its Albums might be on different nodes, forcing a network round-trip to join them.

**Affinity colocation** solves this: when two tables share a colocation key, rows with the same key value are guaranteed to land on the same node. The Chinook schema uses this to keep related data together:

* Albums are colocated by `ArtistId` — all albums by Artist 22 live on the same node as Artist 22
* Tracks are colocated by `AlbumId` — all tracks on Album 133 live with that album
* Invoices are colocated by `CustomerId` — a customer's invoices live with the customer
* InvoiceLines are colocated by `InvoiceId` — line items live with their invoice

The result is that joins between colocated tables execute locally on each node — no data shuffling across the network. Full walkthrough in [`handson2/README.md`](handson2/README.md#colocation-strategy-summary).

---

## 5. Hands-on #3 — Build and Run the Client App

Full instructions: [`handson3/README.md`](handson3/README.md)

Two paths — pick whichever suits your environment. The sidecar path requires no local SDK; the standalone path gives you IDE debugging and faster iteration.

### Java — standalone (host Maven)

```bash
mvn -f handson3/java/pom.xml compile exec:exec
```

### Java — Docker sidecar

```bash
docker compose -f docker/docker-compose.yaml run --rm app mvn -f handson3/java/pom.xml compile exec:exec
```

The sidecar connects to the cluster over the Docker network (`IGNITE_ADDRESS=node1:10800` is baked into the compose service).

### .NET — standalone (host .NET SDK)

```bash
dotnet run --project handson3/dotnet/dotnet.csproj
```

### .NET — Docker sidecar

```bash
docker compose -f docker/docker-compose.yaml run --rm app-dotnet dotnet run --project handson3/dotnet/dotnet.csproj
```

---

## 6. Shutdown

```bash
docker compose -f docker/docker-compose.yaml down
```

The `docker/data/` directory is kept on the host (holds logs and marshaller metadata).

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `docker compose up -d` hangs on the second attempt | Port 10800 still held by a cluster running in another directory | `docker compose -f docker/docker-compose.yaml down` in that directory first |
| Nodes start but produce no logs; `docker/data/` empty (Linux only) | Container runs as UID 10000; host `docker/data/` owned by your user | `chown -R 10000:10000 docker/data/` |
| Java: `Connection refused` | Cluster not running or port not published | `docker compose -f docker/docker-compose.yaml ps` — check node1 is up with port 10800 |
| Java: cache not found (`CacheNotFoundException`) | Schema not loaded | Run the schema.sql step from hands-on #2 |
| Sidecar: `Connection refused` to thin client | `IGNITE_ADDRESS` env var not set or using `localhost` | Sidecar connects via `node1:10800` — check `environment:` in `docker/docker-compose.yaml` |
| .NET: NuGet restore fails | Network issue or package version mismatch | Verify `Apache.Ignite` package resolves; see handson3 README |

