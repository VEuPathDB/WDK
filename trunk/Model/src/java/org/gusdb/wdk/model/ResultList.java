package org.gusdb.wdk.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class ResultList {

    protected QueryInstance instance;
    protected Query query;
    protected String resultTableName;
    
    /**
     * It stores the field information of a TableField. This is not a good idea
     * to have ResultList know the information of higher level objects. But this
     * is the way the ResultList works, should refactor it later.
     * -- Jerric
     */
    protected Map<String, AttributeField> attributeFields = null;

    // public static final String RESULT_TABLE_I = "MultiModeIValue";

    public ResultList(QueryInstance instance, String resultTableName) {
        this.instance = instance;
        this.query = instance.getQuery();
        this.resultTableName = resultTableName;
    }

    public abstract void checkQueryColumns(Query query, boolean checkAll,
            boolean has_result_table_i) throws WdkModelException;

    /**
     * @param attributeName
     * @return
     * @throws WdkModelException
     */
    Object getValue(String columnName) throws WdkModelException {
        if (columnName == ResultFactory.RESULT_TABLE_I) {
            return getResultTableIndexValue();
        } else {
            return getValueFromResult(columnName);
        }
    }

    public Object getResultTableIndexValue() throws WdkModelException {
        throw new WdkModelException(
                "attempting to retrieve value for i from a ResultList that is not an SqlResultList");
    }

    public Query getQuery() {
        return query;
    }

    public Column[] getColumns() {
        return query.getColumns();
    }
    
    void setAttributeFields(Map<String, AttributeField> attributeFields) {
        this.attributeFields = attributeFields;
    }

    public abstract boolean next() throws WdkModelException;

    public abstract void close() throws WdkModelException;

    public void write(StringBuffer buf) throws WdkModelException {
        String newline = System.getProperty("line.separator");
        Iterator rows = getRows();
        while (rows.hasNext()) {
            Map rowMap = (Map) rows.next();
            Iterator colNames = rowMap.keySet().iterator();
            while (colNames.hasNext()) {
                Object colName = colNames.next();
                Object fVal = rowMap.get(colName);
                buf.append(fVal);
                buf.append("\t");
            }
            buf.append(newline);
        }
        close();
    }

    public abstract void print() throws WdkModelException;

    public String getResultTableName() throws WdkModelException {
        if (!hasResultTable())
            throw new WdkModelException("Has no result table");
        return resultTableName;
    }

    public boolean hasResultTable() {
        return resultTableName != null;
    }

    public abstract Object getValueFromResult(String attributeName)
            throws WdkModelException;

    // ////////////////////////////////////////////////////////////////
    // protected
    // ////////////////////////////////////////////////////////////////

    /**
     * @return Iterator of Maps as returned by getRow()
     */
    Iterator getRows() {
        return new ResultListIterator(this);
    }

    /**
     * @return Map of <columnName, RawObject>
     */
    Map<String, Object> getRow() {
        // return new RowMap(this);
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        Column[] cols = getColumns();
        for (int i = 0; i < cols.length; i++) {
            String colName = cols[i].getName();
            try {
                row.put(colName, getValue(colName));
            } catch (WdkModelException e) {
                throw new RuntimeException(e);
            }
        }
        return row;
    }

    public QueryInstance getInstance() {
        return instance;
    }

    // ///////////////////////////////////////////////////////////////////
    // Inner classes
    // ///////////////////////////////////////////////////////////////////

    public class ResultListIterator implements Iterator {

        ResultList rl;
        Map nextCache = null;

        ResultListIterator(ResultList rl) {
            this.rl = rl;
        }

        public boolean hasNext() {
            // if nextCache is not consumed, return true (allow repeated calls)
            if (nextCache != null) {
                return true;
            }

            // ask rl if a next thing is available
            boolean hasNext = false;
            try {
                hasNext = rl.next();

                // if a next thing is available, cache it
                if (hasNext) {
                    nextCache = rl.getRow();
                }
            } catch (WdkModelException e) {
                throw new RuntimeException(e);
            }

            return hasNext;
        }

        public Object next() throws NoSuchElementException {
            // if the next thing is already in the cache, return it and clear
            // cache
            if (nextCache != null) {
                Map theNext = nextCache;
                nextCache = null;
                return theNext;
            }
            // if nothing in cache, ask hasNext for an answer
            else {
                if (hasNext()) {
                    return nextCache;
                } else {
                    throw new NoSuchElementException("no more element left");
                }
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
