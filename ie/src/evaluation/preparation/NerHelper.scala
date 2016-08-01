package evaluation.preparation

import java.util.Properties

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import intel.analytics.IntelPaths

/**
  * Created by xianyan on 7/28/16.
  */
object NerHelper {

  val props: Properties = new Properties
  var pipeline: StanfordCoreNLP = null
  props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner")
  val options2 = "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelPaths.Regex_NER_caseless + ";" + IntelPaths.Regex_NER_cased

  props.setProperty("regexner.mapping", options2)
  pipeline = new StanfordCoreNLP(props)

}
