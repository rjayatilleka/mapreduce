## Parameters

Master: MB to split at, default redundancy, worker addresses
Worker: Fail probability

## DONE

- Design redundant task
- Design job sort
- Design job merge
- Write interfaces
- Make directories
- Implement split
- Implement sort
- Implement timeout on thrift client
- Make sure sort and merge work concurrently
- Implement merge
- Add sort and merge fails

## TODOs

- Implement worker pool
- Implement monitoring
- Implement job sort
  - Make sure job sort works with fails
- Implement job merge
- Add logging
- Make remote-install
- Do evaluation

## Job sort

`jobSort(InputId input)`

- split - list[DataId]
- from - O[DataId]
- map(redundantSort())
- merge()
- ? sub(onerror = fuck, oncomplete = hooray)

## Job merge

`jobMerge(O[DataId] initialIntermediates)`

- Start with O[DataId]
- Take ceil(log base-(chunks per merge) of (total chunks))
- Loop and do the following frp
  - buffer(cpm)
  - map(redundantMerge())
  - O[DataId] nextSetOfIntermediates
- O[DataId] finalResult (size = 1)

## Single request

`request(DataId input, WorkerId worker)`

- lookup worker (host, port)
- make client (workerClient)
- launch task (taskId)
- map(interval(1000)) (taskId, O[int])
  - map(asyncQuery(taskId)) O[Future[QueryResult]]
  - dematerialize [fail, not found, timeout -> fail, complete -> outputDataId]
  - O[dataId]

## Concurrent task

- Make retryable tasks
- Merge them
- First

## Non-concurrent task including retries

- Make runnable
  - Log start, time
  - Get worker from workerpool
  - Run request
  - Timeouts, Fail result -> Exception
  - Log finish
- Submit to executor
- Retry

## Worker Pool

- Add worker by id
- Remove worker by id
- Get workerId

## Monitor All Workers

- For each worker
  - make status stream
  - distinct
  - sub(add/remove from pool)

## Monitor One Worker

- Host, Port
- Make client
- interval(1000)
- map(client.asyncHeartbeat())
- flatMap(O.fromFuture)

