namespace java mapreduce.thrift

/*
 * Master interface, as well as supporting structs/exceptions.
 */

struct Address {
  1:required string hostname;
  2:required i32 port;
}

struct MasterInfo {
  1:required list<Address> servers;
}

service MasterService {
  MasterInfo info(),
  void mergesort(1:string inputFilename),
  void finishSort(1:string taskId, 2:string dataId),
  void finishMerge(1:string taskId, 2:string dataId)
}

exception BusyException {
  1:required string ongoingTaskId;
}

struct WorkerInfo {
  1:required string workerId;
  2:required string runningTaskId;
}

service WorkerService {
  WorkerInfo info(),
  string runSort(1:string dataId) throws (1:BusyException e),
  string runMerge(1:list<string> dataIds) throws (1:BusyException e),
  void cancel(1:string taskId)
}

