package org.gusdb.wdk.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.user.AnswerFactory;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.QueryFactory;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.json.JSONException;
import org.xml.sax.SAXException;

// why is this in impl?

/**
 * @author
 * @modified Jan 6, 2006 - Jerric Add a historyFactory in the model
 */
public class WdkModel {

    public static final String WDK_VERSION = "1.18";

    /**
     * Convenience method for constructing a model from the configuration
     * information
     * 
     * @throws JSONException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws NoSuchAlgorithmException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws WdkUserException
     */
    public static WdkModel construct(String projectId)
            throws WdkModelException, NoSuchAlgorithmException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException, IOException, SAXException, SQLException,
            JSONException, WdkUserException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        ModelXmlParser parser = new ModelXmlParser(gusHome);
        return parser.parseModel(projectId);
    }

    private ModelConfig modelConfig;
    private String projectId;

    private DBPlatform platform;
    private DBPlatform authenPlatform;

    private List<QuerySet> querySetList = new ArrayList<QuerySet>();
    private Map<String, ModelSetI> querySets = new LinkedHashMap<String, ModelSetI>();

    private List<ParamSet> paramSetList = new ArrayList<ParamSet>();
    private Map<String, ModelSetI> paramSets = new LinkedHashMap<String, ModelSetI>();

    private List<RecordClassSet> recordClassSetList = new ArrayList<RecordClassSet>();
    private Map<String, ModelSetI> recordClassSets = new LinkedHashMap<String, ModelSetI>();

    private List<QuestionSet> questionSetList = new ArrayList<QuestionSet>();
    private Map<String, ModelSetI> questionSets = new LinkedHashMap<String, ModelSetI>();

    private Map<String, ModelSetI> allModelSets = new LinkedHashMap<String, ModelSetI>();

    private List<GroupSet> groupSetList = new ArrayList<GroupSet>();
    private Map<String, ModelSetI> groupSets = new LinkedHashMap<String, ModelSetI>();

    private List<XmlQuestionSet> xmlQuestionSetList = new ArrayList<XmlQuestionSet>();
    private Map<String, ModelSetI> xmlQuestionSets = new LinkedHashMap<String, ModelSetI>();

    private List<XmlRecordClassSet> xmlRecordClassSetList = new ArrayList<XmlRecordClassSet>();
    private Map<String, ModelSetI> xmlRecordClassSets = new LinkedHashMap<String, ModelSetI>();

    private List<WdkModelName> wdkModelNames = new ArrayList<WdkModelName>();
    private String displayName;
    private String version; // use default version

    private List<WdkModelText> introductions = new ArrayList<WdkModelText>();
    private String introduction;

    private ResultFactory resultFactory;

    private AnswerFactory answerFactory;

    @Deprecated
    private Map<String, String> properties;

    private String webServiceUrl;

    /**
     * xmlSchemaURL is used by the XmlQuestions. This is the only place where
     * XmlQuestion can find it.
     */
    private URL xmlSchemaURL;

    private File xmlDataDir;

    private UserFactory userFactory;
    private DatasetFactory datasetFactory;
    private QueryFactory queryFactory;

    private List<PropertyList> defaultPropertyLists = new ArrayList<PropertyList>();
    private Map<String, String[]> defaultPropertyListMap = new LinkedHashMap<String, String[]>();

    private List<Categories> categoriesList = new ArrayList<Categories>();
    private Map<String, Categories> categoriesMap = new LinkedHashMap<String, Categories>();

    /**
     * @param initRecordClassList
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public Question getQuestion(String questionFullName)
            throws WdkUserException, WdkModelException {
        Reference r = new Reference(questionFullName);
        QuestionSet ss = getQuestionSet(r.getSetName());
        return ss.getQuestion(r.getElementName());
    }

    public Question[] getQuestions(RecordClass recordClass) {
        String rcName = recordClass.getFullName();
        List<Question> questions = new ArrayList<Question>();
        for (ModelSetI questionSet : questionSets.values()) {
            for (Question question : ((QuestionSet) questionSet).getQuestions()) {
                if (question.getRecordClass().getFullName().equals(rcName))
                    questions.add(question);
            }
        }
        Question[] array = new Question[questions.size()];
        questions.toArray(array);
        return array;
    }

    public RecordClass getRecordClass(String recordClassReference)
            throws WdkModelException {
        Reference r = new Reference(recordClassReference);
        RecordClassSet rs = getRecordClassSet(r.getSetName());
        return rs.getRecordClass(r.getElementName());
    }

    public ResultFactory getResultFactory() {
        return resultFactory;
    }

    public void addWdkModelName(WdkModelName wdkModelName) {
        this.wdkModelNames.add(wdkModelName);
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void addIntroduction(WdkModelText introduction) {
        introductions.add(introduction);
    }

    public String getIntroduction() {
        return introduction;
    }

    @Deprecated
    public Map<String, String> getProperties() {
        return properties;
    }

    @Deprecated
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    // RecordClass Sets

    public RecordClassSet getRecordClassSet(String recordClassSetName)
            throws WdkModelException {

        if (!recordClassSets.containsKey(recordClassSetName)) {
            String err = "WDK Model " + projectId
                    + " does not contain a recordClass set with name "
                    + recordClassSetName;

            throw new WdkModelException(err);
        }
        return (RecordClassSet) recordClassSets.get(recordClassSetName);
    }

    public RecordClassSet[] getAllRecordClassSets() {
        RecordClassSet sets[] = new RecordClassSet[recordClassSets.size()];
        recordClassSets.values().toArray(sets);
        return sets;
    }

    // Query Sets

    public QuerySet getQuerySet(String setName) throws WdkModelException {
        if (!querySets.containsKey(setName)) {
            String err = "WDK Model " + projectId
                    + " does not contain a query set with name " + setName;
            throw new WdkModelException(err);
        }
        return (QuerySet) querySets.get(setName);
    }

    public boolean hasQuerySet(String setName) {
        return querySets.containsKey(setName);
    }

    public QuerySet[] getAllQuerySets() {
        QuerySet sets[] = new QuerySet[querySets.size()];
        querySets.values().toArray(sets);
        return sets;
    }

    public QuestionSet[] getAllQuestionSets() {
        QuestionSet sets[] = new QuestionSet[questionSets.size()];
        questionSets.values().toArray(sets);
        return sets;
    }

    // Question Sets
    public QuestionSet getQuestionSet(String setName) throws WdkModelException {
        if (!questionSets.containsKey(setName)) {
            String err = "WDK Model " + projectId
                    + " does not contain a Question set with name " + setName;
            throw new WdkModelException(err);
        }
        return (QuestionSet) questionSets.get(setName);
    }

    public boolean hasQuestionSet(String setName) {
        return questionSets.containsKey(setName);
    }

    public Map<String, QuestionSet> getQuestionSets() {
        Map<String, QuestionSet> sets = new LinkedHashMap<String, QuestionSet>();
        for (String setName : questionSets.keySet()) {
            sets.put(setName, (QuestionSet) questionSets.get(setName));
        }
        return sets;
    }

    public Map<String, Map<String, Question[]>> getQuestionsByCategories() {
        Map<String, Map<String, Question[]>> allQuestions = new LinkedHashMap<String, Map<String, Question[]>>();
        for (String recordClassName : categoriesMap.keySet()) {
            Categories categories = categoriesMap.get(recordClassName);
            Map<String, Question[]> subQuestions = new LinkedHashMap<String, Question[]>();
            for (Category category : categories.getCategories()) {
                String categoryName = category.getDisplayName();
                Question[] questions = category.getQuestions();
                if (questions.length > 0)
                    subQuestions.put(categoryName, questions);
            }
            if (subQuestions.size() > 0)
                allQuestions.put(recordClassName, subQuestions);
        }
        return allQuestions;
    }

    public ParamSet getParamSet(String setName) throws WdkModelException {
        if (!paramSets.containsKey(setName)) {
            String err = "WDK Model " + projectId
                    + " does not contain a param set with name " + setName;
            throw new WdkModelException(err);
        }
        return (ParamSet) paramSets.get(setName);
    }

    public ParamSet[] getAllParamSets() {
        ParamSet[] sets = new ParamSet[paramSets.size()];
        paramSets.values().toArray(sets);
        return sets;
    }

    public GroupSet[] getAllGroupSets() {
        GroupSet[] array = new GroupSet[groupSets.size()];
        groupSets.values().toArray(array);
        return array;
    }

    public GroupSet getGroupSet(String setName) throws WdkModelException {
        GroupSet groupSet = (GroupSet) groupSets.get(setName);
        if (groupSet == null)
            throw new WdkModelException("The Model does not "
                    + "have a groupSet named " + setName);
        return groupSet;
    }

    public Question getBooleanQuestion(RecordClass recordClass)
            throws WdkModelException {
        // check if the boolean question already exists
        String qname = Question.BOOLEAN_QUESTION_PREFIX
                + recordClass.getFullName().replace('.', '_');
        QuestionSet internalSet = getQuestionSet(Utilities.INTERNAL_QUESTION_SET);

        Question booleanQuestion;
        if (internalSet.contains(qname)) {
            booleanQuestion = internalSet.getQuestion(qname);
        } else {
            booleanQuestion = new Question();
            booleanQuestion.setName(qname);
            booleanQuestion.setRecordClass(recordClass);
            booleanQuestion.setQuery(getBooleanQuery(recordClass));
            booleanQuestion.setWdkModel(this);
            internalSet.addQuestion(booleanQuestion);
        }
        return booleanQuestion;
    }

    public BooleanQuery getBooleanQuery(RecordClass recordClass)
            throws WdkModelException {
        // check if the boolean query already exists
        String queryName = BooleanQuery.getQueryName(recordClass);
        QuerySet internalQuerySet = getQuerySet(Utilities.INTERNAL_QUERY_SET);

        BooleanQuery booleanQuery;
        if (internalQuerySet.contains(queryName)) {
            booleanQuery = (BooleanQuery) internalQuerySet.getQuery(queryName);
        } else {
            booleanQuery = new BooleanQuery(recordClass);
            internalQuerySet.addQuery(booleanQuery);
        }
        return booleanQuery;
    }

    // ModelSetI's
    private void addSet(ModelSetI set, Map<String, ModelSetI> setMap)
            throws WdkModelException {
        String setName = set.getName();
        if (allModelSets.containsKey(setName)) {
            String err = "WDK Model " + projectId
                    + " already contains a set with name " + setName;

            throw new WdkModelException(err);
        }
        setMap.put(setName, set);
        allModelSets.put(setName, set);
    }

    /**
     * Set whatever resources the model needs. It will pass them to its kids
     */
    public void setResources() throws WdkModelException {
        for (ModelSetI modelSet : allModelSets.values()) {
            modelSet.setResources(this);
        }
    }

    /**
     * This method should happen after the resolveReferences, since projectId is
     * set by this method from modelConfig
     * 
     * @param gusHome
     * @throws WdkModelException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void configure(ModelConfig modelConfig) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {

        // assign projectId
        this.projectId = modelConfig.getProjectId();
        this.modelConfig = modelConfig;

        String connectionUrl = modelConfig.getConnectionUrl();
        String login = modelConfig.getLogin();
        String password = modelConfig.getPassword();
        String platformClass = modelConfig.getPlatformClass();
        Integer maxIdle = modelConfig.getMaxIdle();
        Integer minIdle = modelConfig.getMinIdle();
        Integer maxWait = modelConfig.getMaxWait();
        Integer maxActive = modelConfig.getMaxActive();
        Integer initialSize = modelConfig.getInitialSize();

        // also load the connection info for authentication database
        String authenPlatformClass = modelConfig.getAuthenticationPlatformClass();
        String authenLogin = modelConfig.getAuthenticationLogin();
        String authenPassword = modelConfig.getAuthenticationPassword();
        String authenConnection = modelConfig.getAuthenticationConnectionUrl();

        String loginSchema = modelConfig.getLoginSchema();

        String defaultRole = modelConfig.getDefaultRole();
        String smtpServer = modelConfig.getSmtpServer();
        String supportEmail = modelConfig.getSupportEmail();
        String emailSubject = modelConfig.getEmailSubject();
        String emailContent = modelConfig.getEmailContent();

        boolean enableQueryLogger = modelConfig.isEnableQueryLogger();
        String queryLoggerFile = modelConfig.getQueryLoggerFile();

        // initialize authentication factory
        // set the max active as half of the model's configuration

        authenPlatform = (DBPlatform) Class.forName(authenPlatformClass).newInstance();
        authenPlatform.initialize(this, "LOGIN", authenConnection, authenLogin,
                authenPassword, minIdle, maxIdle, maxWait, maxActive / 2);
        userFactory = new UserFactory(this, projectId, authenPlatform,
                loginSchema, defaultRole, smtpServer, supportEmail,
                emailSubject, emailContent);

        platform = (DBPlatform) Class.forName(platformClass).newInstance();
        platform.initialize(this, "QUERY", connectionUrl, login, password, minIdle, maxIdle,
                maxWait, maxActive);
        ResultFactory resultFactory = new ResultFactory(this);
        // 2008
        this.webServiceUrl = modelConfig.getWebServiceUrl();
        this.resultFactory = resultFactory;

        // initialize dataset factory with the login preferences
        datasetFactory = new DatasetFactory(authenPlatform, loginSchema);

        // initialize QueryFactory in user schema too
        queryFactory = new QueryFactory(authenPlatform, loginSchema);

        // initialize answerFactory
        answerFactory = new AnswerFactory(this);

        // resolve references in the model objects
        resolveReferences();
    }

    public ModelConfig getModelConfig() {
        return modelConfig;
    }

    public DBPlatform getQueryPlatform() {
        return platform;
    }

    // Function Added by Cary P. Feb 7, 2008
    public DBPlatform getAuthenticationPlatform() {
        return authenPlatform;
    }

    public UserFactory getUserFactory() throws WdkUserException {
        return userFactory;
    }

    public String getWebServiceUrl() {
        return webServiceUrl;
    }

    public Object resolveReference(String twoPartName) throws WdkModelException {
        String s = "Invalid reference '" + twoPartName + "'. ";

        // ensures <code>twoPartName</code> is formatted correctly
        Reference reference = new Reference(twoPartName);

        String setName = reference.getSetName();
        String elementName = reference.getElementName();

        ModelSetI set = (ModelSetI) allModelSets.get(setName);

        if (set == null) {
            String s3 = s + " There is no set called '" + setName + "'";
            throw new WdkModelException(s3);
        }
        Object element = set.getElement(elementName);
        if (element == null) {
            String s4 = s + " Set '" + setName + "' returned null for '"
                    + elementName + "'";
            throw new WdkModelException(s4);
        }
        return element;
    }

    /**
     * Some elements within the set may refer to others by name. Resolve those
     * references into real object references.
     * 
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     */
    private void resolveReferences() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // set the exception header
        WdkModelException.modelName = getProjectId();
        WdkUserException.modelName = getProjectId();

        // before the resources are excluded, the internal sets need to be
        // created.
        createInternalSets();

        // exclude resources that are not used by this project
        excludeResources();

        // Since we use Map here, the order of the sets in allModelSets are
        // random. However, if QuestionSet is resolved before a RecordSet, and
        // it goes down to resolve: QuestionSet -> Question -> RecordClass, and
        // when we try to resolve the RecordClass, a copy of it has been put
        // into RecordSet yet not being resolved. That means the attribute won't
        // be compatible since one contains nothing.
        // Iterator modelSets = allModelSets.values().iterator();
        // while (modelSets.hasNext()) {
        // ModelSetI modelSet = (ModelSetI) modelSets.next();
        // modelSet.resolveReferences(this);
        // }

        // instead, we first resolve querySets, then recordSets, and then
        // paramSets, and last on questionSets
        for (ModelSetI groupSet : groupSets.values()) {
            groupSet.resolveReferences(this);
        }
        for (ModelSetI querySet : querySets.values()) {
            querySet.resolveReferences(this);
        }
        for (ModelSetI paramSet : paramSets.values()) {
            paramSet.resolveReferences(this);
        }
        for (ModelSetI recordClassSet : recordClassSets.values()) {
            recordClassSet.resolveReferences(this);
        }
        for (ModelSetI questionSet : questionSets.values()) {
            questionSet.resolveReferences(this);
        }
        // resolve references for xml record classes and questions
        for (ModelSetI rcSet : xmlRecordClassSets.values()) {
            rcSet.resolveReferences(this);
        }
        for (ModelSetI qSet : xmlQuestionSets.values()) {
            qSet.resolveReferences(this);
        }
        for (Categories categories : this.categoriesMap.values()) {
            categories.resolveReferences(this);
        }
    }

    private void excludeResources() throws WdkModelException {
        // decide model name, display name, and version
        boolean hasModelName = false;
        for (WdkModelName wdkModelName : wdkModelNames) {
            if (wdkModelName.include(projectId)) {
                if (hasModelName) {
                    throw new WdkModelException("The model has more than one "
                            + "<modelName> for project " + projectId);
                } else {
                    this.displayName = wdkModelName.getDisplayName();
                    this.version = wdkModelName.getVersion();
                    hasModelName = true;
                }
            }
        }
        wdkModelNames = null; // no more use of modelNames

        // decide the introduction
        boolean hasIntroduction = false;
        for (WdkModelText intro : introductions) {
            if (intro.include(projectId)) {
                if (hasIntroduction) {
                    throw new WdkModelException("The model has more than one "
                            + "<introduction> for project " + projectId);
                } else {
                    this.introduction = intro.getText();
                    hasIntroduction = true;
                }
            }
        }
        introductions = null;

        // exclude the property list
        for (PropertyList propList : defaultPropertyLists) {
            if (propList.include(projectId)) {
                String listName = propList.getName();
                if (defaultPropertyListMap.containsKey(listName)) {
                    throw new WdkModelException("The model has more than one "
                            + "defaultPropertyList \"" + listName
                            + "\" for project " + projectId);
                } else {
                    propList.excludeResources(projectId);
                    defaultPropertyListMap.put(listName, propList.getValues());
                }
            }
        }
        defaultPropertyLists = null;

        // remove question sets
        for (QuestionSet questionSet : questionSetList) {
            if (questionSet.include(projectId)) {
                questionSet.excludeResources(projectId);
                addSet(questionSet, questionSets);
            }
        }
        questionSetList = null;

        // remove param sets
        for (ParamSet paramSet : paramSetList) {
            if (paramSet.include(projectId)) {
                paramSet.excludeResources(projectId);
                addSet(paramSet, paramSets);
            }
        }
        paramSetList = null;

        // remove query sets
        for (QuerySet querySet : querySetList) {
            if (querySet.include(projectId)) {
                querySet.excludeResources(projectId);
                addSet(querySet, querySets);
            }
        }
        querySetList = null;

        // remove record class sets
        for (RecordClassSet recordClassSet : recordClassSetList) {
            if (recordClassSet.include(projectId)) {
                recordClassSet.excludeResources(projectId);
                addSet(recordClassSet, recordClassSets);
            }
        }
        recordClassSetList = null;

        // remove group sets
        for (GroupSet groupSet : groupSetList) {
            if (groupSet.include(projectId)) {
                groupSet.excludeResources(projectId);
                addSet(groupSet, groupSets);
            }
        }
        groupSetList = null;

        // remove xml question sets
        for (XmlQuestionSet xmlQSet : xmlQuestionSetList) {
            if (xmlQSet.include(projectId)) {
                xmlQSet.excludeResources(projectId);
                addSet(xmlQSet, xmlQuestionSets);
            }
        }
        xmlQuestionSetList = null;

        // remove xml record class sets
        for (XmlRecordClassSet xmlRSet : xmlRecordClassSetList) {
            if (xmlRSet.include(projectId)) {
                xmlRSet.excludeResources(projectId);
                addSet(xmlRSet, xmlRecordClassSets);
            }
        }
        xmlRecordClassSetList = null;

        // exclude categories
        for (Categories categories : this.categoriesList) {
            if (categories.include(projectId)) {
                String recordClassRef = categories.getRecordClassRef();
                if (categoriesMap.containsKey(recordClassRef))
                    throw new WdkModelException("More than one categories have"
                            + " recordClassRef '" + recordClassRef + "'");
                categories.excludeResources(projectId);
                categoriesMap.put(recordClassRef, categories);
            }
        }
        categoriesList = null;
    }

    private void createInternalSets() {
        // create a param set to hold all internal params, that is, the params
        // created at run-time.
        ParamSet internalParamSet = new ParamSet();
        internalParamSet.setName(Utilities.INTERNAL_PARAM_SET);
        addParamSet(internalParamSet);

        // create a query set to hold all internal queries, that is, the queries
        // created at run-time.
        QuerySet internalQuerySet = new QuerySet();
        internalQuerySet.setName(Utilities.INTERNAL_QUERY_SET);
        addQuerySet(internalQuerySet);

        // create a query set to hold all internal questions, that is, the
        // questions created at run-time.
        QuestionSet internalQuestionSet = new QuestionSet();
        internalQuestionSet.setName(Utilities.INTERNAL_QUESTION_SET);
        addQuestionSet(internalQuestionSet);
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer("WdkModel: projectId='" + projectId
                + "'" + newline + "displayName='" + displayName + "'" + newline
                + "introduction='" + introduction + "'");
        buf.append(showSet("Param", paramSets));
        buf.append(showSet("Query", querySets));
        buf.append(showSet("RecordClass", recordClassSets));
        buf.append(showSet("XmlRecordClass", xmlRecordClassSets));
        buf.append(showSet("Question", questionSets));
        buf.append(showSet("XmlQuestion", xmlQuestionSets));
        return buf.toString();
    }

    protected String showSet(String setType, Map setMap) {
        StringBuffer buf = new StringBuffer();
        String newline = System.getProperty("line.separator");
        buf.append(newline);
        buf.append("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + newline);
        buf.append("ooooooooooooooooooooooooooooo " + setType
                + " Sets oooooooooooooooooooooooooo" + newline);
        buf.append("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + newline + newline);
        for (Object objSet : setMap.values()) {
            ModelSetI set = (ModelSetI) objSet;
            buf.append("=========================== " + set.getName()
                    + " ===============================" + newline + newline);
            buf.append(set).append(newline);
        }
        buf.append(newline);

        return buf.toString();
    }

    public void addQuestionSet(QuestionSet questionSet) {
        questionSetList.add(questionSet);
    }

    public void addRecordClassSet(RecordClassSet recordClassSet) {
        recordClassSetList.add(recordClassSet);
    }

    public void addQuerySet(QuerySet querySet) {
        querySetList.add(querySet);
    }

    public void addParamSet(ParamSet paramSet) {
        paramSetList.add(paramSet);
    }

    public void addGroupSet(GroupSet groupSet) {
        groupSetList.add(groupSet);
    }

    public void addXmlQuestionSet(XmlQuestionSet questionSet) {
        xmlQuestionSetList.add(questionSet);
    }

    public void addXmlRecordClassSet(XmlRecordClassSet recordClassSet) {
        xmlRecordClassSetList.add(recordClassSet);
    }

    // =========================================================================
    // Xml data source related methods
    // =========================================================================

    public XmlQuestionSet[] getXmlQuestionSets() {
        XmlQuestionSet[] qsets = new XmlQuestionSet[xmlQuestionSets.size()];
        xmlQuestionSets.values().toArray(qsets);
        return qsets;
    }

    public XmlQuestionSet getXmlQuestionSet(String setName)
            throws WdkModelException {
        XmlQuestionSet qset = (XmlQuestionSet) xmlQuestionSets.get(setName);
        if (qset == null)
            throw new WdkModelException("WDK Model " + projectId
                    + " does not contain an Xml Question set with name "
                    + setName);
        return qset;
    }

    public XmlRecordClassSet[] getXmlRecordClassSets() {
        XmlRecordClassSet[] rcsets = new XmlRecordClassSet[xmlRecordClassSets.size()];
        xmlRecordClassSets.values().toArray(rcsets);
        return rcsets;
    }

    public XmlRecordClassSet getXmlRecordClassSet(String setName)
            throws WdkModelException {
        XmlRecordClassSet rcset = (XmlRecordClassSet) xmlRecordClassSets.get(setName);
        if (rcset == null)
            throw new WdkModelException("WDK Model " + projectId
                    + " does not contain an Xml Record Class set with name "
                    + setName);
        return rcset;
    }

    public void setXmlSchema(URL xmlSchemaURL) {
        this.xmlSchemaURL = xmlSchemaURL;
    }

    public URL getXmlSchemaURL() {
        return xmlSchemaURL;
    }

    public void setXmlDataDir(File path) {
        this.xmlDataDir = path;
    }

    public File getXmlDataDir() {
        return xmlDataDir;
    }

    public DatasetFactory getDatasetFactory() {
        return datasetFactory;
    }

    public QueryFactory getQueryFactory() {
        return queryFactory;
    }

    public AnswerFactory getAnswerFactory() {
        return answerFactory;
    }

    public String getProjectId() {
        return projectId;
    }

    public Map<String, String> getBooleanOperators() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("and", "INTERSECT");
        map.put("AND", "INTERSECT");
        map.put("intersect", "INTERSECT");
        map.put("INTERSECT", "INTERSECT");
        map.put("&&", "INTERSECT");

        map.put("or", "UNION");
        map.put("OR", "UNION");
        map.put("union", "UNION");
        map.put("UNION", "UNION");
        map.put("||", "UNION");
        map.put("+", "UNION");

        String minus = platform.getMinusOperator();
        map.put("minus", minus);
        map.put("MINUS", minus);
        map.put("not", minus);
        map.put("NOT", minus);
        map.put("except", minus);
        map.put("EXCEPT", minus);
        map.put("-", minus);

        return map;
    }

    public String getQuestionDisplayName(String questionFullName) {
        try {
            Question question = (Question) resolveReference(questionFullName);
            return question.getDisplayName();
        } catch (WdkModelException ex) {
            // question doesn't exist, return null;
            return null;
        }
    }

    /**
     * This method is supposed to be called by the digester
     * 
     * @param propertyList
     */
    public void addDefaultPropertyList(PropertyList propertyList) {
        this.defaultPropertyLists.add(propertyList);
    }

    /**
     * if the property list of the given name doesn't exist, an empty string
     * array will be returned.
     * 
     * @param propertyListName
     * @return
     */
    public String[] getDefaultPropertyList(String propertyListName) {
        if (!defaultPropertyListMap.containsKey(propertyListName))
            return new String[0];
        return defaultPropertyListMap.get(propertyListName);
    }

    public Map<String, String[]> getDefaultPropertyLists() {
        Map<String, String[]> propLists = new LinkedHashMap<String, String[]>();
        for (String plName : defaultPropertyListMap.keySet()) {
            String[] values = defaultPropertyListMap.get(plName);
            String[] array = new String[values.length];
            System.arraycopy(values, 0, array, 0, array.length);
            propLists.put(plName, array);
        }
        return propLists;
    }

    public void addCategories(Categories categories) {
        this.categoriesList.add(categories);
    }

    public Categories[] getCategories() {
        Categories[] array = new Categories[categoriesMap.size()];
        categoriesMap.values().toArray(array);
        return array;
    }
}
