/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 *
 *   Edited by Hasti Mohebali Zadeh, 2023
 */

package ir;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable
{
    public final int docID;

    public double score = 0;

    public final TreeSet<Integer> positions = new TreeSet<>();

    public PostingsEntry(int docID)
    {
        this.docID = docID;
    }

    public PostingsEntry(int docID, int position)
    {
        this.docID = docID;
        addPosition(position);
    }

    public PostingsEntry(int docID, Collection<Integer> positions)
    {
        this.docID = docID;
        addPositions(positions);
    }

    public void addPosition(int position) {
        this.positions.add(position);
    }

    public void addPositions(Collection<Integer> positions) {
        this.positions.addAll(positions);
    }

    public int getDocID()
    {
        return docID;
    }

    public double getScore() { return score; }

    @Override
    public int compareTo(PostingsEntry other)
    {
        return Double.compare(other.score, score);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        return ((PostingsEntry) obj).docID == this.docID;
    }

    @Override
    public int hashCode() {
        return docID;
    }
}

