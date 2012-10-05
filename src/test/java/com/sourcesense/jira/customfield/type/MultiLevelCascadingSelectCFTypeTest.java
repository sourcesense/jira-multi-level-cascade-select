package com.sourcesense.jira.customfield.type;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Alex Cowell
 */
public class MultiLevelCascadingSelectCFTypeTest {

    private static final PersistenceFieldType CASCADE_VALUE_TYPE = MultiLevelCascadingSelectCFType.CASCADE_VALUE_TYPE;
    private static final Long ISSUE_ID = 1L;
    private static final Long CF_ID = 123L;

    private MultiLevelCascadingSelectCFType cfType;
    private CustomFieldValuePersister customFieldValuePersister;
    private GenericConfigManager genericConfigManager;
    private OptionsManager optionsManager;
    private CustomField customField;
    private Issue issue;

    @Before
    public void setUp() throws Exception {
        customField = mock(CustomField.class);
        issue = mock(Issue.class);
        when(issue.getId()).thenReturn(ISSUE_ID);

        optionsManager = mock(OptionsManager.class);
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
        when(fieldConfig.getId()).thenReturn(CF_ID);
        when(fieldConfig.getCustomField()).thenReturn(customField);

        when(genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, CF_ID.toString()))
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
        when(fieldConfig.getId()).thenReturn(CF_ID);
        when(fieldConfig.getCustomField()).thenReturn(customField);

        when(genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, CF_ID.toString()))
                .thenReturn(new CustomFieldParamsImpl(customField, cascadingOptions));

        Map<String, Option> defaultValue = cfType.getDefaultValue(fieldConfig);
        assertNotNull(defaultValue);
        assertEquals(parentOption.getOptionId(), defaultValue.get("0").getOptionId());
        assertEquals(childOption.getOptionId(), defaultValue.get("1").getOptionId());
    }

    @Test
    public void setDefaultValueToNone() throws Exception {
        FieldConfig fieldConfig = mock(FieldConfig.class);
        when(fieldConfig.getId()).thenReturn(CF_ID);

        Map<String, Option> newDefaultValue = null;
        cfType.setDefaultValue(fieldConfig, newDefaultValue);

        verify(genericConfigManager)
                .update(CustomFieldType.DEFAULT_VALUE_TYPE, CF_ID.toString(), null);
    }

    @Test
    public void setDefaultValueToSingleLevelField() throws Exception {
        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", mockOption(10000L, null));

        FieldConfig fieldConfig = mock(FieldConfig.class);
        when(fieldConfig.getId()).thenReturn(CF_ID);
        when(fieldConfig.getCustomField()).thenReturn(customField);
        when(customField.getCustomFieldType()).thenReturn(cfType);

        Map<String, Object> expectedOptions = new HashMap<String, Object>();
        expectedOptions.put("0", "10000");
        CustomFieldParams expectedParams = new CustomFieldParamsImpl(null, expectedOptions);

        cfType.setDefaultValue(fieldConfig, cascadingOptions);

        verify(genericConfigManager)
                .update(CustomFieldType.DEFAULT_VALUE_TYPE, CF_ID.toString(), expectedParams);
    }

    @Test
    public void setDefaultValueToMultipleLevelField() throws Exception {
        Option parentOption = mockOption(10000L, null);
        Option childOption = mockOption(10001L, parentOption);

        Map<String, Option> cascadingOptions = new HashMap<String, Option>();
        cascadingOptions.put("0", parentOption);
        cascadingOptions.put("1", childOption);

        FieldConfig fieldConfig = mock(FieldConfig.class);
        when(fieldConfig.getId()).thenReturn(CF_ID);
        when(fieldConfig.getCustomField()).thenReturn(customField);
        when(customField.getCustomFieldType()).thenReturn(cfType);

        Map<String, Object> expectedOptions = new HashMap<String, Object>();
        expectedOptions.put("0", "10000");
        expectedOptions.put("1", "10001");
        CustomFieldParams expectedParams = new CustomFieldParamsImpl(null, expectedOptions);

        cfType.setDefaultValue(fieldConfig, cascadingOptions);

        verify(genericConfigManager)
                .update(CustomFieldType.DEFAULT_VALUE_TYPE, CF_ID.toString(), expectedParams);
    }

    @Test
    public void getVelocityParametersWithNullIssueShouldNotTryToGetValues() throws Exception {
        FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        Map<String, Object> velocityParameters = cfType.getVelocityParameters(null, customField, fieldLayoutItem);

        assertNotNull(velocityParameters);
        assertEquals(2, velocityParameters.size());
        assertTrue(velocityParameters.containsKey("request"));
        assertEquals(cfType, velocityParameters.get("mlcscftype"));
    }

    @Test
    public void getVelocityParametersWithNonNullIssueShouldPopulateParametersWithValues() throws Exception {
        FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        Map<String, Object> velocityParameters = cfType.getVelocityParameters(issue, customField, fieldLayoutItem);

        assertNotNull(velocityParameters);
        assertEquals(2, velocityParameters.size());
        assertTrue(velocityParameters.containsKey("request"));
        assertEquals(cfType, velocityParameters.get("mlcscftype"));
    }

    @Test
    public void getValueFromIssueWithNoValueShouldReturnNull() throws Exception {
        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, null))
                .thenReturn(Lists.newArrayList());

        Map<String, Option> value = cfType.getValueFromIssue(customField, issue);
        assertNull(value);
    }

    @Test
    public void getValueFromIssueWithJustParentShouldReturnMapWithParentOption() throws Exception {
        Option parentOption = mockOption(10000L, null);

        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, null))
                .thenReturn(Lists.<Object>newArrayList("10000"));
        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, "10000"))
                .thenReturn(Lists.newArrayList());
        when(optionsManager.findByOptionId(parentOption.getOptionId())).thenReturn(parentOption);

        Map<String, Option> value = cfType.getValueFromIssue(customField, issue);
        assertNotNull(value);
        assertEquals(1, value.size());
        assertEquals(parentOption.getOptionId(), value.get("0").getOptionId());
    }

    @Test
    public void getValueFromIssueWithParentAndChildShouldReturnMapWithTwoOptions() throws Exception {
        Option parentOption = mockOption(10000L, null);
        Option childOption = mockOption(10001L, parentOption);

        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, null))
                .thenReturn(Lists.<Object>newArrayList("10000"));
        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, "10000"))
                .thenReturn(Lists.<Object>newArrayList("10001"));
        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, "10001"))
                .thenReturn(Lists.newArrayList());
        when(optionsManager.findByOptionId(parentOption.getOptionId())).thenReturn(parentOption);
        when(optionsManager.findByOptionId(childOption.getOptionId())).thenReturn(childOption);

        Map<String, Option> value = cfType.getValueFromIssue(customField, issue);
        assertNotNull(value);
        assertEquals(2, value.size());
        assertEquals(parentOption.getOptionId(), value.get("0").getOptionId());
        assertEquals(childOption.getOptionId(), value.get("1").getOptionId());
    }

    @Test
    public void getValueFromIssueWithThreeOptionsShouldReturnMapWithThreeOptions() throws Exception {
        Option parentOption = mockOption(10000L, null);
        Option childOption1 = mockOption(10001L, parentOption);
        Option childOption2 = mockOption(10002L, childOption1);

        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, null))
                .thenReturn(Lists.<Object>newArrayList("10000"));
        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, "10000"))
                .thenReturn(Lists.<Object>newArrayList("10001"));
        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, "10001"))
                .thenReturn(Lists.<Object>newArrayList("10002"));
        when(customFieldValuePersister.getValues(customField, ISSUE_ID, CASCADE_VALUE_TYPE, "10002"))
                .thenReturn(Lists.newArrayList());
        when(optionsManager.findByOptionId(parentOption.getOptionId())).thenReturn(parentOption);
        when(optionsManager.findByOptionId(childOption1.getOptionId())).thenReturn(childOption1);
        when(optionsManager.findByOptionId(childOption2.getOptionId())).thenReturn(childOption2);

        Map<String, Option> value = cfType.getValueFromIssue(customField, issue);
        assertNotNull(value);
        assertEquals(3, value.size());
        assertEquals(parentOption.getOptionId(), value.get("0").getOptionId());
        assertEquals(childOption1.getOptionId(), value.get("1").getOptionId());
        assertEquals(childOption2.getOptionId(), value.get("2").getOptionId());
    }

    private Option mockOption(Long optionId, @Nullable Option parent) {
        Option option = mock(Option.class);
        when(option.getOptionId()).thenReturn(optionId);
        when(option.getParentOption()).thenReturn(parent);
        return option;
    }
}
