#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#       osm.py
#       
#       Copyright 2011 Chris Hill <osm@raggedred.net>
#       
#       This program is free software; you can redistribute it and/or modify
#       it under the terms of the GNU General Public License as published by
#       the Free Software Foundation; either version 3 of the License, or
#       (at your option) any later version.
#       
#       This program is distributed in the hope that it will be useful,
#       but WITHOUT ANY WARRANTY; without even the implied warranty of
#       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#       GNU General Public License for more details.
#       
#       You should have received a copy of the GNU General Public License
#       along with this program. If not, see <http://www.gnu.org/licenses/>.

import time

def xesc(s):
	s1=s.replace("&","&amp;")
	s2=s1.replace("<","&lt;")
	s3=s2.replace(">","&gt;")
	s4=s3.replace('"','&quot;')
	return s4

class OSMNode:
	"""Encapsulate an OSM node"""
	def __init__(self,id=0):
		self.NodeID=id
		self.Lon=0.0
		self.Lat=0.0
		self.version=0
		self.time=0
		self.uid=0
		self.user=""
		self.changeset=0
		self.Tags={}

	def AddTag(self,Key,Val):
		self.Tags[Key]=Val

	def printout(self):
		print "Node %i, %f,%f" % (self.NodeID,self.Lat,self.Long)
		if len(self.Tags)==0:
			print "No tags"
		else:
			for t in self.Tags.keys():
				print "%s=%s" % (t,self.Tags[t])
	
	def xout(self,outfile):
		stamp=time.strftime("%Y-%m-%dT%H:%M:%SZ",time.gmtime(self.time))
		#esc={'"':'&quot;'}
		if len(self.Tags)>0:
#			outfile.write('  <node id="%d" version="%d" timestamp="%s" uid="%d" user="%s" changeset="%d" lat="%.7f" lon="%.7f">\n'%(self.NodeID,self.version,stamp,self.uid,xesc(self.user),self.changeset,self.Lat,self.Lon))
			outfile.write('node %d %.7f %.7f'%(self.NodeID,self.Lat,self.Lon))
			for t in self.Tags.keys():
				outfile.write('  "%s" "%s"'%(t,xesc(self.Tags[t])))
			outfile.write('\n')
#			outfile.write('  </node>\n')
		else:
			outfile.write('node %d %.7f %.7f\n'%(self.NodeID,self.Lat,self.Lon))
#			outfile.write('  <node id="%d" version="%d" timestamp="%s" uid="%d" user="%s" changeset="%d" lat="%.7f" lon="%.7f"/>\n'%(self.NodeID,self.version,stamp,self.uid,xesc(self.user),self.changeset,self.Lat,self.Lon))
	
	def xesc(s):
		s1=s.replace("&","&amp;")
		s2=s1.replace("<","&lt;")
		s3=s2.replace(">","&gt;")
		s4=s3.replace('"','&quot;')
		return s4

class OSMWay:
	"""Encapsulate an OSM way"""
	def __init__(self,id=0):
		self.WayID=id
		self.time=0
		self.uid=0
		self.user=""
		self.changeset=0
		self.Tags={}
		self.Nds=[]
	
	def AddTag(self,Key,Val):
		self.Tags[Key]=Val
	
	def AddNd(self,NdID):
		self.Nds.append(NdID)
	
	def xout(self,outfile):
		stamp=time.strftime("%Y-%m-%dT%H:%M:%SZ",time.gmtime(self.time))
		#esc={'"':'&quot;'}
		#outfile.write('  <way id="%d" version="%d" timestamp="%s" uid="%d" user="%s" changeset="%d">\n'%(self.WayID,self.version,stamp,self.uid,xesc(self.user),self.changeset))
		outfile.write(' way %d  '%(self.WayID))
		for n in self.Nds:
			outfile.write(' %d'%(n))
		for t in self.Tags.keys():
			outfile.write('    "%s" "%s"'%(t,xesc(self.Tags[t])))
		outfile.write('\n')

class OSMMember:
	"""Encapsulate an OSM member, part of a relation"""
	def __init__(self,type,ref,role):
		self.type=type
		self.ref=ref
		self.role=role
	
	def xout(self,outfile):
		#esc={'"':'&quot;','&':'&amp;'}
		#outfile.write('    <member type="%s" ref="%d" role="%s"/>\n'%(self.type,self.ref,xesc(self.role)))
		outfile.write('  mem "%s" "%d" "%s"'%(self.type,self.ref,xesc(self.role)))

class OSMRelation:
	def __init__(self,id=0):
		self.RelID=id
		self.time=0
		self.uid=0
		self.user=""
		self.changeset=0
		self.Tags={}
		self.Members=[]
	
	def AddTag(self,Key,Val):
		self.Tags[Key]=Val
	
	def AddMember(self,Member):
		self.Members.append(Member)
	
	def xout(self,outfile):
		stamp=time.strftime("%Y-%m-%dT%H:%M:%SZ",time.gmtime(self.time))
		#esc={'"':'&quot;','&':'&amp;'}
		#outfile.write('  <relation id="%d" version="%d" timestamp="%s" uid="%d" user="%s" changeset="%d">\n'%(self.RelID,self.version,stamp,self.uid,xesc(self.user),self.changeset))
		outfile.write('rel %d  '%(self.RelID))
		for t in self.Tags.keys():
			outfile.write('    "%s" "%s"'%(t,xesc(self.Tags[t])))
		for m in self.Members:
			m.xout(outfile)
		outfile.write('\n')
	
