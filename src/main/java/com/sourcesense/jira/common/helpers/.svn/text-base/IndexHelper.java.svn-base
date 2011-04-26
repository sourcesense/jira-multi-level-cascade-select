package com.sourcesense.jira.common.helpers;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

/**
 * This class is the same of the Plug-in 3.0.
 * At the moment seems to not need any change or adaptation.
 * @author Fabio 
 * Date:  5/11/2010 (date of the conversion by Alessandro Benedetti)
 */
public class IndexHelper {

    public static void addKeyword(Document doc, String name, String value) {
        luceneHelper.addKeyword(doc, name, value);
    }

    public static IndexReader getIndexReader() {
        return readerHelper.getReader();
    }

    private static LuceneHelper luceneHelper = LuceneHelper.getHelper();
    private static ReaderHelper readerHelper = ReaderHelper.getHelper();
}
