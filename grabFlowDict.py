#!/usr/bin/env python
import os
import sys
import re
import pickle

files = os.listdir(os.getcwd())
pattern = re.compile(r'^[a-z]+_[0-9]+') #matches and pulls out session_0908 etc.
d = {}

def parseInfoFile(filename):
	"""parses a .info file"""
	out = {}
	f = open(filename)
	out["summary"] = f.readline().strip()
	for line in f:
		line = line.strip()
		if len(line) > 0:
			pieces = line.split(":",1)
			out[pieces[0].strip()] = pieces[1].strip()
	return out

for f in files:
	try:
		sessionName = re.match(r'^[a-z]+_[0-9]+',f).group(0)
		if not d.has_key(sessionName): d[sessionName] = {}
		if f.split(".")[-1] == "info":
			d[sessionName]["info"] = parseInfoFile(f)
		else:
			g = open(f)
			d[sessionName][f.split(".",1)[-1]] = g.read()
			g.close()
	except:
		continue

pickle.dump(d,open("files.pkl","w"))
			
