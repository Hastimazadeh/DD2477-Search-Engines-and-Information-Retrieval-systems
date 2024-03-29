/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 *
 *   Edited by Hasti Mohebali Zadeh, 2023
 */

package ir;

import java.io.*;
import java.nio.charset.StandardCharsets;


/**
 * Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer
{

    /**
     * The index to be built up by this Indexer.
     */
    Index index;

    /**
     * K-gram index to be built up by this Indexer
     */
    KGramIndex kgIndex;

    /**
     * The next docID to be generated.
     */
    private int lastDocID = 0;

    /**
     * The patterns matching non-standard words (e-mail addresses, etc.)
     */
    String patterns_file;


    /* ----------------------------------------------- */


    /**
     * Constructor
     */
    public Indexer(Index index, KGramIndex kgIndex, String patterns_file)
    {
        this.index = index;
        this.kgIndex = kgIndex;
        this.patterns_file = patterns_file;
    }


    /**
     * Generates a new document identifier as an integer.
     */
    private int generateDocID()
    {
        return lastDocID++;
    }


    /**
     * Tokenizes and indexes the file @code{f}. If <code>f</code> is a directory,
     * all its files and subdirectories are recursively processed.
     */
    // NEW 3.3
    public void processFiles(File f, boolean is_indexing) {
        // do not try to index fs that cannot be read

        //  (!is_indexing || !f.canRead()) has been changed
        //  so that the code will now only proceed with indexing if is_indexing is true and f can be read.
        if (!is_indexing || !f.canRead()) {
            return;
        }

        if (f.isDirectory()) {
            String[] fs = f.list();
            // an IO error could occur
            if (fs != null) {
                for (String s : fs) {
                    processFiles(new File(f, s), true);
                }
            }
        } else {
            // First register the document and get a docID
            int docID = generateDocID();
            if (docID % 1000 == 0) System.err.println("Indexed " + docID + " files");
            try {
                 Reader reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
                 Tokenizer tok = new Tokenizer(reader, true, false, true, patterns_file);
                 int offset = 0;
                 while (tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    insertIntoIndex(docID, token, offset++);
                 }
                 index.docNames.put(docID, f.getPath());
                 index.docLengths.put(docID, offset);
                 reader.close();
            } catch (IOException e) {
               System.err.println("Warning: IOException during indexing.");
            }
        }
    }


    /* ----------------------------------------------- */

    /**
     * Indexes one token.
     */

    public void insertIntoIndex(int docID, String token, int offset)
    {
        index.insert(token, docID, offset);
        /*if (kgIndex != null) {
            kgIndex.insert(token);
        }*/
    }
}
