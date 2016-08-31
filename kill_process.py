import psutil

import psutil, time
import os
import sys

ban = [80001]

def get_pip(p):
    try:
        pid = p.pid
        if pid in ban:
            pid = -1
    except psutil.NoSuchProcess:
        pid = -1
    return pid

def kill_proc(pid):
    if not pid == -1:
        print "kill "+str(pid)+"\n"
        os.system("kill "+str(pid))


def getAllProcessInfo(pname):
    all_processes = list(psutil.process_iter())
    result = []
    for proc in all_processes:
        try:
            if proc.name().lower() == pname.lower():
                result.append(proc)  # return if found one
        except psutil.AccessDenied:
            pass
        except psutil.NoSuchProcess:
            pass
    return result


if '__main__' == __name__:
    baned_pid = sys.argv[1]
    ban.append(int(baned_pid))
    ps = getAllProcessInfo("java")
    for p in ps:
        kill_proc(get_pip(p))

