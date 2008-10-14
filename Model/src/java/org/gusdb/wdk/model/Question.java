package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

/**
 * Question.java
 * 
 * A class representing a binding between a RecordClass and a Query.
 * 
 * Created: Fri June 4 11:19:30 2004 EDT
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2007-01-10 14:54:53 -0500 (Wed, 10 Jan
 *          2007) $ $Author$
 */

public class Question extends WdkModelBase implements AttributeFieldContainer {

    public static final String BOOLEAN_QUESTION_PREFIX = "boolean_question_";

    private static final String DYNAMIC_QUERY_SUFFIX = "_dynamic";

    private static final long serialVersionUID = -446811404645317117L;

    private static final Logger logger = Logger.getLogger(Question.class);

    private String recordClassRef;

    private String queryTwoPartName;

    private String name;

    private String displayName;

    private List<WdkModelText> descriptions = new ArrayList<WdkModelText>();
    private String description;

    private List<WdkModelText> summaries = new ArrayList<WdkModelText>();
    private String summary;

    private List<WdkModelText> helps = new ArrayList<WdkModelText>();
    private String help;

    private QuestionSet questionSet;

    private Query query;

    protected RecordClass recordClass;

    private String category;

    private boolean fullAnswer = false;

    private List<AttributeList> attributeLists = new ArrayList<AttributeList>();

    private String[] defaultSummaryAttributeNames;
    private Map<String, AttributeField> defaultSummaryAttributeFields = new LinkedHashMap<String, AttributeField>();
    private Map<String, Boolean> defaultSortingMap = new LinkedHashMap<String, Boolean>();

    private List<DynamicAttributeSet> dynamicAttributeSets = new ArrayList<DynamicAttributeSet>();
    private DynamicAttributeSet dynamicAttributeSet;
    private Query dynamicAttributeQuery;

    private List<PropertyList> propertyLists = new ArrayList<PropertyList>();
    private Map<String, String[]> propertyListMap = new LinkedHashMap<String, String[]>();

    private WdkModel wdkModel;

    private boolean noSummaryOnSingleRecord = false;

    private boolean ignoreSubType = false;

    // /////////////////////////////////////////////////////////////////////
    // setters called at initialization
    // /////////////////////////////////////////////////////////////////////

    /**
     * default constructor used by model parser
     */
    public Question() {}

    /**
     * copy constructor
     * 
     * @param question
     */
    public Question(Question question) {
        this.category = question.category;
        this.description = question.description;
        this.displayName = question.displayName;

        // TODO - need to deep-copy dynamicAttributeSet as well
        this.dynamicAttributeSet = question.dynamicAttributeSet;

        this.help = question.help;
        this.propertyListMap.putAll(question.propertyListMap);

        // need to deep-copy query as well
        this.query = question.query;
        this.queryTwoPartName = question.queryTwoPartName;
        this.questionSet = question.questionSet;
        this.recordClass = question.recordClass;
        this.recordClassRef = question.recordClassRef;
        this.defaultSortingMap.putAll(question.defaultSortingMap);
        this.summary = question.summary;
        this.defaultSummaryAttributeFields.putAll(question.defaultSummaryAttributeFields);
        this.wdkModel = question.wdkModel;

        this.noSummaryOnSingleRecord = question.noSummaryOnSingleRecord;
    }

    /**
     * @return
     */
    public WdkModel getWdkModel() {
        return this.wdkModel;
    }

    public void setWdkModel(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addDescription(WdkModelText description) {
        this.descriptions.add(description);
    }

    public void addSummary(WdkModelText summary) {
        this.summaries.add(summary);
    }

    public void addHelp(WdkModelText help) {
        this.helps.add(help);
    }

    public void setRecordClassRef(String recordClassRef) {
        this.recordClassRef = recordClassRef;
    }

    public void setRecordClass(RecordClass recordClass) {
        this.recordClass = recordClass;
    }

    public void setQueryRef(String queryTwoPartName) {
        this.queryTwoPartName = queryTwoPartName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void addAttributeList(AttributeList attributeList) {
        this.attributeLists.add(attributeList);
    }

    public void addDynamicAttributeSet(DynamicAttributeSet dynamicAttributes) {
        dynamicAttributes.setQuestion(this);
        this.dynamicAttributeSets.add(dynamicAttributes);
    }

    public Map<String, Field> getFields(FieldScope scope) {
        Map<String, Field> fields = new LinkedHashMap<String, Field>();
        Map<String, AttributeField> attributes = getAttributeFieldMap(scope);
        Map<String, TableField> tables = recordClass.getTableFieldMap(scope);

        fields.putAll(attributes);
        fields.putAll(tables);
        return fields;
    }

    Query getDynamicAttributeQuery() {
        return dynamicAttributeQuery;
    }

    // /////////////////////////////////////////////////////////////////////

    /**
     * make an answer with default page size
     * 
     * @param paramValues
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public Answer makeAnswer(Map<String, Object> paramValues)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        int pageStart = 1;
        int pageEnd = Utilities.DEFAULT_PAGE_SIZE;
        Map<String, Boolean> sortingMap = this.defaultSortingMap;
        AnswerFilterInstance filter = recordClass.getDefaultFilter();
        return makeAnswer(paramValues, pageStart, pageEnd, sortingMap, filter);
    }

    /**
     * make an answer by given page range, sorted by the given attribute list.
     * 
     * @param paramValues
     * @param i
     * @param j
     * @param sortingAttributes
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public Answer makeAnswer(Map<String, Object> paramValues, int pageStart,
            int pageEnd, Map<String, Boolean> sortingAttributes,
            AnswerFilterInstance filter) throws WdkUserException,
            WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException {
        QueryInstance qi = query.makeInstance(paramValues);
        Answer answer = new Answer(this, qi, pageStart, pageEnd,
                sortingAttributes, filter);

        return answer;
    }

    public Param[] getParams() {
        return query.getParams();
    }

    public Map<String, Param> getParamMap() {
        return query.getParamMap();
    }

    public Map<Group, Map<String, Param>> getParamMapByGroups() {
        Param[] params = query.getParams();
        Map<Group, Map<String, Param>> paramGroups = new LinkedHashMap<Group, Map<String, Param>>();
        for (Param param : params) {
            Group group = param.getGroup();
            Map<String, Param> paramGroup;
            if (paramGroups.containsKey(group)) {
                paramGroup = paramGroups.get(group);
            } else {
                paramGroup = new LinkedHashMap<String, Param>();
                paramGroups.put(group, paramGroup);
            }
            paramGroup.put(param.getName(), param);
        }
        return paramGroups;
    }

    public Map<Group, Map<String, Param>> getParamMapByGroups(String displayType) {
        Param[] params = query.getParams();
        Map<Group, Map<String, Param>> paramGroups = new LinkedHashMap<Group, Map<String, Param>>();
        for (Param param : params) {
            Group group = param.getGroup();
            if (!group.getDisplayType().equalsIgnoreCase(displayType))
                continue;
            Map<String, Param> paramGroup;
            if (paramGroups.containsKey(group)) {
                paramGroup = paramGroups.get(group);
            } else {
                paramGroup = new LinkedHashMap<String, Param>();
                paramGroups.put(group, paramGroup);
            }
            paramGroup.put(param.getName(), param);
        }
        return paramGroups;
    }

    public String getDescription() {
        return description;
    }

    public String getSummary() {
        return summary;
    }

    public String getHelp() {
        return help;
    }

    public String getDisplayName() {
        if (displayName == null) displayName = getFullName();
        return displayName;
    }

    /**
     * @deprecated
     */
    public String getCategory() {
        return (category == null) ? "" : category;
    }

    public RecordClass getRecordClass() {
        return this.recordClass;
    }

    public Query getQuery() {
        return this.query;
    }

    public void setQuery(Query q) {
        this.query = q;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        if (questionSet == null) return name;
        else return questionSet.getName() + "." + name;
    }

    public String getQuestionSetName() {
        return questionSet.getName();
    }

    public String toString() {
        String newline = System.getProperty("line.separator");

        StringBuffer saNames = new StringBuffer();
        Map<String, AttributeField> summaryFields = getAttributeFieldMap(FieldScope.NON_INTERNAL);
        for (String saName : summaryFields.keySet()) {
            saNames.append(saName + ", ");
        }
        StringBuffer buf = new StringBuffer("Question: name='" + name + "'"
                + newline + "  recordClass='" + recordClassRef + "'" + newline
                + "  query='" + queryTwoPartName + "'" + newline
                + "  displayName='" + getDisplayName() + "'" + newline
                + "  summary='" + getSummary() + "'" + newline
                + "  description='" + getDescription() + "'" + newline
                + "  summaryAttributes='" + saNames + "'" + newline
                + "  help='" + getHelp() + "'" + newline);
        if (dynamicAttributeSet != null) {
            buf.append(dynamicAttributeSet.toString());
        }
        return buf.toString();
    }

    public boolean isDynamic() {
        return dynamicAttributeSet != null;
    }

    /**
     * A indicator to the controller whether this question should make answers
     * that contains all records in one page or not.
     * 
     * @return the fullAnswer
     */
    public boolean isFullAnswer() {
        return fullAnswer;
    }

    /**
     * Set the indicator to the controller that suggests this question to make
     * answers containing all records in one page, or not.
     * 
     * @param fullAnswer
     *            the fullAnswer to set
     */
    public void setFullAnswer(boolean fullAnswer) {
        this.fullAnswer = fullAnswer;
    }

    /**
     * @return the noSummaryOnSingleRecord
     */
    public boolean isNoSummaryOnSingleRecord() {
        return noSummaryOnSingleRecord;
    }

    /**
     * @param noSummaryOnSingleRecord
     *            the noSummaryOnSingleRecord to set
     */
    public void setNoSummaryOnSingleRecord(boolean noSummaryOnSingleRecord) {
        this.noSummaryOnSingleRecord = noSummaryOnSingleRecord;
    }

    // /////////////////////////////////////////////////////////////////////
    // package methods
    // /////////////////////////////////////////////////////////////////////

    Map<String, AttributeField> getDynamicAttributeFields() {
        return dynamicAttributeSet == null
                ? new LinkedHashMap<String, AttributeField>()
                : dynamicAttributeSet.getAttributeFieldMap();
    }

    /**
     * The difference between this method and getAttribute(SUMMARY) is that the
     * getAttribute(SUMMARY) will get the configured summary list, and if the
     * list is not configured, it will return all non-internal attribute fields;
     * meanwhile this method returns the configured list if it is configured,
     * otherwise it only return a limited number of attribtue fields for display
     * purpose.
     * 
     * @return
     */
    public Map<String, AttributeField> getSummaryAttributeFieldMap() {
        Map<String, AttributeField> attributeFields = new LinkedHashMap<String, AttributeField>();

        // always put primary key as the first field
        AttributeField pkField = recordClass.getPrimaryKeyAttributeField();
        attributeFields.put(pkField.getName(), pkField);

        if (defaultSummaryAttributeFields.size() > 0) {
            attributeFields.putAll(defaultSummaryAttributeFields);
        } else {
            attributeFields = recordClass.getSummaryAttributeFieldMap();
        }
        return attributeFields;
    }

    public Map<String, AttributeField> getAttributeFieldMap() {
        return getAttributeFieldMap(FieldScope.ALL);
    }

    public Map<String, AttributeField> getAttributeFieldMap(FieldScope scope) {
        Map<String, AttributeField> attributeFields = new LinkedHashMap<String, AttributeField>();

        // always put primary key as the first field
        AttributeField pkField = recordClass.getPrimaryKeyAttributeField();
        attributeFields.put(pkField.getName(), pkField);

        attributeFields.putAll(recordClass.getAttributeFieldMap(scope));

        if (dynamicAttributeSet != null)
            attributeFields.putAll(dynamicAttributeSet.getAttributeFieldMap(scope));

        return attributeFields;
    }

    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (resolved) return;

        this.wdkModel = model;

        // it must happen before dynamicAttributeSet, because it is referenced
        // in the dynamicAttributeSet.
        this.recordClass = (RecordClass) model.resolveReference(recordClassRef);

        // the id query is forced to be cache-able.
        query = (Query) model.resolveReference(queryTwoPartName);
        query.setIsCacheable(true);

        // dynamic attribute set need to be initialized after the id query.
        if (dynamicAttributeSet != null) {
            this.dynamicAttributeQuery = createDynamicAttributeQuery();
            dynamicAttributeSet.resolveReferences(model);
        }

        // resolve default summary attributes
        if (defaultSummaryAttributeNames != null) {
            Map<String, AttributeField> attributeFields = getAttributeFieldMap();
            for (String fieldName : defaultSummaryAttributeNames) {
                AttributeField field = attributeFields.get(fieldName);
                if (field == null)
                    throw new WdkModelException("Summary attribute field ["
                            + fieldName + "] defined in question ["
                            + getFullName() + "] is invalid.");
                defaultSummaryAttributeFields.put(fieldName, field);
            }
        }
        defaultSummaryAttributeNames = null;
        
        // make sure we create index on primary keys
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        query.setIndexColumns(pkColumns);

        resolved = true;
    }

    // /////////////////////////////////////////////////////////////////////
    // Protected Methods
    // /////////////////////////////////////////////////////////////////////

    protected void setQuestionSet(QuestionSet questionSet)
            throws WdkModelException {
        this.questionSet = questionSet;
    }

    /**
     * This method is use to clone the question, excluding dynamic attributes
     * 
     * @return
     * @throws WdkModelException
     */
    // public Question getBaseQuestion() throws WdkModelException {
    // Question question = new Question();
    // question.description = this.description;
    // question.summary = this.summary;
    // question.displayName = this.displayName;
    // question.help = this.help;
    // question.name = this.name;
    // question.queryTwoPartName = this.queryTwoPartName;
    // question.questionSet = this.questionSet;
    // question.recordClass = this.recordClass;
    // question.recordClassTwoPartName = this.recordClassTwoPartName;
    //
    // // needs to clone this summary attribute as well
    // Map<String, AttributeField> sumAttributes = new LinkedHashMap<String,
    // AttributeField>();
    // Map<String, AttributeField> attributes =
    // recordClass.getAttributeFieldMap();
    // for (String attrName : summaryAttributeMap.keySet()) {
    // if (attributes.containsKey(attrName))
    // sumAttributes.put(attrName, summaryAttributeMap.get(attrName));
    // }
    // question.summaryAttributeMap = sumAttributes;
    //
    // // clone the query too, but excludes the columns in dynamic attribute
    // Set<String> excludedColumns = new LinkedHashSet<String>();
    // // TEST
    // StringBuffer sb = new StringBuffer();
    //
    // if (this.dynamicAttributeSet != null) {
    // attributes = dynamicAttributeSet.getAttributeFieldMap();
    // for (AttributeField field : attributes.values()) {
    // if (field instanceof ColumnAttributeField) {
    // ColumnAttributeField cfield = (ColumnAttributeField) field;
    // excludedColumns.add(cfield.getColumn().getName());
    // // TEST
    // sb.append(cfield.getColumn().getName() + ", ");
    // }
    // }
    // }
    // logger.debug("Excluded fields: " + sb.toString());
    //
    // Query newQuery = this.query.getBaseQuery(excludedColumns);
    // question.query = newQuery;
    //
    // return question;
    // }
    public Map<String, Boolean> getSortingAttributeMap() {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        int count = 0;
        for (String attrName : defaultSortingMap.keySet()) {
            map.put(attrName, defaultSortingMap.get(attrName));
            count++;
            if (count >= User.SORTING_LEVEL) break;
        }

        // no sorting map defined, use the definition in recordClass
        if (map.size() == 0) map = recordClass.getSortingAttributeMap();

        return map;
    }

    /**
     * This method is supposed to be called by the digester
     * 
     * @param propertyList
     */
    public void addPropertyList(PropertyList propertyList) {
        this.propertyLists.add(propertyList);
    }

    /**
     * if the property list of the given name doesn't exist, it will try to get
     * a default property list from the WdkModel.
     * 
     * @param propertyListName
     * @return
     */
    public String[] getPropertyList(String propertyListName) {
        if (!propertyListMap.containsKey(propertyListName))
            return wdkModel.getDefaultPropertyList(propertyListName);
        return propertyListMap.get(propertyListName);
    }

    public Map<String, String[]> getPropertyLists() {
        // get the default property lists
        Map<String, String[]> propLists = wdkModel.getDefaultPropertyLists();
        // replace the default ones with the ones defined in the question
        for (String plName : propertyListMap.keySet()) {
            String[] values = propertyListMap.get(plName);
            String[] array = new String[values.length];
            System.arraycopy(values, 0, array, 0, array.length);
            propLists.put(plName, array);
        }
        return propLists;
    }

    /**
     * @return the ignoreSubType
     */
    public boolean isIgnoreSubType() {
        return ignoreSubType;
    }

    /**
     * @param ignoreSubType
     *            the ignoreSubType to set
     */
    public void setIgnoreSubType(boolean ignoreSubType) {
        this.ignoreSubType = ignoreSubType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude descriptions
        boolean hasDescription = false;
        for (WdkModelText description : descriptions) {
            if (description.include(projectId)) {
                if (hasDescription) {
                    throw new WdkModelException("The question " + getFullName()
                            + " has more than one description for project "
                            + projectId);
                } else {
                    this.description = description.getText();
                    hasDescription = true;
                }
            }
        }
        descriptions = null;

        // exclude summaries
        boolean hasSummary = false;
        for (WdkModelText summ : summaries) {
            if (summ.include(projectId)) {
                if (hasSummary) {
                    throw new WdkModelException("The question " + getFullName()
                            + " has more than one summary for project "
                            + projectId);
                } else {
                    this.summary = summ.getText();
                    hasSummary = true;
                }
            }
        }
        summaries = null;

        // exclude helps
        boolean hasHelp = false;
        for (WdkModelText help : helps) {
            if (help.include(projectId)) {
                if (hasHelp) {
                    throw new WdkModelException("The question " + getFullName()
                            + " has more than one help for project "
                            + projectId);
                } else {
                    this.help = help.getText();
                    hasHelp = true;
                }
            }
        }
        helps = null;

        // exclude summary and sorting attribute list
        boolean hasAttributeList = false;
        for (AttributeList attributeList : attributeLists) {
            if (attributeList.include(projectId)) {
                if (hasAttributeList) {
                    throw new WdkModelException("The question " + getFullName()
                            + " has more than one <attributesList> for "
                            + "project " + projectId);
                } else {
                    this.defaultSummaryAttributeNames = attributeList.getSummaryAttributeNames();
                    this.defaultSortingMap = attributeList.getSortingAttributeMap();
                    hasAttributeList = true;
                }
            }
        }
        attributeLists = null;

        // exclude dynamic attribute set
        boolean hasDynamicAttributes = false;
        for (DynamicAttributeSet dynamicAttributeSet : dynamicAttributeSets) {
            if (dynamicAttributeSet.include(projectId)) {
                if (hasDynamicAttributes) {
                    throw new WdkModelException("The question " + getFullName()
                            + " has more than one <dynamicAttributes> for "
                            + "project " + projectId);
                } else {
                    dynamicAttributeSet.excludeResources(projectId);
                    this.dynamicAttributeSet = dynamicAttributeSet;
                    hasDynamicAttributes = true;
                }
            }
        }
        dynamicAttributeSets = null;

        // exclude property lists
        for (PropertyList propList : propertyLists) {
            if (propList.include(projectId)) {
                String listName = propList.getName();
                if (propertyListMap.containsKey(listName)) {
                    throw new WdkModelException("The question " + getFullName()
                            + " has more than one propertyList \"" + listName
                            + "\" for project " + projectId);
                } else {
                    propList.excludeResources(projectId);
                    propertyListMap.put(propList.getName(),
                            propList.getValues());
                }
            }
        }
        propertyLists = null;
    }

    private Query createDynamicAttributeQuery() throws WdkModelException {
        SqlQuery query = new SqlQuery();
        query.setIsCacheable(false);
        query.setName(this.query.getName() + DYNAMIC_QUERY_SUFFIX);
        this.query.getQuerySet().addQuery(query);

        // set the columns, which as the same column as the id query
        query.setColumns(this.query.getColumns());
        return query;
    }
}
