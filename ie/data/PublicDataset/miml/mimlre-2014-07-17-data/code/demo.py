#!/usr/bin/env python
#

import sys, bz2, csv, argparse, codecs

from sklearn import svm
from sklearn.linear_model import LogisticRegression
from sklearn.feature_extraction import DictVectorizer

import CoreNLP_pb2, KBP_pb2
from google.protobuf.internal.decoder import _DecodeVarint
      
from protobuf_json import pb2json


"""
  Create an argument parser for command line arguments.
"""
def parseArgs():
  parser = argparse.ArgumentParser(description='Train a model on sentence-level annotations')
  parser.add_argument('--sentenceCSV', dest='sentenceCSV', required=True,
                      help='The CSV file containing the annotated sentences, in *.bz2 format.')
  parser.add_argument('--sentenceProto', dest='sentenceProto', required=True,
                      help='The file containing the sentences as protocol buffers.')
  parser.add_argument('--testSize', dest='testSize', type=int, default=10000,
                      help='The size of the test set')
  parser.add_argument('--trainSize', dest='trainSize', type=int, default=10000000,
                      help='The size of the train set')
  
  return parser.parse_args()

"""
  Read the CSV file of sentences.
  This file contains a the raw text of the sentences in a more easy-to-use form,
  and importantly contains the annotated label for each of these sentences.
  
  In theory, you could get by using only the output of this function, though
  the annotated protocol buffers contain a fair bit more data.
"""
def readRawSentences(annotatedSentencesFile):
  rows = {}
  f = bz2.BZ2File(annotatedSentencesFile, mode="r")
  c = codecs.iterdecode(f, "ascii", errors="ignore")
  g = csv.reader(c)
  for e in g:
    rows[e[0]] = e
  f.close
  return rows

"""
  Read the protocol buffers, containing the serialized sentences.
  For a specification of what's in the proto, see CoreNLP.proto; 
  additionally, KBP.proto defines some useful extensions to the base Sentence
  class with some useful additions. For example, antecedent annotations (from
  coref), and of course the sentence gloss key each sentence corresponds to.

  This single file contains all of the protocol buffers for annotated sentences.
  They are delimited by the size of the buffer (canonically, to be used by the

"""
def readProtoSentences(protoFile):
  protos = {}
  with open(protoFile, 'rb') as f:
    # -- Read the file --
    print ("  reading file '%s'..." % protoFile)
    data = bz2.decompress(f.read())
    # -- Parse the file --
    # In Java. there's a parseDelimitedFrom() method that does this easier
    print ("  got protobuf stream.")
    pos = 0
    while (pos < len(data)):
      # (read the proto)
      (size, pos) = _DecodeVarint(data, pos)
      proto = CoreNLP_pb2.Sentence()
      proto.ParseFromString(data[pos:(pos+size)])
      pos += size
      # (add the proto to the map)
      key = proto.Extensions[KBP_pb2.sentenceKey]
      yield (key, proto)

"""
  Avoid having to try to import the featurizer each time by caching
  whether we found the custom featurizer
"""
global FOUND_FEATURIZER;
FOUND_FEATURIZER = None

"""
  Featurize the given sentence. The output should be a dictionary; the input
  is a protocol buffer corresponding to the sentence.
"""
def featurize(proto):
  features = {}

  # Try to find an external featurizer
  global FOUND_FEATURIZER
  if FOUND_FEATURIZER == None:
    try:
      print ("Attempting to import custom featurizer (in featurizer class, featurizeJSON() function)...")
      from featurizer import featurizeJSON
      dnspython_available = True
      print ("  success.")
      FOUND_FEATURIZER = True
    except ImportError:
      print ("  FAILED; using default [bag-of-word] featurizer")
      FOUND_FEATURIZER = False

  # Featurize
  if FOUND_FEATURIZER:
    from featurizer import featurizeJSON
    features = featurizeJSON(pb2json(proto))
  else:
    # Bag-of-words features
    for token in proto.token:
      features['word_' + token.word] = 1

  # Return
  return features


"""
  The main method, reading in the data, processing it, and then training+evaluating.
"""
def main():
  # -- Indexers --
  # Label indexer
  labelIndexer = {}
  index2label = []
  def indexLabel(string):
    if not string in labelIndexer:
      labelIndexer[string] = len(labelIndexer)
      index2label.append(string)
    return labelIndexer[string]
  NO_RELATION = indexLabel("no_relation")
  # Feature indexer
  featureIndexer = DictVectorizer(sparse=False)

  # -- Get Data --
  args = parseArgs();
  print ("Reading sentences...")
  annotatedSentences = readRawSentences(args.sentenceCSV)
  print ("Reading protos...")
  protos = readProtoSentences(args.sentenceProto)

  # -- Train --
  # Construct training data
  rawFeatures = []
  y = []
  for example in protos:
    if len(rawFeatures) >= args.trainSize + args.testSize:
      break;
    (key, proto) = example
    csvRow = annotatedSentences[key]
    label = csvRow[1]  # CSV header is <key, relation, confidence, distance, ...(sentence info)...>
    rawFeatures.append(featurize(proto))
    y.append(indexLabel(label))
  
  # Create feature vectors
  X = featureIndexer.fit_transform(rawFeatures)
  
  # Run classifier
  print "Training..."
  clf = LogisticRegression()
  clf.fit(X[args.testSize:], y[args.testSize:])

  # -- Evaluate --
  print "Evaluating..."
  # Run predictions
  correct = 0
  inGuess = 0
  inKey   = 0
  for i in range(0, args.testSize):
    guess = clf.predict(X[i])
    gold  = y[i]
#    print ("guess: %s [%d]   gold: %s [%d]" % ( index2label[guess], guess, index2label[gold], gold ))
    if guess != NO_RELATION:
      inGuess += 1
      if guess == gold:
        correct += 1
    if gold != NO_RELATION:
      inKey += 1

  # Report P/R
  p = float(correct) / float(inGuess if inGuess > 0 else 1)
  r = float(correct) / float(inKey if inKey > 0 else 1)
  f1 = 2.0 * p * r / ((p + r) if (p + r > 0) else 1)
  print ("----------"  )
  print ("P:  %f" % p  )
  print ("R:  %f" % r  )
  print ("F1: %f" % f1 )
  print ("----------"  )


if __name__ == "__main__":
  main()
