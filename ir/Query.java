/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 *
 *   Edited by Hasti Mohebali Zadeh, 2023
 */

package ir;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


/**
 * A class for representing a query as a list of words, each of which has
 * an associated weight.
 */
public class Query
{

    /**
     * Help class to represent one query term, with its associated weight.
     */
    class QueryTerm
    {
        String term;

        // NEW
        // made it equal to 1 so it doesn't make a difference when searched with a query but changes for relevance feedback
        double weight = 1;

        QueryTerm(String t, double w)
        {
            term = t;
            weight = w;
        }
    }

    /**
     * Representation of the query as a list of terms with associated weights.
     * In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryTerms = new ArrayList<>();

    /**
     * Relevance feedback constant alpha (= weight of original query terms).
     * Should be between 0 and 1.
     * (only used in assignment 3).
     */
    double alpha = 0.2;

    /**
     * Relevance feedback constant beta (= weight of query terms obtained by
     * feedback from the user).
     * (only used in assignment 3).
     */
    double beta = 1 - alpha;


    /**
     * Creates a new empty Query
     */
    public Query()
    {
    }


    /**
     * Creates a new Query from a string of words
     */
    public Query(String queryString)
    {
        StringTokenizer tok = new StringTokenizer(queryString);
        while (tok.hasMoreTokens()) {
            queryTerms.add(new QueryTerm(tok.nextToken(), 1.0));
        }
    }


    /**
     * Returns the number of terms
     */
    public int size()
    {
        return queryTerms.size();
    }


    /**
     * Returns the Manhattan query length
     */
    public double length()
    {
        double len = 0;
        for (QueryTerm t : queryTerms) {
            len += t.weight;
        }
        return len;
    }


    /**
     * Returns a copy of the Query
     */
    public Query copy()
    {
        Query queryCopy = new Query();
        for (QueryTerm t : queryTerms) {
            queryCopy.queryTerms.add(new QueryTerm(t.term, t.weight));
        }
        return queryCopy;
    }


    /**
     * Expands the Query using Relevance Feedback
     *
     * @param results       The results of the previous query.
     * @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     * @param engine        The search engine object
     */

    /// NEW
    // assignment 3
    // 3.1
    public void relevanceFeedback(PostingsList results, boolean[] docIsRelevant, Engine engine)
    {
        // count the number of relevant documents
        int numOfRelevantDoc = 0;
        for (boolean isRelevant : docIsRelevant) {
            if (isRelevant) {
                numOfRelevantDoc++;
            }
        }
        // System.out.println("numOfRelevantDoc:" + numOfRelevantDoc);

        /*----- ROCCHIO ALGORITHM starts -----*/

        // map query terms and their UPDATED weights
        Map<String, Double> newQueryVector = new HashMap<>();

        // formula: alpha * initialQueryVector
        // Add "alpha * initial_query" to newQueryVector
        // initial query vector comes from the original queryTerms objects
        for (QueryTerm queryTerm : queryTerms) {
            newQueryVector.put(queryTerm.term, alpha * queryTerm.weight);
        }


        // topRetrievedDocs for 3.1:
        int topRetrievedDocs = 10; // Select two documents in the top ten list of retrieved documents

        // topRetrievedDocs for 3.2:
        // int topRetrievedDocs = docIsRelevant.length;

        // Calculate the weight for each word
        // formula: beta * relevantDocumentsCentroid
        // want to add "beta/numOfRelevantDoc" to newQueryVector
        double betaWeight = beta / numOfRelevantDoc;

        for (int i = 0; i < topRetrievedDocs; i++) {
            if (!docIsRelevant[i]) {
                continue; // go to next document if non-relevant
            }

            // if the document "i" is relevant:

            // get the relevant document (PE) "i" in the results list (PL)
            PostingsEntry entry = results.get(i);

            // Getting terms in the document from the index
            String docName = engine.index.docNames.get(entry.docID);

            // tokenize the document and get a list of words
            List<String> wordsList = getWords(docName);

            // for each word in this list of words
            for (String word : wordsList) {
                // update the weight in the newQueryVector by ADDING "betaWeight = beta / numOfRelevantDoc" to the existing weight
                newQueryVector.merge(word, betaWeight, Double::sum);
                /*
                if (newQueryVector.containsKey(word)) {
                    newQueryVector.put(word, newQueryVector.get(word) + weight);
                }
                else {
                    newQueryVector.put(word, weight);
                }
                 */
            }
        }

        /*--- UPDATING queryTerms ---*/
        // update the queryTerms object with the terms and their adjusted weights from the newQueryVector
        //  converting the key-value pairs from the newQueryVector map into a list of QueryTerm objects
        queryTerms = (ArrayList<QueryTerm>) newQueryVector.entrySet().stream()
                .map(q -> new QueryTerm(q.getKey(), q.getValue()))
                .collect(Collectors.toList());
    }

    /* extracting words from a document file */
    public ArrayList<String> getWords(String docName) {
        ArrayList<String> words = new ArrayList<>();
        try {
            Reader reader = new InputStreamReader(new FileInputStream(docName), StandardCharsets.UTF_8);
            Tokenizer tokenizer = new Tokenizer(reader, true, false, true,
                "C:\\Users\\HP\\Documents\\courses\\Search Engines and Information Retrieval systems - DD2477\\Assignments\\SearchEngine-Assignment\\assignment1\\patterns.txt"
            );
            while (tokenizer.hasMoreTokens()) {
                words.add(tokenizer.nextToken());
            }
        } catch (Exception e) {
            System.err.println( "Warning: IOException during indexing." );
        }
        return words;
    }

}


