#!/bin/bash

rm -rf "$HOME/logs"
mkdir "$HOME/logs"

for redundancy in {4..7}; do
  rq=`expr 8 - $wq`

  for percent in {0..90..10}; do
    filename="test/eval/${percent}percentWrites.txt"

    echo "Test case: $wq $rq $filename ----------"

    # launch orchestrator and save pid
    bin/orchestrator jaya0089 test/configs/eval.txt $rq $wq useSignal > /dev/null 2>&1 &
    orch_pid=$!
    echo "orch pid $orch_pid"
    sleep 4

    # launch 4 clients and save all pids
    bin/client < $filename > "$HOME/jaya0089-mapreduce/log/ca.log" 2>&1 &
    ca_pid=$!
    echo "ca pid $ca_pid"
    bin/client < $filename > "$HOME/jaya0089-mapreduce/log/cb.log" 2>&1 &
    cb_pid=$!
    echo "cb pid $cb_pid"
    bin/client < $filename > "$HOME/jaya0089-mapreduce/log/cc.log" 2>&1 &
    cc_pid=$!
    echo "cc pid $cc_pid"
    bin/client < $filename > "$HOME/jaya0089-mapreduce/log/cd.log" 2>&1 &
    cd_pid=$!
    echo "cd pid $cd_pid"

    # wait on all 4 clients
    wait $ca_pid $cb_pid $cc_pid $cd_pid
    echo "waited on clients"

    # interrupt and wait on orchestrator
    kill -SIGUSR1 $orch_pid
    echo "signaled orch"

    wait $orch_pid
    echo "waited on orch"

    make takelogs
    cp -r logs "$HOME/logs/logs-${wq}-${percent}"
    echo "copied logs"

    echo $'----------\n\n'
  done

done
