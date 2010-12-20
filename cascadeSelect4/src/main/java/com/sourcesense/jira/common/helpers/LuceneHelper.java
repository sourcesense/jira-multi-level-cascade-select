package com.sourcesense.jira.common.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

/**
 * This class is the same of the Plug-in 3.0.
 * At the moment seems to not need any change or adaptation.
 * @author Fabio 
 * Date:  5/11/2010 (date of the conversion by Alessandro Benedetti)
 */
public abstract class LuceneHelper {

    public static LuceneHelper getHelper() {
        try {
            return new LuceneHelper1x();
        } catch (ClassNotFoundException e) {
            try {
                return new LuceneHelper2x();
            } catch (ClassNotFoundException e1) {
                log.error("Cannot find any Lucene helper class");
                return new NullLuceneHelper();
            }
        }
    }

    public abstract void addKeyword(Document doc, String name, String value);

    private static class LuceneHelper1x extends LuceneHelper {

        public LuceneHelper1x() throws ClassNotFoundException {
            try {
                field = classLoader.loadClass(FIELD);
                method = field.getMethod("Keyword", new Class[]{String.class, String.class});
                addMethod = Document.class.getMethod("add", new Class[] {field});
            } catch (NoSuchMethodException e) {
                throw new ClassNotFoundException(e.getMessage());
            }
        }

        @Override
        public void addKeyword(Document doc, String name, String value) {
            try {
                addMethod.invoke(doc, new Object[] {method.invoke(field, new Object[] {name, value})});
            } catch (IllegalAccessException e) {
                log.error(e);
            } catch (InvocationTargetException e) {
                log.error(e);
            }
        }

        private Class field;
        private static final String FIELD = "org.apache.lucene.document.Field";
        private Method method;
        private Method addMethod;
    }

    private static class LuceneHelper2x extends LuceneHelper {
        private LuceneHelper2x() throws ClassNotFoundException {
            field = classLoader.loadClass(FIELD);

            Class storeClass = classLoader.loadClass(FIELD_STORE);
            Class indexClass = classLoader.loadClass(FIELD_INDEX);
            try {
                Field storeField = storeClass.getField(FIELD_STORE_YES);
                Field indexField = indexClass.getField(FIELD_INDEX_UN_TOKENIZED);
                store = storeField.get(storeClass);
                index = indexField.get(indexClass);
            } catch (NoSuchFieldException e) {
                throw new ClassNotFoundException("Field not found");
            } catch (IllegalAccessException e) {
                throw new ClassNotFoundException(e.getMessage());
            }

            try {
                addMethod = Document.class.getMethod("add", new Class[] {classLoader.loadClass(FIELDABLE)});
            } catch (NoSuchMethodException e) {
                log.error(e);
                throw new ClassNotFoundException("Method not found");
            }
        }

        @Override
        public void addKeyword(Document doc, String name, String value) {
            try {
                Constructor constructor = field.getConstructor(new Class[]{String.class, String.class, store.getClass(), index.getClass()});
                addMethod.invoke(doc, new Object[] {constructor.newInstance(new Object[] {name, value, store, index})});
            } catch (NoSuchMethodException e) {
                log.error(e);
            } catch (IllegalAccessException e) {
                log.error(e);
            } catch (InvocationTargetException e) {
                log.error(e);
            } catch (InstantiationException e) {
                log.error(e);
            }
        }

        private Class field;
        private static final String FIELD = "org.apache.lucene.document.Field";
        private static final String FIELDABLE = "org.apache.lucene.document.Fieldable";
        private static final String FIELD_STORE = "org.apache.lucene.document.Field$Store";
        private static final String FIELD_INDEX = "org.apache.lucene.document.Field$Index";
        private static final String FIELD_INDEX_UN_TOKENIZED = "UN_TOKENIZED";
        private static final String FIELD_STORE_YES = "YES";
        private Object store;
        private Object index;
        private Method addMethod;
    }

    private static class NullLuceneHelper extends LuceneHelper {
        @Override
        public void addKeyword(Document doc, String name, String value) {
        }
    }

    private static final Logger log = Logger.getLogger(LuceneHelper.class);
    protected static final ClassLoader classLoader = LuceneHelper.class.getClassLoader();
}
