package com.intel.ie.analytics;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


public class IntelKBPModel {

    static Properties props = StringUtils.argsToProperties();
    static StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    static {

        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,regexner,parse,mention,coref");
        props.setProperty("regexner.mapping", "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelPaths.combined);
        props.setProperty("ner.model","model/english.all.3class.distsim.crf.ser.gz," +
                "model/english.muc.7class.distsim.crf.ser.gz," +
                "model/english.conll.4class.distsim.crf.ser.gz," +
                "model/new-model.ser.gz,");

        pipeline = new StanfordCoreNLP(props);
        pipeline.addAnnotator(new IntelKBPAnnotator("kbp", props));
    }

    public static void main(String[] args) throws IOException {

        IOUtils.console("sentence> ", line -> {
            HashMap<RelationTriple, String> triple = extract(line);
            for (RelationTriple s: triple.keySet()){
                System.out.println(s);
            }
        });
    }

    public static HashMap<RelationTriple, String> extract(String doc) {

        Annotation ann = new Annotation(doc);
        pipeline.annotate(ann);
        HashMap<RelationTriple, String> relations = new HashMap<RelationTriple, String>();

        for (CoreMap sentence : ann.get(CoreAnnotations.SentencesAnnotation.class)) {
            for(RelationTriple r : sentence.get(CoreAnnotations.KBPTriplesAnnotation.class)){
                if(r.relationGloss().trim().equals("per:title")
                        || r.relationGloss().trim().equals("per:employee_of")
                        || r.relationGloss().trim().equals("org:top_members/employees")){
                    relations.put(r, sentence.toString());
                }
            }
        }
        return relations;
    }

    private static String getOriginalText(List<CoreLabel> list){
        String str = "";
        for(CoreLabel cl: list){
            str += cl.originalText() + " ";
        }
        return str;
    }
}
