package com.sourcesense.jira.customfield.type;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Cowell
 */
public class MultiLevelCascadingSelectCFTypeTest {

    private static final PersistenceFieldType CASCADE_VALUE_TYPE = MultiLevelCascadingSelectCFType.CASCADE_VALUE_TYPE;
    private static final Long ISSUE_ID = 1L;

    private MultiLevelCascadingSelectCFType cfType;
    private CustomFieldValuePersister customFieldValuePersister;
    private GenericConfigManager genericConfigManager;
    private CustomField customField;
    private Issue issue;

    @Before
    public void setUp() throws Exception {
        customField = mock(CustomField.class);
        issue = mock(Issue.class);
        when(issue.getId()).thenReturn(ISSUE_ID);

        OptionsManager optionsManager = mock(OptionsManager.class);
        customFieldValuePersister = mock(CustomFieldValuePersister.class);
        genericConfigManager = mock(GenericConfigManager.class);
        JqlSelectOptionsUtil jqlSelectOptionsUtil = mock(JqlSelectOptionsUtil.class);

        cfType = new MultiLevelCascadingSelectCFType(optionsManager, customFieldValuePersister, genericConfigManager, jqlSelectOptionsUtil, null);
    }

    @Test
    public void createValueWithOneLevel() throws Exception {
        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", mockOption(10000L, null));

        cfType.createValue(customField, issue, cascadingOptions);

        verify(customFieldValuePersister).createValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10000"), null);
    }

    @Test
    public void createValueWithTwoLevels() throws Exception {
        Option parentOption = mockOption(10000L, null);
        Option childOption = mockOption(10001L, parentOption);

        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", parentOption);
        cascadingOptions.put("1", childOption);

        cfType.createValue(customField, issue, cascadingOptions);

        verify(customFieldValuePersister).createValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10000"), null);
        verify(customFieldValuePersister).createValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10001"), "10000");
    }

    @Test
    public void createValueWithThreeLevels() throws Exception {
        Option parentOption = mockOption(10000L, null);
        Option childOption1 = mockOption(10001L, parentOption);
        Option childOption2 = mockOption(10002L, childOption1);

        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", parentOption);
        cascadingOptions.put("1", childOption1);
        cascadingOptions.put("2", childOption2);

        cfType.createValue(customField, issue, cascadingOptions);

        verify(customFieldValuePersister).createValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10000"), null);
        verify(customFieldValuePersister).createValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10001"), "10000");
        verify(customFieldValuePersister).createValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10002"), "10001");
    }

    @Test
    public void updateValueWithNoLevels() throws Exception {
        Map<String, Option> cascadingOptions = null;

        cfType.updateValue(customField, issue, cascadingOptions);

        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, null);
        verify(customFieldValuePersister, never()).updateValues(eq(customField), eq(ISSUE_ID), eq(CASCADE_VALUE_TYPE), anyCollection(), anyString());
    }

    @Test
    public void updateValueWithOneLevel() throws Exception {
        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", mockOption(10000L, null));

        cfType.updateValue(customField, issue, cascadingOptions);

        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, null);
        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10000"), null);
    }

    @Test
    public void updateValueWithTwoLevels() throws Exception {
        Option parentOption = mockOption(10000L, null);
        Option childOption = mockOption(10001L, parentOption);

        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", parentOption);
        cascadingOptions.put("1", childOption);

        cfType.updateValue(customField, issue, cascadingOptions);

        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, null);
        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10000"), null);
        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10001"), "10000");
    }

    @Test
    public void updateValueWithThreeLevels() throws Exception {
        Option parentOption = mockOption(10000L, null);
        Option childOption1 = mockOption(10001L, parentOption);
        Option childOption2 = mockOption(10002L, childOption1);

        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", parentOption);
        cascadingOptions.put("1", childOption1);
        cascadingOptions.put("2", childOption2);

        cfType.updateValue(customField, issue, cascadingOptions);

        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, null);
        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10000"), null);
        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10001"), "10000");
        verify(customFieldValuePersister).updateValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, Lists.newArrayList("10002"), "10001");
    }

    @Test
    public void nonNullSingleLevelOptionShouldNotBeEqualToNull() throws Exception {
        Option nullOption = null;
        Option option = mockOption(10000L, null);

        assertFalse(cfType.equalsOption(option, nullOption));
        assertFalse(cfType.equalsOption(nullOption, option));
    }

    @Test
    public void nonNullMultipleLevelOptionShouldNotBeEqualToNull() throws Exception {
        Option nullOption = null;
        Option parent = mockOption(10000L, null);
        Option option = mockOption(10001L, parent);

        assertFalse(cfType.equalsOption(option, nullOption));
        assertFalse(cfType.equalsOption(nullOption, option));
    }

    @Test
    public void twoNullOptionsShouldBeEqual() throws Exception {
        Option nullOption1 = null;
        Option nullOption2 = null;

        assertTrue(cfType.equalsOption(nullOption1, nullOption2));
    }

    @Test
    public void anySingleLevelOptionShouldBeEqualToItself() throws Exception {
        Option option = mockOption(10000L, null);

        assertTrue(cfType.equalsOption(option, option));
    }

    @Test
    public void anyMultipleLevelOptionShouldBeEqualToItself() throws Exception {
        Option parent = mockOption(10000L, null);
        Option option = mockOption(10001L, parent);

        assertTrue(cfType.equalsOption(option, option));
    }

    @Test
    public void twoEqualSingleLevelOptionsShouldBeEqual() throws Exception {
        Option option1 = mockOption(10000L, null);
        Option option2 = mockOption(10000L, null);

        assertTrue(cfType.equalsOption(option1, option2));
        assertTrue(cfType.equalsOption(option2, option1));
    }

    @Test
    public void twoEqualMultipleLevelOptionsShouldBeEqual() throws Exception {
        Option parent1 = mockOption(10000L, null);
        Option option1 = mockOption(10001L, parent1);

        Option parent2 = mockOption(10000L, null);
        Option option2 = mockOption(10001L, parent2);

        assertTrue(cfType.equalsOption(option1, option2));
        assertTrue(cfType.equalsOption(option2, option1));
    }

    @Test
    public void optionEqualityShouldBeTransitive() throws Exception {
        Option parent1 = mockOption(10000L, null);
        Option option1 = mockOption(10001L, parent1);

        Option parent2 = mockOption(10000L, null);
        Option option2 = mockOption(10001L, parent2);

        Option parent3 = mockOption(10000L, null);
        Option option3 = mockOption(10001L, parent3);

        assertTrue(cfType.equalsOption(option1, option2));
        assertTrue(cfType.equalsOption(option2, option3));
        assertTrue(cfType.equalsOption(option1, option3));
    }

    @Test
    public void twoSingleLevelOptionsWithDifferentIdsShouldNotBeEqual() throws Exception {
        Option option1 = mockOption(10000L, null);
        Option option2 = mockOption(20000L, null);

        assertFalse(cfType.equalsOption(option1, option2));
        assertFalse(cfType.equalsOption(option2, option1));
    }

    @Test
    public void twoMultipleLevelOptionsWithDifferentIdsShouldNotBeEqual() throws Exception {
        Option parent = mockOption(10000L, null);
        Option option1 = mockOption(10001L, parent);
        Option option2 = mockOption(20001L, parent);

        assertFalse(cfType.equalsOption(option1, option2));
        assertFalse(cfType.equalsOption(option2, option1));
    }

    @Test
    public void twoMultipleLevelOptionsWithDifferentParentsShouldNotBeEqual() throws Exception {
        Option parent1 = mockOption(10000L, null);
        Option option1 = mockOption(10001L, parent1);

        Option parent2 = mockOption(20000L, null);
        Option option2 = mockOption(10001L, parent2);

        assertFalse(cfType.equalsOption(option1, option2));
        assertFalse(cfType.equalsOption(option2, option1));
    }

    @Test
    public void twoOptionsWithDifferentLevelsShouldNotBeEqual() throws Exception {
        Option option1 = mockOption(10001L, null);
        Option parent2 = mockOption(10000L, null);
        Option option2 = mockOption(10001L, parent2);

        assertFalse(cfType.equalsOption(option1, option2));
        assertFalse(cfType.equalsOption(option2, option1));
    }

    @Test
    public void getDefaultValueOfSingleLevelField() throws Exception {
        Option option = mockOption(10000L, null);

        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", option);

        FieldConfig fieldConfig = mock(FieldConfig.class);
        when(fieldConfig.getId()).thenReturn(123L);
        when(fieldConfig.getCustomField()).thenReturn(customField);

        when(genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, String.valueOf(123L)))
                .thenReturn(new CustomFieldParamsImpl(customField, cascadingOptions));

        Map<String, Option> defaultValue = cfType.getDefaultValue(fieldConfig);
        assertNotNull(defaultValue);
        assertEquals(option.getOptionId(), defaultValue.get("0").getOptionId());
    }

    @Test
    public void getDefaultValueOfMultipleLevelField() throws Exception {
        Option parentOption = mockOption(10000L, null);
        Option childOption = mockOption(10001L, parentOption);

        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", parentOption);
        cascadingOptions.put("1", childOption);

        FieldConfig fieldConfig = mock(FieldConfig.class);
        when(fieldConfig.getId()).thenReturn(123L);
        when(fieldConfig.getCustomField()).thenReturn(customField);

        when(genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, String.valueOf(123L)))
                .thenReturn(new CustomFieldParamsImpl(customField, cascadingOptions));

        Map<String, Option> defaultValue = cfType.getDefaultValue(fieldConfig);
        assertNotNull(defaultValue);
        assertEquals(parentOption.getOptionId(), defaultValue.get("0").getOptionId());
        assertEquals(childOption.getOptionId(), defaultValue.get("1").getOptionId());
    }

    private Option mockOption(Long optionId, Option parent) {
        Option option = mock(Option.class);
        when(option.getOptionId()).thenReturn(optionId);
        when(option.getParentOption()).thenReturn(parent);
        return option;
    }
}
