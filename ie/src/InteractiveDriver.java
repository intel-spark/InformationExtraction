import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class InteractiveDriver {

    public static void main(String[] args) throws IOException {
        Properties props = StringUtils.argsToProperties(args);
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,regexner,parse,mention,coref,kbp");
        props.setProperty("regexner.mapping", "ignorecase=true,validpospattern=^(NN|JJ).*,edu/stanford/nlp/models/kbp/regexner_caseless.tab;edu/stanford/nlp/models/kbp/regexner_cased.tab");
        Set<String> interested = Stream.of("per:title", "per:employee_of", "org:top_members/employees").collect(Collectors.toSet());
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        IOUtils.console("sentence> ", line -> {
            Annotation ann = new Annotation(line);
            pipeline.annotate(ann);
            for (CoreMap sentence : ann.get(CoreAnnotations.SentencesAnnotation.class)) {
                sentence.get(CoreAnnotations.KBPTriplesAnnotation.class).forEach(r -> {
                     String relation = r.relationGloss();
                    if(interested.contains(relation)) {
                        System.err.println(r);
                    }
                });
            }
        });
    }

}
