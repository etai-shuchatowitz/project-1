package preprocess.utils;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class StanfordNLPClient {

    public static void annotateDocument(String text)  {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        props.setProperty("ner.useSUTime", "false"); // hack for StanfordNLP 3.9.1

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);

        // System.out.println(tokensAndNERTags);
    }



}
