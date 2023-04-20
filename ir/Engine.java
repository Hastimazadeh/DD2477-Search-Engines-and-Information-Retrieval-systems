/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 *
 *   Edited by Hasti Mohebali Zadeh, 2023
 */

package ir;

import java.util.ArrayList;
import java.io.File;

/**
 * This is the main class for the search engine.
 */
public class Engine
{

    /**
     * The inverted index.
     */
    Index index = new HashedIndex();
    //Index index = new PersistentHashedIndex();

    /**
     * The indexer creating the search index.
     */
    Indexer indexer;

    /**
     * K-gram index
     */

    // old was here:
    //KGramIndex kgIndex ;

    // 3.3  - part 2
    // instantiate the variable kgIndex in the Engine class
    KGramIndex kgIndex = new KGramIndex(2);

    /**
     * The searcher used to search the index.
     */
    Searcher searcher;

    /**
     * Spell checker
     */
    SpellChecker speller;

    /**
     * The engine GUI.
     */
    SearchGUI gui;

    /**
     * Directories that should be indexed.
     */
    ArrayList<String> dirNames = new ArrayList<>();

    /**
     * Lock to prevent simultaneous access to the index.
     */
    Object indexLock = new Object();

    /**
     * The patterns matching non-standard words (e-mail addresses, etc.)
     */
    String patterns_file = null;

    /**
     * The file containing the logo.
     */
    String pic_file = "";

    /**
     * The file containing the pageranks.
     */
    String rank_file = "";

    /**
     * For persistent indexes, we might not need to do any indexing.
     */
    boolean is_indexing = true;


    /* ----------------------------------------------- */


    /**
     * Constructor.
     * Indexes all chosen directories and files
     */
    public Engine(String[] args)
    {
        decodeArgs(args);
        indexer = new Indexer(index, kgIndex, patterns_file);
        searcher = new Searcher(index, kgIndex);
        gui = new SearchGUI(this);
        gui.init();
        /*
         *   Calls the indexer to index the chosen directory structure.
         *   Access to the index is synchronized since we don't want to
         *   search at the same time we're indexing new files (this might
         *   corrupt the index).
         */
        if (is_indexing) {
            synchronized (indexLock) {
                gui.displayInfoText("Indexing, please wait...");
                long startTime = System.currentTimeMillis();
                for (String dirName : dirNames) {
                    File dokDir = new File(dirName);
                    indexer.processFiles(dokDir, is_indexing);

                    // added this line for 3.3 - part 2
                    // translate: kgIndex.insert(token);
                    kgIndex.init(
                            "C:\\Users\\HP\\Documents\\courses\\Search Engines and Information Retrieval systems - DD2477\\Assignments\\SearchEngine-Assignment\\assignment1\\kgram_test.txt",
                            patterns_file
                    );

                }
                long elapsedTime = System.currentTimeMillis() - startTime;
                gui.displayInfoText(String.format("Indexing done in %.1f seconds.", elapsedTime / 1000.0));
                index.cleanup();
            }
        } else {
            gui.displayInfoText("Index is loaded from disk");
        }
    }


    /* ----------------------------------------------- */

    /**
     * Decodes the command line arguments.
     */
    private void decodeArgs(String[] args)
    {
        int i = 0;
        while (i < args.length) {
            if ("-d".equals(args[i])) {
                i++;
                if (i < args.length) {
                    dirNames.add(args[i++]);
                }
            } else if ("-p".equals(args[i])) {
                i++;
                if (i < args.length) {
                    patterns_file = args[i++];
                }
            } else if ("-l".equals(args[i])) {
                i++;
                if (i < args.length) {
                    pic_file = args[i++];
                }
            } else if ("-r".equals(args[i])) {
                i++;
                if (i < args.length) {
                    rank_file = args[i++];
                }
            } else if ("-ni".equals(args[i])) {
                i++;
                is_indexing = false;
            } else {
                System.err.println("Unknown option: " + args[i]);
                break;
            }
        }
    }


    /* ----------------------------------------------- */


    public static void main(String[] args)
    {
        Engine e = new Engine(args);
    }

}

