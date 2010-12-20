package com.sourcesense.jira.customfield.searcher;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.LazyLoadedOption;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.bean.FieldVisibilityBean;


/**
 * User: fabio
 * Date: Jun 13, 2007
 * Time: 1:53:11 PM
 */
public class TestMultiLevelCascadingSelectSearcher {/*extends EasyMockTestCase {
    MockControl ctrlOptionsManager;
    OptionsManager mockOptionsManager;

    FieldVisibilityBean mockFieldVisibilityBean;

    MockControl ctrlCustomField;
    CustomField mockCustomField;

    MultiLevelCascadingSelectSearcher searcher;
    GenericValue option1001;
    GenericValue option1002;
    GenericValue option1003;
    
    public TestMultiLevelCascadingSelectSearcher() {
        ctrlOptionsManager = MockControl.createControl(OptionsManager.class);
        mockOptionsManager = (OptionsManager) ctrlOptionsManager.getMock();
        ctrlOptionsManager.setDefaultMatcher(MockControl.EQUALS_MATCHER);

        ctrlCustomField = MockControl.createControl(CustomField.class);
        mockCustomField = (CustomField) ctrlCustomField.getMock();

        option1001 = new MockGenericValue("CustomFieldOption",
                EasyMap.build("id", new Long(1001),
                        "parentoptionid", null,
                        "value", "holden 1001",
                        "customfieldconfig", new Long(10001),
                        "sequence", new Long(1)));

        option1002 = new MockGenericValue("CustomFieldOption",
                EasyMap.build("id", new Long(1002),
                        "parentoptionid", new Long(1001),
                        "value", "holden 1002",
                        "customfieldconfig", new Long(10001),
                        "sequence", new Long(2)));

        option1003 = new MockGenericValue("CustomFieldOption",
                EasyMap.build("id", new Long(1003),
                        "parentoptionid", new Long(1002),
                        "value", "holden 1003",
                        "customfieldconfig", new Long(10001),
                        "sequence", new Long(3)));

        mockFieldVisibilityBean = new MockFieldVisibilityBean();
    }


    protected void setUp() throws Exception {
        _reset();
        searcher = new MultiLevelCascadingSelectSearcher(new SelectConverter(mockOptionsManager), mockOptionsManager, mockFieldVisibilityBean);
    }

    public void testIndexing() {
        Document doc = new Document();
        CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl(mockCustomField);
        customFieldParams.addValue(null, EasyList.build(new LazyLoadedOption(option1001, null, mockOptionsManager, null)));
        customFieldParams.addValue("1", EasyList.build(new LazyLoadedOption(option1002, null, mockOptionsManager, null)));
        customFieldParams.addValue("2", EasyList.build(new LazyLoadedOption(option1003, null, mockOptionsManager, null)));

        mockCustomField.getId();
        ctrlCustomField.setReturnValue("customfield_123", MockControl.ONE_OR_MORE);

        _startTestPhase();
        searcher.index(doc, mockCustomField, customFieldParams);
        Field field = doc.getField("customfield_123");
        assertTrue(field.isIndexed());
        assertFalse(field.isTokenized());
        assertEquals("1001", field.stringValue());

        field = doc.getField("customfield_123:1");
        assertTrue(field.isIndexed());
        assertFalse(field.isTokenized());
        assertEquals("1002", field.stringValue());

        field = doc.getField("customfield_123:2");
        assertTrue(field.isIndexed());
        assertFalse(field.isTokenized());
        assertEquals("1003", field.stringValue());
        _verifyAll();
    }


    public void testSearchParameterCreation()
    {
        CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl(mockCustomField);
        customFieldParams.addValue(null, EasyList.build(option1001.getString("id")));
        customFieldParams.addValue("1", EasyList.build(option1002.getString("id")));
        customFieldParams.addValue("2", EasyList.build(option1003.getString("id")));

        SearchParameter expectedParameterForChildChild = new StringParameter("customfield_123:2", "customfield_123", "1003");
        SearchParameter expectedParameterForChild = new StringParameter("customfield_123:1", "customfield_123", "1002");
        SearchParameter expectedParameterForParent = new StringParameter("customfield_123", "1001");

        mockCustomField.getId();
        ctrlCustomField.setReturnValue("customfield_123", MockControl.ONE_OR_MORE);
        _startTestPhase();
        final List searchParams = searcher.makeSearchParameters(mockCustomField, customFieldParams);
        assertEquals(3, searchParams.size());
        assertTrue(searchParams.contains(expectedParameterForChildChild));
        assertTrue(searchParams.contains(expectedParameterForChild));
        assertTrue(searchParams.contains(expectedParameterForParent));
        _verifyAll();
    }


    public void testNoSearchParamForMinusOne()
    {
        CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl(mockCustomField);
        customFieldParams.addValue(null, EasyList.build(option1001.getString("id")));
        customFieldParams.addValue("1", EasyList.build(option1002.getString("id")));
        customFieldParams.addValue("2", EasyList.build("-1"));

        SearchParameter expectedParameterForChild = new StringParameter("customfield_123:1", "customfield_123", "1002");
        SearchParameter expectedParameterForParent = new StringParameter("customfield_123", "1001");

        mockCustomField.getId();
        ctrlCustomField.setReturnValue("customfield_123", MockControl.ONE_OR_MORE);
        _startTestPhase();
        final List searchParams = searcher.makeSearchParameters(mockCustomField, customFieldParams);
        assertEquals(2, searchParams.size());
        assertTrue(searchParams.contains(expectedParameterForChild));
        assertTrue(searchParams.contains(expectedParameterForParent));
        _verifyAll();
    }

    public MockControl[] _getRegisteredMockControllers() {
        return new MockControl[] {ctrlOptionsManager, ctrlCustomField};
    }*/
}
