# Design Document

## Dependencies

- libthrift 0.9.3 + transitives for rpc
- slf4j 1.7.12 for logging
- metrics-core 3.1.0 for logging


## Java 8

This uses Java 8 for reducing boilerplate. Since Java 8 isn't default on lab
machines, in my Makefile I have it set up to find the Java 8 compiler and
runtime. It queries OpenDNS for the machine's external IP, converts it to a
hostname, and checks if it ends in 'cselabs.umn.edu.'.


## Thrift

I use Thrift to generate three sets of files: the addresses, the storage
service, and the coordinator service. The addresses are shared between both services.
All interprocess and some intraprocess communication is done with Thrift
generated structs and exceptions.


## Metrics

I use metrics-core from Dropwizard to measure how long a read or write
request takes. It generates stats on the timers and writes them to a csv file.


## Common

### ThriftClient

Thrift requires a fair amount of boilerplate on the client side to make a
transport, a protocol, a client, and then close them. It also throws the
checked exception TException on everything. So ThriftClient and ThriftFunction
handle the boilerplate for every use case (including retries). I can pass a
lambda of just the work I need done.

## Storage

Storage code is in `filesys.storage`. It runs a thrift service called
StorageService. It has 6 classes:

- Storage is the actual application. It launches a StorageServer and
  optionally a CoordinatorServer.
- StorageParameters handles argument parsing.
- StorageServer binds the given socket and launches a thrift service.
- StorageHandler implements the thrift service.
  - It keeps a read-write lock internally for its own data (since sync
    doesn't obtain the global write lock).
  - When it gets a read/write request, it contacts the coordinator to
    obtain the global lock and get a quorum. Then it does the read/write
    and contacts the coordinator again to unlock.
  - It also publishes a sync interface for the sync daemon.
- VersionQuery takes care of contacting an entire quorum and finding which
  has the highest version.
- SyncDaemon runs in its own thread and will every 3 seconds pull data from
  the other storages. Then it pushes it to its owner storage through the sync
  interface.


## Coordinator

Coordinator code is in `filesys.coordinator`. The coordinator is spawned by a
Storage at localhost:50000.

- CoordinatorServer runs the thrift service.
- CoordinatorParameters handles argument parsing.
- CoordinatorHandler implements the thrift service.
  - It keeps a per-filename read-write lock using an in-order fairness policy.
  - It creates quorums and locks and unlocks the read-write lock.
- LockTaskManager handles a lock for a single filename. It launches a thread
  to block on the lock, and uses semaphores to communicate with the Thrift
  server's request threads.


## Client

- Client is the application. It reads input line by line and passes it to the
  Commander.
- Commander is the class that runs various commands, like checking on storages,
  getting info, and doing reads and writes.


## Orchestrator

If I may be so boastful, Orchestrator is a *brilliant* piece of engineering.
It doesn't use Thrift. What it does is parse a config of servers desired, and
then launch those servers as desired. It's a mix between a CM system such as
Ansible and a server manager like Terraform, specialized to this assignment.

It ensures that every server is shut down, every process waited on, and every
port is unbound. And to top it all off, it grabs the logs from all servers and
downloads them to `logs/`.
