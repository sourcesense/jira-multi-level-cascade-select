package com.sourcesense.jira.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericPK;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;
import org.ofbiz.core.util.UtilMisc;
import org.ofbiz.core.util.UtilValidate;

import com.atlassian.core.ofbiz.CoreFactory;

public class MockGenericValue extends GenericValue {
    Map fields;
    boolean created = false;
    boolean stored = false;
    boolean refreshed = false;
    boolean removed = false;
    Map related = new HashMap();

    GenericDelegator gd;

    public MockGenericValue(GenericValue value) {
        this(value.getEntityName());
        this.fields = value.getFields(value.getAllKeys());
    }

    public MockGenericValue(String entityName) {
        super(new ModelEntity(), null);
        this.entityName = entityName;
        this.fields = new HashMap();
    }

    public MockGenericValue(String entityName, Map fields) {
        this(entityName);

        if (fields != null)
            this.fields = fields;
    }

    @Override
    public Object get(String name) {
        return fields.get(name);
    }

    @Override
    public void set(String name, Object value) {
        fields.put(name, value);
    }

    @Override
    public Collection getAllKeys() {
        return fields.keySet();
    }

    @Override
    public Map getFields(Collection collection)
    {
        Map selectedFields = new HashMap();
        for (Iterator iterator = collection.iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            selectedFields.put(key, fields.get(key));
        }
        return selectedFields;
    }

    @Override
    public Map getAllFields()
    {
        return fields;
    }

    @Override
    public List getRelated(String s) throws GenericEntityException {
        final Object related = this.related.get(s);
        return related != null ? (List) related : Collections.EMPTY_LIST;
    }

    @Override
    public List getRelated(String s, Map map, List order) throws GenericEntityException {
        return CoreFactory.getGenericDelegator().getRelated(s, map, order, this);
    }

    public void setRelated(String s, List relatedGVs) {
        related.put(s, relatedGVs);
    }

    @Override
    public GenericValue create() throws GenericEntityException {
        created = true;
        return CoreFactory.getGenericDelegator().create(this);
    }

    public boolean isCreated() {
        return created;
    }

    public boolean isStored() {
        return stored;
    }

    public boolean isRemoved() {
        return removed;
    }

    public boolean isRefreshed() {
        return refreshed;
    }

    @Override
    public ModelEntity getModelEntity() {
        return new MockModelEntity(this);
    }

    @Override
    public boolean matchesFields(Map keyValuePairs) {
        if (fields == null) return true;
        if (keyValuePairs == null || keyValuePairs.size() == 0) return true;
        Iterator entries = keyValuePairs.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry anEntry = (Map.Entry) entries.next();
            if (!UtilValidate.areEqual(anEntry.getValue(), this.fields.get(anEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public GenericPK getPrimaryKey() {
        return new GenericPK(getModelEntity(), UtilMisc.toMap("id", fields.get("id")));
    }


    @Override
    public void setDelegator(GenericDelegator internalDelegator) {
        this.gd = internalDelegator;
    }

    @Override
    public GenericDelegator getDelegator() {
        return gd;
    }

    @Override
    public void store() throws GenericEntityException {
        stored = true;
        CoreFactory.getGenericDelegator().store(this);
    }

    @Override
    public void remove() throws GenericEntityException {
        removed = true;
        CoreFactory.getGenericDelegator().removeValue(this);
    }

    @Override
    public void removeRelated(String relationName) throws GenericEntityException {
        related.remove(relationName);
    }

    @Override
    public void refresh() throws GenericEntityException {
        refreshed = true;
        CoreFactory.getGenericDelegator().refresh(this);
    }

    @Override
    public String toString() {
        StringBuffer theString = new StringBuffer();
        theString.append("[GenericEntity:");
        theString.append(getEntityName());
        theString.append(']');

        Iterator entries = fields.entrySet().iterator();
        Map.Entry anEntry = null;
        while (entries.hasNext()) {
            anEntry = (Map.Entry) entries.next();
            theString.append('[');
            theString.append(anEntry.getKey());
            theString.append(',');
            theString.append(anEntry.getValue());
            theString.append(']');
        }
        return theString.toString();
    }

    @Override
    public Object dangerousGetNoCheckButFast(ModelField modelField) {
        if (modelField == null) throw new IllegalArgumentException("Cannot get field with a null modelField");
        return fields.get(modelField.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MockGenericValue)) return false;
        if (!super.equals(o)) return false;

        final MockGenericValue mockGenericValue = (MockGenericValue) o;

        if (fields != null ? !fields.equals(mockGenericValue.fields) : mockGenericValue.fields != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + (fields != null ? fields.hashCode() : 0);
        result = 29 * result + (created ? 1 : 0);
        return result;
    }

    @Override
    public void setString(String s, String s1) {
        this.set(s, s1);
    }

    @Override
    public List getRelatedOrderBy(String relationName, List orderBy) throws GenericEntityException {
        return CoreFactory.getGenericDelegator().getRelatedOrderBy(relationName, orderBy, this);
    }

    @Override
    public List getRelatedByAnd(String relationName, Map fields) throws GenericEntityException {
        return CoreFactory.getGenericDelegator().getRelatedByAnd(relationName, fields, this);
    }

    public class MockModelEntity extends ModelEntity {
        GenericValue value;

        public MockModelEntity() {

        }

        public MockModelEntity(GenericValue value) {
            this.value = value;
            this.setEntityName(value.getEntityName());
        }

        @Override
        public List getAllFieldNames() {
            List fieldnames = new ArrayList();

            for (Iterator iterator = value.getAllKeys().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                fieldnames.add(key);
            }

            return fieldnames;
        }

        @Override
        public ModelField getField(String fieldName) {
            ModelField field = null;

            if (value.getAllKeys().contains(fieldName)) {
                field = new ModelField();
                field.setName(fieldName);
            }

            return field;
        }
    }
}
