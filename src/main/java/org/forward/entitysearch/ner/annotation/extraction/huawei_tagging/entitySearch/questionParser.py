from sys import argv

file = open(argv[1], "r")
for line in file:
	print(line)