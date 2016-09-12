package com.intel.ie.training;

import com.intel.ie.IntelConfig;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import java.util.Properties;

public class TrainNerModel {
    public static void main(String[] args) {
        String path = IntelConfig.DEPARTMENT_TRAIN_PROPERTY;
        Properties props = StringUtils.propFileToProperties(path);

        SeqClassifierFlags flags = new SeqClassifierFlags(props);
        CRFClassifier<CoreLabel> crf = new CRFClassifier<CoreLabel>(flags);
        crf.train();
        String modelPath = props.getProperty("serializeTo");
        crf.serializeClassifier(modelPath);
        System.out.println("Build model to " + modelPath);
    }
}
