package com.sourcesense.jira.common.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;

/**
 * This class is the same of the Plug-in 3.0.
 * At the moment seems to not need any change or adaptation.
 * @author Fabio 
 * Date:  5/11/2010 (date of the conversion by Alessandro Benedetti)
 */
public abstract class ReaderHelper {

    public static ReaderHelper getHelper() {
        try {
            return new ReaderSearcherCache();
        } catch (ClassNotFoundException e) {
        }
        try {
            return new ReaderQueryProfiler();
        } catch (ClassNotFoundException e) {
        }
        log.error("Cannot find any class with getReader method");
        return new NullReader();
    }

    private ReaderHelper(Class clazz) {
        this.clazz = clazz;
    }

    public final IndexReader getReader() {
        try {
            Method method = clazz.getMethod(GET_READER, new Class[]{});
            return (IndexReader) method.invoke(method.getDeclaringClass(), new Object[] {});
        } catch (NoSuchMethodException e) {
            log.error(e);
        } catch (IllegalAccessException e) {
            log.error(e);
        } catch (InvocationTargetException e) {
            log.error(e);
        }
        return null;
    }

    private static class ReaderQueryProfiler extends ReaderHelper {
        public ReaderQueryProfiler() throws ClassNotFoundException {
            super(classLoader.loadClass(THREAD_LOCAL_QUERY_PROFILER));
        }

        private static final String THREAD_LOCAL_QUERY_PROFILER = "com.atlassian.jira.web.filters.ThreadLocalQueryProfiler";
    }

    private static class ReaderSearcherCache extends ReaderHelper {
        public ReaderSearcherCache() throws ClassNotFoundException {
            super(classLoader.loadClass(THREAD_LOCAL_SEARCHER_CACHE));
        }

        private static final String THREAD_LOCAL_SEARCHER_CACHE = "com.atlassian.jira.util.searchers.ThreadLocalSearcherCache";
    }

    private static class NullReader extends ReaderHelper {
        public NullReader() {
            super(Object.class);
        }
    }

    private Class clazz;
    private static final String GET_READER = "getReader";
    private static final Logger log = Logger.getLogger(ReaderHelper.class);
    protected static final ClassLoader classLoader = ReaderHelper.class.getClassLoader();
}
