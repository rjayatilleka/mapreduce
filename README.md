# CSCI 5105 Programming Assignment 2
_Ramith Jayatilleka_

Design document is in `DESIGN.md`. Evaluation is in `EVALUATION.md`. Those
documents as well as this readme are available in pdf form under `pdfs/`.
The evaluation includes images, so you will need to view the pdf to see them.

## Building and Installing

- `make`
  - Builds the project
- `make clean`
  - Deletes non-source folders
- `make install`
  - Builds and installs to `$HOME/jaya0089-mapreduce`
- `make uninstall`
  - Deletes `$HOME/jaya0089-mapreduce`

## Running

### Orchestrator

Orchestrator is the test harness I wrote (in Python3). It will read a config,
SSH into each host, and launch the storages. After exiting, it cleans
up all remote processes and ports. It also copies logs to the local `logs/`
and metrics to the local `logs/metrics/`.

#### Requirements

- Each host has Filesys installed at `$HOME/jaya0089-mapreduce`.
  - Running `make install` once on a Cselabs machine solves this.
- Passwordless SSH is setup for each host.

#### Launching

Run `bin/orchestrator <username> <config_path> <read_quorum> <write_quorum>`.
Ex: `bin/orchestrator jaya0089 test/configs/localhost.txt 4 4`.

`<username>` should be the user to ssh to the hosts as.
`<config_path>` is the path to the config. I wrote a few configs to
`test/configs`.
`<read_quorum>` and `<write_quorum>` are self-explanatory.

Press Enter to shutdown the servers. It will clean up all remote processes.

#### Config

```
<Storage Host:Port> // First one also launches coordinator
<Storage Host:Port>
<Storage Host:Port>
...
```

```
csel-x31-01.cselabs.umn.edu:50001
csel-x31-01.cselabs.umn.edu:50002
csel-x31-02.cselabs.umn.edu:50001
```

The example above will:

- Launch a storage at csel-x31-01:50001, which spawns a coordinator at csel-x31-01:50000.
- Launch a storage at csel-x31-01:50002.
- Launch a storage at csel-x31-02:50001.

### Client

The client needs to run on the same host as the coordinator. Run `bin/client`.
The interface runs commands line by line:

```
read example.txt

> ----- BEGIN read example.txt -----
> ~~~
> ~~~
> ----- END -----
```

#### Commands

- `coordinatorInfo`
  - Displays info about the coordinator and the storage list.
- `storageInfo <host> <port>`
  - Displays info about the storage.
- `allStorageInfo`
  - Displays info about all storages.
- `write <filename> <contents>`
  - Picks a random storage and writes to it.
- `read <filename>`
  - Picks a random storage and reads from it.

### Launching Storages Directly

- `bin/storage <port> <coordinatorHost>`
  - Launches a storage at `localhost:<port>`, which will find the coordinator
    at `<coordinatorHost>:50000`.
- `bin/storage <port> - <read_quorum> <write_quorum> [<server host:port>]...`
  - Launches a storage at `localhost:<port>`, which will launch a coordinator
    at `localhost:50000`.

## Testing

First, launch the servers. Then run the client with the three pre-written test
cases in `test/testcases/`.

- `bin/client < test/testcases/fileNotFound.txt`
  - This just tries a read on a non-existent file.
  - Tries to read `example.txt`, and will fail.
  - Will display `File not found`.
- `bin/client < test/testcases/fileFound.txt`
  - This does a write to a file, and then reads it back.
  - Writes `Example content` to `example.txt`.
  - Reads `example.txt`, and displays the content.
- `bin/client < test/testcases/putGet.txt`
  - This doesn't do a read and write on the file system. It does a put 
    directly on one storage host at `localhost:50001`, and then tries to
    retrieve the version and content.
  - Does a getVersion on `example.txt`, shows FileNotFoundException.
  - Does a getContents on `example.txt`, shows FileNotFoundException.
  - Does a put on `example.txt` with version 0, and contents "Hello".
  - Does a getVersion on `example.txt`, shows "0".
  - Does a getContents on `example.txt`, shows "Hello".
