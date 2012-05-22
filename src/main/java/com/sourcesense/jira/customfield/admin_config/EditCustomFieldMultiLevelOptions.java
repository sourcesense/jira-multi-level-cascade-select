package com.sourcesense.jira.customfield.admin_config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import webwork.action.Action;
import webwork.action.ActionContext;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;
import com.atlassian.jira.web.action.admin.customfields.EditCustomFieldDefaults;
import com.opensymphony.util.TextUtils;
import com.sourcesense.jira.customfield.MutableOption;

/**
 * This class is the same of the Plug-in 3.0.
 * At the moment seems to not need any change or adaptation.
 * @author Fabio 
 * Date:  5/11/2010 (date of the conversion by Alessandro Benedetti)
 */
public class EditCustomFieldMultiLevelOptions extends AbstractEditConfigurationItemAction {
  // ------------------------------------------------------------------------------------------------------
  // Properties

  private String addValue;

  private String selectedValue;

  private boolean confirm;

  private Collection<String> hlFields;

  private Long selectedParentOptionId;

  private Map customFieldValuesHolder = new HashMap();

  private Options options;

  private Object defaultValues;

  private static final String NEW_OPTION_POSITION_PREFIX = "newOptionPosition_";

  private static final String NEW_LABEL_PREFIX = "newLabel_";

  private static final String ACTION_NAME = "EditCustomFieldMultiLevelOptions";

  // ----------------------------------------------------------------------------------------------------
  // Dependencies

  private final IssueManager issueManager;

  private final OptionsManager optionsManager;

  // ----------------------------------------------------------------------------------------------------
  // Constructors
  public EditCustomFieldMultiLevelOptions(IssueManager issueManager, OptionsManager optionsManager) {
    this.issueManager = issueManager;
    this.optionsManager = optionsManager;
    this.hlFields = new LinkedList<String>();
  }

  // --------------------------------------------------------------------------------------------------
  // Action Methods
  @Override
  public String doDefault() throws Exception {
    setReturnUrl(null);
    if (!(getCustomField().getCustomFieldType() instanceof MultipleSettableCustomFieldType))
      addErrorMessage(getText("admin.errors.customfields.cannot.set.options", "'" + getCustomField().getCustomFieldType().getName() + "'"));

    EditCustomFieldDefaults.populateDefaults(getFieldConfig(), customFieldValuesHolder);

    return super.doDefault();
  }

  @Override
  protected void doValidation() {
    if (getCustomField() == null) {
      addErrorMessage(getText("admin.errors.customfields.no.field.selected.for.edit"));
    }
  }

  public String doConfigureOption() {
    Map parameters = ActionContext.getParameters();
    if (parameters.containsKey("moveOptionsToPosition")) {
      // Move the options to a different position
      return changeOptionPositions(parameters);
    }

    if (parameters.containsKey("saveLabel")) {
      // Move the options to a different position
      return changeOptionsLabel(parameters);
    }

    throw new IllegalStateException("Unknown operation.");
  }

  /**
   * operates the change of Options Position (Up and Down in the edit config scenario) If the change
   * of position is not possible,an error is returned.
   * 
   * @param parameters
   * @return
   */
  private String changeOptionPositions(Map<String, Object> parameters) {
    Map<Integer, Option> optionPositions = new TreeMap<Integer, Option>();
    // Loop through the submitted parameters and find out which options to move
    for (String paramName : parameters.keySet()) {
      if (paramName.startsWith(NEW_OPTION_POSITION_PREFIX) && TextUtils.stringSet(getTextValueFromParams(paramName))) {
        String fieldId = paramName.substring(NEW_OPTION_POSITION_PREFIX.length());
        Integer newOptionPosition = null;
        try {
          newOptionPosition = Integer.valueOf(getTextValueFromParams(paramName));
          Integer newIndex = new Integer(newOptionPosition.intValue() - 1);
          if (newOptionPosition.intValue() <= 0 || newOptionPosition.intValue() > getDisplayOptions().size()) {
            addError(paramName, getText("admin.errors.invalid.position"));
          } else if (optionPositions.containsKey(newIndex)) {
            addError(paramName, getText("admin.errors.invalid.position"));
          } else {
            optionPositions.put(newIndex, getOptions().getOptionById(Long.decode(fieldId)));
          }
        } catch (NumberFormatException e) {
          addError(paramName, getText("admin.errors.invalid.position"));
        }
      }
    }

    if (!invalidInput()) {
      getOptions().moveOptionToPosition(optionPositions);
      // Mark fields for highlighting
      for (Iterator<Option> iterator = optionPositions.values().iterator(); iterator.hasNext();) {
        populateHlField(iterator.next());
      }

      return redirectToView();
    }

    return getResult();
  }

  /**
   * operates the function of renaming the label of an option
   * 
   * @param parameters
   * @return
   */
  private String changeOptionsLabel(Map<String, Object> parameters) {
    List<Option> options = new ArrayList<Option>();
    for (String paramName : parameters.keySet()) {
      if (paramName.startsWith(NEW_LABEL_PREFIX) && TextUtils.stringSet(getTextValueFromParams(paramName))) {
        String fieldId = paramName.substring(NEW_LABEL_PREFIX.length());
        String value = getTextValueFromParams(paramName);
        MutableOption option = new MutableOption(optionsManager.findByOptionId(OptionUtils.safeParseLong(fieldId)));
        if (!option.getValue().equals(value)) {
          option.setValue(value);
          options.add(option);
        }
      }
    }
    if (!options.isEmpty()) {
      optionsManager.updateOptions(options);
      populateHlField(options);
    }
    return redirectToView();
  }

  /**
   * operates a redirection from a view of the Custom Field to another.
   * 
   * @return
   */
  private String redirectToView() {
    StringBuffer redirectUrl = new StringBuffer(ACTION_NAME).append("!default.jspa?fieldConfigId=").append(getFieldConfigId());
    if (getSelectedParentOptionId() != null) {
      redirectUrl.append("&selectedParentOptionId=" + getSelectedParentOptionId());
    }
    for (Iterator<String> iterator = hlFields.iterator(); iterator.hasNext();) {
      redirectUrl.append("&currentOptions=").append(iterator.next());
    }
    return getRedirect(redirectUrl.toString());
  }

  /**
   * Extracts the text value from an input param
   * 
   * @param newPositionFieldName
   * @return
   */
  private String getTextValueFromParams(String newPositionFieldName) {
    String[] newFieldPositionArray = (String[]) ActionContext.getParameters().get(newPositionFieldName);

    if (newFieldPositionArray != null && newFieldPositionArray.length > 0)
      return newFieldPositionArray[0];
    else
      return "";
  }

  public String doAdd() throws Exception {
    doValidation();
    if (!TextUtils.stringSet(addValue)) {
      addError("addValue", getText("admin.errors.customfields.invalid.select.list.value"));
    }

    if (invalidInput())
      return getResult();

    Options options = getOptions();

    if (options.getOptionForValue(getAddValue(), getSelectedParentOptionId()) != null) {
      addError("addValue", getText("admin.errors.customfields.value.already.exists"));
      return Action.ERROR;
    }

    // set the options
    options.addOption(options.getOptionById(getSelectedParentOptionId()), getAddValue());
    if (!getDisplayOptions().isEmpty())
      hlFields.add(getAddValue());

    return redirectToView();

  }

  public String doSort() throws Exception {
    getOptions().sortOptionsByValue(getSelectedParentOption());

    return getRedirect(getRedirectUrl());
  }

  public String doMoveToFirst() throws Exception {
    populateHlField(getSelectedOption());
    getOptions().moveToStartSequence(getSelectedOption());

    return redirectToView();
  }

  public String doMoveUp() throws Exception {
    populateHlField(getSelectedOption());
    getOptions().decrementSequence(getSelectedOption());

    return redirectToView();
  }

  public String doMoveDown() throws Exception {
    populateHlField(getSelectedOption());
    getOptions().incrementSequence(getSelectedOption());

    return redirectToView();
  }

  public String doMoveToLast() throws Exception {
    populateHlField(getSelectedOption());
    getOptions().moveToLastSequence(getSelectedOption());

    return redirectToView();
  }

  private void populateHlField(Option option) {
    hlFields.add(option.getValue());
  }

  private void populateHlField(List<Option> options) {
    for (Option opt : options) {
      populateHlField(opt);
    }
  }

  public String getNewPositionTextBoxName(int optionId) {
    return NEW_OPTION_POSITION_PREFIX + optionId;
  }

  public String getNewLabelTextBoxName(int optionId) {
    return NEW_LABEL_PREFIX + optionId;
  }

  public String getNewPositionValue(int optionId) {
    return getTextValueFromParams(getNewPositionTextBoxName(optionId));
  }

  public String doRemove() throws Exception {
    if (!confirm)
      return "confirmdelete";

    removeValuesFromIssues();
    getOptions().removeOption(getSelectedOption());

    return getRedirect(getRedirectUrl());
  }

  @Override
  protected String doExecute() throws Exception {
    return INPUT;
  }

  // --------------------------------------------------------------------------------------------------
  // Helper Methods

  private void removeValuesFromIssues() {
    Collection<Issue> issues = getAffectedIssues();
    for (Issue issue : issues) {
      MultipleSettableCustomFieldType customFieldType = (MultipleSettableCustomFieldType) getCustomField().getCustomFieldType();
      customFieldType.removeValue(getCustomField(), issue, getSelectedOption());
    }
  }

  public Collection<Issue> getAffectedIssues() {
    final MultipleSettableCustomFieldType customFieldType = (MultipleSettableCustomFieldType) getCustomField().getCustomFieldType();
    Set<Long> ids = customFieldType.getIssueIdsWithValue(getCustomField(), getSelectedOption());
    Collection<Issue> issues = new ArrayList<Issue>(ids.size());
    for (Long id : ids) {
      final Issue issue = IssueImpl.getIssueObject(issueManager.getIssue(id));
      final FieldConfig relevantConfigFromGv = getCustomField().getRelevantConfig(issue);
      if (getFieldConfig().equals(relevantConfigFromGv)) {
        issues.add(issue);
      }
    }
    return issues;
  }

  // -------------------------------------------------------------------------------------------
  // Non-trivial accessors

  // ------------------------------------------------------------------------------------------
  // Private Helper Methods

  public Options getOptions() {
    if (options == null) {
      Long selectedParentOptionId = getSelectedParentOptionId();
      options = getCustomField().getOptions(selectedParentOptionId != null ? selectedParentOptionId.toString() : null, getFieldConfig(), null);
    }
    return options;
  }

  public Collection<Option> getDisplayOptions() {
    final Options options = getOptions();
    Collection<Option> result = new ArrayList<Option>();
    final Option parentOption = options.getOptionById(getSelectedParentOptionId());
    if (parentOption != null) {
      return parentOption.getChildOptions();
    } else {
      return options;
    }
  }

  public Option getSelectedOption() {
    return getOptions().getOptionById(OptionUtils.safeParseLong(getSelectedValue()));
  }

  public Option getSelectedParentOption() {
    return getOptions().getOptionById((getSelectedParentOptionId()));
  }

  public Object getDefaultValues() {
    if (defaultValues == null) {
      Object dbDefaultValues = getCustomField().getCustomFieldType().getDefaultValue(getFieldConfig());
      if (dbDefaultValues instanceof String) {
        final Collection<Object> tempCollection = new ArrayList<Object>(1);
        tempCollection.add(dbDefaultValues);
        defaultValues = tempCollection;
      } else {
        defaultValues = dbDefaultValues;
      }
    }

    return defaultValues;
  }

  public boolean isDefaultValue(String value) {
    Object defaults = getDefaultValues();

    if (defaults instanceof Collection) {
      Collection defCollection = (Collection) defaults;

      Option option = options.getOptionById(OptionUtils.safeParseLong(value));

      if (option != null) {
        return defCollection.contains(option.getValue());
      } else {
        return false;
      }
    } else if (defaults instanceof CustomFieldParams) {
      CustomFieldParams fieldParams = (CustomFieldParams) defaults;
      Collection allFieldValues = fieldParams.getAllValues();
      for (Object defaultOptionId : allFieldValues) {
        if (value != null && value.equals(defaultOptionId)) {
          return true;
        } else if (defaultOptionId instanceof Option) {
          Option option = (Option) defaultOptionId;
          if (option.getOptionId().toString().equals(value))
            return true;
        }

      }
    }

    return false;
  }

  public int getButtonRowSize() {
    int rowSize = 2;
    if (getDisplayOptions().size() > 1)
      rowSize++;

    return rowSize;
  }

  public boolean isCascadingSelect() {
    return getCustomField().getCustomFieldType() instanceof CascadingSelectCFType;
  }

  private String getRedirectUrl() {
    if (getSelectedParentOptionId() == null) {
      return getBaseUrl();
    } else {
      return getUrlWithParent("default");
    }
  }

  private String getBaseUrl() {
    return getBaseUrl("default");
  }

  private String getBaseUrl(String action) {
    return ACTION_NAME + "!" + action + ".jspa?fieldConfigId=" + getFieldConfig().getId();
  }

  public String getSelectedParentOptionUrlPreifx() {
    return getBaseUrl() + "&selectedParentOptionId=";
  }

  public String getSelectedParentOptionUrlPrefix(String action) {
    return getBaseUrl(action) + "&selectedParentOptionId=";
  }

  public String getDoActionUrl(Option option, String action) {

    return getUrlWithParent(action) + "&selectedValue=" + (option != null ? option.getOptionId().toString() : "");
  }

  public String getUrlWithParent(String action) {
    if (getSelectedParentOptionId() == null) {
      return getBaseUrl(action);
    } else {
      return getBaseUrl(action) + "&selectedParentOptionId=" + getSelectedParentOptionId();

    }
  }

  // ---------------------------------------------------------------------- Accessors & Mutators for
  // action properties
  public String getAddValue() {
    return addValue;
  }

  public void setAddValue(String addValue) {
    this.addValue = addValue;
  }

  public String getSelectedValue() {
    return selectedValue;
  }

  public void setSelectedValue(String selectedValue) {
    this.selectedValue = selectedValue;
  }

  public Long getSelectedParentOptionId() {
    return selectedParentOptionId;
  }

  public void setSelectedParentOptionId(Long selectedParentOptionId) {
    this.selectedParentOptionId = selectedParentOptionId;
  }

  public void setConfirm(boolean confirm) {
    this.confirm = confirm;
  }

  public Collection<String> getHlOptions() {
    return hlFields;
  }

  public void setCurrentOptions(String[] currentFields) {
    this.hlFields = Arrays.asList(currentFields);
  }
}