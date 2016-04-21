#!/usr/bin/env python3

import subprocess
import signal
import os
import time
import sys
import atexit

username = sys.argv[1]
config = [x.strip('\n') for x in open(sys.argv[2], 'r').readlines()]

chunkSize = sys.argv[3]
cpm = sys.argv[4]
redundancy = sys.argv[5]
failProb = sys.argv[6]
masterHost = config[0]

workerHosts = [x.split(':') for x in config[1:]] 
workerServerParam = ' '.join(config[1:])

useSignal = (len(sys.argv) == 8)

sshPrefix = 'ssh -o BatchMode=yes -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -o LogLevel=quiet ' + username + '@'

processes = {}

# Terminate servers
def terminate():
    for name in processes:
        processes[name].communicate(b'\n')

    for name in processes:
        processes[name].wait()
        print('Waited on ' + name)

atexit.register(terminate)

def runMasterLauncher(host, port):
    pre = sshPrefix + host + ' "cd jaya0089-mapreduce && '
    post = '"'

    clearLogProc = subprocess.Popen(
            pre + 'rm -f *.log' + post,
            stdin = subprocess.DEVNULL,
            shell = True)
    clearLogProc.wait()

    launcherCmd = ' '.join([
        'python3 test/launchMaster.py',
        host,
        chunkSize,
        cpm,
        redundancy,
        workerServerParam])

    processes['launch-master'] = subprocess.Popen(
            pre + launcherCmd + post,
            shell = True,
            stdin = subprocess.PIPE)

def runWorkerLauncher(host, port):
    pre = sshPrefix + host + ' "cd jaya0089-mapreduce && '
    post = '"'

    launcherCmd = ' '.join([
        'python3 test/launchWorker.py',
        host,
        port,
        failProb])

    processes['launch-worker-' + host + '-' + port] = subprocess.Popen(
            pre + launcherCmd + post,
            shell = True,
            stdin = subprocess.PIPE)

# Launch servers
runMasterLauncher(masterHost, 50000)

for hostport in workerHosts:
    runWorkerLauncher(hostport[0], hostport[1])

# Wait
if useSignal:
    signal.pause()
else:
    input('Press Enter to close servers.')

