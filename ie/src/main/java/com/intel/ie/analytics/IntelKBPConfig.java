package com.intel.ie.analytics;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class IntelKBPConfig {
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

    public static IntelEnsembleStrategy ENSEMBLE_STRATEGY = IntelEnsembleStrategy.valueOf(prop.getProperty("ENSEMBLE_STRATEGY"));

    public static boolean bSeprateFormerTitle = Boolean.parseBoolean(prop.getProperty("bSeprateFormerTitle"));
}
