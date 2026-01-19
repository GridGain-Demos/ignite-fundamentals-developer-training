# Hands-on #1: Starting a cluster

This guide walks you through the process of setting up and running an GridGain cluster using Docker containers. Follow these steps to get a three-node cluster up and running quickly.

## Prerequisites

- Docker and Docker Compose installed on your system
- Basic familiarity with command-line operations
- Java 11 or higher installed (for connecting to the cluster)

## Step 1: Understand the Docker Compose Configuration

1. View the file named [`docker-compose.yml`](docker-compose.yaml) in the current directory:

## Step 2: Start the GridGain Cluster

1. Open a terminal in the directory containing your `docker-compose.yml` file
2. Run the following command to start the cluster:

```bash
docker compose up -d
```

3. Verify that all containers are running:

```bash
docker compose ps
```

You should see all three nodes with "running" status.

## Step 3: Initialize the Cluster

1. Start the Ignite CLI in Docker:

```bash
docker run --rm -it --network=gridgain9_default gridgain/gridgain9:9.1.8 cli
```

2. Inside the CLI, connect to one of the nodes:

```
connect http://node1:10300
```

3. Initialize the cluster with a name and metastorage group:

```
cluster init --name=gridgain9 --metastorage-group=node1,node2,node3
```

4. Exit the CLI by typing `exit` or pressing Ctrl+D

## Step 4: Verify Your Cluster

To verify your cluster is running correctly, you can use a simple Java client to connect to it. Create a simple test class that establishes a connection to the cluster.

## Understanding Port Configuration

The Docker Compose file exposes two types of ports for each node:

- **10300-10302**: REST API ports for administrative operations
- **10800-10802**: Client connection ports for your applications

## Stopping the Cluster

When you're done working with the cluster, you can stop it using:

```bash
docker compose down
```

This will stop and remove all the containers. Your data will be lost unless you've configured persistent storage.
