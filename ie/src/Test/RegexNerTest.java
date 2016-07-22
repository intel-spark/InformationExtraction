package Test;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;
import intel.analytics.IntelPaths;

import java.util.List;
import java.util.Properties;

/**
 * Created by yuhao on 7/22/16.
 */
public class RegexNerTest {

    public static void main(String[] args){
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
        String options = "ignorecase=true,validpospattern=^(NN|JJ).*,data/RegexNER/regexner_caseless.tab;data/RegexNER/regexner_cased.tab";
        String options2 = "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelPaths.Regex_NER_caseless + ";" + IntelPaths.Regex_NER_cased;

        props.setProperty("regexner.mapping", options2);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String doc = "Ashley Bacon Chief Risk Officer";
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
}
