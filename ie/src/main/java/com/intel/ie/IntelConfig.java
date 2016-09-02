package com.intel.ie;

import com.intel.ie.analytics.IntelEnsembleStrategy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class IntelConfig {
    static Properties prop;

    static {
        prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);
        } catch (IOException e) {
            System.err.println("configure file not found!");
        }
    }

    static String getPath(String label) {
        return prop.getProperty("root") + prop.get(label);
    }

    static String getPaths(String label) {
        String[] paths = prop.getProperty(label).split(",");
        StringBuffer res = new StringBuffer();
        for (String path : paths) {
            res.append(prop.getProperty("root") + path + ",");
        }
        return res.toString();
    }

    // Model related
    public static String NER_MODELS = getPaths("NER_MODEL");
    public static String Regex_NER_cased = getPath("Regex_NER_cased");
    public static String Regex_NER_caseless = getPath("Regex_NER_caseless");
    public static String Regex_NER_more = getPath("Regex_NER_more");
    public static String combined = Regex_NER_caseless
            + ";" + Regex_NER_cased
            + ";" + Regex_NER_more;
    public static String KBP_TOKENSREGEX_DIR = getPath("KBP_TOKENSREGEX_DIR");
    public static String KBP_SEMGREX_DIR = getPath("KBP_SEMGREX_DIR");
    public static String Intel_KBP_CLASSIFIER = getPath("Intel_KBP_CLASSIFIER");
    public static String INTEL_RELATION_CORP = getPath("INTEL_RELATION_CORP");
    public static String DEPARTMENT_TRAIN_PROPERTY = getPath("DEPARTMENT_TRAIN_PROPERTY");

    // Dataset
    public static String NER_LABELED_PATH = getPath("NER_LABELED_PATH");
    public static String MANUAL_LABEL_PATH = getPath("MANUAL_LABEL_PATH");
    public static String RELATION_EXTRACTION_PATH = getPath("RELATION_EXTRACTION_PATH");
    public static String COMPANY_PAGE_PATH = getPath("COMPANY_PAGE_PATH");
    public static String URL_LIST = getPath("URL_LIST");
    public static String RAW_PAGE_PATH = getPath("RAW_PAGE_PATH");
    public static String PROXY_URL = prop.getProperty("PROXY_URL");
    public static String PROXY_PORT = prop.getProperty("PROXY_PORT");


    // ensemble strategy
    public static IntelEnsembleStrategy ENSEMBLE_STRATEGY = IntelEnsembleStrategy.valueOf(prop.getProperty("ENSEMBLE_STRATEGY"));

    // separate former title or not
    public static boolean bSeprateFormerTitle = Boolean.parseBoolean(prop.getProperty("bSeprateFormerTitle"));

    // Filter Model
    public static String BAD_WORDS_FILE = getPath("BAD_WORDS_FILE");
    
    //get annotators
    public static String annotators = prop.getProperty("ANNOTATORS");
}
