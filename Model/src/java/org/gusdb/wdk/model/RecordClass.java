package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordClass extends WdkModelBase {

    public static final String PRIMARY_KEY_NAME = "primaryKey";
    public static final String PROJECT_ID_NAME = "projectId";
    public static final String PRIMARY_KEY_MACRO = "\\$\\$primaryKey\\$\\$";
    public static final String PROJECT_ID_MACRO = "\\$\\$projectId\\$\\$";

    // private static final Logger logger = Logger.getLogger(RecordClass.class);

    private List<AttributeQueryReference> attributesQueryRefList = new ArrayList<AttributeQueryReference>();
    private Map<String, AttributeQueryReference> attributesQueryRefs = new LinkedHashMap<String, AttributeQueryReference>();

    private List<AttributeField> attributeFieldList = new ArrayList<AttributeField>();
    private Map<String, AttributeField> attributeFieldsMap = new LinkedHashMap<String, AttributeField>();

    private List<TableField> tableFieldList = new ArrayList<TableField>();
    private Map<String, TableField> tableFieldsMap = new LinkedHashMap<String, TableField>();

    private String name;
    private String type;
    private String idPrefix;
    private String fullName;
    private String displayName;
    private String attributeOrdering;
    private Map<String, Question> questions = new LinkedHashMap<String, Question>();

    private List<NestedRecord> nestedRecordQuestionRefList = new ArrayList<NestedRecord>();
    private Map<String, NestedRecord> nestedRecordQuestionRefs = new LinkedHashMap<String, NestedRecord>();

    private List<NestedRecordList> nestedRecordListQuestionRefList = new ArrayList<NestedRecordList>();
    private Map<String, NestedRecordList> nestedRecordListQuestionRefs = new LinkedHashMap<String, NestedRecordList>();

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

    private List<ReporterRef> reporterList = new ArrayList<ReporterRef>();
    private Map<String, ReporterRef> reporterMap = new LinkedHashMap<String, ReporterRef>();

    private List<SubType> subTypeList = new ArrayList<SubType>();
    private SubType subType;

    public RecordClass() {
        // make sure these keys are at the front of the list
        // it doesn't make sense, since you can't guarantee the order in a map
        attributeFieldsMap.put(PRIMARY_KEY_NAME, null);
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

    public String getDisplayName() {
		return (displayName == null)? getFullName() : displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
     *                comma separated list of attributes in a summary containing
     *                this recordClass.
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
     *                two part query name (set.name)
     */
    public void addAttributesQueryRef(AttributeQueryReference attributesQueryRef) {
        attributesQueryRefList.add(attributesQueryRef);
    }

    public void addAttributeField(AttributeField attributeField)
            throws WdkModelException {
        attributeField.setRecordClass(this);
        if (attributeFieldList != null) {
            // invoked by the model parser, the excludeResources() hasn't been
            // called yet
            attributeFieldList.add(attributeField);
        } else {
            // invoked when resolving references. Add directlly into the map
            String fieldName = attributeField.getName();
            if (attributeFieldsMap.containsKey(fieldName))
                throw new WdkModelException("The AttributeField " + fieldName
                        + " is duplicated in the recordClass " + getFullName());
            attributeFieldsMap.put(fieldName, attributeField);
        }
    }

    public void addTableField(TableField tableField) {
        tableField.setRecordClass(this);
        tableFieldList.add(tableField);
    }

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
        nestedRecordQuestionRefList.add(nr);
    }

    public void addNestedRecordListQuestionRef(NestedRecordList nrl) {

        nestedRecordListQuestionRefList.add(nrl);
    }

    public void addReporterRef(ReporterRef reporter) {
        reporterList.add(reporter);
    }

    public void addSubType(SubType subType) {
        subTypeList.add(subType);
    }

    // ////////////////////////////////////////////////////////////
    // public getters
    // ////////////////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

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

    public SubType getSubType() {
        return subType;
    }

    public RecordInstance makeRecordInstance(String recordId)
            throws WdkModelException {
        return new RecordInstance(this, recordId);
    }

    public RecordInstance makeRecordInstance(String projectId, String recordId)
            throws WdkModelException {
        String sourceId = lookupSourceId(projectId, recordId);
        return new RecordInstance(this, projectId, sourceId);
    }

    public Map<String, ReporterRef> getReporterMap() {
        return new LinkedHashMap<String, ReporterRef>(reporterMap);
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer("Record: name='" + name + "'").append(newline);

        buf.append("--- Attributes ---").append(newline);
        for (AttributeField attribute : attributeFieldsMap.values()) {
            buf.append(attribute).append(newline);
        }

        buf.append("--- Tables ---").append(newline);
        for (TableField table : tableFieldsMap.values()) {
            buf.append(table).append(newline);
        }
        return buf.toString();
    }

    /*
     * <sanityRecord ref="GeneRecordClasses.GeneRecordClass"
     * primaryKey="PF11_0344"/>
     */
    public String getSanityTestSuggestion() throws WdkModelException {
        String indent = "    ";
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer(newline + newline + indent
                + "<sanityRecord ref=\"" + getFullName() + "\"" + newline
                + indent + indent + indent + "primaryKey=\"FIX_pk\">" + newline);
        buf.append(indent + "</sanityRecord>");
        return buf.toString();
    }

    // /////////////////////////////////////////////////////////////////////////
    // package scope methods
    // /////////////////////////////////////////////////////////////////////////

    /**
     * @param recordSetName
     *                name of the recordSet to which this record belongs.
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
            throw new WdkModelException(message);
        }
        return tableField;
    }

    void resolveReferences(WdkModel model) throws WdkModelException {
        // Added by Jerric
        // resolve projectParam
        AbstractEnumParam projectParam = null;
        if (hasProjectId()) {
            projectParam = (AbstractEnumParam) model.resolveReference(projectParamRef.getTwoPartName());
            projectParam = (AbstractEnumParam) projectParam.clone();
            projectParam.setDefault(projectParamRef.getDefault());
        }

        // create PrimaryKeyField
        primaryKeyField = new PrimaryKeyField(PRIMARY_KEY_NAME, getType(),
                "Some help here", projectParam);
        primaryKeyField.setIdPrefix(this.idPrefix);
        primaryKeyField.setDelimiter(this.delimiter);
        attributeFieldsMap.put(PRIMARY_KEY_NAME, primaryKeyField);

        // resolve the references for attribute queries
        for (AttributeQueryReference reference : attributesQueryRefs.values()) {
            // add attributes to the record class. it must be performed before
            // next step.
            Map<String, AttributeField> fieldMap = reference.getAttributeFieldMap();
            Collection<AttributeField> fields = fieldMap.values();
            for (AttributeField field : fields) {
                addAttributeField(field);
            }

            // resolve Query and associate columns with the attribute fields
            Query query = (Query) model.resolveReference(reference.getTwoPartName());
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

        for (NestedRecord nestedRecord : nestedRecordQuestionRefs.values()) {
            nestedRecord.resolveReferences(model);
        }

        for (NestedRecordList nestedRecordList : nestedRecordListQuestionRefs.values()) {
            nestedRecordList.resolveReferences(model);
        }

        // resolve reference for alias query
        if (aliasQueryName != null) {
            aliasQuery = (Query) model.resolveReference(aliasQueryName);
        }

        // resolve reference for sub type query
        if (subType != null) subType.resolveReferences(model);
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
        for (NestedRecord nextNr : nestedRecordQuestionRefs.values()) {
            Question q = nextNr.getQuestion();
            addNestedRecordQuestion(q);
        }

        nestedRecordListQuestions = new LinkedHashMap<String, Question>();
        for (NestedRecordList nextNrl : nestedRecordListQuestionRefs.values()) {
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
                    throw new WdkModelException(message);
                }
                orderedAttsMap.put(nextAtt, nextAttField);
            }
        }
        // add all attributes not in the ordering
        for (String nextAtt : attributeFieldsMap.keySet()) {
            if (!orderedAttsMap.containsKey(nextAtt)) {
                AttributeField nextField = attributeFieldsMap.get(nextAtt);
                orderedAttsMap.put(nextAtt, nextField);
            }
        }
        return orderedAttsMap;
    }

    private String lookupSourceId(String projectId, String aliasName)
            throws WdkModelException {
        // nothing to look up
        if (aliasQuery == null) return aliasName;

        // create a query instance with alias as the primaryKey
        QueryInstance qinstance = aliasQuery.makeInstance();
        qinstance.setIsCacheable(false);
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(PRIMARY_KEY_NAME, aliasName);

        // check if we need to add projectId too
        if (hasProjectId()) params.put(PROJECT_ID_NAME, projectId);

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

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude reporters
        for (ReporterRef reporter : reporterList) {
            if (reporter.include(projectId)) {
                reporter.excludeResources(projectId);
                String reporterName = reporter.getName();
                if (reporterMap.containsKey(reporterName))
                    throw new WdkModelException("The reporter " + reporterName
                            + " is duplicated in recordClass "
                            + this.getFullName());
                reporterMap.put(reporterName, reporter);
            }
        }
        reporterList = null;

        // exclude subTypes
        for (SubType subType : subTypeList) {
            if (subType.include(projectId)) {
                subType.excludeResources(projectId);
                this.subType = subType;
                break;
            }
        }
        subTypeList = null;

        // exclude attributes
        for (AttributeField field : attributeFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (attributeFieldsMap.containsKey(fieldName))
                    throw new WdkModelException("The attributeField "
                            + fieldName + " is duplicated in recordClass "
                            + getFullName());
                attributeFieldsMap.put(fieldName, field);
            }
        }
        attributeFieldList = null;

        // exclude table fields
        for (TableField field : tableFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (attributeFieldsMap.containsKey(fieldName))
                    throw new WdkModelException("The table " + fieldName
                            + " is duplicated in recordClass " + getFullName());
                tableFieldsMap.put(fieldName, field);
            }
        }
        tableFieldList = null;

        // exclude query refs
        for (AttributeQueryReference queryRef : attributesQueryRefList) {
            if (queryRef.include(projectId)) {
                String refName = queryRef.getTwoPartName();
                if (attributesQueryRefs.containsKey(refName)) {
                    throw new WdkModelException("recordClass " + getFullName()
                            + " has more than one attributeQueryRef \""
                            + refName + "\"");
                } else {
                    queryRef.excludeResources(projectId);
                    attributesQueryRefs.put(refName, queryRef);
                }
            }
        }
        attributesQueryRefList = null;

        // exclude nested records
        for (NestedRecord nestedRecord : nestedRecordQuestionRefList) {
            if (nestedRecord.include(projectId)) {
                String refName = nestedRecord.getTwoPartName();
                if (nestedRecordQuestionRefs.containsKey(refName)) {
                    throw new WdkModelException("recordClass " + getFullName()
                            + " has more than one nestedRecord \"" + refName
                            + "\"");
                } else {
                    nestedRecord.excludeResources(projectId);
                    nestedRecordQuestionRefs.put(refName, nestedRecord);
                }
            }
        }
        nestedRecordQuestionRefList = null;

        // exclude nested record lists
        for (NestedRecordList recordList : nestedRecordListQuestionRefList) {
            if (recordList.include(projectId)) {
                String refName = recordList.getTwoPartName();
                if (nestedRecordListQuestionRefs.containsKey(refName)) {
                    throw new WdkModelException("recordClass " + getFullName()
                            + " has more than one nestedRecordList \""
                            + refName + "\"");
                } else {
                    recordList.excludeResources(projectId);
                    nestedRecordListQuestionRefs.put(refName, recordList);
                }
            }
        }
        nestedRecordListQuestionRefList = null;
    }

    public boolean hasProjectId() {
        return (projectParamRef != null);
    }
}
