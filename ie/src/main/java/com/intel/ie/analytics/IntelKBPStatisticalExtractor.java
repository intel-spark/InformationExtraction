package com.intel.ie.analytics;


import com.intel.ie.IntelConfig;
import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.classify.RVFDataset;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static edu.stanford.nlp.util.logging.Redwood.Util.endTrack;
import static edu.stanford.nlp.util.logging.Redwood.Util.forceTrack;

/**
 * A relation extractor to work with Victor's new KBP data.
 */
@SuppressWarnings("FieldCanBeLocal")
public class IntelKBPStatisticalExtractor extends KBPStatisticalExtractor {
    private static final long serialVersionUID = 1L;

    @ArgumentParser.Option(name = "train", gloss = "The dataset to train on")
    public static File TRAIN_FILE = new File(IntelConfig.INTEL_RELATION_CORP);

    @ArgumentParser.Option(name = "intel_model", gloss = "The path to the intel_model")
    private static String MODEL = IntelConfig.Intel_KBP_CLASSIFIER;


    @ArgumentParser.Option(name = "feature_threshold", gloss = "The minimum number of times to see a feature to count it")
    private static int FEATURE_THRESHOLD = 0;

    @ArgumentParser.Option(name = "sigma", gloss = "The regularizer for the classifier")
    private static double SIGMA = 1.0;

    /**
     * Create a new KBP relation extractor, from the given implementing classifier.
     *
     * @param classifier The implementing classifier.
     */
    public IntelKBPStatisticalExtractor(Classifier<String, String> classifier) {
        super(classifier);
    }

    public static IntelKBPRelationExtractor loadStatisticalExtractor() throws IOException, ClassNotFoundException {
        log.info("Loading KBP classifier from " + MODEL);
        Object object = edu.stanford.nlp.io.IOUtils.readObjectFromURLOrClasspathOrFileSystem(MODEL);
        IntelKBPRelationExtractor statisticalExtractor;
        if (object instanceof LinearClassifier) {
            //noinspection unchecked
            statisticalExtractor = new IntelKBPStatisticalExtractor((Classifier<String, String>) object);
        } else if (object instanceof IntelKBPStatisticalExtractor) {
            statisticalExtractor = (IntelKBPStatisticalExtractor) object;
        } else if (object instanceof edu.stanford.nlp.ie.KBPStatisticalExtractor) {
            edu.stanford.nlp.ie.KBPStatisticalExtractor kbp = (edu.stanford.nlp.ie.KBPStatisticalExtractor) object;
            statisticalExtractor = new IntelKBPStatisticalExtractor(kbp.classifier);
        } else {
            throw new ClassCastException(object.getClass() + " cannot be cast into a " + IntelKBPStatisticalExtractor.class);
        }
        return statisticalExtractor;
    }

    public static void trainModel() throws IOException {
        forceTrack("Training data");
        List<Pair<KBPInput, String>> trainExamples = DatasetUtils.readDataset(TRAIN_FILE);
        log.info("Read " + trainExamples.size() + " examples");
        log.info("" + trainExamples.stream().map(Pair::second).filter(NO_RELATION::equals).count() + " are " + NO_RELATION);
        endTrack("Training data");

        // Featurize + create the dataset
        forceTrack("Creating dataset");
        RVFDataset<String, String> dataset = new RVFDataset<>();
        final AtomicInteger i = new AtomicInteger(0);
        long beginTime = System.currentTimeMillis();
        trainExamples.stream().parallel().forEach(example -> {
            if (i.incrementAndGet() % 1000 == 0) {
                log.info("[" + Redwood.formatTimeDifference(System.currentTimeMillis() - beginTime) +
                        "] Featurized " + i.get() + " / " + trainExamples.size() + " examples");
            }
            Counter<String> features = features(example.first);  // This takes a while per example
            synchronized (dataset) {
                dataset.add(new RVFDatum<>(features, example.second));
            }
        });
        trainExamples.clear();  // Free up some memory
        endTrack("Creating dataset");

        // Train the classifier
        log.info("Training classifier:");
        Classifier<String, String> classifier = trainMultinomialClassifier(dataset, FEATURE_THRESHOLD, SIGMA);
        dataset.clear();  // Free up some memory

        // Save the classifier
        IOUtils.writeObjectToFile(new IntelKBPStatisticalExtractor(classifier), MODEL_FILE);
    }


    public static void main(String[] args) {
        try {
            trainModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
