package preprocess.utils;

import java.io.IOException;
import java.util.*;

public class CreateNGrams {

    public static Map<String, Integer> createNGrams(Map<String, Integer> nGramFrequency, String string) throws IOException {

        String[] words = string.split(" ");
        int maxRange = 5;
        for (int i = 0; i < words.length-maxRange+1; i++) {
            List<String> buildPhrase = new ArrayList<>();
            for(int j = i; j < i+maxRange; j++) {
                buildPhrase.add(words[j]);
//                if (buildPhrase.size() > 1) {
                    String phrase = String.join(" ", buildPhrase);
                    if (nGramFrequency.get(phrase) == null) {
                        nGramFrequency.put(phrase, 1);
                    } else {
                        int tempInt = nGramFrequency.get(phrase);
                        tempInt++;
                        nGramFrequency.put(phrase, tempInt);
                    }
//                }
            }
        }

        return nGramFrequency;
    }
}
