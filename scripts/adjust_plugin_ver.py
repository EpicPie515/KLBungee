import sys
import fileinput
for line in fileinput.input("src/main/resources/plugin.yml", inplace=True):
    if line.strip().startswith('version:'):
        line = 'version: ' + sys.argv[1] + '\n'
    sys.stdout.write(line)