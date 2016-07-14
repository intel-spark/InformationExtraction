package relation;

import edu.stanford.nlp.ie.machinereading.structure.EntityMention;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ie.machinereading.structure.RelationMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.RelationExtractorAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by yuhao on 6/10/16.
 */
public class RelationExtractor {

  static Properties props = StringUtils.argsToProperties();
  static StanfordCoreNLP pipeline = new StanfordCoreNLP();
  static RelationExtractorAnnotator r = new RelationExtractorAnnotator(props);

  public static void init() {
    props.setProperty("annotators", "parse,ner");
  }

  public static HashMap<String, String> extract(String sentence) {
      Annotation doc = new Annotation(sentence);
      pipeline.annotate(doc);
      r.annotate(doc);
      HashMap<String, String> map = new HashMap<String, String>();
      for(CoreMap s: doc.get(CoreAnnotations.SentencesAnnotation.class)){
        List<RelationMention> rls  = s.get(MachineReadingAnnotations.RelationMentionsAnnotation.class);
        for(RelationMention rl: rls){
          if(rl.getType().equals("Work_For")){
            System.out.println(rl);
            String organization = "";
            String people = "";
            for (EntityMention entity: rl.getEntityMentionArgs()){
              if(entity.getType().equals("ORGANIZATION")){
                organization = entity.getValue();
              }
              if(entity.getType().equals("PEOPLE")){
                people = entity.getValue();
              }
            }
            map.put(people, organization);
          }
        }
      }
      return map;
  }

  public static void main(String[] args){
    extract("Yuhao works for Intel");
  }
}
