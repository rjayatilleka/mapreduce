#!/bin/bash

rm -rf "$HOME/logs"
mkdir "$HOME/logs"

chunkSize=1000000
cpm=8
inputFile=40

for redundancy in {1..4}; do
  for failProb in {0..90..10}; do
    echo "Test case: $chunkSize $cpm $failProb $redundancy ----------"

    # launch orchestrator and save pid
    bin/orchestrator jaya0089 test/configs/eval.txt $chunkSize $cpm $redundancy $failProb useSignal > /dev/null 2>&1 &
    orch_pid=$!
    echo "orch pid $orch_pid"
    sleep 4

    # launch client and save pid
    echo "mergesort $inputFile" | bin/client > "$HOME/jaya0089-mapreduce/log/client.log" 2>&1 &
    client_pid=$!
    echo "client_pid $client_pid"

    # wait on client
    wait $client_pid
    echo "waited on clients"

    # interrupt and wait on orchestrator
    kill -SIGUSR1 $orch_pid
    echo "signaled orch"

    wait $orch_pid
    echo "waited on orch"

    make takelogs
    cp -r logs "$HOME/logs/logs-${redundancy}-${failProb}"
    echo "copied logs"

    echo $'----------\n\n'
  done
done
