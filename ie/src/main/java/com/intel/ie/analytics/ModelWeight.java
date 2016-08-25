package com.intel.ie.analytics;

import edu.stanford.nlp.ie.KBPSemgrexExtractor;
import edu.stanford.nlp.ie.KBPTokensregexExtractor;

/**
 * Created by xianyan on 8/25/16.
 */
public interface ModelWeight {

    Double KBPStatisticalWeightIntel = 0.5289;
    Double KBPStatisticalWeightDefault = 0.4474;
    Double KBPTokenWeight = 0.6218;
    Double KBPSemgrexWeight = 0.3653;

    static Double getWeight(IntelKBPRelationExtractor extractor) {
        if (extractor.getClass().equals(IntelKBPStatisticalExtractor.class)) return KBPStatisticalWeightIntel;
        else if (extractor.getClass().equals(DefaultKBPStatisticalExtractor.class)) return KBPStatisticalWeightDefault;
        else if (extractor.getClass().equals(KBPSemgrexExtractor.class)) return KBPSemgrexWeight;
        else if (extractor.getClass().equals(KBPTokensregexExtractor.class)) return KBPTokenWeight;
        else return 1.0;
    }
}
