package com.sourcesense.jira.mocks;

import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutStorageException;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.opensymphony.user.User;

/**
 * User: fabio
 * Date: Jun 13, 2007
 * Time: 2:40:35 PM
 */
public class MockFieldVisibilityBean extends FieldVisibilityBean {

    @Override
    public boolean isFieldHidden(User remoteUser, String id) throws FieldLayoutStorageException {
        return false;
    }

    @Override
    public boolean isFieldHidden(String fieldId, GenericValue issue) {
        return false;
    }

    @Override
    public boolean isCustomFieldHidden(Long projectId, Long customFieldId, String issueTypeId) {
        return false;
    }

    @Override
    public boolean isFieldHidden(Long projectId, String fieldId, Long issueTypeId) {
        return false;
    }

    @Override
    public boolean isFieldHidden(Long projectId, String fieldId, String issueTypeId) {
        return false;
    }

    @Override
    public boolean isFieldHiddenInAllSchemes(Long projectId, String fieldId, List issueTypes) {
        return false;
    }

    @Override
    public boolean isFieldHiddenInAllSchemes(Long projectId, String fieldId) {
        return false;
    }

    @Override
    public boolean isFieldHiddenInAllSchemes(String fieldId, SearchContext context, User user) {
        return false;
    }
}
