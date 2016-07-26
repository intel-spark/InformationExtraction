package Test;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;
import intel.analytics.IntelPaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by yuhao on 7/22/16.
 */
public class RegexNerTest {

    static Properties props = new Properties();
    static StanfordCoreNLP pipeline;
    static {
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
        String options2 = "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelPaths.Regex_NER_caseless + ";" + IntelPaths.Regex_NER_cased  + ";" + IntelPaths.Regex_NER_department_caseless;

        props.setProperty("regexner.mapping", options2);
        pipeline = new StanfordCoreNLP(props);
    }



    public static void main(String[] args){

        String doc = "Johny Srouji Senior Vice President Hardware Technologies";
        Annotation document = new Annotation(doc);

        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                System.out.println(ne);
            }

        }
    }

    public static List<String> extractNER(String doc){
        Annotation document = new Annotation(doc);

        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> result = new ArrayList<String>();
        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                result.add(ne);
            }
        }
        return result;

    }


}
