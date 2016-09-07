Active Learning Data
====================

This folder contains the dataset associated with the active learning paper:

    INSERT CITATION HERE!!!

Annotated Sentences
-------------------
A CSV file contains the entire set of annotated sentences, bzip'd, in:

    annotated_sentences.csv.bz2

The columns of this file are as follows:

  * key: The sentence key, used in cross-referencing this sentence with other
    resources
  * relation: The annotated relation, as determined by Turkers
  * confidence: A metric of Turker confidence. It is defined as the percentage of
    Turkers which chose this relation over the NO_RELATION option.
  * jsDistance: The Jensen Shannon divergence of this example, as determined
    by the committee of relation extractors during example selection.
    The larger this distance, the more uncertain we were about this example.
  * sentence: The plain-text gloss of the sentence. Note that this string is encoded as
    HTML, exactly as it was shown to the Turkers. This also means that commas are escaped,
    for easier CSV reading.
  * entity: The plain-text gloss of the entity.
  * entityCharOffsetBegin: The character offset of the beginning of the entity, 
    in the original string.
  * entityCharOffsetEnd: The character offset of the end of the entity.
  * slotValue: The plain-text gloss of the slot value.
  * slotValueCharOffsetBegin: The character offset of the beginning of the slot value, 
    in the original string.
  * slotValueCharOffsetEnd: The character offset of the end of the slot value.


In addition, the fully annotated sentences are provided in a file of concatenated
  protocol buffers:

    delimitedSentences.proto.bz

This file should be read with the Java function
  `CoreNLPProtos.Sentence.parseDelimitedFrom(<input stream>)`,
  or in other languages taking into consideration that every protocol buffer is
  prepended with the size of the buffer, as a VarInt.
Each proto contains all the annotations for the MIML-RE featurizer, in addition to
  some useful additions (e.g., antecedent for every token).
See the associated CoreNLP.proto and KBP.proto files for a summary of fields included
  in these files.

Lastly, to reproduce the experiments in the paper, four files are provided listing
  the sentence keys used for each of the training conditions.
The keys in these files should be cross-referenced with the keys in either the CSV or
  the proto files (in the latter case, the key annotation is an extension to Sentence
  as defined in KBP.proto).
These are:

  * pilot_sampleJS.keys: The keys used in the pilot study -- these are not
    used independently of other keys in the experiments.
  * uniform.keys: The sentences from uniformly sampling from the latent labels.
  * highJS.keys: The sentences with the largest JS divergence in the committee of
    relation extractors.

Sample Code
-----------
A bit of sample code is provided to demonstrate the usage of the data, in the `code/` directory.
In particular, the directory contains compiled protocol buffers for Python, as well as a simple
  program to train and evaluate a relation extractor from the data.
This program can be invoked in the `code/` directory with the command:

    python demo.py --sentenceCSV=../annotated_sentences.csv.bz2 --sentenceProto=../delimitedSentences.proto.bz2 --trainSize=10000 --testSize=1000

Note that both files passed in are bzip'd, and further note that the train and test
  dataset sizes can be specified.

