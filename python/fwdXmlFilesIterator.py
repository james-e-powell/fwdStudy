from xml.dom import minidom
import re
import os
import porter
import math
import ssdeep
import sys
import string
import fileinput
from bs4 import BeautifulSoup
from numpy import zeros,dot
from numpy.linalg import norm

# import real stop words
stop_words = [w.strip().lower() for w in open('stopword-list.txt','r').readlines()]

# running Enthought Canopy Python 2.7.6 | 64-bit | (default, Apr 11 2014, 11:55:30) 
#
# Description
# this program parses the XML data files for the FwdStudy and extracts data 
# per entry, it uses this data to locate original and copies of harvested content 
# to generate similarity scores, finally it combines all this data into SQL inserts
#
# usage:
# python fwdXmlFilesIterator.py xmldata-aug15/arxiv.xml ~jepowell/Desktop/FwdStudy/content/ fwd_arxiv_test > fwd_arxiv_test.sql

def range_bytes (): return range(256)
def range_printable(): return (ord(c) for c in string.printable)
def H(data, iterator=range_bytes):
  try:
    if not data:
        return 0
    entropy = 0
    for x in iterator():
        try: 
          p_x = float(data.count(chr(x)))/len(data)
          if p_x > 0:
              entropy += - p_x*math.log(p_x, 2)
              # entropy += - p_x*math.log(p_x, 2) / math.log(256, 2)

        except:
          p_x = 0
  except:
    entropy = 0
  
  return entropy

def Entropy(text):
  try:
    import math
    log2=lambda x:math.log(x)/math.log(2)
    exr={}
    infoc=0
    for each in text:
        try:
            exr[each]+=1
        except:
            exr[each]=1
    textlen=len(text)
    for k,v in exr.items():
        freq  =  1.0*v/textlen
        infoc+=freq*log2(freq)
    infoc*=-1
  except:
    infoc = 0
  return infoc

def KLD(values, value2):

  try:
    map = {}
    map2 = {}
    for sequence in values:
      valKeys = map.keys()
      if sequence not in valKeys: 
        map[sequence]=float(0)
      map[sequence]+=float(1)

    for sequence in value2:
      valKeys = map2.keys()
      if sequence not in valKeys:
        map2[sequence]=float(0)
      map2[sequence]+=float(1)

    result = 0.0
    frequency2=0.0
    for token in map.keys():  
      frequency1 = float(map[token] / len(values))
      # print("Frequency1 "+ " " + token + " " + str(frequency1))

      valKeys = map2.keys()
      if token in valKeys:
        frequency2 = float(map2[token] / len(value2))
      try:
        result += float(frequency1) * (math.log(float(frequency1)/float(frequency2)) / math.log(2))
      except: 
        result = result

  except:
    result = 0
  return result;

arglist = sys.argv
argcount = len(sys.argv)
filename = arglist[1]
basedir = arglist[2]
dbname = arglist[3]
entryCount = 0

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
  entryCount+=1

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

  # print "opening " + basedir + str(original)
  f = open(basedir+str(original), 'r')
  doc1 = f.read()
  rawdoc1 = doc1
  try:
    soup = BeautifulSoup(doc1, "lxml")
    doc1 = soup.get_text()
  except:
    doc1 = rawdoc1
  entropy1 = H(doc1.replace(' ',''), range_printable)
  entropy2 = Entropy(doc1)
  # H(str, range_printable)

  # insert into fwd_arxiv values ('http://21cma.bao.ac.cn', 'arxiv/articles/2014-03-06/1403.0941.pdf', '2014-03-06', 'original', '0', '20140306', 'text/html', '200', '200', '', '10174', '', '96:fyEJcXhyW9tzcCn9b9BxLp+PVj8+qi7iYqgv0BNhaVjE6vCVHIOfJW+n:bsyWr39/xLp+PVj8+vOY0ZafveIyl', '')
  try: 
    print ('insert into ' + dbname + ' values (NULL,\'' + url + '\',\'' + citedByNode + '\',\'' + initialDate + '\',\'original\',' + str(count) + ',\'' + htype + '\',\'' + laststatus + '\',\'' + statuschain + '\',\'' + citeDate + '\',' + size + ',\'\',\'' + hash + '\',NULL,' + str(entropy1) + ',' + str(entropy2) + ',NULL);')
  except:
    print (' a failure occurred ')
  count+=1

  for copyNode in copyNodes:

      copy = copyNode.firstChild.nodeValue
      entryCount+=1

      initialDate = copyNode.getAttribute('date')
      htype = copyNode.getAttribute('htype')
      statuschain = copyNode.getAttribute('status')
      laststatus = copyNode.getAttribute('laststatus')
      hash = copyNode.getAttribute('hash')
      size = copyNode.getAttribute('size')
      sim = copyNode.getAttribute('sim')

      # print "opening " + basedir + str(original)
      # f = open(basedir+str(original), 'r')
      # doc1 = f.read()
      # rawdoc1 = doc1
      # try:
      #   soup = BeautifulSoup(doc1, "lxml")
      #   doc1 = soup.get_text()
      doc1_hash = ssdeep.hash(doc1)
      # except:
      #   doc1 = rawdoc1
      #   doc1_hash = ssdeep.hash(doc1)

      # print "opening " + basedir + str(copy)
      f = open(basedir+str(copy), 'r')
      doc2 = f.read()
      rawdoc2 = doc2
      try:
        soup = BeautifulSoup(doc2, "lxml")
        doc2 = soup.get_text()
        doc2_hash = ssdeep.hash(doc2)
      except:
        doc2 = rawdoc2
        doc2_hash = ssdeep.hash(doc2)

      entropy1 = H(doc2.replace(' ',''), range_printable)
      entropy2 = Entropy(doc2)

      doc1TokenList = re.split(r'(\d+|\W+)', doc1)
      doc2TokenList = re.split(r'(\d+|\W+)', doc2)
      kldScore = KLD(doc1TokenList, doc2TokenList)
      kldScore = 0
      simScore = compare(doc1,doc2)
      hashSimScore = ssdeep.compare(doc1_hash, doc2_hash)
      normalizedSimScore = ((90 - simScore) / 90) * 100

      try:
        print ('insert into ' + dbname + ' values (NULL,\'' + url + '\',\'' + citedByNode + '\',\'' + initialDate + '\',\'copy\',' + str(count) + ',\'' + htype + '\',\'' + laststatus + '\',\'' + statuschain + '\',\'' + citeDate + '\',' + size + ',\'' + sim + '\',\'' + hash + '\',' + str(normalizedSimScore) + ',' + str(entropy1) + ',' + str(entropy2) + ',' + str(kldScore) + ');')
      except:
        print (' a copy failure occurred ')

      # try: 
      #   print ("\"%s\",\"%s\",\"%s\",%f,%f,%f" % (url, original, copy, simScore, hashSimScore, normalizedSimScore))
      # except:
      #   print ("\"%s\",\"%s\",\"%s\",%f,%f,%f" % ('', '', '', simScore, hashSimScore, normalizedSimScore))

      count+=1

print ('Numer of entries was ' + str(entryCount))
