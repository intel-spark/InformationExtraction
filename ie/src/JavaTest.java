import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ie.machinereading.structure.RelationMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.RelationExtractorAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.util.List;
import java.util.Properties;

/**
 * Created by yuhao on 6/10/16.
 */
public class JavaTest {

  public static void main(String[] args){
    try{
      Properties props = StringUtils.argsToProperties(args);
//      props.setProperty("annotators", "parse,ner");
      StanfordCoreNLP pipeline = new StanfordCoreNLP();
      String sentence = "Stacy J. Smith is executive vice president and chief financial " +
        "officer (CFO) for Intel Corporation. In this role, he is responsible for leading the " +
        "Worldwide Finance Organization and overseeing Finance, accounting and reporting, Tax, Treasury," +
        " Internal Audit, Investor Relations, Intel Capital and Information Technology.";
      Annotation doc = new Annotation(sentence);
      pipeline.annotate(doc);


      RelationExtractorAnnotator r = new RelationExtractorAnnotator(props);
      r.annotate(doc);
      for(CoreMap s: doc.get(CoreAnnotations.SentencesAnnotation.class)){
        System.out.println("For sentence " + s.get(CoreAnnotations.TextAnnotation.class));
        List<RelationMention> rls  = s.get(MachineReadingAnnotations.RelationMentionsAnnotation.class);
        for(RelationMention rl: rls){
          System.out.println(rl.toString());
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}
