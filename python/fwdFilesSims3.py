from xml.dom import minidom
import re
import os
import porter
import math
import ssdeep
import sys
from bs4 import BeautifulSoup
from numpy import zeros,dot
from numpy.linalg import norm

# import real stop words
stop_words = [w.strip().lower() for w in open('stopword-list.txt','r').readlines()]

# running Enthought Canopy Python 2.7.6 | 64-bit | (default, Apr 11 2014, 11:55:30) 
# usage:
#   python fwdFilesSims3.py xmldata-aug15/plos.xml /Volumes/LaCie/FwdStudy/content/ fwd_plos2


arglist = sys.argv
argcount = len(sys.argv)
filename = arglist[1]
basedir = arglist[2]
dbname = arglist[3]

f = open(filename, 'r')
r = f.read()

splitter=re.compile ( "[a-zA-Z\-']+", re.I )
stemmer=porter.PorterStemmer()

def add_word(word,d):
 """
    Adds a word the a dictionary for words/count
    first checks for stop words
	the converts word to stemmed version
 """
 w=word.lower() 
 if w not in stop_words:
  ws=stemmer.stem(w,0,len(w)-1)
  d.setdefault(ws,0)
  d[ws] += 1

def doc_vec(doc,key_idx):
 v=zeros(len(key_idx))
 for word in splitter.findall(doc):
  keydata=key_idx.get(stemmer.stem(word,0,len(word)-1).lower(), None)
  # if keydata: v[keydata[0]] = 1
  if keydata: v[keydata[0]] += 1
 return v

def compare(doc1,doc2):

 # strip all punctuation but - and '
 # convert to lower case
 # store word/occurance in dict
 all_words=dict()

 for dat in [doc1,doc2]:
  [add_word(w,all_words) for w in splitter.findall(dat)]
 
 # build an index of keys so that we know the word positions for the vector
 key_idx=dict() # key-> ( position, count )
 keys=all_words.keys()
 keys.sort()
 for i in range(len(keys)):
  key_idx[keys[i]] = (i,all_words[keys[i]])
 del keys
 del all_words

 v1=doc_vec(doc1,key_idx)
 v2=doc_vec(doc2,key_idx)
 # return math.acos(float(dot(v1,v2) / (norm(v1) * norm(v2))))
 # return math.acos(float(dot(v1,v2) / (norm(v1) * norm(v2)))) 
 try:
    degreeScore = math.degrees(math.acos(float(dot(v1,v2) / (norm(v1) * norm(v2)))))
 except:
    degreeScore = 0 
 return degreeScore

xmldoc = minidom.parseString(r)

rowNodes = []
rowNodes = xmldoc.getElementsByTagName("row")

maxCount = 0

for row in rowNodes:

  original = row.getElementsByTagName('original')[0].firstChild.nodeValue

  citedByNode = row.getElementsByTagName("citedby")[0].firstChild.nodeValue
  url = row.getElementsByTagName('url')[0].firstChild.nodeValue
  citeDate = row.getElementsByTagName('citedate')[0].firstChild.nodeValue
  originalNode = row.getElementsByTagName('original')[0]
  initialDate = originalNode.getAttribute('date')
  htype = originalNode.getAttribute('htype')
  statuschain = originalNode.getAttribute('status')
  laststatus = originalNode.getAttribute('laststatus')
  hash = originalNode.getAttribute('hash')
  size = originalNode.getAttribute('size')
  
  copyNodes = row.getElementsByTagName('copy')
  count=0

  # insert into fwd_arxiv values ('http://21cma.bao.ac.cn', 'arxiv/articles/2014-03-06/1403.0941.pdf', '2014-03-06', 'original', '0', '20140306', 'text/html', '200', '200', '', '10174', '', '96:fyEJcXhyW9tzcCn9b9BxLp+PVj8+qi7iYqgv0BNhaVjE6vCVHIOfJW+n:bsyWr39/xLp+PVj8+vOY0ZafveIyl', '')

  try:
    print ('insert into ' + dbname + ' values (\'\',\'' + url + '\',\'' + citedByNode + '\',\'' + initialDate + '\',\'original\',' + str(count) + ',\'' + htype + '\',\'' + laststatus + '\',\'' + statuschain + '\',\'' + citeDate + '\',\'' + size + '\',\'\',\'' + hash + '\',\'\');')
  except:
    print ('')
  count+=1

  for copyNode in copyNodes:

      copy = copyNode.firstChild.nodeValue

      initialDate = copyNode.getAttribute('date')
      htype = copyNode.getAttribute('htype')
      statuschain = copyNode.getAttribute('status')
      laststatus = copyNode.getAttribute('laststatus')
      hash = copyNode.getAttribute('hash')
      size = copyNode.getAttribute('size')
      sim = copyNode.getAttribute('sim')
 
      f = open(basedir+str(original), 'r')
      doc1 = f.read()
      rawdoc1 = doc1
      soup = BeautifulSoup(doc1, "lxml")
      doc1 = soup.get_text()
      doc1_hash = ssdeep.hash(doc1)

      f = open(basedir+str(copy), 'r')
      doc2 = f.read()
      rawdoc2 = doc2
      soup = BeautifulSoup(doc2, "lxml")
      doc2 = soup.get_text()
      doc2_hash = ssdeep.hash(doc2)

      simScore = compare(doc1,doc2)
      hashSimScore = ssdeep.compare(doc1_hash, doc2_hash)
      normalizedSimScore = ((90 - simScore) / 90) * 100

      print ('insert into ' + dbname + ' values (\'\',\'' + url + '\',\'' + citedByNode + '\',\'' + initialDate + '\',\'copy\',' + str(count) + ',\'' + htype + '\',\'' + laststatus + '\',\'' + statuschain + '\',\'' + citeDate + '\',\'' + size + '\',\'' + sim + '\',\'' + hash + '\',\'' + str(normalizedSimScore) + '\');')

      # try: 
      #   print ("\"%s\",\"%s\",\"%s\",%f,%f,%f" % (url, original, copy, simScore, hashSimScore, normalizedSimScore))
      # except:
      #   print ("\"%s\",\"%s\",\"%s\",%f,%f,%f" % ('', '', '', simScore, hashSimScore, normalizedSimScore))

      count+=1
