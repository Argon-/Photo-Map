#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#       parsepbf.py
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

import fileformat_pb2
import osmformat_pb2
import osm
import sys
import os.path
from struct import unpack
import zlib
import time
from optparse import OptionParser

class PBFParser:
	"""Manage the process of parsing an osm.pbf file"""
	
	def __init__(self,filehandle,xmlfile):
		"""PBFParser constuctor"""
		self.Nodes = []
		self.Ways=[]
		self.Relations=[]
		self.fpbf = filehandle
		self.xmlfile=xmlfile
		if xmlfile==0:
			self.xmlout=False
		else:
			self.xmlout=True
		self.blobhead=fileformat_pb2.BlobHeader()
		self.blob=fileformat_pb2.Blob()
		self.hblock=osmformat_pb2.HeaderBlock()
		self.primblock=osmformat_pb2.PrimitiveBlock()
		self.membertype = {0:'node',1:'way',2:'relation'}
	
	def init(self,pn):
		"""Check the file headers"""
		self.progname=pn
		# read the blob header
		if self.readPBFBlobHeader()==False:
			return False
		
		#read the blob
		if self.readBlob()==False:
			return False
		
		#check the contents of the first blob are supported
		self.hblock.ParseFromString(self.BlobData)
		for rf in self.hblock.required_features:
			if rf in ("OsmSchema-V0.6","DenseNodes"):
				pass
			else:
				print "not a required feature %s"%(rf)
				return False
		return True
	
	def parse(self):
		"""work through the data extracting OSM objects"""
		while self.readNextBlock():
			for pg in self.primblock.primitivegroup:
				#self.loadgroup(pg)
				if len(pg.dense.id)>0:
					self.processDense(pg.dense)
				if len(pg.nodes)>0:
					self.processNodes(pg.nodes)
				if len(pg.ways)>0:
					self.processWays(pg.ways)
				if len(pg.relations)>0:
					self.processRels(pg.relations)
		
	
	def readPBFBlobHeader(self):
		"""Read a blob header, store the data for later"""
		size=self.readint()
		if size <= 0:
			return False
		
		if self.blobhead.ParseFromString(self.fpbf.read(size))==False:
			return False
		return True
	
	def readBlob(self):
		"""Get the blob data, store the data for later"""
		size=self.blobhead.datasize
		if self.blob.ParseFromString(self.fpbf.read(size))==False:
			return False
		if self.blob.raw_size > 0:
			# uncompress the raw data
			self.BlobData=zlib.decompress(self.blob.zlib_data)
			#print "uncompressed BlobData %s"%(self.BlobData)
		else:
			#the data does not need uncompressing
			self.BlobData=raw
		return True
	
	def readNextBlock(self):
		"""read the next block. Block is a header and blob, then extract the block"""
		# read a BlobHeader to get things rolling. It should be 'OSMData'
		if self.readPBFBlobHeader()== False:
			return False
		if self.blobhead.type != "OSMData":
			print "Expected OSMData, found %s"%(self.blobhead.type)
			return False
		
		# read a Blob to actually get some data
		if self.readBlob()==False:
			return False
		
		# extract the primative block
		self.primblock.ParseFromString(self.BlobData)
		return True
	
	def processDense(self, dense):
		"""process a dense node block"""
		NANO=1000000000L
		#DenseNode uses a delta system of encoding os everything needs to start at zero
		lastID=0
		lastLat=0
		lastLon=0
		tagloc=0
		cs=0
		ts=0
		uid=0
		user=0
		gran=float(self.primblock.granularity)
		latoff=float(self.primblock.lat_offset)
		lonoff=float(self.primblock.lon_offset)
		for i in range(len(dense.id)):
			lastID+=dense.id[i]
			lastLat+=dense.lat[i]
			lastLon+=dense.lon[i]
			lat=float(lastLat*gran+latoff)/NANO
			lon=float(lastLon*gran+lonoff)/NANO
			user+=dense.denseinfo.user_sid[i]
			uid+=dense.denseinfo.uid[i]
			vs=dense.denseinfo.version[i]
			ts+=dense.denseinfo.timestamp[i]
			cs+=dense.denseinfo.changeset[i]
			suser=self.primblock.stringtable.s[user]
			tm=ts*self.primblock.date_granularity/1000
			node=osm.OSMNode(lastID)
			node.Lon=lon
			node.Lat=lat
			node.user=suser
			node.uid=uid
			node.version=vs
			node.changeset=cs
			node.time=tm
			if tagloc<len(dense.keys_vals):  # don't try to read beyond the end of the list
				while dense.keys_vals[tagloc]!=0:
					ky=dense.keys_vals[tagloc]
					vl=dense.keys_vals[tagloc+1]
					tagloc+=2
					sky=self.primblock.stringtable.s[ky]
					svl=self.primblock.stringtable.s[vl]
					node.AddTag(sky,svl)
			tagloc+=1
			if self.xmlout:
				node.xout(self.xmlfile)
			else:
				self.Nodes.append(node)
		
	def processNodes(self,nodes):
		NANO=1000000000L
		gran=float(self.primblock.granularity)
		latoff=float(self.primblock.lat_offset)
		lonoff=float(self.primblock.lon_offset)
		for nd in nodes:
			nodeid=nd.id
			lat=float(nd.lat*gran+latoff)/NANO
			lon=float(nd.lon*gran+lonoff)/NANO
			vs=nd.info.version
			ts=nd.info.timestamp
			uid=nd.info.uid
			user=nd.info.user_sid
			cs=nd.info.changeset
			tm=ts*self.primblock.date_granularity/1000
			node=osm.OSMNode(lastID)
			node.Lon=lon
			node.Lat=lat
			node.user=suser
			node.uid=uid
			node.version=vs
			node.changeset=cs
			node.time=tm
			for i in range(len(nd.keys)):
				ky=nd.keys[i]
				vl=nd.vals[i]
				sky=self.primblock.stringtable.s[ky]
				svl=self.primblock.stringtable.s[vl]
				node.AddTag(sky,svl)
			if self.xmlout:
				node.xout(self.xmlfile)
			else:
				self.Nodes.append(node)
	
	def processWays(self,ways):
		"""process the ways in a block, extracting id, nds & tags"""
		for wy in ways:
			wayid=wy.id
			vs=wy.info.version
			ts=wy.info.timestamp
			uid=wy.info.uid
			user=self.primblock.stringtable.s[wy.info.user_sid]
			cs=wy.info.changeset
			tm=ts*self.primblock.date_granularity/1000
			way=osm.OSMWay(wayid)
			way.user=user
			way.uid=uid
			way.version=vs
			way.changeset=cs
			way.time=tm
			ndid=0
			for nd in wy.refs:
				ndid+=nd
				way.AddNd(ndid)
			for i in range(len(wy.keys)):
				ky=wy.keys[i]
				vl=wy.vals[i]
				sky=self.primblock.stringtable.s[ky]
				svl=self.primblock.stringtable.s[vl]
				way.AddTag(sky,svl)
			if self.xmlout:
				way.xout(self.xmlfile)
			else:
				self.Ways.append(way)
	
	def processRels(self,rels):
		for rl in rels:
			relid=rl.id
			vs=rl.info.version
			ts=rl.info.timestamp
			uid=rl.info.uid
			user=self.primblock.stringtable.s[rl.info.user_sid]
			cs=rl.info.changeset
			tm=ts*self.primblock.date_granularity/1000
			rel=osm.OSMRelation(relid)
			rel.user=user
			rel.uid=uid
			rel.version=vs
			rel.changeset=cs
			rel.time=tm
			memid=0
			for i in range(len(rl.memids)):
				role=rl.roles_sid[i]
				memid+=rl.memids[i]
				memtype=self.membertype[rl.types[i]]
				memrole=self.primblock.stringtable.s[role]
				member=osm.OSMMember(memtype,memid,memrole)
				rel.AddMember(member)				
			for i in range(len(rl.keys)):
				ky=rl.keys[i]
				vl=rl.vals[i]
				sky=self.primblock.stringtable.s[ky]
				svl=self.primblock.stringtable.s[vl]
				rel.AddTag(sky,svl)
			if self.xmlout:
				rel.xout(self.xmlfile)
			else:
				self.Relations.append(rel)
	
	def readint(self):
		"""read an integer in network byte order and change to machine byte order. Return -1 if eof"""
		be_int=self.fpbf.read(4)
		if len(be_int) == 0:
			return -1
		else:
			le_int=unpack('!L',be_int)
			return le_int[0]
	
	def outputxmlhead(self):
		NANO=1000000000L
		self.xmlfile.write('<?xml version="1.0" encoding="UTF-8"?>\n')
		self.xmlfile.write('<osm version="0.6" generator="%s">\n'%self.progname)
		minlat=float(self.hblock.bbox.bottom)/NANO
		minlon=float(self.hblock.bbox.left)/NANO
		maxlat=float(self.hblock.bbox.top)/NANO
		maxlon=float(self.hblock.bbox.right)/NANO
		self.xmlfile.write('  <bounds minlat="%.7f" minlon="%.7f" maxlat="%.7f" maxlon="%.7f"/>\n'%(minlat,minlon,maxlat,maxlon))
	
	def outputxmltrail(self):
		self.xmlfile.write('</osm>\n')

def savexml(parser,filename):
	NANO=1000000000L
	xmlout = open(filename,"w")
	xmlout.write('<?xml version="1.0" encoding="UTF-8"?>\n')
	xmlout.write('<osm version="0.6" generator="classy 1.0">\n')
	minlat=float(parser.hblock.bbox.bottom)/NANO
	minlon=float(parser.hblock.bbox.left)/NANO
	maxlat=float(parser.hblock.bbox.top)/NANO
	maxlon=float(parser.hblock.bbox.right)/NANO
	xmlout.write('  <bounds minlat="%.7f" minlon="%.7f" maxlat="%.7f" maxlon="%.7f"/>\n'%(minlat,minlon,maxlat,maxlon))
	for n in parser.Nodes:
		n.xout(xmlout)
	for w in parser.Ways:
		w.xout(xmlout)
	for r in parser.Relations:
		r.xout(xmlout)
	xmlout.write('</osm>\n')
	xmlout.close

def main():
	# the main part of the program starts here
	# extract the command line options
	parser = OptionParser(usage="%prog PBFfile [options]", version="%prog 1.3")
	parser.add_option("-q", "--quiet",action="store_false", dest="verbose", default=True,help="don't print status messages to stdout")
	parser.add_option("-x", "--xml",action="store", dest="xmlout", default="",help="output xml to this file")
	(options, args) = parser.parse_args()
	
	if len(args) != 1 :
		print "You must enter the binary filename (*.pbf)"
		sys.exit(1)
	
	PBFFile=args[0] # the left over stuff when the options have been extracted
	
	if  not os.path.exists(PBFFile) :
		print "The binary file %s cannot be found" % (PBFFile)
		sys.exit(1)
	
	if options.verbose :
		print "Parse a binary OSM file"
	
	if options.verbose :
		print "Loading the PBF file: %s"%(PBFFile)
	
	# options sorted out, so now process the file
	# open the file and xml out file if needed
	fpbf= open(PBFFile, "rb")
	if options.xmlout!="":
		fxml = open(options.xmlout,"w")
		if options.verbose:
			print "Output XML to: %s"%(options.xmlout)
	else:
		fxml=0

	# create the parser object
	p=PBFParser(fpbf,fxml)

	#check the file head
	if p.init("pbfparser.py 1.3")==False:
		print "Header trouble"
		exit(1)

	if p.xmlout:
		p.outputxmlhead()
		
	# parse the rest of the file
	p.parse()

	if p.xmlout:
		p.outputxmltrail()
	
	#close the file()
	fpbf.close()
	if p.xmlout:
		fxml.close()

	#if options.xmlout!="":
		#savexml(p,options.xmlout)

	#if options.verbose:
		#print "%d Nodes"%(len(p.Nodes))
		#print "%d Ways"%(len(p.Ways))
		#print "%d Relations"%(len(p.Relations))

	# done ------------------------------------------------------

# start here
if __name__ == '__main__': main()
