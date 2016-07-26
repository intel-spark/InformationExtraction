# InformationExtraction

How to run:

1. Download latest stanford coreNLP model from [here](http://nlp.stanford.edu/software/stanford-english-corenlp-models-current.jar).
2. Uncompress the downloaded jar file, and replace the edu folder in project ie.
3. The project needs Scala 2.10.4 and Spark 1.6. If using intellij, add the lib folder, Spark assembly jar file and scale SDK to the project library. If using Spark 2.0+, please use Scala 2.11.8.
4. Run SparkBatchTest, input data file paths or sentences to test.