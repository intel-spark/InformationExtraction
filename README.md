# InformationExtraction

**Documentation for the project is available in project [wiki](https://github.com/intel-analytics/InformationExtraction/wiki)**

### How to build:

1. Download latest stanford coreNLP model from [here](https://drive.google.com/open?id=0B9zID9CU9HQeMEt6clEwT2dFdms).
2. Put the downloaded model file into lib folder.
3. Run "mvn clean package" in the project directory.
4. The deployment package will be ready at ie-dist folder.

### How to run on Spark cluster:

1. Finish the build process.
2. Copy the ie-dist folder to your spark cluster.
3. RunSparkBatchDriver.sh

### Setup development env with Intellij:

1. Download latest stanford coreNLP model from [here](https://drive.google.com/open?id=0B9zID9CU9HQeMEt6clEwT2dFdms).
2. Put the downloaded model file into lib folder.
3. Open/import the project as Maven project.
4. Add lib folder to the project library. File/project structure, and click Build.

### How to customize/extend:
