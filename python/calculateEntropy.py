import re
import os
import porter
import math
import ssdeep
import sys
import string
import fileinput
from bs4 import BeautifulSoup
from numpy.linalg import norm

def range_bytes (): return range(256)
def range_printable(): return (ord(c) for c in string.printable)
# def H(data, iterator=range_bytes):
def H(data, iterator=range_printable):
    if not data:
        return 0
    entropy = 0
    for x in iterator():
        try: 
          p_x = float(data.count(chr(x)))/len(data)
          if p_x > 0:
              entropy += - p_x*math.log(p_x, 2)
        except:
          p_x = 0
    return entropy

# running Enthought Canopy Python 2.7.6 | 64-bit | (default, Apr 11 2014, 11:55:30) 

# example run
# python calculateEntropy.py ~jepowell/Desktop/FwdStudy/2014-03-05/2014-04-16/
# python calculateEntropy.py ~jepowell/Desktop/FwdStudy/2014-03-05/2014-04-16/ plos/mar_content/2014-03-03/2014-10-03/
# python calculateEntropy.py /Volumes/LaCie/plos/articles/ /Volumes/LaCie/plos/content/

argcount = len(sys.argv)
arglist = sys.argv
sourceDirectory = arglist[1]
compareDirectory = arglist[2]

print ("filename, sourcedir, entropy score")

splitter=re.compile ( "[a-zA-Z\-']+", re.I )

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

       f = open(compareDirectory+'/'+filename, 'r')
       doc2 = f.read()
       rawdoc2 = doc2
       soup = BeautifulSoup(doc2, "lxml")
       doc2 = soup.get_text()

       entropyScore = H(doc1)
       copyEntropyScore = H(doc2)
       print ("%s,%s,%s,%f,%f" % (filename, sourceDirectory, compareDirectory, entropyScore, copyEntropyScore))

