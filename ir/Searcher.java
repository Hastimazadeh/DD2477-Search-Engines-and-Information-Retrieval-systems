/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 *
 *   Edited by Hasti Mohebali Zadeh, 2023
 */

package ir;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Searches an index for results of a query.
 */
public class Searcher {
    /**
     * The index to be searched by this Searcher.
     */
    final Index index;

    /**
     * The k-gram index to be searched by this Searcher
     */
    final KGramIndex kgIndex;

    /**
     * Constructor
     */
    public Searcher(Index index, KGramIndex kgIndex) {
        this.index = index;
        this.kgIndex = kgIndex;
    }
    // for 2.5.2
    double w = 0.005;

    /**
     * Searches the index for postings matching the query.
     *
     * @return A postings list representing the result of the query.
     */
    public PostingsList search(Query query, QueryType queryType, RankingType rankingType, NormalizationType normType) {

        // 3.3 so that we get the k-gram from search engine
        /* each query term is converted into its corresponding k-grams
        ** retrieves postings associated with the k-grams from the k-gram index*/

        // translate: String[] kgrams = args.get("kgram").split(" ");
        String[] kg = query.queryTerms.stream()// get the query terms of the query
                .map(queryTerm -> queryTerm.term) // map to term
                .toArray(String[]::new); // put the terms in array

        kgIndex.searchKG(kg);

        /* --------------------------------------------- */

        final List<PostingsList> postings = getPostings(query.queryTerms);

        if (postings.isEmpty())
            return null;

        PostingsList result = null;

        switch (queryType) {
            case INTERSECTION_QUERY: { // assignment 1
                result = handleQuery(postings, this::intersect);
                break;
            }
            case PHRASE_QUERY: { // assignment 1
                result = handleQuery(postings, this::positionalIntersect);
                break;
            }
            case RANKED_QUERY: { // assignment 2
                String filePath = "PagerankScore.txt";
                readPagerank(filePath);

                switch (rankingType) {
                    case TF_IDF: { // 2.1 and 2.2 and 3.1
                        result = rankedTfIdf(query.queryTerms);
                        break;
                    }
                    case PAGERANK: { //2.5
                        result = rankedPageRank(query.queryTerms);
                        break;
                    }
                    case COMBINATION: { //2.5 , 3.1
                        result = rankedCombo(query.queryTerms);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /* --------------------------------------------- */
    // For assignment 1
    public PostingsList handleQuery(
            List<PostingsList> postings,
            BiFunction<PostingsList, PostingsList, PostingsList> searchFunction
    ) {
        PostingsList intersection = postings.get(0);

        for (int i = 1; i < postings.size(); i++) {
            intersection = searchFunction.apply(intersection, postings.get(i));
        }
        return intersection;
    }

    public PostingsList intersect(PostingsList p1, PostingsList p2) {
        final PostingsList answer = new PostingsList();

        int i = 0, j = 0;

        while (i < p1.size() && j < p2.size()) {
            final PostingsEntry e1 = p1.get(i);
            final int docId2 = p2.get(j).docID;

            if (e1.docID == docId2) {
                answer.add(e1);
                i++;
                j++;
            } else if (e1.docID < docId2) {
                i++;
            } else {
                j++;
            }
        }
        return answer;
    }

    public PostingsList positionalIntersect(PostingsList postingsList1, PostingsList postingsList2) {
        final PostingsList answer = new PostingsList();

        final int p1Size = postingsList1.size();
        final int p2Size = postingsList2.size();

        int p1Index = 0, p2Index = 0;

        while (p1Index < p1Size && p2Index < p2Size) {
            // Entry from each list
            final PostingsEntry entry1 = postingsList1.get(p1Index);
            final PostingsEntry entry2 = postingsList2.get(p2Index);

            if (entry1.docID < entry2.docID) {
                p1Index++;
                continue;
            } else if (entry1.docID > entry2.docID) {
                p2Index++;
                continue;
            }

            final List<Integer> l = new ArrayList<>();

            // Positions of each entry
            final TreeSet<Integer> entry1Positions = entry1.positions;
            final TreeSet<Integer> entry2Positions = entry2.positions;

            for (int pos1 : entry1Positions) {
                for (int pos2 : entry2Positions) {
                    if (pos2 - pos1 == 1) {
                        l.add(pos2);
                        break;
                    } else if (pos2 > pos1) {
                        break;
                    }
                }
                while (!l.isEmpty() && Math.abs(l.get(0) - pos1) != 1) {
                    l.remove(0);
                }
                if (!l.isEmpty()) {
                    answer.add(new PostingsEntry(entry1.docID, l));
                    l.clear();
                }
            }
            p1Index++;
            p2Index++;
        }
        return answer;
    }

    /* --------------------------------------------- */

    // 2.2 and 2.1 and 2.5.2 tf_idf (everything between the lines)
    // changed for 3.1

    private PostingsList rankedTfIdf(List<Query.QueryTerm> queryTerms) {

        // Get all the searched terms
        List<String> terms = queryTerms.stream().map(queryTerm -> queryTerm.term).toList();

        // map: <doc, score>
        // Create empty dictionary to hold document scores
        Map<PostingsEntry, Double> docScores = new HashMap<>();

        // Loop through all search query terms, for each term,
        // changed the following 2 lines for 3.1
        // used to be:
        // for ( term : terms ) {
        for (Query.QueryTerm queryTerm : queryTerms) {
            String term = queryTerm.term;
            // Retrieve documents containing the search term
            // "allDocuments" is a list that has all the documents that have that term
            PostingsList allDocuments = index.getPostings(term);

            // Loop through all retrieved documents
            // for each document in this list (of documents that have the term)
            for (PostingsEntry document : allDocuments.getList()) {

                // Calculate the score for the document
                // 3.1: multiply by queryTerm.weight
                double score = calculateTfIdfScore(document, term) * queryTerm.weight;

                // if the document already exists in the list of scores, add the SCORE to it only
                if (docScores.containsKey(document)) {
                    docScores.put(document, docScores.get(document) + score);
                }
                // otherwise, put the document in the list of score and its score
                else {
                    docScores.put(document, score);
                }
            }
        }

        // a list called results which will be the documents that have the term and ranked
        PostingsList results = new PostingsList();

        for (String term : terms) {

            // Retrieve documents containing the search term
            PostingsList allDocuments = index.getPostings(term);

            // for every doc in this list of doc, if it's not in results list, add it
            for (PostingsEntry document : allDocuments.getList()) {
                if (!results.fastContains(document)) // 3.1 used fastContains to make it faster
                    results.add(document);
            }
        }

        // for every doc in this list of doc ==> doc Score / doc length
        for (PostingsEntry document : docScores.keySet()) {
            document.score = docScores.get(document) / index.docLengths.get(document.docID);
        }

        // sort the docs by score and put them in results list
        results.sortByScore();
        return results;
    }


    // tf * idf
    // later, it will be divided by length of the doc (in rankedTfIdf method)
    private double calculateTfIdfScore(PostingsEntry document, String term) {
        double tf = getTF(document, term);
        double idf = getIDF(term);
        return tf * idf;
    }

    // calculating the term frequency : number of occurrences of term in doc
    // how many positions that term has in the doc aka tf
    public double getTF(PostingsEntry document, String term) {
        return document.positions.size();
    }

    // calculating Inverse Document Frequency : idf = log(N/df)
    private double getIDF(String term) {
        double N = index.getNrOfDocuments(); // number of ALL the documents in the corpus
        double df = index.getPostings(term).size(); // number of documents in the corpus that contain the term
        double idf = Math.log(N / df);

        // this print is for task 2.3
        // System.out.printf(" idf - %s: %f\n", term, Math.round(idf * 10000.0) / 10000.0);

        return idf; // calculating the idf
    }

    /* --------------------------------------------- */

    // 2.5.2 (everything between the lines)
    // read page rank from file to a hashtable
    Hashtable<String, Double> pageRankHT = new Hashtable<>(); // <docTitle, pageRankValue>

    // Inspired from readDocs()
    void readPagerank(String filename) {
        int fileIndex = 0;
        try {
            System.err.print("(readPagerank:) Reading titles file... ");
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            /// MAX_NUMBER_OF_DOCS = 2000000
            while ((line = in.readLine()) != null && fileIndex < 2000000) {
                // format => docTitle=pageRankValue;docID ( UC_Davis.f=0.012531641141929038;245 )
                int index1 = line.indexOf("="); // separate by "="
                int index2 = line.indexOf(";"); // separate by ";"

                // new format in HT => <docTitle, pageRankValue>
                String docTitle = line.substring(0, index1);
                // take between index1 and index2 and convert from string to double
                Double pageRankValue = Double.parseDouble(line.substring(index1 + 1, index2));

                pageRankHT.put(docTitle, pageRankValue);
                fileIndex++;
            }
            // from here it's EXACTLY like readDocs()//
            if (fileIndex >= 2000000) {
                System.err.print("(readPagerank:) stopped reading since documents table is full. ");
            } else {
                System.err.print("(readPagerank:) done. ");
            }
        } catch (FileNotFoundException e) {
            System.err.println("(readPagerank:) File " + filename + " not found!");
        } catch (IOException e) {
            System.err.println("(readPagerank:) Error reading file " + filename);
        }
        System.err.println("(readPagerank:) Read " + fileIndex + " number of documents");
    }

    private PostingsList rankedPageRank(List<Query.QueryTerm> queryTerms){

        // Get all the searched terms
        List<String> terms = queryTerms.stream().map(queryTerm -> queryTerm.term).toList();

        // Loop through all search query terms, for each term,
        for (String term : terms) {

            // Retrieve documents containing the search term
            // "allDocuments" is a list that has all the documents that have that term
            PostingsList allDocuments = index.getPostings(term);

            // Loop through all retrieved documents
            // for each document in this list (of documents that have the term)
            for (PostingsEntry document : allDocuments.getList()) {

                // get the doc name of each document using doc ID
                String filename = Index.docNames.get(document.docID);
                // get the doc title
                filename = filename.substring(filename.lastIndexOf("\\") + 1);
                // set the doc.score to the score corresponding to the doc title in HT
                document.score = pageRankHT.get(filename);
            }
        }

        /* a list called results which will be the documents that have the term and ranked */
        PostingsList results = new PostingsList();

        for (String term : terms) {

            // Retrieve documents containing the search term
            // "documents" is a list that has all the documents that have that term */
            PostingsList allDocuments = index.getPostings(term);

            // for every doc in this list of doc, if it's not in results list, add it
            for (PostingsEntry document : allDocuments.getList()) {
                if (!results.getList().contains(document))
                    results.add(document);
            }
        }
        // sort the docs by score and put them in results list
        results.sortByScore();
        return results;
    }

    private PostingsList rankedCombo(List<Query.QueryTerm> queryTerms) {

        // Get all the searched terms
        List<String> terms = queryTerms.stream().map(queryTerm -> queryTerm.term).toList();

        // map: <doc, score>
        // Create empty dictionary to hold document scores
        Map<PostingsEntry, Double> docScores = new HashMap<>();

        // Loop through all search query terms, for each term,
        // changed the following 2 lines for 3.1
        // used to be:
        // for ( term : terms ) {
        for (Query.QueryTerm queryTerm : queryTerms) {
            String term = queryTerm.term;

            // Retrieve documents containing the search term
            // "documents" is a list that has all the documents that have that term
            PostingsList allDocuments = index.getPostings(term);

            // Loop through all retrieved documents
            // for each document in this list (of documents that have the term)
            for (PostingsEntry document : allDocuments.getList()) {

                // 3.1: multiply by queryTerm.weight
                double scoreTfIdf = calculateTfIdfScore(document, term) * queryTerm.weight;
                /// if the document already exists in the list of scores, add the SCORE to it only
                if (docScores.containsKey(document)) {
                    docScores.put(document, docScores.get(document) + scoreTfIdf);
                }
                else {
                    ///otherwise, put the document in the list of score and its score
                    docScores.put(document, scoreTfIdf);
                }
            }
        }

        // a list called results which will be the documents that have the term and ranked
        PostingsList results = new PostingsList();

        for (String term : terms) {

            // Retrieve documents containing the search term
            // "allDocuments" is a list that has all the documents that have that term
            PostingsList allDocuments = index.getPostings(term);

            // for every doc in this list of doc, if it's not in results list, add it
            for (PostingsEntry document : allDocuments.getList()) {
                if (!results.fastContains(document))// 3.1 used fastContains to make it faster
                    results.add(document);
            }
        }

        // for every doc in this list of doc scores
        for (PostingsEntry document : docScores.keySet()) {

            // normalize the tf-idf score
            double scoreTfIdfFinal = docScores.get(document) / index.docLengths.get(document.docID);

            // get the page rank score
            String filename = Index.docNames.get(document.docID);
            filename = filename.substring(filename.lastIndexOf("\\") + 1);
            //double scorePageRank = pageRankHT.get(filename);
            double scorePageRank = pageRankHT.getOrDefault(filename, 0.0);
            /*if (pageRankHT.get(filename) == null) {
                System.out.println(filename);
            }*/

            // set the doc.score to a linear combination of both scores
            document.score = (1 - w) * scorePageRank + w * scoreTfIdfFinal;
        }
        // sort the docs by score and put them in results list
        results.sortByScore();
        return results;
    }
    /* --------------------------------------------- */
    public List<PostingsList> getPostings(List<Query.QueryTerm> queryTerms) {
        final List<PostingsList> postings = new ArrayList<>();

        for (Query.QueryTerm queryTerm : queryTerms) {
            final PostingsList postingsList = index.getPostings(queryTerm.term);
            if (postingsList != null) {
                postings.add(postingsList);
            }
        }
        return postings;
    }



}