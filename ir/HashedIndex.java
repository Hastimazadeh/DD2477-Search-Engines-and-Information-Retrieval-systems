/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 *
 *   Edited by Hasti Mohebali Zadeh, 2023
 */


package ir;

import java.util.HashMap;


/**
 * Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index
{
    /**
     * The index as a hashtable.
     */
    private final HashMap<String, PostingsList> index = new HashMap<>();


    /**
     * Inserts this token in the hashtable.
     */
    public void insert(String token, int docID, int offset)
    {
        PostingsEntry entry = new PostingsEntry(docID, offset);

        if (index.containsKey(token)) {
            getPostings(token).add(entry);
        } else {
            PostingsList list = new PostingsList();
            list.add(entry);
            index.put(token, list);
        }
    }

    /**
     * Returns the postings for a specific term, or null
     * if the term is not in the index.
     */
    public PostingsList getPostings(String token)
    {
        return index.get(token);
    }

    public int getNrOfDocuments()
    {
        return docLengths.size();
    }

    /**
     * No need for cleanup in a HashedIndex.
     */
    public void cleanup()
    {
    }
}
