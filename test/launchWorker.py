import subprocess
import signal
import os
import time
import sys
import atexit

params = {
    'localHostname': sys.argv[1],
    'port': sys.argv[2],
    'failProb': sys.argv[3]
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

def worker(params):
    name = 'worker-' + params['localHostname'] + '-' + params['port']
    args = ['bin/worker', params['port'], params['failProb']]
    runServer(name, args)

# Launch servers
worker(params)

# Wait
try:
    input()
except EOFError:
    pass
