#!/usr/bin/env python3

import subprocess
import signal
import os
import time
import sys
import atexit

username = sys.argv[1]
config = [x.strip('\n') for x in open(sys.argv[2], 'r').readlines()]

readQuorum = sys.argv[3]
writeQuorum = sys.argv[4]
coordinatorHost = config[0].split(':')[0]

storageHosts = [x.split(':') for x in config] 
coordinatorServerParam = ' '.join(config)

useSignal = (len(sys.argv) == 6)

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

def runCoordStorageLauncher(host, port):
    pre = sshPrefix + host + ' "cd jaya0089-mapreduce && '
    post = '"'

    clearLogProc = subprocess.Popen(
            pre + 'rm -f *.log' + post,
            stdin = subprocess.DEVNULL,
            shell = True)
    clearLogProc.wait()

    launcherCmd = ' '.join([
        'python3 test/launchCoordStorage.py',
        host,
        port,
        readQuorum,
        writeQuorum,
        coordinatorServerParam])

    processes['coordStorage'] = subprocess.Popen(
            pre + launcherCmd + post,
            shell = True,
            stdin = subprocess.PIPE)

def runStorageLauncher(host, port):
    pre = sshPrefix + host + ' "cd jaya0089-mapreduce && '
    post = '"'

    launcherCmd = ' '.join([
        'python3 test/launchStorage.py',
        host,
        port,
        coordinatorHost])

    processes['storage-' + host + '-' + port] = subprocess.Popen(
            pre + launcherCmd + post,
            shell = True,
            stdin = subprocess.PIPE)

# Launch servers
runCoordStorageLauncher(storageHosts[0][0], storageHosts[0][1])

for hostport in storageHosts[1:]:
    runStorageLauncher(hostport[0], hostport[1])

# Wait
if useSignal:
    signal.pause()
else:
    input('Press Enter to close servers.')

