package com.sourcesense.jira.customfield;

import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.mock.MockGenericValue;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.LazyLoadedOption;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.sourcesense.jira.mocks.EasyMockTestCase;

/**
 * User: fabio
 * Date: Jun 13, 2007
 * Time: 11:13:35 AM
 */
public class TestMultiLevelCascadingSelectValue extends EasyMockTestCase {
    MockControl ctrlOptionsManager;
    OptionsManager mockOptionsManager;
    MockControl ctrlFieldConfig;
    FieldConfigManager mockFieldConfig;

    MultiLevelCascadingSelectValue cascadingSelectValue;
    GenericValue option1001;
    GenericValue option1002;
    GenericValue option1003;

    public TestMultiLevelCascadingSelectValue() {
        ctrlOptionsManager = MockControl.createControl(OptionsManager.class);
        mockOptionsManager = (OptionsManager) ctrlOptionsManager.getMock();
        ctrlOptionsManager.setDefaultMatcher(MockControl.EQUALS_MATCHER);
        
        ctrlFieldConfig = MockControl.createControl(FieldConfigManager.class);
        mockFieldConfig = (FieldConfigManager) ctrlFieldConfig.getMock();
        ctrlFieldConfig.setDefaultMatcher(MockControl.EQUALS_MATCHER);

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

    }

    @Override
    protected void setUp() throws Exception {
        _reset();

        cascadingSelectValue = new MultiLevelCascadingSelectValue(mockOptionsManager, "1001:1002:1003");
    }

    public void testCreateValue() {
        mockOptionsManager.findByOptionId(new Long(1001));
        ctrlOptionsManager.setReturnValue(new LazyLoadedOption(option1001, mockOptionsManager,mockFieldConfig), MockControl.ONE_OR_MORE);

        mockOptionsManager.findByOptionId(new Long(1002));
        ctrlOptionsManager.setReturnValue(new LazyLoadedOption(option1002,mockOptionsManager, mockFieldConfig), MockControl.ONE_OR_MORE);

        mockOptionsManager.findByOptionId(new Long(1003));
        ctrlOptionsManager.setReturnValue(new LazyLoadedOption(option1003, mockOptionsManager, mockFieldConfig), MockControl.ONE_OR_MORE);

        _startTestPhase();
        //String[] result = cascadingSelectValue.getMultiSearchValue();
        String[] result = cascadingSelectValue.getSearchValue().split(":");
        assertEquals("1001", result[0]);
        assertEquals("1002", result[1]);
        assertEquals("1003", result[2]);
        assertEquals("holden 1001 - holden 1002 - holden 1003", cascadingSelectValue.toString());
        _verifyAll();
    }

    @Override
    public MockControl[] _getRegisteredMockControllers() {
        return new MockControl[] {ctrlOptionsManager};
    }
}