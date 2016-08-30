package com.intel.ie.evaluation.preparation

import java.util.Properties

import com.intel.ie.analytics.IntelKBPConfig
import edu.stanford.nlp.pipeline.StanfordCoreNLP

/**
  * Created by xianyan on 7/28/16.
  */
object NerHelper {

  val props: Properties = new Properties
  var pipeline: StanfordCoreNLP = null
  props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner")
  val options2 = "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelKBPConfig.Regex_NER_caseless + ";" + IntelKBPConfig.Regex_NER_cased

  props.setProperty("regexner.mapping", options2)
  pipeline = new StanfordCoreNLP(props)

}
