package com.intel.ie.analytics;


import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.logging.Redwood;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * An ensemble of other KBP relation extractors.
 * Currently, this class just takes the union of the given extractors.
 * That is, it returns the first relation returned by any extractor
 * (ties broken by the order the extractors are passed to the constructor),
 * and only returns no_relation if no extractor proposed a relation.
 */
@SuppressWarnings("FieldCanBeLocal")
public class IntelKBPEnsembleExtractor implements IntelKBPRelationExtractor {
    protected static final Redwood.RedwoodChannels logger = Redwood.channels(edu.stanford.nlp.ie.KBPRelationExtractor.class);

    @ArgumentParser.Option(name = "model", gloss = "The path to the model")
    private static String STATISTICAL_MODEL = IntelKBPConfig.KBP_CLASSIFIER;

    @ArgumentParser.Option(name = "semgrex", gloss = "Semgrex patterns directory")
    private static String SEMGREX_DIR = IntelKBPConfig.KBP_SEMGREX_DIR;

    @ArgumentParser.Option(name = "tokensregex", gloss = "Tokensregex patterns directory")
    private static String TOKENSREGEX_DIR = IntelKBPConfig.KBP_TOKENSREGEX_DIR;

    @ArgumentParser.Option(name = "predictions", gloss = "Dump model predictions to this file")
    private static Optional<String> PREDICTIONS = Optional.empty();

    @ArgumentParser.Option(name = "test", gloss = "The dataset to test on")
    private static File TEST_FILE = new File("test.conll");

    /**
     * The extractors to run, in the order of priority they should be run in.
     */
    private final IntelKBPRelationExtractor[] extractors;

    /**
     * Ensemble strategy
     */
    private IntelEnsembleStrategy ensembleStrategy = IntelEnsembleStrategy.HIGHEST_SCORE;

    /**
     * Creates a new ensemble extractor from the given argument extractors.
     *
     * @param extractors A varargs list of extractors to union together.
     */
    IntelKBPEnsembleExtractor(IntelKBPRelationExtractor... extractors) {
        this.extractors = extractors;
    }

    public IntelKBPEnsembleExtractor setEnsembleStrategy(IntelEnsembleStrategy ensembleStrategy) {
        this.ensembleStrategy = ensembleStrategy;
        return this;
    }
    
    @Override
    public Pair<String, Double> classify(KBPInput input) {
        switch (ensembleStrategy) {

            case HIGHEST_SCORE:
                return classifyWithHighestScore(input);
            case VOTE:
                return classifyWithVote(input);
            case WEIGHTED_VOTE:
                return classifyWithWeightedVote(input);
            case HIGH_RECALL:
                return classifyWithHighRecall(input);
            case HIGH_PRECISION:
                return classifyWithHighPrecision(input);
            default:
                throw new UnsupportedClassVersionError(ensembleStrategy + " not supported");
        }
    }

    private Pair<String, Double> classifyWithHighPrecision(KBPInput input) {
        Pair<String, Double> prediction = Pair.makePair(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION, 1.0);
        for (IntelKBPRelationExtractor extractor : extractors) {
            if (!extractor.getClass().equals(IntelKBPTokensregexExtractor.class)) continue;
            return extractor.classify(input);
        }
        return prediction;
    }

    private Pair<String, Double> classifyWithHighRecall(KBPInput input) {
        Pair<String, Double> prediction = Pair.makePair(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION, 1.0);
        for (IntelKBPRelationExtractor extractor : extractors) {
            Pair<String, Double> classifierPrediction = extractor.classify(input);
            logger.info(extractor + ": " + classifierPrediction);
            if (!classifierPrediction.first.equals(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION)) {
                if (prediction.first.equals(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION))
                    prediction = classifierPrediction;
                else {
                    prediction = classifierPrediction.second > prediction.second ? classifierPrediction : prediction;
                }
            }
        }
        return prediction;
    }

    private Pair<String, Double> classifyWithVote(KBPInput input) {
        HashMap<String, Double> relation2Weights = new HashMap<>();
        Pair<String, Double> prediction = Pair.makePair(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION, 0.0);
        for (IntelKBPRelationExtractor extractor : extractors) {
            Pair<String, Double> classifierPrediction = extractor.classify(input);
            logger.info(extractor + ": " + classifierPrediction);
            Double weight = relation2Weights.get(classifierPrediction.first);
            Double newWeight = weight == null ? 1.0 / extractors.length : weight + 1.0 / extractors.length;
            relation2Weights.put(classifierPrediction.first, newWeight);
            if (newWeight > prediction.second) prediction = Pair.makePair(classifierPrediction.first, newWeight);
        }
        return prediction;
    }

    private Pair<String, Double> classifyWithWeightedVote(KBPInput input) {
        HashMap<String, Double> relation2Weights = new HashMap<>();
        Pair<String, Double> prediction = Pair.makePair(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION, 0.0);
        for (IntelKBPRelationExtractor extractor : extractors) {
            Pair<String, Double> classifierPrediction = extractor.classify(input);
            logger.info(extractor + ": " + classifierPrediction);
//            if (classifierPrediction.first.equals(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION)) continue;
            Double weight = relation2Weights.get(classifierPrediction.first);
            Double newWeight = weight == null ? ModelWeight.getWeight(extractor) : weight + ModelWeight.getWeight(extractor);
            relation2Weights.put(classifierPrediction.first, newWeight);
            if (newWeight > prediction.second) prediction = Pair.makePair(classifierPrediction.first, newWeight);
        }
        return prediction;
    }

    private Pair<String, Double> classifyWithHighestScore(KBPInput input) {
        Pair<String, Double> prediction = Pair.makePair(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION, 1.0);
        for (IntelKBPRelationExtractor extractor : extractors) {
            Pair<String, Double> classifierPrediction = extractor.classify(input);
            logger.info(extractor + ": " + classifierPrediction);
            if (prediction.first.equals(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION) ||
                    (!classifierPrediction.first.equals(edu.stanford.nlp.ie.KBPRelationExtractor.NO_RELATION) &&
                            classifierPrediction.second > prediction.second)
                    ) {
                // The last prediction was NO_RELATION, or this is not NO_RELATION and has a higher score
                prediction = classifierPrediction;
            }
        }
        return prediction;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        RedwoodConfiguration.standard().apply();  // Disable SLF4J crap.
        ArgumentParser.fillOptions(edu.stanford.nlp.ie.KBPEnsembleExtractor.class, args);

        Object object = IOUtils.readObjectFromURLOrClasspathOrFileSystem(STATISTICAL_MODEL);
        IntelKBPRelationExtractor statisticalExtractor;
        if (object instanceof LinearClassifier) {
            //noinspection unchecked
            statisticalExtractor = new IntelKBPStatisticalExtractor((Classifier<String, String>) object);
        } else if (object instanceof IntelKBPStatisticalExtractor) {
            statisticalExtractor = (IntelKBPStatisticalExtractor) object;
        } else {
            throw new ClassCastException(object.getClass() + " cannot be cast into a " + IntelKBPStatisticalExtractor.class);
        }
        logger.info("Read statistical model from " + STATISTICAL_MODEL);
        IntelKBPRelationExtractor extractor = new IntelKBPEnsembleExtractor(
                new IntelKBPTokensregexExtractor(TOKENSREGEX_DIR),
                new IntelKBPSemgrexExtractor(SEMGREX_DIR),
                statisticalExtractor
        );

        List<Pair<KBPInput, String>> testExamples = DatasetUtils.readDataset(TEST_FILE);

        extractor.computeAccuracy(testExamples.stream(), PREDICTIONS.map(x -> {
            try {
                return "stdout".equalsIgnoreCase(x) ? System.out : new PrintStream(new FileOutputStream(x));
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }));

    }
}
