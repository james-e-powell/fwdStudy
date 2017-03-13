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

# example run
# python drift.py ~jepowell/Desktop/Link\ Response\ Study/content/plos/mar_content/2014-03-03/2014-04-16/ ~jepowell/Desktop/Link\ Response\ Study/content/plos/mar_content/2014-03-03/2014-10-03/ > forHerbert.txt


argcount = len(sys.argv)
arglist = sys.argv
sourceDirectory = arglist[1]
compareDirectory = arglist[2]

print ("filename, sourcedir, comparedir, angle between term vectors, hash similarity score, normalized score")

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
 
 
results = {}
count = 0

# for dirname, dirnames, filenames in os.walk('./fwd_link'):
# directoryContents = os.walk(sourceDirectory)
# for filename in directoryContents:
for dirname, dirnames, filenames in os.walk(sourceDirectory):
   for filename in filenames: 
       # print (filename)
       f = open(sourceDirectory + '/'+filename, 'r')
       doc1 = f.read()
       rawdoc1 = doc1
       soup = BeautifulSoup(doc1, "lxml")
       doc1 = soup.get_text()
       doc1_hash = ssdeep.hash(doc1)

       f = open(compareDirectory+'/'+filename, 'r')
       doc2 = f.read()
       rawdoc2 = doc2
       soup = BeautifulSoup(doc2, "lxml")
       doc2 = soup.get_text()
       doc2_hash = ssdeep.hash(doc2)

       simScore = compare(doc1,doc2)
       hashSimScore = ssdeep.compare(doc1_hash, doc2_hash)
       normalizedSimScore = ((90 - simScore) / 90) * 100
       print ("%s,%s,%s,%f,%f,%f" % (filename, sourceDirectory, compareDirectory, simScore, hashSimScore, normalizedSimScore))

