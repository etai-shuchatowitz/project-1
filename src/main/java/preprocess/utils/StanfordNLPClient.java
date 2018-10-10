package preprocess.utils;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class StanfordNLPClient {

    public static String annotateDocument(String text)  {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        props.setProperty("ner.useSUTime", "false"); // hack for StanfordNLP 3.9.1

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);

        StringBuffer stringBuffer = new StringBuffer();

        doc.tokens().forEach(token -> {
           stringBuffer.append(token.lemma() + " ");
        });

        return stringBuffer.toString().trim();
    }

    public static void main(String[] args) {
        System.out.println(annotateDocument("i will have so much fun today"));
    }



}
