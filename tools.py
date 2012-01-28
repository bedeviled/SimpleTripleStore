#!/usr/bin/env python

def goIn(d):
	print [noDictionaries(v) for v in d.values()]
	if False in [noDictionaries(v) for v in d.values()]:
		for v in d.values():
			if isinstance(v,dict):
				goIn(v)
	else:
		for v in d.values():
			print v	

def findLeaves(d):
	if isinstance(d,dict):
		if False not in [noDictionaries(v) for v in d.values()]:
			for v in d.values():
				print "v =",v
		else:
			for v in d.values():
				findLeaves(v)
	elif isinstance(d,list) or isinstance(d,tuple):
		for v in d:
			findLeaves(v)	

def noDictionaries(it):
	flag = True
	if isinstance(it,dict):
		return False
	if not getattr(it, '__iter__', False):
		return True
	for k in it:
		if isinstance(k,dict):
			flag &= False
		elif isinstance(k,list) or isinstance(k,tuple):
			flag &= noDictionaries(k)
	return flag
