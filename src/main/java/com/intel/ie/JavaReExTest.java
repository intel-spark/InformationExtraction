package com.intel.ie;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.RelationExtractorAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.logging.Redwood;

import java.util.*;

import edu.stanford.nlp.ie.machinereading.BasicRelationExtractor;
import edu.stanford.nlp.ie.machinereading.Extractor;
import edu.stanford.nlp.ie.machinereading.MachineReading;
import edu.stanford.nlp.ie.machinereading.domains.roth.RothCONLL04Reader;
import edu.stanford.nlp.ie.machinereading.domains.roth.RothEntityExtractor;
import edu.stanford.nlp.ie.machinereading.structure.EntityMention;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations.RelationMentionsAnnotation;
import edu.stanford.nlp.ie.machinereading.structure.RelationMention;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

/**
 * Created by yuhao on 7/5/16.
 */
public class JavaReExTest {

  public static void main(String[] args){
    try{
      Properties props = StringUtils.argsToProperties(args);
//      props.setProperty("annotators", "tokenize,ssplit,lemma,pos,parse,ner");
      StanfordCoreNLP pipeline = new StanfordCoreNLP();
      String sentence = "John Gerspach was named Chief Financial Officer of Citi in July 2009.";
      Annotation doc = new Annotation(sentence);
      pipeline.annotate(doc);
      RelationExtractorAnnotator r = new RelationExtractorAnnotator(props);
      r.annotate(doc);

      for(CoreMap s: doc.get(CoreAnnotations.SentencesAnnotation.class)){
        System.out.println("For sentence " + s.get(CoreAnnotations.TextAnnotation.class));
        List<RelationMention> rls  = s.get(RelationMentionsAnnotation.class);
        for(RelationMention rl: rls){
          System.out.println(rl.toString());
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }


}
