package org.gusdb.wdk.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.user.User;

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

public class Question extends WdkModelBase implements Serializable {

    private static final long serialVersionUID = -446811404645317117L;

    private static final Logger logger = Logger.getLogger(Question.class);

    private String recordClassTwoPartName;

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

    private String[] summaryAttributeNames;
    private Map<String, AttributeField> summaryAttributeMap = new LinkedHashMap<String, AttributeField>();
    private Map<String, Boolean> sortingAttributeMap = new LinkedHashMap<String, Boolean>();

    private List<DynamicAttributeSet> dynamicAttributeSets = new ArrayList<DynamicAttributeSet>();
    private DynamicAttributeSet dynamicAttributeSet;

    private List<PropertyList> propertyLists = new ArrayList<PropertyList>();
    private Map<String, String[]> propertyListMap = new LinkedHashMap<String, String[]>();

    private WdkModel wdkModel;

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
        this.recordClassTwoPartName = question.recordClassTwoPartName;
        this.sortingAttributeMap.putAll(question.sortingAttributeMap);
        this.summary = question.summary;
        this.summaryAttributeMap.putAll(question.summaryAttributeMap);
        this.summaryAttributeNames = question.summaryAttributeNames;
        this.wdkModel = question.wdkModel;
    }

    /**
     * @return
     */
    public WdkModel getWdkModel() {
        return this.wdkModel;
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

    public void setRecordClassRef(String recordClassTwoPartName) {

        this.recordClassTwoPartName = recordClassTwoPartName;
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
        this.dynamicAttributeSets.add(dynamicAttributes);
    }

    public Map<String, AttributeField> getSummaryAttributes() {
        return summaryAttributeMap;
    }

    public Map<String, AttributeField> getReportMakerAttributeFields() {
        Map<String, AttributeField> rmfields = recordClass.getReportMakerAttributeFieldMap();
        if (dynamicAttributeSet != null)
            rmfields.putAll(dynamicAttributeSet.getReportMakerAttributeFieldMap());
        return rmfields;
    }

    public Map<String, TableField> getReportMakerTableFields() {
        Map<String, TableField> rmfields = recordClass.getReportMakerTableFieldMap();
        return rmfields;
    }

    public Map<String, Field> getReportMakerFields() {
        Map<String, Field> fields = new LinkedHashMap<String, Field>();
        Map<String, AttributeField> attributes = getReportMakerAttributeFields();
        Map<String, TableField> tables = getReportMakerTableFields();

        for (String name : attributes.keySet()) {
            fields.put(name, attributes.get(name));
        }
        for (String name : tables.keySet()) {
            fields.put(name, tables.get(name));
        }
        return fields;
    }

    // /////////////////////////////////////////////////////////////////////

    /**
     * make an answer that returns all records in one page.
     * 
     * @param paramValues
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public Answer makeAnswer(Map<String, Object> paramValues)
            throws WdkUserException, WdkModelException {
        return makeAnswer(paramValues, sortingAttributeMap);
    }

    /**
     * make an answer that returns all records in one page, sorted by the given
     * attribute list.
     * 
     * @param paramValues
     * @param sortingAttributes
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public Answer makeAnswer(Map<String, Object> paramValues,
            Map<String, Boolean> sortingAttributes) throws WdkUserException,
            WdkModelException {
        // get the result size by making a temp answer
        Answer answer = makeAnswer(paramValues, 1, 1, sortingAttributes);
        int resultSize = answer.getResultSize();

        // skip empty answers and one-record answers
        if (resultSize <= 1) return answer;

        // make an answer containing all records
        return makeAnswer(paramValues, 1, resultSize, sortingAttributes);
    }

    /**
     * make an answer by given page range.
     * 
     * @param paramValues
     * @param i
     * @param j
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public Answer makeAnswer(Map<String, Object> paramValues, int i, int j)
            throws WdkUserException, WdkModelException {
        return makeAnswer(paramValues, i, j, sortingAttributeMap);
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
     */
    public Answer makeAnswer(Map<String, Object> paramValues, int i, int j,
            Map<String, Boolean> sortingAttributes) throws WdkUserException,
            WdkModelException {
        QueryInstance qi = query.makeInstance();
        qi.setValues(paramValues);
        Answer answer = new Answer(this, qi, i, j, sortingAttributes);

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

    public String getCategory() {
        return (category == null) ? "" : category;
    }

    public RecordClass getRecordClass() {
        return this.recordClass;
    }

    public Query getQuery() {
        return this.query;
    }

    public void setRecordClass(RecordClass rc) {
        this.recordClass = rc;
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
        for (String saName : summaryAttributeMap.keySet()) {
            saNames.append(saName + ", ");
        }
        StringBuffer buf = new StringBuffer("Question: name='" + name + "'"
                + newline + "  recordClass='" + recordClassTwoPartName + "'"
                + newline + "  query='" + queryTwoPartName + "'" + newline
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

    /*
     * <sanityQuestion ref="GeneQuestions.GenesByEcNumber" minOutputLength="1"
     * maxOutputLength="3" pageStart="1" pageEnd="20"> <sanityParam
     * name="pf_organism" value="Plasmodium falciparum"/> <sanityParam
     * name="ec_number_pattern" value="6.1.1.12"/> </sanityQuestion>
     */
    public String getSanityTestSuggestion() throws WdkModelException {
        String indent = "    ";
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer(newline + newline + indent
                + "<sanityQuestion ref=\"" + getFullName() + "\"" + newline
                + indent + indent + indent + "pageStart=\"1\" pageEnd=\"20\""
                + newline + indent + indent + indent
                + "minOutputLength=\"FIX_min_len\" "
                + "maxOutputLength=\"FIX_max_len\">" + newline);
        for (Param param : getQuery().getParams()) {
            String paramName = param.getName();
            String value = param.getDefault();
            if (value == null) value = "FIX_null_dflt";
            buf.append(indent + indent + "<sanityParam name=\"" + paramName
                    + "\" value=\"" + value + "\"/>" + newline);
        }
        buf.append(indent + "</sanityQuestion>");
        return buf.toString();
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

    // /////////////////////////////////////////////////////////////////////
    // package methods
    // /////////////////////////////////////////////////////////////////////

    Map<String, AttributeField> getDynamicAttributeFields() {
        return dynamicAttributeSet == null ? null
                : dynamicAttributeSet.getAttributeFields();
    }

    void setResources(WdkModel model) throws WdkModelException {
        this.wdkModel = model;
        if (dynamicAttributeSet != null) {
            dynamicAttributeSet.setQuestion(this);
            dynamicAttributeSet.setResources(wdkModel);
        }
        initSummaryAttributes();
    }

    Map<String, AttributeField> getAttributeFields() {
        Map<String, AttributeField> attributeFields = new LinkedHashMap<String, AttributeField>(
                recordClass.getAttributeFieldMap());
        if (dynamicAttributeSet != null) {
            attributeFields.putAll(dynamicAttributeSet.getAttributeFields());
        }
        return attributeFields;
    }

    void resolveReferences(WdkModel model) throws WdkModelException {

        this.query = (Query) model.resolveReference(queryTwoPartName);
        Object rc = model.resolveReference(recordClassTwoPartName);
        setRecordClass((RecordClass) rc);
    }

    boolean isSummaryAttribute(String attName) {
        return summaryAttributeMap.get(attName) != null;
    }

    void setSummaryAttributesMap(Map<String, AttributeField> summaryAtts) {
        this.summaryAttributeMap = summaryAtts;
    }

    // /////////////////////////////////////////////////////////////////////
    // Protected Methods
    // /////////////////////////////////////////////////////////////////////

    protected void setQuestionSet(QuestionSet questionSet)
            throws WdkModelException {
        this.questionSet = questionSet;
    }

    private void initSummaryAttributes() throws WdkModelException {
        if (summaryAttributeNames != null) {
            summaryAttributeMap = new LinkedHashMap<String, AttributeField>();
            Map<String, AttributeField> attMap = getAttributeFields();

            for (String name : summaryAttributeNames) {
                if (attMap.get(name) == null) {
                    throw new WdkModelException("Question " + getName()
                            + " has unknown summary attribute: '" + name + "'");
                }
                summaryAttributeMap.put(name, attMap.get(name));
            }
        } else {
            Map<String, AttributeField> recAttrsMap = getRecordClass().getAttributeFieldMap();
            summaryAttributeMap = new LinkedHashMap<String, AttributeField>(
                    recAttrsMap);
            Iterator<String> ramI = recAttrsMap.keySet().iterator();
            String attribName = null;
            while (ramI.hasNext()) {
                attribName = ramI.next();
                AttributeField attr = recAttrsMap.get(attribName);
                if (attr.getInternal()) {
                    summaryAttributeMap.remove(attribName);
                }
            }
        }
    }

    /**
     * This method is use to clone the question, excluding dynamic attributes
     * 
     * @return
     * @throws WdkModelException
     */
    public Question getBaseQuestion() throws WdkModelException {
        Question question = new Question();
        question.description = this.description;
        question.summary = this.summary;
        question.displayName = this.displayName;
        question.help = this.help;
        question.name = this.name;
        question.queryTwoPartName = this.queryTwoPartName;
        question.questionSet = this.questionSet;
        question.recordClass = this.recordClass;
        question.recordClassTwoPartName = this.recordClassTwoPartName;

        // needs to clone this summary attribute as well
        Map<String, AttributeField> sumAttributes = new LinkedHashMap<String, AttributeField>();
        Map<String, AttributeField> attributes = recordClass.getAttributeFieldMap();
        for (String attrName : summaryAttributeMap.keySet()) {
            if (attributes.containsKey(attrName))
                sumAttributes.put(attrName, summaryAttributeMap.get(attrName));
        }
        question.summaryAttributeMap = sumAttributes;

        // clone the query too, but excludes the columns in dynamic attribute
        Set<String> excludedColumns = new LinkedHashSet<String>();
        // TEST
        StringBuffer sb = new StringBuffer();

        if (this.dynamicAttributeSet != null) {
            attributes = dynamicAttributeSet.getAttributeFields();
            for (AttributeField field : attributes.values()) {
                if (field instanceof ColumnAttributeField) {
                    ColumnAttributeField cfield = (ColumnAttributeField) field;
                    excludedColumns.add(cfield.getColumn().getName());
                    // TEST
                    sb.append(cfield.getColumn().getName() + ", ");
                }
            }
        }
        logger.debug("Excluded fields: " + sb.toString());

        Query newQuery = this.query.getBaseQuery(excludedColumns);
        question.query = newQuery;

        return question;
    }

    public String getSignature() throws WdkModelException {
        return query.getSignature();
    }

    public Map<String, Boolean> getDefaultSortingAttributes() {
        Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();
        int count = 0;
        for (String attrName : sortingAttributeMap.keySet()) {
            sortMap.put(attrName, sortingAttributeMap.get(attrName));
            count++;
            if (count >= User.SORTING_LEVEL) break;
        }
        return sortMap;
    }

    /**
     * @return the sortingAttributeMap
     */
    public Map<String, Boolean> getSortingAttributeMap() {
        return new LinkedHashMap<String, Boolean>(this.sortingAttributeMap);
    }

    /**
     * @return the summaryAttributeNames
     */
    public String[] getSummaryAttributeNames() {
        if (summaryAttributeNames == null) return null;

        String[] array = new String[summaryAttributeNames.length];
        System.arraycopy(summaryAttributeNames, 0, array, 0, array.length);
        return array;
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
                    this.summaryAttributeNames = attributeList.getSummaryAttributeNames();
                    this.sortingAttributeMap = attributeList.getSortingAttributeMap();
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
}
