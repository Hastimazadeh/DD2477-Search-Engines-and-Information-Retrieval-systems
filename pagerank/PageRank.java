/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 *
 *   Edited by Hasti Mohebali Zadeh, 2023
 */

//package pagerank;

import java.io.*;
import java.util.*;


public class PageRank {

    /// NEW (class Term)
    // Used for printing the name and probability for 2.5.1 and 2.5.2
    public class Term implements Comparable<Term> {
        public double probability;
        public String docName;
        public Term(String docName, double probability) {
            this.docName = docName;
            this.probability = probability;
        }
        public int compareTo(Term other) {
            return Double.compare(other.probability, probability);
        }
        public void print() {
            String value = String.format("%.5f", probability); // 2.5.1
            //String value = Double.toString(probability); // 2.5.2
            System.out.println(docName + ": " + value);
        }
    }

    /// NEW
    public ArrayList<Term> pagerankArray = new ArrayList<>();

    /* --------------------------------------------- */

    /**
     * Maximal number of documents. We're assuming here that we
     * don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     * Mapping from document names to document numbers.
     */
    HashMap<String, Integer> docNumber = new HashMap<>();

    /**
     * Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**
     * A memory-efficient representation of the transition matrix.
     * The outlinks are represented as a HashMap, whose keys are
     * the numbers of the documents linked from.<p>
     * <p>
     * The value corresponding to key i is a HashMap whose keys are
     * all the numbers of documents j that i links to.<p>
     * <p>
     * If there are no outlinks from i, then the value corresponding
     * key i is null.
     */
    HashMap<Integer, HashMap<Integer, Boolean>> link = new HashMap<>();

    /**
     * The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     * The probability that the surfer will be bored, stop
     * following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     * Convergence criterion: Transition probabilities do not
     * change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /* --------------------------------------------- */
    public PageRank(String filename) {
        int noOfDocs = readDocs(filename);
        iterate(noOfDocs, 100);
        // 2.5.2 (don't need the following 3 lines for 2.5.1)
        /* String file = "davisTitles.txt";
        readDocName(file);
        writePageRank(noOfDocs);*/
    }
    /* --------------------------------------------- */
    /**
     * Reads the documents and fills the data structures.
     *
     * @return the number of documents read.
     */
    int readDocs(String filename) {
        int fileIndex = 0;
        try {
            System.err.print("Reading file... ");
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = in.readLine()) != null && fileIndex < MAX_NUMBER_OF_DOCS) {
                int index = line.indexOf(";");
                String title = line.substring(0, index);
                Integer fromdoc = docNumber.get(title);
                //  Have we seen this document before?
                if (fromdoc == null) {
                    // This is a previously unseen doc, so add it to the table.
                    fromdoc = fileIndex++;
                    docNumber.put(title, fromdoc);
                    docName[fromdoc] = title;
                }
                // Check all outlinks.
                StringTokenizer tok = new StringTokenizer(line.substring(index + 1), ",");
                while (tok.hasMoreTokens() && fileIndex < MAX_NUMBER_OF_DOCS) {
                    String otherTitle = tok.nextToken();
                    Integer otherDoc = docNumber.get(otherTitle);
                    if (otherDoc == null) {
                        // This is a previousy unseen doc, so add it to the table.
                        otherDoc = fileIndex++;
                        docNumber.put(otherTitle, otherDoc);
                        docName[otherDoc] = otherTitle;
                    }
                    // Set the probability to 0 for now, to indicate that there is
                    // a link from fromdoc to otherDoc.
                    if (link.get(fromdoc) == null) {
                        link.put(fromdoc, new HashMap<Integer, Boolean>());
                    }
                    if (link.get(fromdoc).get(otherDoc) == null) {
                        link.get(fromdoc).put(otherDoc, true);
                        out[fromdoc]++;
                    }
                }
            }
            if (fileIndex >= MAX_NUMBER_OF_DOCS) {
                System.err.print("(readDocs:) stopped reading since documents table is full. ");
            } else {
                System.err.print("(readDocs:) done. ");
            }
        } catch (FileNotFoundException e) {
            System.err.println("(readDocs:) File " + filename + " not found!");
        } catch (IOException e) {
            System.err.println("(readDocs:) Error reading file " + filename);
        }
        System.err.println("(readDocs:) Read " + fileIndex + " number of documents");
        return fileIndex;
    }

    /* --------------------------------------------- */

    /// NEW
    // power iteration algorithm
    // compute the PageRank scores for a set of web pages based on their links to other pages
    /*
    *   Chooses a probability vector a, and repeatedly computes
    *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
    */
    void iterate(int numberOfDocs, int maxIterations) {

        // to store the updated PageRank scores for the current iteration
        double[] next_a = new double[numberOfDocs];
        Arrays.fill(next_a, 0);
        next_a[0] = 1;
        // to store the PageRank scores from the previous iteration
        double[] current_a = new double[numberOfDocs];

        // Repeat until convergence or the maximum number of iterations is reached.
        for (int iteration = 1; iteration <= maxIterations; iteration++) {

            // make current a same as the next a
            current_a = Arrays.copyOf(next_a, next_a.length);

            // updates the PageRank scores from the previous iteration to the next iteration for each page i.
            for (int i = 0; i < numberOfDocs; i++) {
                double sum = 0.0;
                for (int j = 0; j < numberOfDocs; j++) {

                    // page j does not have any outgoing links
                    if (link.get(j) == null) {
                        // randomly jump to any page with equal probability
                        // current_a[j] : the current PageRank score for page j
                        // both are probability of jumping to any page at random
                        sum += current_a[j] * (((1 - BORED) * (1.0 / numberOfDocs)) + BORED * (1.0 / numberOfDocs));
                    }
                    //  page j does have an outgoing link to page i
                    else if (link.get(j).get(i) != null) {
                        //current_a[j] : the current PageRank score for page j
                        // ((1 - BORED) * (1.0 / out[j])) : the probability of following any of the links
                        // BORED * (1.0 / numberOfDocs) : probability of jumping to any page at random
                        sum += current_a[j] * (((1 - BORED) * (1.0 / out[j])) + BORED * (1.0 / numberOfDocs));
                    }
                    // page j does not have a link to page i
                    // if link.get(j).get(i) == null
                    else {
                        // current_a[j] : the current PageRank score for page j
                        // ((1 - BORED) * (1.0 / out[j])) = 0
                        // BORED * (1.0 / numberOfDocs) : probability of jumping to any page at random
                        sum += current_a[j] *  (BORED * (1.0 / numberOfDocs));
                    }
                }
                // next_a[i] contains the updated PageRank score for each web page i
                next_a[i] = sum;
            }

            // Check for convergence.
            double normDiff = 0.0;
            // The loop sums up these differences to calculate the norm difference.
            for (int i = 0; i < numberOfDocs; i++) {
                normDiff += Math.abs(next_a[i] - current_a[i]);
            }
            if (normDiff < EPSILON) { // has converged
                System.out.println("Converged after " + iteration + " iterations");
                break;
            }
        }
        // adding, sorting, printing the top 30
        for (int i = 0; i < numberOfDocs; ++i) {
            pagerankArray.add(new Term(docName[i], next_a[i]));
        }
        Collections.sort(pagerankArray);
        // print the top 30
        int count = 0;
        for (PageRank.Term term : pagerankArray) {
            if(count == 30) {
                break;
            }
            count ++;
            term.print();
        }
    }

    /* --------------------------------------------- */
    /// NEW
    /// 2.5.2

    Hashtable<String, String> docTitles = new Hashtable<String, String>(); // <docID, docTitle>

    // Inspired from readDocs()
    void readDocName(String filename) {
        int fileIndex = 0;
        try {
            System.err.print("(readDocName:) Reading titles file... ");
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = in.readLine()) != null && fileIndex < MAX_NUMBER_OF_DOCS) {
                int index = line.indexOf(";"); // separate by ";"
                /// NEW
                // format => docID;docTitle  (13354;Elly.f)
                String docID = line.substring(0, index);
                String docTitle = line.substring(index + 1);
                docTitles.put(docID, docTitle);
                fileIndex++;
            }
            // from here it's EXACTLY like readDocs()//
            if (fileIndex >= MAX_NUMBER_OF_DOCS) {
                System.err.print("(readDocName:) stopped reading since documents table is full. ");
            } else {
                System.err.print("(readDocName:) done. ");
            }
        } catch (FileNotFoundException e) {
            System.err.println("(readDocName:) File " + filename + " not found!");
        } catch (IOException e) {
            System.err.println("(readDocName:) Error reading file " + filename);
        }
        System.err.println("(readDocName:) Read " + fileIndex + " number of documents");
    }

    // write pagerank to a txt file => PagerankScores.txt
    void writePageRank(int numberOfDocs) {
        // BufferedWriter object to write the output to the file.
        BufferedWriter bufferedWriter = null;
        try {
            FileWriter fileWriter  = new FileWriter("PagerankScore.txt");
            bufferedWriter = new BufferedWriter(fileWriter );

            // For each document in the pagerankArray
            for (int i = 0; i < numberOfDocs; i++) {
                String prob = Double.toString(pagerankArray.get(i).probability);
                String docTitle = docTitles.get(pagerankArray.get(i).docName);
                // format => docTitle=prob;docID ( UC_Davis.f=0.012531641141929038;245 )
                // String docID = pagerankArray.get(i).docName
                bufferedWriter.write(docTitle + "=" + prob + ";" + pagerankArray.get(i).docName);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    /* --------------------------------------------- */

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please give the name of the link file");
        } else {
            new PageRank(args[0]);
        }
    }
}