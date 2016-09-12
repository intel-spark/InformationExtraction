package com.intel.ie.analytics;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


public class IntelKBPModel {

    static Properties props = StringUtils.argsToProperties();
    static StanfordCoreNLP pipeline = null;

    static {
        props.setProperty("annotators", IntelConfig.annotators);
        props.setProperty("regexner.mapping", "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelConfig.combined);
        props.setProperty("ner.model", IntelConfig.NER_MODELS);

        pipeline = new StanfordCoreNLP(props);
        pipeline.addAnnotator(new IntelKBPAnnotator("kbp", props));
    }

    public static void main(String[] args) throws IOException {

        IOUtils.console("sentence> ", line -> {
            HashMap<RelationTriple, String> triple = extract(line);
            for (RelationTriple s : triple.keySet()) {
                System.out.println(s);
            }
        });
    }

    public static HashMap<RelationTriple, String> extract(String doc) {

        Annotation ann = new Annotation(doc
                .replaceAll("\u00a0", " ")
                .replaceAll("\u200B|\u200C|\u200D|\uFEFF", ""));
        pipeline.annotate(ann);
        HashMap<RelationTriple, String> relations = new HashMap<RelationTriple, String>();

        for (CoreMap sentence : ann.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (RelationTriple r : sentence.get(CoreAnnotations.KBPTriplesAnnotation.class)) {
                if (r.relationGloss().trim().equals("per:title")
                        || r.relationGloss().trim().equals("per:employee_of")
                        || r.relationGloss().trim().equals("org:top_members/employees")
                        || r.relationGloss().trim().equals("per:former_title")) {
                    relations.put(r, sentence.toString());
                }
            }
        }
        return relations;
    }

    private static String getOriginalText(List<CoreLabel> list) {
        String str = "";
        for (CoreLabel cl : list) {
            str += cl.originalText() + " ";
        }
        return str;
    }
}
