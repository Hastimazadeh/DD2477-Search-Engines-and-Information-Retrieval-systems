/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 *
 *   Edited by Hasti Mohebali Zadeh, 2023
 */

package ir;

import java.util.*;

public class PostingsList
{
    /**
     * The postings list
     */
    private final List<PostingsEntry> list = new ArrayList<>();

    /**
     * Number of postings in this list.
     */
    public int size()
    {
        return list.size();
    }

    /**
     * Returns the ith posting.
     */
    public PostingsEntry get(int i)
    {
        return list.get(i);
    }

    public List<PostingsEntry> getList()
    {
        return list;
    }

    public void add(PostingsEntry entry)
    {
        int index = Collections.binarySearch(list, entry, Comparator.comparing(PostingsEntry::getDocID));

        // Not in the list
        if (index < 0) {
            index = -index - 1;
            list.add(index, entry);
        } else {
            list.get(index).addPositions(entry.positions);
        }
    }

    // added this for 3.1 to make the ranked retrieval after selection faster
    public boolean fastContains(PostingsEntry entry){
        return Collections.binarySearch(list, entry, Comparator.comparing(PostingsEntry::getDocID)) >= 0;
    }

    public void sortByScore()
    {
        list.sort(Comparator.comparing(PostingsEntry::getScore, Comparator.reverseOrder()));
    }
}

