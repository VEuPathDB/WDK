package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.AttributeValue;
import org.gusdb.wdk.model.FieldScope;
import org.gusdb.wdk.model.PrimaryKeyAttributeValue;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.TableValue;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

/**
 * A wrapper on a {@link RecordInstance} that provides simplified access for
 * consumption by a view
 */
public class RecordBean {

    private RecordInstance recordInstance;

    public RecordBean(RecordInstance recordInstance) {
        this.recordInstance = recordInstance;
    }

    /**
     * modified by Jerric
     * 
     * @return
     */
    public PrimaryKeyAttributeValue getPrimaryKey() {
        return recordInstance.getPrimaryKey();
    }

    public RecordClassBean getRecordClass() {
        return new RecordClassBean(recordInstance.getRecordClass());
    }

    public String[] getSummaryAttributeNames() {
        return recordInstance.getSummaryAttributeNames();
    }

    public Map<String, RecordBean> getNestedRecords() throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException, SQLException,
            JSONException {
        Map<String, RecordInstance> nri = recordInstance.getNestedRecordInstances();
        Map<String, RecordBean> nriBeans = new LinkedHashMap<String, RecordBean>();
        for (String recordName : nri.keySet()) {
            RecordBean nextNrBean = new RecordBean(nri.get(recordName));
            nriBeans.put(recordName, nextNrBean);
        }
        return nriBeans;
    }

    public Map<String, RecordBean[]> getNestedRecordLists()
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        Map<String, RecordInstance[]> nrl = recordInstance.getNestedRecordInstanceLists();
        Map<String, RecordBean[]> nrlBeans = new LinkedHashMap<String, RecordBean[]>();
        for (String recordName : nrl.keySet()) {
            RecordInstance nextNrl[] = nrl.get(recordName);
            RecordBean[] nextNrBeanList = new RecordBean[nextNrl.length];
            for (int i = 0; i < nextNrl.length; i++) {
                nextNrBeanList[i] = new RecordBean(nextNrl[i]);
            }
            nrlBeans.put(recordName, nextNrBeanList);
        }
        return nrlBeans;
    }

    /**
     * @return Map of attributeName -->
     *         {@link org.gusdb.wdk.model.AttributeFieldValue}
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public Map<String, AttributeValue> getAttributes()
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        return new AttributeValueMap(recordInstance, FieldScope.ALL);
    }

    /**
     * @return Map of attributeName -->
     *         {@link org.gusdb.wdk.model.AttributeFieldValue}
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public Map<String, AttributeValue> getSummaryAttributes()
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        return new AttributeValueMap(recordInstance, FieldScope.NON_INTERNAL);
    }

    /**
     * @return Map of tableName --> {@link org.gusdb.wdk.model.TableValue}
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public Map<String, TableValue> getTables() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        return new TableValueMap(recordInstance, FieldScope.ALL);
    }

    private class AttributeValueMap implements Map<String, AttributeValue> {

        private class AttributeValueEntry implements
                Map.Entry<String, AttributeValue> {

            private RecordInstance recordInstance;
            private String fieldName;
            private AttributeValue value;

            public AttributeValueEntry(RecordInstance recordInstance,
                    String fieldName) {
                this.recordInstance = recordInstance;
                this.fieldName = fieldName;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Map.Entry#getKey()
             */
            public String getKey() {
                return fieldName;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Map.Entry#getValue()
             */
            public AttributeValue getValue() {
                if (value == null) {
                    try {
                        value = recordInstance.getAttributeValue(fieldName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
                return value;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Map.Entry#setValue(java.lang.Object)
             */
            public AttributeValue setValue(AttributeValue value) {
                throw new UnsupportedOperationException("wot supported");
            }

        }

        private RecordInstance recordInstance;
        private Map<String, AttributeField> fields;
        private Map<String, AttributeValue> values;

        public AttributeValueMap(RecordInstance recordInstance, FieldScope scope) {
            this.recordInstance = recordInstance;
            this.fields = recordInstance.getAttributeFieldMap(scope);
            this.values = new LinkedHashMap<String, AttributeValue>();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#clear()
         */
        public void clear() {
            throw new UnsupportedOperationException("wot supported");
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
            throw new UnsupportedOperationException("wot supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#entrySet()
         */
        public Set<Entry<String, AttributeValue>> entrySet() {
            Set<Entry<String, AttributeValue>> entries = new LinkedHashSet<Entry<String, AttributeValue>>();
            for (String fieldName : fields.keySet()) {
                entries.add(new AttributeValueEntry(recordInstance, fieldName));
            }
            return entries;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#get(java.lang.Object)
         */
        public AttributeValue get(Object key) {
            if (!fields.containsKey(key))
                throw new RuntimeException("The attribute [" + key
                        + "] doesn't exist in record instance.");
            AttributeValue value = values.get(key);
            if (value == null) {
                String fieldName = (String) key;
                try {
                    value = recordInstance.getAttributeValue(fieldName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
                values.put(fieldName, value);
            }
            return value;
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
            throw new UnsupportedOperationException("wot supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#putAll(java.util.Map)
         */
        public void putAll(Map<? extends String, ? extends AttributeValue> m) {
            throw new UnsupportedOperationException("wot supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#remove(java.lang.Object)
         */
        public AttributeValue remove(Object key) {
            throw new UnsupportedOperationException("wot supported");
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
            for (String fieldName : fields.keySet()) {
                values.add(get(fieldName));
            }
            return values;
        }
    }

    private class TableValueMap implements Map<String, TableValue> {

        private class TableValueEntry implements Map.Entry<String, TableValue> {

            private RecordInstance recordInstance;
            private String fieldName;
            private TableValue value;

            public TableValueEntry(RecordInstance recordInstance,
                    String fieldName) {
                this.recordInstance = recordInstance;
                this.fieldName = fieldName;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Map.Entry#getKey()
             */
            public String getKey() {
                return fieldName;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Map.Entry#getValue()
             */
            public TableValue getValue() {
                if (value == null) {
                    try {
                        value = recordInstance.getTableValue(fieldName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
                return value;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Map.Entry#setValue(java.lang.Object)
             */
            public TableValue setValue(TableValue value) {
                throw new UnsupportedOperationException("wot supported");
            }

        }

        private RecordInstance recordInstance;
        private Map<String, TableField> fields;
        private Map<String, TableValue> values;

        public TableValueMap(RecordInstance recordInstance, FieldScope scope) {
            this.recordInstance = recordInstance;
            this.fields = recordInstance.getRecordClass().getTableFieldMap(
                    scope);
            this.values = new LinkedHashMap<String, TableValue>();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#clear()
         */
        public void clear() {
            throw new UnsupportedOperationException("wot supported");
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
            throw new UnsupportedOperationException("wot supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#entrySet()
         */
        public Set<Entry<String, TableValue>> entrySet() {
            Set<Entry<String, TableValue>> entries = new LinkedHashSet<Entry<String, TableValue>>();
            for (String fieldName : fields.keySet()) {
                entries.add(new TableValueEntry(recordInstance, fieldName));
            }
            return entries;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#get(java.lang.Object)
         */
        public TableValue get(Object key) {
            if (!fields.containsKey(key))
                throw new RuntimeException("The table [" + key
                        + "] doesn't exist in record instance.");
            TableValue value = values.get(key);
            if (value == null) {
                String fieldName = (String) key;
                try {
                    value = recordInstance.getTableValue(fieldName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
                values.put(fieldName, value);
            }
            return value;
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
        public TableValue put(String key, TableValue value) {
            throw new UnsupportedOperationException("wot supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#putAll(java.util.Map)
         */
        public void putAll(Map<? extends String, ? extends TableValue> m) {
            throw new UnsupportedOperationException("wot supported");
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Map#remove(java.lang.Object)
         */
        public TableValue remove(Object key) {
            throw new UnsupportedOperationException("wot supported");
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
        public Collection<TableValue> values() {
            List<TableValue> values = new ArrayList<TableValue>();
            for (String fieldName : fields.keySet()) {
                values.add(get(fieldName));
            }
            return values;
        }
    }
}
