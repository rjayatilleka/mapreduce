namespace java mapreduce.thrift

/*
 * Master interface, as well as supporting structs/exceptions.
 */

struct Address {
  1:required string hostname;
  2:required i32 port;
}

struct MasterInfo {
  1:required i32 chunkSize;
  2:required i32 redundancy;
  3:required list<Address> servers;
}

service MasterService {
  MasterInfo info(),
  void mergesort(1:string inputFilename)
}

struct WorkerInfo {
  1:required string workerId;
}

exception TaskFailException {
}

service WorkerService {
  void heartbeat(),
  WorkerInfo info(),
  string runSort(1:string dataId) throws (1:TaskFailException e),
  string runMerge(1:list<string> dataIds) throws (1:TaskFailException e)
}

