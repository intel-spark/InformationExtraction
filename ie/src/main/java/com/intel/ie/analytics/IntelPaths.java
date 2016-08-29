package com.intel.ie.analytics;

public class IntelPaths {

    public static String Regex_NER_cased = "data/kbp/regexner_cased.tab";
    public static String Regex_NER_caseless = "data/kbp/regexner_caseless.tab";    

    public static String combined = Regex_NER_caseless
            + ";" + Regex_NER_cased;

    public static String KBP_TOKENSREGEX_DIR = "data/kbp/tokensregex";
    public static String KBP_SEMGREX_DIR = "data/kbp/semgrex";

    public static String KBP_CLASSIFIER = "data/kbp/tac-re-lr.ser.gz";

    public static IntelEnsembleStrategy ENSEMBLE_STRATEGY = IntelEnsembleStrategy.HIGH_PRECISION;
    
    public static boolean bSeprateFormerTitle = false;
}
