package Test;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.RegexNERAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import com.intel.ie.analytics.IntelPaths;
import com.intel.ie.analytics.KBPModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class NerWithDepartmentTest {

    static Properties props = new Properties();
    static StanfordCoreNLP pipeline;
    static {
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,regexner");
        props.setProperty("regexner.mapping", "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelPaths.combined);
        props.setProperty("ner.model","model/english.all.3class.distsim.crf.ser.gz," +
                "model/english.muc.7class.distsim.crf.ser.gz," +
                "model/english.conll.4class.distsim.crf.ser.gz," +
                "model/intel-english.3class.distsim.crf.ser.gz,");
        pipeline = new StanfordCoreNLP(props);
    }

    public static void main(String[] args){
        File file = new File(args[0]);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;

            while ((line = reader.readLine()) != null) {
                extractNER(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
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
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                result.add(ne);
                System.out.println(word + "\t" + ne);
            }
        }
        return result;
    }


}
