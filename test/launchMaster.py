import subprocess
import signal
import os
import time
import sys
import atexit

params = {
    'localHostname': sys.argv[1],
    'chunkSize': sys.argv[2],
    'cpm': sys.argv[3],
    'redundancy': sys.argv[4],
    'servers': ' '.join(sys.argv[5:])
}

processes = {}

# Terminate servers
def terminate():
    for name in processes:
        try:
            os.killpg(processes[name].pid, signal.SIGINT)
            print('Sent SIGINT to ' + name)
        except PermissionError:
            print('Permission error SIGINT to ' + name + ', pid = ' + str(processes[name].pid))

    for name in processes:
        processes[name].wait()
        print('Waited on ' + name)

atexit.register(terminate)

def runServer(name, args):
    outputFile = open('log/' + name + '.log', 'w')

    processes[name] = subprocess.Popen(
            ' '.join(args),
            shell = True,
            preexec_fn = os.setsid,
            stdout = outputFile,
            stderr = subprocess.STDOUT)

    print('Launched ' + name)

def master(params):
    name = 'master-' + params['localHostname'] + '-' + params['port']
    args = ['bin/master', params['chunkSize'], params['cpm'], params['redundancy'], params['servers']]
    runServer(name, args)

# Launch servers
master(params)

# Wait
try:
    input()
except EOFError:
    pass
