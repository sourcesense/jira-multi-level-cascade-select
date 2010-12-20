package com.sourcesense.jira.customfield.searcher;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.atlassian.jira.issue.index.DocumentConstants;

final class TermQueryFactory
{
    /**
     * @param fieldName the index field to be visible
     * @return the term query <code>visiblefieldids:fieldName</code>
     */
    static Query visibilityQuery(final String fieldName)
    {
        return new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, fieldName));
    }

    /**
     * @param fieldName the index field to be non empty
     * @return the term query <code>nonemptyfieldids:fieldName</code>
     */
    static Query nonEmptyQuery(final String fieldName)
    {
        return new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, fieldName));
    }
}