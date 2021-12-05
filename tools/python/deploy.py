import subprocess
import sys

HOST = "manoj.jtunnel.net"
# Ports are handled in ~/.ssh/config since we use OpenSSH
COMMAND = "uname -a"

ssh = subprocess.Popen(["ssh", "%s" % HOST, COMMAND],
                       shell=False,
                       stdout=subprocess.PIPE,
                       stderr=subprocess.PIPE)
result = ssh.stdout.readlines()
if result == []:
  error = ssh.stderr.readlines()
  print >> sys.stderr, "ERROR: %s" % error
else:
  print(result)
