import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert Mastalerek on 2014-10-11.
 */
public class Main {
    static List<String> documents;
    static List<String> keywords;
    static List<String> stemmedDocuments;
    static List<String> stemmedKeywords;
    static Stemmer stemmer;
    static Data data;

    public static void main(String[] args) {
        data = new Data();
        stemmer = new Stemmer();
        documents = data.readDocuments();
        keywords = data.readKeywords();

        stemmedDocuments = stemming(documents);
        stemmedKeywords = stemming(keywords);
    }

    private static List<String> stemming(List<String> list) {
        List<String> stemmedList = new ArrayList<String>();
        List<String> clearList = clearNonCharacters(list);
        String stemmedSentence = "";

        for(String listElement : clearList) {
            String[] words = listElement.split(" ");
            for(String word : words) {
                word = word.toLowerCase();
                char[] letters = word.toCharArray();
                stemmer.add(letters, letters.length);
                stemmer.stem();
                stemmedSentence += " " + stemmer.toString();
            }
            stemmedList.add(stemmedSentence);
            System.out.println(stemmedSentence);
            stemmedSentence = "";
        }

        return stemmedList;
    }

    private static List<String> clearNonCharacters(List<String> list) {
        List<String> clearList = new ArrayList<String>();
        String clearSentence = "";

        for(String listElement : list) {
            char[] letters = listElement.toCharArray();
            for(char character : letters) {
                if(!Character.isLetter(character)) {
                    character = ' ';
                }
                clearSentence += character;
            }
            clearSentence = clearSentence.replaceAll("\\s+", " ").trim();
            clearList.add(clearSentence);
            clearSentence = "";
        }
        return clearList;
    }
}
