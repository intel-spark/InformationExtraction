package edu.stanford.blp.ie;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class KBPModel {

    static Properties props = StringUtils.argsToProperties();
    static StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    static {
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,regexner,parse,mention,coref,kbp");
        props.setProperty("regexner.mapping", "ignorecase=true,validpospattern=^(NN|JJ).*,edu/stanford/nlp/models/kbp/regexner_caseless.tab;edu/stanford/nlp/models/kbp/regexner_cased.tab");
        pipeline = new StanfordCoreNLP(props);
    }

    public static void main(String[] args) throws IOException {

        IOUtils.console("sentence> ", line -> {
            RelationTriple[] triple = extract(line);
            for(RelationTriple s: triple){

                System.out.println(s);
            }
        });
    }

    public static RelationTriple[] extract(String doc) {

        Annotation ann = new Annotation(doc);
        pipeline.annotate(ann);
        ArrayList<RelationTriple> relations = new ArrayList<RelationTriple>();

        for (CoreMap sentence : ann.get(CoreAnnotations.SentencesAnnotation.class)) {
            for(RelationTriple r : sentence.get(CoreAnnotations.KBPTriplesAnnotation.class)){
                if(r.relationGloss().trim().equals("per:title")
                        || r.relationGloss().trim().equals("per:employee_of")
                        || r.relationGloss().trim().equals("org:top_members/employees")){
                    relations.add(r);
                }

//                if(r.relation.size() > 1){
//                    System.err.println("more than one relation in a triple: "+ r);
//                    System.exit(1);
//                }
//
//                for(CoreLabel t: r.relation){
//
//                    if(t.value().trim().equals("per:title")){
//                        title = getOriginalText(r.object);
//                    }
//                    if(t.value().trim().equals("per:employee_of")){
//                        name = getOriginalText(r.subject);
//                        organization = getOriginalText(r.object);
//                        source = sentence.toString();
//                    }
//                }
            }
        }
        return relations.toArray(new RelationTriple[0]);
    }

    private static String getOriginalText(List<CoreLabel> list){
        String str = "";
        for(CoreLabel cl: list){
            str += cl.originalText() + " ";
        }
        return str;
    }


}
