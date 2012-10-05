package com.sourcesense.jira.customfield.type;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.config.item.DefaultValueConfigItem;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.beans.CustomFieldOptionJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraUtils;
import com.google.common.collect.Lists;
import com.sourcesense.jira.common.OptionsMap;
import com.sourcesense.jira.customfield.MultiLevelCascadingSelectCustomFieldOperationsHandler;
import com.sourcesense.jira.customfield.admin_config.SettableMultiLevelOptionsConfigItem4;

import javax.annotation.Nullable;

/**
 * This class represents the MultiLevelCascading Select Custom Field Type.
 * Rimane il dubbio se è compatibile tutta la classe con 5.0 o se devo riscriverla secondo questa linea , attendo source code
 *
 * @author Alessandro Benedetti
 */

public class MultiLevelCascadingSelectCFType extends CascadingSelectCFType {
    public static String EMPTY_VALUE = "_none_";

    public static String EMPTY_VALUE_ID = "-2";

    public static long EMPTY_VALUE_ID_LONG = -2;

    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;

    private final OptionsManager optionsManager;
    private final CustomFieldValuePersister customFieldValuePersister;
    private final GenericConfigManager genericConfigManager;
    private final JiraBaseUrls jiraBaseUrls;

    public MultiLevelCascadingSelectCFType(
            OptionsManager optionsManager,
            CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager,
            JqlSelectOptionsUtil jqlSelectOptionsUtil,
            JiraBaseUrls jiraBaseUrls
    ) {
        super(optionsManager, customFieldValuePersister, genericConfigManager, jiraBaseUrls);//null is a test value for JiraBaseUrls
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
        this.optionsManager = optionsManager;
        this.customFieldValuePersister = customFieldValuePersister;
        this.jiraBaseUrls = jiraBaseUrls;
        this.genericConfigManager = genericConfigManager;
    }

    // --------------------------------------------------------------------------------------------- Persistance Methods

    //these methods all operate on the object level

    /**
     * Create a cascading select-list instance for an issue.
     * <p/>
     * Updated 5.04 TO CHECK
     */
    @Override
    public void createValue(CustomField field, Issue issue, Map<String, Option> cascadingOptions) {
        Option currentOpt;
        Option currentParent;
        String parentId = null;
        Set<String> levels = cascadingOptions.keySet();
        for (int i = 0; i < levels.size(); i++) {
            currentOpt = cascadingOptions.get("" + i);
            if (i > 0) {
                currentParent = cascadingOptions.get("" + (i - 1));
                parentId = currentParent.getOptionId().toString();
            }
            if (currentOpt != null) {
                customFieldValuePersister.createValues(field, issue.getId(), CASCADE_VALUE_TYPE, Lists.newArrayList(currentOpt.getOptionId().toString()), parentId);
            }
        }
    }

    /**
     * Updated to 5.04 TO CHECK
     */
    @Override
    public void updateValue(CustomField field, Issue issue, Map<String, Option> cascadingOptions) {
        // clear old stuff first
        customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, null);

        if (cascadingOptions != null) {
            Option currentOpt;
            Option currentParent;
            String parentId = null;
            Set<String> levels = cascadingOptions.keySet();
            for (int i = 0; i < levels.size(); i++) {
                currentOpt = cascadingOptions.get("" + i);
                if (i > 0) {
                    currentParent = cascadingOptions.get("" + (i - 1));
                    parentId = currentParent.getOptionId().toString();
                }
                if (currentOpt != null) {
                    customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, Lists.newArrayList(currentOpt.getOptionId().toString()), parentId);
                }
            }
        }
    }

    /*
     * Ok 5.0
     */
    public boolean equalsOption(Option op1, Option op2) {
        if (op1 == null || op2 == null) {
            return op1 == op2;
        }

        if (equalsOption(op1.getParentOption(), op2.getParentOption())) {
            if (op1.getOptionId() == null) {
                return op2.getOptionId() == null;
            }
            return (op1.getOptionId().equals(op2.getOptionId()));
        }
        return false;
    }

    /**
     * takes in input a fileConfig and an Option, then it extracts the list of options from the input
     * config and from this list ,it checks if the input option belong to the selected Set.
     * Ok 5.0
     *
     * @param config
     * @param option
     * @return
     */
    public boolean optionValidForConfig(FieldConfig config, Option option) {
        final Options options = optionsManager.getOptions(config);
        if (options != null && option != null) {
            Option realOption = options.getOptionById(option.getOptionId());
            return equalsOption(realOption, option);
        }
        return false;
    }

    /**
     * checks the input Option verifying that: 1)it's null 2)it's valid for the input FileConfig
     * 3)it's son of the parent in input
     * Ok 5.0
     *
     * @param customFieldId
     * @param option
     * @param parentOption
     * @param errorCollectionToAddTo
     * @param config
     * @return
     */
    private boolean checkOption(String customFieldId, Option option, Option parentOption, ErrorCollection errorCollectionToAddTo, FieldConfig config) {
        if (option == null) {
            errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.parent", "'" + parentOption + "'", "'" + config.getName() + "'"));
            return false;
        }
        if (!optionValidForConfig(config, option)) {
            errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.for.context", "'" + parentOption + "'", "'" + config.getName() + "'"));
            return false;
        }

        return true;
    }


    /*Ok 5.0*/
    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config) {
        log.debug("Pre- Validate Error collection: [" + errorCollectionToAddTo.getErrors() + "]");

        if (relevantParams == null || relevantParams.isEmpty()) {
            return;
        }

        Option parentOption = null;
        String customFieldId = config.getCustomField().getId();
        int count = relevantParams.getAllKeys().size();
        for (int i = 0; i < count; i++) {
            Option option1 = transformToOption(config, relevantParams.getFirstValueForKey(i == 0 ? null : String.valueOf(i)));
            if (option1 != null && !option1.toString().contains(":") && !option1.toString().equals(EMPTY_VALUE)) {
                if (option1 != null) {
                    log.debug("check option: [" + option1 + "]");
                    if (!checkOption(customFieldId, option1, parentOption, errorCollectionToAddTo, config)) {
                        return;
                    }
                }
                parentOption = option1;
            } else {
                Collection<String> valueStrings = relevantParams.getAllValues();
                String[] splittedStrings = null;
                for (String s : valueStrings) {
                    splittedStrings = s.split(":");
                }
                for (int j = 0; j < splittedStrings.length; j++) {
                    // this part probably is useless, but for sure is not useful for "none" options
                    if (!splittedStrings[j].equals(EMPTY_VALUE_ID)) {
                        Long longOptionValue = new Long(splittedStrings[j]);
                        Option option = jqlSelectOptionsUtil.getOptionById(longOptionValue);
                        if (option != null) {
                            log.debug("check option: [" + option + "]");
                            if (!checkOption(customFieldId, option, parentOption, errorCollectionToAddTo, config)) {
                                return;
                            }
                        }
                        parentOption = option;
                    }
                }

            }
        }
        log.debug("Post-Validate Error collection: [" + errorCollectionToAddTo.getErrors() + "]");
    }

    /**
     * add to the default file Config the specific ConfigItem for the multi level cascading select
     * custom field
     * Ok 5.0
     *
     * @see com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType#getConfigurationItemTypes()
     */
    @Override
    public List getConfigurationItemTypes() {
        final List configurationItemTypes = Lists.newArrayList(JiraUtils.loadComponent(DefaultValueConfigItem.class));
        configurationItemTypes.add(new SettableMultiLevelOptionsConfigItem4(optionsManager));
        return configurationItemTypes;
    }

    public OptionsMap getOptionMapFromOptions(Options options) {

        return new OptionsMap(options);
    }

    /**
     * return the velocity parameter for the issue and custom field in input no woking for bugged
     *
     * @see com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType#getVelocityParameters(com.atlassian.jira.issue.Issue,
     *      com.atlassian.jira.issue.fields.CustomField,
     *      com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem)
     *      <p/>
     *      Ok 5.0
     */
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        map.put("mlcscftype", this);
        return map;
    }

    // --------------------------------------------------------------------------------------  CustomFieldParams methods

    /**
     * updated 5.04 OK
     */
    public Map<String, Option> getValueFromIssue(CustomField field, Issue issue) {
        Option parentOption = getOptionValueForParentId(field, null, issue);

        if (parentOption != null) {
            Map<String, Option> options = new HashMap<String, Option>();
            options.put("0", parentOption);

            int i = 1;
            while (true) {
                Option childOption = getOptionValueForParentId(field, parentOption.getOptionId().toString(), issue);

                if (childOption != null) {
                    options.put("" + i, childOption);
                    i++;
                    parentOption = childOption;
                } else {
                    break;
                }
            }
            return options;
        } else {
            return null;
        }
    }

    @Nullable
    private Option getOptionValueForParentId(CustomField field, @Nullable String sParentOptionId, Issue issue) {
        Collection values = customFieldValuePersister.getValues(field, issue.getId(), CASCADE_VALUE_TYPE, sParentOptionId);

        if (values != null && !values.isEmpty()) {
            String optionId = (String) values.iterator().next();
            return optionsManager.findByOptionId(OptionUtils.safeParseLong(optionId));
        } else {
            return null;
        }
    }


    public Map<String, Option> getValueFromCustomFieldParams(CustomFieldParams relevantParams) throws FieldValidationException {
        if (relevantParams != null && !relevantParams.isEmpty()) {
            return getOptionMapFromCustomFieldParams(relevantParams);
        } else {
            return null;
        }

    }

    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters) {
        return parameters;
    }


    //----------------------------------------------------------------------------------------- - Private Helper Methods
    /*Builds an optionMap for the customFieldParams,
   * Jira 5.04 OK
   * */
    private Map<String, Option> getOptionMapFromCustomFieldParams(CustomFieldParams params) throws FieldValidationException {
        Map<String, Option> options = new HashMap<String, Option>();

        int count = params.getAllKeys().size();
        for (int i = 0; i < count; i++) {
            Option option1 = transformToOption(null, params.getFirstValueForKey(i == 0 ? null : String.valueOf(i)));
            if (option1 != null && !option1.toString().contains(":") && !option1.toString().equals(EMPTY_VALUE)) {
                if (option1 != null) {
                    log.debug("check option: [" + option1 + "]");
                    options.put("" + i, option1);
                }
            }
        }


        return options;
    }

    /*
     * forse inutile fare l'override, vediamo
     */
    private Option extractOptionFromParams(String key, CustomFieldParams relevantParams) throws FieldValidationException {
        Collection<String> params = relevantParams.getValuesForKey(key);
        if (params != null && !params.isEmpty()) {
            String selectValue = params.iterator().next();
            if (ObjectUtils.isValueSelected(selectValue) && selectValue != null) {
                return (Option) this.getSingularObjectFromString((String) selectValue);
            }
        }

        return null;
    }


    /*
     * transforms the object(Option) in input in an Option.
     * Ok 5.0
     */
    private Option transformToOption(FieldConfig config, Object value) {
        if (value instanceof Option) {
            return (Option) value;
        }
        if (value instanceof String && EMPTY_VALUE_ID.equals(value)) {
            return this.optionsManager.createOption(config, EMPTY_VALUE_ID_LONG, EMPTY_VALUE_ID_LONG, EMPTY_VALUE);
        } else if (value instanceof String && !"-1".equals(value) && !"-3".equals(value)) {
            return this.getSingularObjectFromString((String) value);
        }
        return null;
    }

    public String getChangelogValue(CustomField field, Map<String, Option> cascadingOptions) {
        if (cascadingOptions != null) {
            StringBuilder sb = new StringBuilder();
            Option currentOpt;
            Set<String> levels = cascadingOptions.keySet();
            for (int i = 0; i < levels.size(); i++) {
                currentOpt = cascadingOptions.get("" + i);
                if (currentOpt != null) {
                    sb.append("Level: " + i);
                    sb.append(" -> " + currentOpt.getValue() + " ");
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }
//-------------------------------------------------------------------------------------------------------- Defaults

    @Override
    public Map<String, Option> getDefaultValue(FieldConfig fieldConfig) {
        final Object o = genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (o != null) {
            final CustomFieldParams params = new CustomFieldParamsImpl(fieldConfig.getCustomField(), o);
            //return getOptionMapFromCustomFieldParams(params);
            Map<String, Option> options = new HashMap<String, Option>();

            int count = params.getAllKeys().size();
            for (int i = 0; i < count; i++) {
                Option option = transformToOption(null, params.getFirstValueForKey(String.valueOf(i)));
                if (option != null) {
                    options.put("" + i, option);
                }
            }

            return options;
        } else {
            return null;
        }
    }

    @Override
    public void setDefaultValue(FieldConfig fieldConfig, Map<String, Option> cascadingOptions) {
        if (cascadingOptions != null) {
            final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(fieldConfig.getCustomField(), cascadingOptions);
            customFieldParams.transformObjectsToStrings();
            customFieldParams.setCustomField(null);

            genericConfigManager.update(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), customFieldParams);
        } else {
            genericConfigManager.update(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), null);
        }
    }


//---------------------------------- Json representation


    /**
     * I'dont' know where is used
     */
    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, FieldLayoutItem fieldLayoutItem) {
        Map<String, Option> options = getValueFromIssue(field, issue);
        Option parent;
        Option child;
        if (options == null) {
            return new FieldJsonRepresentation(new JsonData(null));
        }
        FieldJsonRepresentation fieldJsonRepresentation = new FieldJsonRepresentation(null);
        for (int i = 1; i < options.size(); i++) {
            parent = options.get("" + (i - 1));
            child = options.get("" + i);
            JsonData jsonData = new JsonData(CustomFieldOptionJsonBean.shortBean(parent, child, jiraBaseUrls));
            fieldJsonRepresentation.setRenderedData(jsonData);
        }
        return fieldJsonRepresentation;
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field) {
        return new MultiLevelCascadingSelectCustomFieldOperationsHandler(optionsManager, field, getI18nBean());
    }

    /*
   * Non corretto, cercare come poterlo fare
   * */
    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field) {
        FieldConfig fieldConfig = field.getRelevantConfig(issueCtx);
        final Object o = genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (o != null) {
            final CustomFieldParams params = new CustomFieldParamsImpl(fieldConfig.getCustomField(), o);
            Map<String, Option> options = getOptionMapFromCustomFieldParams(params);
            if (options == null) {
                return new JsonData(null);
            }
            Option parent = options.get(PARENT_KEY);
            Option child = options.get(CHILD_KEY);
            JsonData jsonData = new JsonData(CustomFieldOptionJsonBean.shortBean(parent, child, jiraBaseUrls));
            System.out.println(jsonData.asString());
            return jsonData;
        } else {
            return null;
        }
    }

    private final Logger log = Logger.getLogger(MultiLevelCascadingSelectCFType.class);
}
