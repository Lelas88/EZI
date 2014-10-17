import java.util.*;

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
    static String query = "machine learning for data mining";
//    static String query = "information retrieval";
    static String stemmedQuery;
    static int index = 0;

    public static void main(String[] args) {
        data = new Data();
        stemmer = new Stemmer();
        documents = data.readDocuments();
        keywords = data.readKeywords();

        final List<Double> tfidfValues = tfidf(documents, keywords, query);

        Map<String, Double> documentsWithTFIDF = new HashMap<String, Double>();
        for(int i = 0; i<tfidfValues.size(); i++) {
            documentsWithTFIDF.put(documents.get(i), tfidfValues.get(i));
        }
        Map<String, Double> sortedDocuments = sortByTFIDF(documentsWithTFIDF);
        System.out.println(sortedDocuments + "\n");
    }

    private static Map<String,Double> sortByTFIDF(Map<String, Double> documents) {
        List list = new LinkedList(documents.entrySet());
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry)o2).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    private static List<Double> tfidf(List<String> documentsList, List<String> keywordsList, String userQuery) {
        stemmedDocuments = generateStemmedList(documents);
        stemmedKeywords = generateStemmedList(keywords);

        //stemming Algorithm for users query
        stemmedQuery = stemming(query);

        //--------------------------------------------------------------------------------------------------------------
        //binary representation of stemmed documents (number of each keyword occurrence in each document)
        List<List<Integer>> listOfDocumentTerms = new ArrayList<List<Integer>>();
        List<Integer> documentsBinaryRepresentation;
        for(String document : stemmedDocuments) {
            documentsBinaryRepresentation = generateBinaryVectors(document, stemmedKeywords);
            listOfDocumentTerms.add(documentsBinaryRepresentation);
        }

        //binary representation of a stemmed query (number of each keyword occurrence in users query)
        List<Integer> queryBinaryVector = generateBinaryVectors(stemmedQuery, stemmedKeywords);

        //documents TF representation
        List<List<Double>> documentsTFVectors = new ArrayList<List<Double>>();
        List<Double> documentsTFSingleVector;
        for(List<Integer> term : listOfDocumentTerms) {
            documentsTFSingleVector = generateTFRepresentation(term);
            documentsTFVectors.add(documentsTFSingleVector);
        }

        //binary representation of a stemmed query (number of each keyword occurrence in users query)
        List<Double> queryTFSingleVector = generateTFRepresentation(queryBinaryVector);

        //--------------------------------------------------------------------------------------------------------------
        //IDF query representation
        List<Double> queryIDFRepresentation = generateQueryIDFRepresentation(queryTFSingleVector, documentsTFVectors);

        //IDF vector for users query
        List<Double> queryIDFVector = generateQueryIDFVector(queryTFSingleVector, documentsTFVectors);

        //IDF total value for query
        double queryTotalIDF = 0;
        for(Double idf : queryIDFVector) {
            queryTotalIDF += Math.pow(idf, 2);
        }
        queryTotalIDF = Math.sqrt(queryTotalIDF);

        //--------------------------------------------------------------------------------------------------------------
        //IDF vectors for documents
        double idf = 0;
        List<List<Double>> documentsIDFsVectors = new ArrayList<List<Double>>();
        List<Double> docIDFSingleVector = new ArrayList<Double>();
        for(List<Double> documentTFVector : documentsTFVectors) {
            for(Double singleTFDocumentValue : documentTFVector) {
                idf = singleTFDocumentValue * queryIDFRepresentation.get(index);
                docIDFSingleVector.add(idf);
                idf = 0;
                index++;
            }
            documentsIDFsVectors.add(docIDFSingleVector);
            docIDFSingleVector = new ArrayList<Double>();
            index = 0;
        }

        //IDF values for documents
        double documentTotalIDFValue = 0;
        List<Double> documentsTotalIDFs = new ArrayList<Double>();
        for(List<Double> docIDFVector : documentsIDFsVectors) {
            for(Double docIDFValue : docIDFVector) {
                documentTotalIDFValue += Math.pow(docIDFValue, 2);
            }
            documentTotalIDFValue = Math.sqrt(documentTotalIDFValue);
            documentsTotalIDFs.add(documentTotalIDFValue);
            documentTotalIDFValue = 0;
        }

        //--------------------------------------------------------------------------------------------------------------
        //cosine similarity measure
        List<Double> simDocsList = generateSimValues(queryIDFVector, documentsIDFsVectors, queryTotalIDF, documentsTotalIDFs);
        return simDocsList;
    }

    private static List<Double> generateQueryIDFRepresentation(List<Double> queryTFVector, List<List<Double>> docTFVectors) {
        List<Double> queryIDFs = new ArrayList<Double>();
        int numberOfDocuments = documents.size();
        int documentsWithInformation = 0;
        double idf = 0;
        int index = 0;

        for(Double queryTSValue : queryTFVector) {
            for(List<Double> docTFVector : docTFVectors) {
                if(docTFVector.get(index) > 0) {
                    documentsWithInformation++;
                }
            }
            idf = documentsWithInformation != 0 ? Math.log10(numberOfDocuments / documentsWithInformation) : 0;
            queryIDFs.add(idf);
            documentsWithInformation = 0;
            index++;
        }

        return queryIDFs;
    }

    private static List<Double> generateSimValues(List<Double> queryIDFVector, List<List<Double>> documentIDFVectors, Double queryTotalIDF, List<Double> documentsTotalIDFs) {
        List<Double> denominatorsList = new ArrayList<Double>();
        List<Double> nominatorsList = new ArrayList<Double>();
        List<Double> simList = new ArrayList<Double>();
        double sim = 0;
        double denominator = 0;
        double nominator = 0;
        int index = 0;

        for(List<Double> documentIDFVector : documentIDFVectors) {
            for(Double documentIDFValue : documentIDFVector) {
                denominator += documentIDFValue * queryIDFVector.get(index);
                index++;
            }
            denominatorsList.add(denominator);
            denominator = 0;
            index = 0;
        }

        index = 0;
        for(Double documentTotalIDF : documentsTotalIDFs) {
            nominator = documentTotalIDF == 0 || queryTotalIDF == 0 ? 0 :documentTotalIDF * queryTotalIDF;
            sim = denominatorsList.get(index) == 0 ? 0 : denominatorsList.get(index) / nominator;
            simList.add(sim);
            index++;
        }

        return simList;
    }

    private static List<Double> generateQueryIDFVector(List<Double> queryTFVector, List<List<Double>> docTFVectors) {
        List<Double> queryIDFs = new ArrayList<Double>();
        int documentsWithInformation = 0;
        int numberOfDocuments = documents.size();
        double idf = 0;
        int index = 0;

        queryIDFs = generateQueryIDFRepresentation(queryTFVector, docTFVectors);

        index = 0;
        for(Double idfValue : queryIDFs) {
            idfValue *= queryTFVector.get(index);
            queryIDFs.set(index, idfValue);
            index++;
        }

        return queryIDFs;
    }

    private static List<Double> generateTFRepresentation(List<Integer> vector) {
        List<Double> tfVector = new ArrayList<Double>();
        int max = 0;
        double tfValue;

        for(Integer element : vector)
            if (max < element) max = element;

        for(Integer element : vector) {
            if(element != 0) {
                tfValue = (double)element / (double)max;
                tfVector.add(tfValue);
            } else {
                tfVector.add(0.0);
            }
        }
        return tfVector;
    }

    private static List<Integer> generateBinaryVectors(String doc, List<String> keywords) {
        List<Integer> binaryList = new ArrayList<Integer>();
        Integer occurrence = 0;

        for(String keyword : keywords) {
            String[] words = doc.split(" ");
            for(String word : words) {
                if(keyword.equals(word)) occurrence++;
            }
            binaryList.add(occurrence);
            occurrence = 0;
        }

        return binaryList;
    }

    private static List<String> generateStemmedList(List<String> list) {
        List<String> stemmedList = new ArrayList<String>();
        List<String> clearList = clearNonCharacters(list);
        String stemmedSentence = "";

        for(String listElement : clearList) {
            stemmedSentence = stemming(listElement);
            stemmedList.add(stemmedSentence);
//            System.out.println(stemmedSentence);
            stemmedSentence = "";
        }

        return stemmedList;
    }

    private static String stemming(String listElement) {
        String stemmedSentence = "";
        String[] words = listElement.split(" ");
        for(String word : words) {
            word = word.toLowerCase();
            char[] letters = word.toCharArray();
            stemmer.add(letters, letters.length);
            stemmer.stem();

            if(stemmedSentence == "")
                stemmedSentence += stemmer.toString();
            else
                stemmedSentence += " " + stemmer.toString();
        }
        return stemmedSentence;
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
