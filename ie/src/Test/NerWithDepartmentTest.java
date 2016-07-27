package Test;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.RegexNERAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;
import intel.analytics.IntelPaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class NerWithDepartmentTest {

    static Properties props = new Properties();
    static StanfordCoreNLP pipeline;
    static {
//        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
        props.put("annotators", "tokenize, ssplit, pos, lemma");
//        String options2 = "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelPaths.Regex_NER_caseless + ";" + IntelPaths.Regex_NER_cased;
//        props.setProperty("regexner.mapping", options2);
        pipeline = new StanfordCoreNLP(props);

        props.setProperty("ner.model","edu/stanford/nlp/models/ner/apple-model.ser.gz,edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz," +
                "edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz,edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz");
        pipeline.addAnnotator(new NERCombinerAnnotator("ner", props));

        String options2 = "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelPaths.Regex_NER_caseless + ";" + IntelPaths.Regex_NER_cased;
        props.setProperty("regexner.mapping", options2);
        pipeline.addAnnotator(new RegexNERAnnotator("regexNer", props));
    }



    public static void main(String[] args){

//        String doc = "Johny Senior Vice President Human Resource";
        String doc = "Angela Ahrendts is Senior Vice President Retail and Online Stores";
//        String doc =
//                "Eddy Cue Senior Vice President Internet Software and Services\n" +
//                "Craig Federighi Senior Vice President Software Engineering\n" +
//                "Jonathan Ive Chief Design Officer\n" +
//                "Johny Srouji Senior Vice President Hardware Technologies\n" +
//                "Luca Maestri Senior Vice President and Chief Financial Officer\n" +
//                "Dan Riccio Senior Vice President Hardware Engineering\n" +
//                "Philip W. Schiller Senior Vice President Worldwide Marketing\n" +
//                "Bruce Sewell Senior Vice President and General Counsel\n" +
//                "Jeff Williams Chief Operating Officer\n" +
//                "Angela Ahrendts is Senior Vice President Retail and Online Stores";
        Annotation document = new Annotation(doc);

        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                System.out.println(word + ": " + ne);
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
