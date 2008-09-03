package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.json.JSONException;

public class TableValue implements Iterable<Map<String, AttributeValue>>,
        Iterator<Map<String, AttributeValue>> {

    private static class TableValueRow extends AttributeValueContainer
            implements Map<String, AttributeValue> {

        private class TableValueRowEntry implements
                Entry<String, AttributeValue> {

            private String name;
            private AttributeValue value;

            public TableValueRowEntry(String name, AttributeValue value) {
                this.name = name;
                this.value = value;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Map.Entry#getKey()
             */
            public String getKey() {
                return name;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Map.Entry#getValue()
             */
            public AttributeValue getValue() {
                return value;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Map.Entry#setValue(java.lang.Object)
             */
            public AttributeValue setValue(AttributeValue value) {
                this.value = value;
                return value;
            }

        }

        private Map<String, AttributeField> fields;

        private TableValueRow(TableValue tableValue)
                throws NoSuchAlgorithmException, WdkModelException,
                JSONException, SQLException {
            super(tableValue.primaryKey);
            this.fields = tableValue.getTableField().getAttributeFieldMap();
        }

        /* (non-Javadoc)
         * @see org.gusdb.wdk.model.AttributeValueContainer#getAttributeFieldMap()
         */
        @Override
        protected Map<String, AttributeField> getAttributeFieldMap() {
            return fields;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#clear()
         */
        public void clear() {
            throw new UnsupportedOperationException("Not supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key) {
            return fields.containsKey(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value) {
            for (String name : fields.keySet()) {

                AttributeValue attributeValue = get(name);
                if (attributeValue.equals(value)) return true;
            }
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#entrySet()
         */
        public Set<Entry<String, AttributeValue>> entrySet() {
            Set<Entry<String, AttributeValue>> entries = new LinkedHashSet<Entry<String, AttributeValue>>();
            for (String name : fields.keySet()) {
                AttributeValue value = get(name);
                entries.add(new TableValueRowEntry(name, value));
            }
            return entries;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#get(java.lang.Object)
         */
        public AttributeValue get(Object key) {
            try {
                return getAttributeValue((String) key);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            } catch (WdkModelException ex) {
                throw new RuntimeException(ex);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            } catch (WdkUserException ex) {
                throw new RuntimeException(ex);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty() {
            return fields.isEmpty();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#keySet()
         */
        public Set<String> keySet() {
            return fields.keySet();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public AttributeValue put(String key, AttributeValue value) {
            throw new UnsupportedOperationException("Not supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#putAll(java.util.Map)
         */
        public void putAll(
                Map<? extends String, ? extends AttributeValue> values) {
            throw new UnsupportedOperationException("Not supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#remove(java.lang.Object)
         */
        public AttributeValue remove(Object key) {
            throw new UnsupportedOperationException("Not supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#size()
         */
        public int size() {
            return fields.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#values()
         */
        public Collection<AttributeValue> values() {
            List<AttributeValue> values = new ArrayList<AttributeValue>();
            for (String name : fields.keySet()) {
                values.add(get(name));
            }
            return values;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.gusdb.wdk.model.AttributeValueContainer#fillColumnAttributeValues(org.gusdb.wdk.model.query.Query)
         */
        @Override
        protected void fillColumnAttributeValues(Query attributeQuery)
                throws WdkModelException, NoSuchAlgorithmException,
                JSONException, SQLException {
        // do nothing, since the data is filled by the parent TableValue
        }
    }

    private PrimaryKeyAttributeValue primaryKey;
    private TableField tableField;
    private ResultList resultList;

    private TableValueRow nextRow;

    public TableValue(PrimaryKeyAttributeValue primaryKey, TableField tableField)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        this.primaryKey = primaryKey;
        this.tableField = tableField;

        // run the table query, and get the resultList
        Query query = tableField.getQuery();
        QueryInstance instance = query.makeInstance(primaryKey.getValues());
        this.resultList = instance.getResults();

        readNextRow();
    }

    public TableField getTableField() {
        return tableField;
    }

    /**
     * Must be called to close the table.
     * 
     * @return null
     * @throws SQLException
     * @throws WdkModelException
     */
    public void close() throws WdkModelException {
        resultList.close();
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        String classnm = this.getClass().getName();
        StringBuffer buf = new StringBuffer(classnm + ": name='"
                + tableField.getName() + "'" + newline + "  displayName='"
                + tableField.getDisplayName() + "'" + newline + "  help='"
                + tableField.getHelp() + "'" + newline);
        return buf.toString();
    }

    public void write(StringBuffer buf) throws WdkModelException {
        String newline = System.getProperty("line.separator");
        // display the headers of the table
        AttributeField[] fields = tableField.getAttributeFields();
        for (AttributeField attributeField : fields) {
            buf.append('[');
            buf.append(attributeField.getDisplayName());
            buf.append("]\t");
        }
        buf.append(newline);
        // print rows
        for (Map<String, AttributeValue> row : this) {
            for (String name : row.keySet()) {
                AttributeValue value = row.get(name);
                buf.append("'");
                buf.append(value);
                buf.append("'\t");
            }
            buf.append(newline);
        }
    }

    public void toXML(StringBuffer buf, String rowTag, String ident)
            throws WdkModelException {
        String newline = System.getProperty("line.separator");
        for (Map<String, AttributeValue> row : this) {
            buf.append(ident + "<" + rowTag + ">" + newline);
            for (String name : row.keySet()) {
                // get the value
                AttributeValue value = row.get(name);
                buf.append(ident + "    " + "<" + name + ">");
                buf.append(value);
                buf.append("</" + name + ">" + newline);
            }
            buf.append(ident + "</" + rowTag + ">" + newline);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Map<String, AttributeValue>> iterator() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return (nextRow != null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#next()
     */
    public Map<String, AttributeValue> next() {
        Map<String, AttributeValue> row = nextRow;
        try {
            readNextRow();
        } catch (WdkModelException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return row;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }

    private void readNextRow() throws WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        nextRow = null;
        if (resultList.next()) {
            nextRow = new TableValueRow(this);

            // fill in the column attributes
            for (AttributeField field : tableField.getAttributeFields()) {
                if (!(field instanceof ColumnAttributeField)) continue;

                Object value = resultList.get(field.getName());
                ColumnAttributeValue attributeValue = new ColumnAttributeValue(
                        (ColumnAttributeField) field, value);
                nextRow.addColumnAttributeValue(attributeValue);
            }
        }
    }
}
