import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert Mastalerek on 2014-10-11.
 */
public class Data {

    public List<String> readDocuments() {
        List<String> documents = new ArrayList<String>();
        int counter = 0;

        try {
            File file = new File("./src/data/documents.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            int index = 0;
            String line;
            String document = "";

            while ((line = br.readLine()) != null) {
                if(line.length() > 0) {
                    document += "\n" + line;
                } else {
                    documents.add(index, document);
                    document = "";
                    index++;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Number of documents read: " + documents.size());

        return documents;
    }

    public List<String> readKeywords() {
        List<String> keywords = new ArrayList<String>();

        try {
            File file = new File("./src/data/keywords.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                keywords.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Number of keywords read: " + keywords.size());

        return keywords;
    }
}
