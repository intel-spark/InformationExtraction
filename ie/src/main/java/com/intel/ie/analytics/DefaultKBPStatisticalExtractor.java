package com.intel.ie.analytics;


import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.util.ArgumentParser;

import java.io.IOException;

/**
 * default stanford model
 * A relation extractor to work with Victor's new KBP data.
 */
@SuppressWarnings("FieldCanBeLocal")
public class DefaultKBPStatisticalExtractor extends KBPStatisticalExtractor {
    @ArgumentParser.Option(name = "default_model", gloss = "The path to the default_model")
    private static String model = DefaultPaths.DEFAULT_KBP_CLASSIFIER;

    /**
     * Create a new KBP relation extractor, from the given implementing classifier.
     *
     * @param classifier The implementing classifier.
     */
    public DefaultKBPStatisticalExtractor(Classifier<String, String> classifier) {
        super(classifier);
    }

    public static IntelKBPRelationExtractor loadStatisticalExtractor() throws IOException, ClassNotFoundException {
        log.info("Loading KBP classifier from " + model);
        Object object = IOUtils.readObjectFromURLOrClasspathOrFileSystem(model);
        IntelKBPRelationExtractor statisticalExtractor;
        if (object instanceof LinearClassifier) {
            //noinspection unchecked
            statisticalExtractor = new DefaultKBPStatisticalExtractor((Classifier<String, String>) object);
        } else if (object instanceof DefaultKBPStatisticalExtractor) {
            statisticalExtractor = (DefaultKBPStatisticalExtractor) object;
        } else if (object instanceof edu.stanford.nlp.ie.KBPStatisticalExtractor) {
            edu.stanford.nlp.ie.KBPStatisticalExtractor kbp = (edu.stanford.nlp.ie.KBPStatisticalExtractor) object;
            statisticalExtractor = new DefaultKBPStatisticalExtractor(kbp.classifier);
        } else {
            throw new ClassCastException(object.getClass() + " cannot be cast into a " + DefaultKBPStatisticalExtractor.class);
        }
        return statisticalExtractor;
    }
}
