package com.sourcesense.jira.customfield.statistics;

import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.FieldConfigSchemeImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutStorageException;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.map.EasyMap;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.Group;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.user.User;
import com.sourcesense.jira.customfield.MultiLevelCascadingSelectValue;
import com.sourcesense.jira.customfield.statistic.MultiLevelCascadingSelectStatisticsMapper;
import com.sourcesense.jira.mocks.EasyMockTestCase;

/**
 * User: fabio
 * Date: Jun 18, 2007
 * Time: 10:42:06 AM
 */
public class TestMultiLevelCascadingSelectStatisticsMapper extends EasyMockTestCase {
    MockControl mockFieldManager;
    MockControl mockFieldConfig;
    FieldConfigScheme fieldConfigScheme;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GenericValue issueTypeGV = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug", "description", "A Bug"));

        fieldConfigScheme = new FieldConfigSchemeImpl((long)1,"1", "test scheme", "scheme description", EasyMap.build(issueTypeGV.getString("id"), null), null);

        // Add 'anything' to the contexts so we get past is isEnabled() check
//        fieldConfigScheme.setsetContexts(EasyList.build("one value"));

    }

    public void testMultiSearchValue() throws GenericEntityException, ImmutableException, DuplicateEntityException, FieldLayoutStorageException {
        GenericValue issueTypeGV = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug", "description", "A Bug"));

        
        User admin = UserUtils.createUser("admin", "");
        Group group = GroupUtils.getGroupSafely("jira-administrator");
        group.addUser(admin);

        JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
        authenticationContext.setUser(admin);

        CustomFieldManager customFieldManager = ManagerFactory.getCustomFieldManager();
        CustomField customField = customFieldManager.createCustomField("multilevel", "cascading multi level",
                customFieldManager.getCustomFieldType("com.sourcesense.jira.plugin.cascadingselect:multi-level-cascading-select"),
                customFieldManager.getCustomFieldSearcher("com.sourcesense.jira.plugin.cascadingselect:multi-level-cascading-select-searcher"),
                EasyList.build(GlobalIssueContext.getInstance()), EasyList.buildNull());

        FieldConfig ctrlFieldConfig = customField.getReleventConfig(new SearchContextImpl());

        mockFieldManager = MockControl.createControl(FieldManager.class);
        FieldManager ctrlFieldManager = (FieldManager) mockFieldManager.getMock();

        ManagerFactory.addService(FieldManager.class, ctrlFieldManager);

        ctrlFieldManager.isFieldHidden(null, (String) null);
        mockFieldManager.setMatcher(MockControl.ALWAYS_MATCHER);
        mockFieldManager.setReturnValue(false, 2);

        OptionsManager optionsManager = ManagerFactory.getOptionsManager();
        _startTestPhase();

        Option option1 = optionsManager.createOption(ctrlFieldConfig, null, new Long(1), "option 1");
        Option option2 = optionsManager.createOption(ctrlFieldConfig, option1.getOptionId(), new Long(2), "option 2");
        Option option3 = optionsManager.createOption(ctrlFieldConfig, option2.getOptionId(), new Long(3), "option 3");
        Option option4 = optionsManager.createOption(ctrlFieldConfig, option1.getOptionId(), new Long(4), "option 4");

        MultiLevelCascadingSelectStatisticsMapper mapper = new MultiLevelCascadingSelectStatisticsMapper(customField, optionsManager);

        MultiLevelCascadingSelectValue value = new MultiLevelCascadingSelectValue(optionsManager, option1.getOptionId() + ":" + option2.getOptionId() + ":" + option3.getOptionId());
        SearchRequest sr = mapper.getSearchUrlSuffix(value, new SearchRequest());
        assertEquals("&"+customField.getId()+"="+option1.getOptionId()+"&"+customField.getId()+":1="+option2.getOptionId()+"&"+customField.getId()+":2="+option3.getOptionId(), sr.getQuery().getQueryString());

        MultiLevelCascadingSelectValue value2 = new MultiLevelCascadingSelectValue(optionsManager, option1.getOptionId() + ":" + option4.getOptionId());
        SearchRequest searchRequest2 = mapper.getSearchUrlSuffix(value2, new SearchRequest());
        assertEquals("&"+customField.getId()+"="+option1.getOptionId()+"&"+customField.getId()+":1="+option4.getOptionId(), searchRequest2.getQuery());
        _verifyAll();
    }

    @Override
    public MockControl[] _getRegisteredMockControllers() {
        return new MockControl[]{mockFieldManager};
    }
}
