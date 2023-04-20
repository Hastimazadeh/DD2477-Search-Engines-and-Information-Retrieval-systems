/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Dmytro Kalpakchi, 2018
 *
 *   Edited by Hasti Mohebali Zadeh, 2023
 */

package ir;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;


public class KGramIndex
{

    /**
     * Mapping from term ids to actual term strings
     */
    HashMap<Integer, String> id2term = new HashMap<>();

    /**
     * Mapping from term strings to term ids
     */
    HashMap<String, Integer> term2id = new HashMap<>();

    /**
     * Index from k-grams to list of term ids that contain the k-gram
     */
    HashMap<String, List<KGramPostingsEntry>> index = new HashMap<>();

    /**
     * The ID of the last processed term
     */
    int lastTermID = -1;

    /**
     * Number of symbols to form a K-gram
     */
    int K = 3;

    public KGramIndex(int k)
    {
        K = k;
        if (k <= 0) {
            System.err.println("The K-gram index can't be constructed for a negative K value");
            System.exit(1);
        }
    }

    /**
     * Generate the ID for an unknown term
     */
    private int generateTermID()
    {
        return ++lastTermID;
    }

    public int getK()
    {
        return K;
    }

    /* --------------------------------------------------------- */
    // 3.3 - part 1
    // the following 3 methods were there but empty


    /**
     * Get intersection of two postings lists
     */
    private List<KGramPostingsEntry> intersect(List<KGramPostingsEntry> p1, List<KGramPostingsEntry> p2) {
        // a list containing the common elements (combined postings entries) between p1 and p2 based on their tokenID field
        List<KGramPostingsEntry> combinedPL = new ArrayList<>();

        // pointers to iterate over p1 and p2
        int i = 0;
        int j = 0;

        // if either p1 or p2 is empty, return empty list (no intersect)
        if (p1 == null || p2 == null)
            return combinedPL;

        // iterate until either i reaches end of p1 or j reaches end of p2
        while (i < p1.size() && j < p2.size()) {

            // compare the tokenID of p1 and p2 (KGramPostingsEntry objects) at the current positions of i and j
            // If the tokenIDs are equal
            if (p1.get(i).tokenID == p2.get(j).tokenID) {
                // create a new KGramPostingsEntry object with the tokenID value,
                KGramPostingsEntry matchingKGPE = new KGramPostingsEntry(p1.get(i).tokenID);
                // add it to the combinedPL list
                combinedPL.add(matchingKGPE);
                // increment both i and j
                i++;
                j++;
            }

            // If the tokenID value in p1 is less than the tokenID value in p2
            else if (p1.get(i).tokenID < p2.get(j).tokenID) {
                // increment i to move to the next element in p1
                i++;
            }

            // If the tokenID value in p1 is greater than the tokenID value in p2
            else {
                // increment j to move to the next element in p2
                j++;
            }
        }

        return combinedPL;
    }



    /**
     * Inserts all k-grams from a token into the index.
     */

    /// inserting a given token (term) and its corresponding k-grams into the index
    public void insert(String token) {

        // check if the token already exists in the index (to avoid duplicate entries in the index)
        // If the term ID is not null (token already exists in the index)
        if (getIDByTerm(token) != null) {
            return; // return without doing anything (don't insert the token into the index)
        }

        /*-- If the token does not exist in the index: --*/

        // generate a new term ID for the token
        // generateTermID(): increments the lastTermID counter and returns the new term ID
        int newID = generateTermID();

        // create a new KGramPostingsEntry object with the new term ID
        // stores the postings entries (the list of term IDs) for the k-grams extracted from the token
        KGramPostingsEntry kgPE = new KGramPostingsEntry(newID);

        // add/mark the start and end symbols to the token
        // so that k-grams that span across the start or end of the token can be captured
        String modifiedToken = "^" + token + "$";

        // add it to the mapping between the ORIGINAL TOKEN and its NEW ID in both ways
        term2id.put(token, newID);
        id2term.put(newID, token);


        String extractedKGram;
        int tokenLen = token.length();

        /* want to extract all the k-grams from the token
        *  a word of length n: n+3‒k k-grams // getK(): length of the k-gram */

        // iterate over the chars of the token to the (tokenLen + 3 - getK())th pos
        // for each pos in the token (that k-gram is being extracted):
        for (int i = 0; i < (tokenLen + 3 - getK()); i++) {
            // extract the k-gram from the MODIFIED token (extracts a substring of length "k" starting from "i")
            // substring: takes the start and end positions of the substring and returns the corresponding substring
            extractedKGram = modifiedToken.substring(i, i + getK());

            // if index doesn't have the extracted k-gram
            if (!index.containsKey(extractedKGram)) {
                // add a new entry in the index map with the k-gram as the key <<AND>> an empty list as the value
                index.put(extractedKGram, new ArrayList<KGramPostingsEntry>());
            }

            // if the extracted k-gram's list of PEs in the index map doesn't have KGramPostingsEntry object aka "kgPE"
            /* if KGramPostingsEntry object aka "kgPE" (with the new term ID)
             doesn't already exist in the list of PEs for the k-gram in the index map */
            if (!index.get(extractedKGram).contains(kgPE)) {
                // then add it
                index.get(extractedKGram).add(kgPE);
            }
        }
    }

    /**
     * Get postings for the given k-gram
     */
    public List<KGramPostingsEntry> getPostings(String kgram)
    {
        // check if the given k-gram exists in the index map
        if (index.containsKey(kgram)) {
            // return the corresponding postings list associated with that k-gram
            return index.get(kgram);
        } else {
            return null;
        }
    }

    /* --------------------------------------------------------- */

    /**
     * Get id of a term
     */
    public Integer getIDByTerm(String term)
    {
        return term2id.get(term);
    }

    /**
     * Get a term by the given id
     */
    public String getTermByID(Integer id)
    {
        return id2term.get(id);
    }

    private static HashMap<String, String> decodeArgs(String[] args)
    {
        HashMap<String, String> decodedArgs = new HashMap<>();
        int i = 0, j = 0;
        while (i < args.length) {
            if ("-p".equals(args[i])) {
                i++;
                if (i < args.length) {
                    decodedArgs.put("patterns_file", args[i++]);
                }
            } else if ("-f".equals(args[i])) {
                i++;
                if (i < args.length) {
                    decodedArgs.put("file", args[i++]);
                }
            } else if ("-k".equals(args[i])) {
                i++;
                if (i < args.length) {
                    decodedArgs.put("k", args[i++]);
                }
            } else if ("-kg".equals(args[i])) {
                i++;
                if (i < args.length) {
                    decodedArgs.put("kgram", args[i++]);
                }
            } else {
                System.err.println("Unknown option: " + args[i]);
                break;
            }
        }
        return decodedArgs;
    }

    /* --------------------------------------------------------- */

    // 3.3 - part 2
    // the following 2 methods did not exist before and were added

    /* reads the contents of the file, tokenizes it and inserts the tokens into a k-gram index */
    /* This method is used in Engine class (L 113) in while it's indexing and is taken from main method */
    public void init(String kGramTestFile, String patternsFile)
    {
        try {
            File f = new File(kGramTestFile);
            Reader reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
            Tokenizer tok = new Tokenizer(reader, true, false, true, patternsFile);
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                insert(token);
                // kgIndex.insert(token);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize k-gram index");
        }
    }
    /* searches a k-gram index for matching terms based on the input kgrams array, and prints the results */
    /* This method is used in Searcher class (L 54) and is taken from main method */
    // translate: String[] kgrams = args.get("kgram").split(" ");
    public void searchKG(String[] kgrams)
    {
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgrams) {
            if (kgram.length() != K) {
                System.err.println("Cannot search k-gram index: " + kgram.length() + "-gram provided instead of " + K + "-gram");
                return;
            }

            if (postings == null) {
                postings = getPostings(kgram);
            } else {
                postings = intersect(postings, getPostings(kgram));
            }
        }
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int showTopResults = 10;
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s).");

            // PRINT THIS OUT if needed
            if (resNum > showTopResults) {
                System.err.println("The first " + showTopResults + " of them are:");
                resNum = showTopResults;
            }

            for (int i = 0; i < resNum; i++) {
                System.err.println( (i + 1) + ": " + getTermByID(postings.get(i).tokenID));
            }
        }
    }
    /* --------------------------------------------------------- */

    public static void main(String[] arguments) throws FileNotFoundException, IOException
    {
        HashMap<String, String> args = decodeArgs(arguments);

        int k = Integer.parseInt(args.getOrDefault("k", "3"));
        KGramIndex kgIndex = new KGramIndex(k);

        // init STARTS
        File f = new File(args.get("file"));
        Reader reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
        Tokenizer tok = new Tokenizer(reader, true, false, true, args.get("patterns_file"));
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            kgIndex.insert(token);///////
        }
        // init ENDS

        String[] kgrams = args.get("kgram").split(" ");////////

        // searchKG STARTS
        List<KGramPostingsEntry> postings = null;
        for (String kgram : kgrams) {
            if (kgram.length() != k) {
                System.err.println("Cannot search k-gram index: " + kgram.length() + "-gram provided instead of " + k + "-gram");
                System.exit(1);
            }
            if (postings == null) {
                postings = kgIndex.getPostings(kgram);
            } else {
                postings = kgIndex.intersect(postings, kgIndex.getPostings(kgram));
            }
        }
        if (postings == null) {
            System.err.println("Found 0 posting(s)");
        } else {
            int resNum = postings.size();
            System.err.println("Found " + resNum + " posting(s)");
            if (resNum > 10) {
                System.err.println("The first 10 of them are:");
                resNum = 10;
            }
            for (int i = 0; i < resNum; i++) {
                System.err.println(kgIndex.getTermByID(postings.get(i).tokenID));
            }
        }
        // searchKG ENDS
    }
}
