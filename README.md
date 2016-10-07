# InformationExtraction

**Documentation for the project is available in project [wiki](https://github.com/intel-analytics/InformationExtraction/wiki)**

### How to build:

1. Download stanford coreNLP model from [here](https://drive.google.com/open?id=0B9zID9CU9HQeMEt6clEwT2dFdms).
2. Put the downloaded model file into lib folder.
3. Run "mvn clean package" in the project directory.
4. The deployment package should be ready at ie-dist folder.

### How to get the zip distribution of ie-dist:

1. Run "cd ie-dist-module && mvn clean package && cd .." in the projct directory.
2. The target assembly file is named as "ie-dist-1.0-assembly.zip" in the project directory.

### How to run on Spark cluster:

1. Finish the build process.
2. Copy the ie-dist folder to your spark cluster.
3. RunSparkBatchDriver.sh will start the interactive processing terminal, where you can input sentences or (hdfs) file paths.
4. To run Relation Evaluation, please refer to RunRelationEvaluation.sh, where you might need to change the file location according to your cluster setttings. Please copy the data folder to your cluster and upload to hdfs before running evaluation (This only need to be performed once).

### Setup development env with Intellij:

1. Download latest stanford coreNLP model from [here](https://drive.google.com/open?id=0B9zID9CU9HQeMEt6clEwT2dFdms).
2. Put the downloaded model file into lib folder.
3. Open/import the project as Maven project.
4. Add lib folder to the project library, and click Build.

### How to customize/extend:

1. Refer to config.properties for configuration change, such like pipeline components, NER models, dictionary and regex rules;
2. Cutomized training for NER and Relation Extractor can be supported by com.intel.ie.training package.
