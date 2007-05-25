package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class represents the type of results that will be output by
 * Query and Question.
 * 
 */
public class RecordClass {

    public static final String PRIMARY_KEY_NAME = "primaryKey";
    public static final String PROJECT_ID_NAME = "projectId";
    public static final String PRIMARY_KEY_MACRO = "\\$\\$primaryKey\\$\\$";
    public static final String PROJECT_ID_MACRO = "\\$\\$projectId\\$\\$";

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.RecordClass");

    private Set<AttributeQueryReference> attributesQueryRefs = new LinkedHashSet<AttributeQueryReference>();
    private Map<String, AttributeField> attributeFieldsMap = new LinkedHashMap<String, AttributeField>();
    private Map<String, TableField> tableFieldsMap = new LinkedHashMap<String, TableField>();
    private String name;
    private String type;
    private String idPrefix;
    private String fullName;
    private String attributeOrdering;
    private Map<String, Question> questions = new LinkedHashMap<String, Question>();
    private List<NestedRecord> nestedRecordQuestionRefs = new ArrayList<NestedRecord>();
    private List<NestedRecordList> nestedRecordListQuestionRefs = new ArrayList<NestedRecordList>();

    /**
     * This object is not initialized until the first time the RecordClass is
     * asked for a nestedRecordQuestion. At that point it is given the questions
     * in <code>nestedRecordQuestionRefs</code>;
     */
    private Map<String, Question> nestedRecordQuestions;

    /**
     * This object is not initialized until the first time the RecordClass is
     * asked for a nestedRecordListQuestion. At that point it is given the
     * questions in <code>nestedRecordListQuestionRefs</code>;
     */
    private Map<String, Question> nestedRecordListQuestions;

    /**
     * The delimiter used by two-part primary key, ":" by default
     */
    private String delimiter = ":";

    /**
     * The PrimaryKeyField of a RecordClass
     */
    private PrimaryKeyField primaryKeyField;

    /**
     * The reference for a FlatVocabParam that contains project info. It can be
     * optional
     */
    private ParamReference projectParamRef;

    /**
     * the reference to a query that returns a list of alias ids of the given
     * gene id
     */
    private String aliasQueryName = null;
    private Query aliasQuery = null;

    private Map<String, ReporterRef> reporters;

    public RecordClass() {
        // make sure these keys are at the front of the list
        // it doesn't make sense, since you can't guarantee the order in a map
        attributeFieldsMap.put(PRIMARY_KEY_NAME, null);
        
        reporters = new LinkedHashMap<String, ReporterRef>();
    }

    // ////////////////////////////////////////////////////////////////////
    // Called at model creation time
    // ////////////////////////////////////////////////////////////////////

    public void setName(String name) {
        this.name = name;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Added by Jerric
     * 
     * @param delimiter
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Added by Jerric
     * 
     * @param projectParamRef
     */
    public void setProjectParamRef(ParamReference projectParamRef) {
        this.projectParamRef = projectParamRef;
    }

    /**
     * @param attList
     *            comma separated list of attributes in a summary containing
     *            this recordClass.
     */
    /*
     * public void setSummaryAttributeList (String attList){
     * this.summaryAttributeList = attList; }
     */

    public void setAttributeOrdering(String attOrder) {
        this.attributeOrdering = attOrder;
    }

    public void setAliasQueryRef(String queryRef) {
        this.aliasQueryName = queryRef;
    }

    /**
     * @param attributesQueryRef
     *            two part query name (set.name)
     */
    public void addAttributesQueryRef(AttributeQueryReference attributesQueryRef) {
        attributesQueryRefs.add(attributesQueryRef);
    }

    /**
     * Add an AttributeField to this RecordClass.
     * @param attributeField The attribute.
     * @throws WdkModelException If the attribute is already in the RecordClass.
     */
    public void addAttributeField(AttributeField attributeField)
            throws WdkModelException {
        // check if the name duplicates
        String name = attributeField.getName();
        if (attributeFieldsMap.containsKey(name)) {
            String message = "Duplicates in name of AttributeField: " + name;
            logger.finest(message);
            throw new WdkModelException(message);
        }
        attributeFieldsMap.put(name, attributeField);
    }

    public void addTableField(TableField tableField) throws WdkModelException {
        // check if the name duplicates
        String name = tableField.getName();
        if (tableFieldsMap.containsKey(name)) {
            String message = "Duplicates in name of TableField: " + name;
            logger.finest(message);
            throw new WdkModelException(message);
        }
        tableFieldsMap.put(name, tableField);
    }

    /**
     * Add a <code>Question</code> as having a RecordClass of this type.
     * @param q The Question which has a RecordClass of this type.
     */
    public void addQuestion(Question q) {
        questions.put(q.getFullName(), q);
    }

    public void addNestedRecordQuestion(Question q) {

        nestedRecordQuestions.put(q.getFullName(), q);
    }

    public void addNestedRecordListQuestion(Question q) {
        nestedRecordListQuestions.put(q.getFullName(), q);
    }

    public void addNestedRecordQuestionRef(NestedRecord nr) {
        nestedRecordQuestionRefs.add(nr);
    }

    public void addNestedRecordListQuestionRef(NestedRecordList nrl) {

        nestedRecordListQuestionRefs.add(nrl);
    }
    
    public void addReporterRef(ReporterRef reporter) {
        reporters.put(reporter.getName(), reporter);
    }

    // ////////////////////////////////////////////////////////////
    // public getters
    // ////////////////////////////////////////////////////////////

    /**
     * @return The name of this RecordClass.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The full name of the Record Class. (e.g. EstsRecordClass.EstIds)
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @return The first part of the full name.
     */
    public String getIdPrefix() {
        return idPrefix;
    }

    public String getType() {
        return type;
    }

    public Map<String, TableField> getTableFieldMap() {
        return new LinkedHashMap<String, TableField>(tableFieldsMap);
    }

    public TableField[] getTableFields() {
        TableField[] tableFields = new TableField[tableFieldsMap.size()];
        tableFieldsMap.values().toArray(tableFields);
        return tableFields;
    }

    public Map<String, AttributeField> getAttributeFieldMap() {
        return new LinkedHashMap<String, AttributeField>(attributeFieldsMap);
    }

    public AttributeField[] getAttributeFields() {
        AttributeField[] attributeFields = new AttributeField[attributeFieldsMap.size()];
        attributeFieldsMap.values().toArray(attributeFields);
        return attributeFields;
    }

    public Map<String, AttributeField> getReportMakerAttributeFieldMap() {
        Map<String, AttributeField> rmfields = new LinkedHashMap<String, AttributeField>();
        Set<String> names = attributeFieldsMap.keySet();
        for (String name : names) {
            AttributeField field = attributeFieldsMap.get(name);
            if (field.getInReportMaker()) rmfields.put(name, field);
        }
        return rmfields;
    }

    public Map<String, TableField> getReportMakerTableFieldMap() {
        Map<String, TableField> rmfields = new LinkedHashMap<String, TableField>();
        Set<String> names = tableFieldsMap.keySet();
        for (String name : names) {
            TableField field = tableFieldsMap.get(name);
            if (field.getInReportMaker()) rmfields.put(name, field);
        }
        return rmfields;
    }

    public Field[] getFields() {
        int attributeCount = attributeFieldsMap.size();
        int tableCount = tableFieldsMap.size();
        Field[] fields = new Field[attributeCount + tableCount];
        // copy attribute fields
        attributeFieldsMap.values().toArray(fields);
        // copy table fields
        TableField[] tableFields = getTableFields();
        System.arraycopy(tableFields, 0, fields, attributeCount, tableCount);
        return fields;
    }

    public Question[] getNestedRecordQuestions() {
        if (nestedRecordQuestions == null) {
            initNestedRecords();
        }
        Question[] returnedNq = new Question[nestedRecordQuestions.size()];
        nestedRecordQuestions.values().toArray(returnedNq);
        return returnedNq;
    }

    public Question[] getNestedRecordListQuestions() {
        if (nestedRecordListQuestions == null) {
            initNestedRecords();
        }
        Question[] returnedNq = new Question[nestedRecordListQuestions.size()];
        nestedRecordListQuestions.values().toArray(returnedNq);
        return returnedNq;
    }

    /**
     * @return all Questions in the current model that are using this record
     *         class as their return type.
     */
    public Question[] getQuestions() {
        Question[] returnedQuestions = new Question[questions.size()];
        questions.values().toArray(returnedQuestions);
        return returnedQuestions;
    }

    public Reference getReference() throws WdkModelException {
        return new Reference(getFullName());
    }

    /**
     * Added by Jerric
     * 
     * @return returns the delimiter for separating projectID & primaryKey
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Added by Jerric
     * 
     * @return
     */
    public PrimaryKeyField getPrimaryKeyField() {
        return primaryKeyField;
    }

    public RecordInstance makeRecordInstance(String recordId)
            throws WdkModelException {
        return new RecordInstance(this, recordId);
    }

    public RecordInstance makeRecordInstance(String projectId, String recordId)
            throws WdkModelException {
        String sourceId = lookupSourceId(recordId);
        return new RecordInstance(this, projectId, sourceId);
    }
    
    public Map<String, ReporterRef> getReporterMap() {
        return new LinkedHashMap<String, ReporterRef>(reporters);
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer("Record: name='" + name + "'").append(newline);

        buf.append("--- Attributes ---").append(newline);
        Iterator attributes = attributeFieldsMap.values().iterator();
        while (attributes.hasNext()) {
            buf.append(attributes.next()).append(newline);
        }

        buf.append("--- Tables ---").append(newline);
        Iterator tables = tableFieldsMap.values().iterator();
        while (tables.hasNext()) {
            buf.append(tables.next()).append(newline);
        }
        return buf.toString();
    }

    /*
      <sanityRecord ref="GeneRecordClasses.GeneRecordClass"
                    primaryKey="PF11_0344"/>
    */
    public String getSanityTestSuggestion () throws WdkModelException {
	String indent = "    ";
        String newline = System.getProperty("line.separator");
	StringBuffer buf = new StringBuffer(
	      newline + newline
	    + indent + "<sanityRecord ref=\"" + getFullName() + "\"" 
	    + newline
	    + indent + indent + indent
	    + "primaryKey=\"FIX_pk\">"
	    + newline);
	buf.append(indent + "</sanityRecord>");
	return buf.toString();
    }


    // /////////////////////////////////////////////////////////////////////////
    // package scope methods
    // /////////////////////////////////////////////////////////////////////////

    /**
     * @param recordSetName
     *            name of the recordSet to which this record belongs.
     */
    void setFullName(String recordSetName) {
        this.fullName = recordSetName + "." + name;
    }

    AttributeField getAttributeField(String attributeName)
            throws WdkModelException {
        AttributeField attributeField = attributeFieldsMap.get(attributeName);
        if (attributeField == null) {
            String message = "RecordClass " + getName()
                    + " doesn't have an attribute field with name '"
                    + attributeName + "'.";
            logger.finest(message);
            throw new WdkModelException(message);
        }
        return attributeField;
    }

    TableField getTableField(String tableName) throws WdkModelException {
        TableField tableField = tableFieldsMap.get(tableName);
        if (tableField == null) {
            String message = "Record " + getName()
                    + " does not have a table field with name '" + tableName
                    + "'.";
            logger.finest(message);
            throw new WdkModelException(message);
        }
        return tableField;
    }

    void resolveReferences(WdkModel model) throws WdkModelException {
        // Added by Jerric
        // resolve projectParam
        FlatVocabParam projectParam = null;
        if (projectParamRef != null) {
            projectParam = (FlatVocabParam) model.resolveReference(
                    projectParamRef.getTwoPartName(), getName(),
                    this.getClass().getName(), "projectParamRef");
            projectParam = (FlatVocabParam) projectParam.clone();
            projectParam.setDefault(projectParamRef.getDefault());
        }

        // create PrimaryKeyField
        primaryKeyField = new PrimaryKeyField(PRIMARY_KEY_NAME, getType(),
                "Some help here", projectParam);
        primaryKeyField.setIdPrefix(this.idPrefix);
        primaryKeyField.setDelimiter(this.delimiter);
        attributeFieldsMap.put(PRIMARY_KEY_NAME, primaryKeyField);

        // resolve the references for attribute queries
        for (AttributeQueryReference reference : attributesQueryRefs) {
            // add attributes to the record class. it must be performed before
            // next step.
            Map<String, AttributeField> fieldMap = reference.getAttributeFieldMap();
            Collection<AttributeField> fields = fieldMap.values();
            for (AttributeField field : fields) {
                addAttributeField(field);
            }

            // resolve Query and associate columns with the attribute fields
            Query query = (Query) model.resolveReference(
                    reference.getTwoPartName(), getName(),
                    this.getClass().getName(), "attributesQueryRef");
            Column[] columns = query.getColumns();
            for (Column column : columns) {
                AttributeField field = fieldMap.get(column.getName());
                if (field != null && field instanceof ColumnAttributeField) {
                    ((ColumnAttributeField) field).setColumn(column);
                }
            }
        }

        // resolve the references for table queries
        Collection<TableField> tableFields = tableFieldsMap.values();
        for (TableField tableField : tableFields) {
            tableField.resolveReferences(model);
        }

        if (attributeOrdering != null) {
            Map<String, AttributeField> orderedAttributes = sortAllAttributes();
            attributeFieldsMap = orderedAttributes;
        }

        for (NestedRecord nestedRecord : nestedRecordQuestionRefs) {
            nestedRecord.resolveReferences(model);
        }

        for (NestedRecordList nestedRecordList : nestedRecordListQuestionRefs) {
            nestedRecordList.resolveReferences(model);
        }

        // resolve reference for alias query
        if (aliasQueryName != null) {
            aliasQuery = (Query) model.resolveReference(aliasQueryName, name,
                    RecordClass.class.getName(), "aliasQueryRef");
        }
    }

    /**
     * Called when the RecordClass is asked for a NestedRecordQuestion or
     * NestedRecordQuestionList. Cannot be done upon RecordClass initialization
     * because the Questions are not guaranteed to have their resources set,
     * which throws a NullPointerException when the Question is asked for the
     * name of its QuestionSet.
     */

    public void initNestedRecords() {
        nestedRecordQuestions = new LinkedHashMap<String, Question>();
        for (int i = 0; i < nestedRecordQuestionRefs.size(); i++) {
            NestedRecord nextNr = nestedRecordQuestionRefs.get(i);
            Question q = nextNr.getQuestion();
            addNestedRecordQuestion(q);
        }

        nestedRecordListQuestions = new LinkedHashMap<String, Question>();
        for (int i = 0; i < nestedRecordListQuestionRefs.size(); i++) {
            NestedRecordList nextNrl = nestedRecordListQuestionRefs.get(i);
            Question q = nextNrl.getQuestion();
            addNestedRecordListQuestion(q);
        }
    }

    private Map<String, AttributeField> sortAllAttributes()
            throws WdkModelException {
        String orderedAtts[] = attributeOrdering.split(",");
        Map<String, AttributeField> orderedAttsMap = new LinkedHashMap<String, AttributeField>();

        // primaryKey first
        String primaryKey = "primaryKey";
        orderedAttsMap.put(primaryKey, attributeFieldsMap.get(primaryKey));

        for (String nextAtt : orderedAtts) {
            nextAtt = nextAtt.trim();
            if (!primaryKey.equals(nextAtt)) {
                AttributeField nextAttField = attributeFieldsMap.get(nextAtt);

                if (nextAttField == null) {
                    String message = "RecordClass " + getName()
                            + " defined attribute " + nextAtt
                            + " in its attribute ordering, but that is not a "
                            + "valid attribute for this RecordClass";
                    logger.finest(message);
                    throw new WdkModelException(message);
                }
                orderedAttsMap.put(nextAtt, nextAttField);
            }
        }
        // add all attributes not in the ordering
        Iterator<String> allAttNames = attributeFieldsMap.keySet().iterator();
        while (allAttNames.hasNext()) {
            String nextAtt = allAttNames.next();
            if (!orderedAttsMap.containsKey(nextAtt)) {
                AttributeField nextField = attributeFieldsMap.get(nextAtt);
                orderedAttsMap.put(nextAtt, nextField);
            }
        }
        return orderedAttsMap;
    }
    
    private String lookupSourceId(String aliasName) throws WdkModelException {
        // nothing to look up
        if (aliasQuery == null) return aliasName;

        // create a query instance with alias as the primaryKey
        QueryInstance qinstance = aliasQuery.makeInstance();
        qinstance.setIsCacheable(false);
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(PRIMARY_KEY_NAME, aliasName);
        qinstance.setValues(params);
        ResultList resultList = qinstance.getResult();
        if (resultList.next()) {
            String columnName = aliasQuery.getColumns()[0].getName();
            String sourceId = (String) resultList.getValue(columnName);
            aliasName = sourceId;
        }
        resultList.close();
        return aliasName;
    }
}
