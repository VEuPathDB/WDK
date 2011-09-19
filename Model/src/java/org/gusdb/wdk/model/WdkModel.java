package org.gusdb.wdk.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.user.AnswerFactory;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.FavoriteFactory;
import org.gusdb.wdk.model.user.QueryFactory;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.json.JSONException;
import org.xml.sax.SAXException;

// why is this in impl?

/**
 * @author
 * @modified Jan 6, 2006 - Jerric Add a stepFactory in the model
 */
public class WdkModel {

    public static final String WDK_VERSION = "2.2.0";

    private static final Logger logger = Logger.getLogger(WdkModel.class);

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
    public static WdkModel construct(String projectId, String gusHome)
            throws WdkModelException, NoSuchAlgorithmException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException, IOException, SAXException, SQLException,
            JSONException, WdkUserException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        int index = stackTrace.length - 1;
        String tip = "";
        if (index >= 0) tip = "called by " + stackTrace[index].getClassName();
        logger.debug("Constructing wdk model [" + projectId + "] (GUS_HOME="
                + gusHome + "); " + tip);

        ModelXmlParser parser = new ModelXmlParser(gusHome);
        WdkModel wdkModel = parser.parseModel(projectId);
        
        logger.debug("Model ready to use.");
        return wdkModel;
    }

    private ModelConfig modelConfig;
    private String projectId;

    private DBPlatform platform;
    private DBPlatform userPlatform;

    private List<QuerySet> querySetList = new ArrayList<QuerySet>();
    private Map<String, QuerySet> querySets = new LinkedHashMap<String, QuerySet>();

    private List<ParamSet> paramSetList = new ArrayList<ParamSet>();
    private Map<String, ParamSet> paramSets = new LinkedHashMap<String, ParamSet>();

    private List<RecordClassSet> recordClassSetList = new ArrayList<RecordClassSet>();
    private Map<String, RecordClassSet> recordClassSets = new LinkedHashMap<String, RecordClassSet>();

    private List<QuestionSet> questionSetList = new ArrayList<QuestionSet>();
    private Map<String, QuestionSet> questionSets = new LinkedHashMap<String, QuestionSet>();

    private Map<String, ModelSetI> allModelSets = new LinkedHashMap<String, ModelSetI>();

    private List<GroupSet> groupSetList = new ArrayList<GroupSet>();
    private Map<String, GroupSet> groupSets = new LinkedHashMap<String, GroupSet>();

    private List<XmlQuestionSet> xmlQuestionSetList = new ArrayList<XmlQuestionSet>();
    private Map<String, XmlQuestionSet> xmlQuestionSets = new LinkedHashMap<String, XmlQuestionSet>();

    private List<XmlRecordClassSet> xmlRecordClassSetList = new ArrayList<XmlRecordClassSet>();
    private Map<String, XmlRecordClassSet> xmlRecordClassSets = new LinkedHashMap<String, XmlRecordClassSet>();

    private List<WdkModelName> wdkModelNames = new ArrayList<WdkModelName>();
    private String displayName;
    private String version; // use default version
    private String releaseDate;

    private List<WdkModelText> introductions = new ArrayList<WdkModelText>();
    private String introduction;

    private List<MacroDeclaration> macroList = new ArrayList<MacroDeclaration>();
    private Set<String> modelMacroSet = new LinkedHashSet<String>();
    private Set<String> jspMacroSet = new LinkedHashSet<String>();
    private Set<String> perlMacroSet = new LinkedHashSet<String>();

    private ResultFactory resultFactory;

    private AnswerFactory answerFactory;

    private Map<String, String> properties;

    /**
     * xmlSchemaURL is used by the XmlQuestions. This is the only place where
     * XmlQuestion can find it.
     */
    private URL xmlSchemaURL;

    private File xmlDataDir;

    private UserFactory userFactory;
    private StepFactory stepFactory;
    private DatasetFactory datasetFactory;
    private QueryFactory queryFactory;
    private BasketFactory basketFactory;
    private FavoriteFactory favoriteFactory;

    private List<PropertyList> defaultPropertyLists = new ArrayList<PropertyList>();
    private Map<String, String[]> defaultPropertyListMap = new LinkedHashMap<String, String[]>();

    private List<SearchCategory> categoryList = new ArrayList<SearchCategory>();
    private Map<String, SearchCategory> categoryMap = new LinkedHashMap<String, SearchCategory>();
    private Map<String, SearchCategory> rootCategoryMap = new LinkedHashMap<String, SearchCategory>();

    private String secretKey;

    private User systemUser;


    /**
     * @param initRecordClassList
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public Question getQuestion(String questionFullName)
            throws WdkModelException {
        Reference r = new Reference(questionFullName);
        QuestionSet ss = getQuestionSet(r.getSetName());
        return ss.getQuestion(r.getElementName());
    }

    public Question[] getQuestions(RecordClass recordClass) {
        String rcName = recordClass.getFullName();
        List<Question> questions = new ArrayList<Question>();
        for (QuestionSet questionSet : questionSets.values()) {
            for (Question question : questionSet.getQuestions()) {
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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties,
            Set<String> replacedMacros) throws WdkModelException {
        // make sure all the declared model macros are present
        for (String macro : modelMacroSet) {
            // macro not provided, error
            if (!properties.containsKey(macro))
                throw new WdkModelException("Required model macro '" + macro
                        + "' is not defined in the model.prop file");
            // macro provided but not used, warning, but not error
            if (!replacedMacros.contains(macro))
                logger.warn("The model macro '" + macro + "' is never used in"
                        + " the model xml files.");
        }
        // make sure all the declared jsp macros are present
        for (String macro : jspMacroSet) {
            if (!properties.containsKey(macro))
                throw new WdkModelException("Required jsp macro '" + macro
                        + "' is not defined in the model.prop file");
        }
        // make sure all the declared perl macros are present
        for (String macro : perlMacroSet) {
            if (!properties.containsKey(macro))
                throw new WdkModelException("Required perl macro '" + macro
                        + "' is not defined in the model.prop file");
        }
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
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
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
            booleanQuestion.setDisplayName("Combine "
                    + recordClass.getDisplayName() + " results");
            booleanQuestion.setRecordClassRef(recordClass.getFullName());
            BooleanQuery booleanQuery = getBooleanQuery(recordClass);
            booleanQuestion.setQueryRef(booleanQuery.getFullName());
            booleanQuestion.excludeResources(projectId);
            booleanQuestion.resolveReferences(this);

            internalSet.addQuestion(booleanQuestion);
        }
        return booleanQuestion;
    }

    public BooleanQuery getBooleanQuery(RecordClass recordClass)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // check if the boolean query already exists
        String queryName = BooleanQuery.getQueryName(recordClass);
        QuerySet internalQuerySet = getQuerySet(Utilities.INTERNAL_QUERY_SET);

        BooleanQuery booleanQuery;
        if (internalQuerySet.contains(queryName)) {
            booleanQuery = (BooleanQuery) internalQuerySet.getQuery(queryName);
        } else {
            booleanQuery = new BooleanQuery(recordClass);

            // make sure we create index on primary keys
            String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
            booleanQuery.setIndexColumns(pkColumns);

            internalQuerySet.addQuery(booleanQuery);

            booleanQuery.excludeResources(projectId);
            booleanQuery.resolveReferences(this);
            booleanQuery.setDoNotTest(true);
            booleanQuery.setIsCacheable(true); // cache the boolean query
        }
        return booleanQuery;
    }

    // ModelSetI's
    private <T extends ModelSetI> void addSet(T set, Map<String, T> setMap)
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
        String projectId = modelConfig.getProjectId().trim();
        if (projectId.length() == 0 || projectId.indexOf('\'') >= 0)
            throw new WdkModelException("The projectId/modelName cannot be "
                    + "empty, and cannot have single quote in it: " + projectId);
        this.projectId = projectId;
        this.modelConfig = modelConfig;
        ModelConfigAppDB appDB = modelConfig.getAppDB();
        ModelConfigUserDB userDB = modelConfig.getUserDB();

        // initialize authentication factory
        // set the max active as half of the model's configuration

        platform = (DBPlatform) Class.forName(appDB.getPlatformClass()).newInstance();
        platform.initialize(this, "APP", appDB);
        userPlatform = (DBPlatform) Class.forName(userDB.getPlatformClass()).newInstance();
        userPlatform.initialize(this, "USER", userDB);

        resultFactory = new ResultFactory(this);
        userFactory = new UserFactory(this);
        stepFactory = new StepFactory(this);
        datasetFactory = new DatasetFactory(this);
        queryFactory = new QueryFactory(this);
        answerFactory = new AnswerFactory(this);
        basketFactory = new BasketFactory(this);
        favoriteFactory = new FavoriteFactory(this);

        // set the exception header
        WdkModelException.modelName = getProjectId();
        WdkUserException.modelName = getProjectId();

        // exclude resources that are not used by this project
        excludeResources();

        // internal sets will be created if author hasn't define them
        createInternalSets();

        // it has to be called after internal sets are created, but before
        // recordClass references are resolved.
        addBasketReferences();

        // resolve references in the model objects
        resolveReferences();

        // create boolean questions
        createBooleanQuestions();
    }

    private void addBasketReferences() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        for (RecordClassSet rcSet : recordClassSets.values()) {
            for (RecordClass recordClass : rcSet.getRecordClasses()) {
                if (recordClass.isUseBasket()) {
                    basketFactory.createAttributeQueryRef(recordClass);
                    basketFactory.createRealtimeBasketQuestion(recordClass);
                    basketFactory.createSnapshotBasketQuestion(recordClass);
                    basketFactory.createBasketAttributeQuery(recordClass);
                }
            }
        }
    }

    public ModelConfig getModelConfig() {
        return modelConfig;
    }

    public DBPlatform getQueryPlatform() {
        return platform;
    }

    // Function Added by Cary P. Feb 7, 2008
    public DBPlatform getUserPlatform() {
        return userPlatform;
    }

    public UserFactory getUserFactory() {
        return userFactory;
    }

    public StepFactory getStepFactory() {
        return stepFactory;
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
        for (GroupSet groupSet : groupSets.values()) {
            groupSet.resolveReferences(this);
        }
        for (QuerySet querySet : querySets.values()) {
            querySet.resolveReferences(this);
        }
        for (ParamSet paramSet : paramSets.values()) {
            paramSet.resolveReferences(this);
        }
        for (RecordClassSet recordClassSet : recordClassSets.values()) {
            recordClassSet.resolveReferences(this);
        }
        for (QuestionSet questionSet : questionSets.values()) {
            questionSet.resolveReferences(this);
        }
        // resolve references for xml record classes and questions
        for (XmlRecordClassSet rcSet : xmlRecordClassSets.values()) {
            rcSet.resolveReferences(this);
        }
        for (XmlQuestionSet qSet : xmlQuestionSets.values()) {
            qSet.resolveReferences(this);
        }
        for (SearchCategory category : this.categoryMap.values()) {
            category.resolveReferences(this);
            if (category.getParent() == null)
                rootCategoryMap.put(category.getName(), category);
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
                    this.releaseDate = wdkModelName.getReleaseDate();
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
        for (SearchCategory category : this.categoryList) {
            if (category.include(projectId)) {
                String name = category.getName();
                if (categoryMap.containsKey(name))
                    throw new WdkModelException("The category name '" + name
                            + "' is duplicated");
                category.excludeResources(projectId);
                categoryMap.put(name, category);
            }
        }
        categoryList = null;

        // exclude categories
        for (MacroDeclaration macro : macroList) {
            if (macro.include(projectId)) {
                String name = macro.getName();
                macro.excludeResources(projectId);
                if (macro.isUsedByModel()) {
                    if (modelMacroSet.contains(name))
                        throw new WdkModelException("More than one model "
                                + "macros '" + name + "' are defined");
                    modelMacroSet.add(name);
                }
                if (macro.isUsedByJsp()) {
                    if (jspMacroSet.contains(name))
                        throw new WdkModelException("More than one jsp "
                                + "macros '" + name + "' are defined");
                    jspMacroSet.add(name);
                }
                if (macro.isUsedByPerl()) {
                    if (perlMacroSet.contains(name))
                        throw new WdkModelException("More than one perl "
                                + "macros '" + name + "' are defined");
                    perlMacroSet.add(name);
                }
            }
        }
        macroList = null;
    }

    /**
     * this method has be to called after the excluding, but before resolving.
     * 
     * @throws WdkModelException
     */
    private void createInternalSets() throws WdkModelException {
        // create a param set to hold all internal params, that is, the params
        // created at run-time.
        boolean hasSet = false;
        for (ParamSet paramSet : paramSets.values()) {
            if (paramSet.getName().equals(Utilities.INTERNAL_PARAM_SET)) {
                hasSet = true;
                break;
            }
        }
        if (!hasSet) {
            ParamSet internalParamSet = new ParamSet();
            internalParamSet.setName(Utilities.INTERNAL_PARAM_SET);
            addSet(internalParamSet, paramSets);
            internalParamSet.excludeResources(projectId);
        }

        // create a query set to hold all internal queries, that is, the queries
        // created at run-time.
        hasSet = false;
        for (QuerySet querySet : querySets.values()) {
            if (querySet.getName().equals(Utilities.INTERNAL_QUERY_SET)) {
                hasSet = true;
                break;
            }
        }
        if (!hasSet) {
            QuerySet internalQuerySet = new QuerySet();
            internalQuerySet.setName(Utilities.INTERNAL_QUERY_SET);
            internalQuerySet.setDoNotTest(true);
            addQuerySet(internalQuerySet);
            internalQuerySet.excludeResources(projectId);
        }

        // create a query set to hold all internal questions, that is, the
        // questions created at run-time.
        hasSet = false;
        for (QuestionSet questionSet : questionSets.values()) {
            if (questionSet.getName().equals(Utilities.INTERNAL_QUESTION_SET)) {
                hasSet = true;
                break;
            }
        }
        if (!hasSet) {
            QuestionSet internalQuestionSet = new QuestionSet();
            internalQuestionSet.setInternal(true);
            internalQuestionSet.setName(Utilities.INTERNAL_QUESTION_SET);
            internalQuestionSet.setDoNotTest(true);
            addQuestionSet(internalQuestionSet);
            internalQuestionSet.excludeResources(projectId);
        }
    }

    private void createBooleanQuestions() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        for (RecordClassSet recordClassSet : getAllRecordClassSets()) {
            for (RecordClass recordClass : recordClassSet.getRecordClasses()) {
                getBooleanQuestion(recordClass);
            }
        }
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

    protected String showSet(String setType,
            Map<String, ? extends ModelSetI> setMap) {
        StringBuffer buf = new StringBuffer();
        String newline = System.getProperty("line.separator");
        buf.append(newline);
        buf.append("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + newline);
        buf.append("ooooooooooooooooooooooooooooo " + setType
                + " Sets oooooooooooooooooooooooooo" + newline);
        buf.append("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + newline + newline);
        for (ModelSetI set : setMap.values()) {
            buf.append("=========================== " + set.getName()
                    + " ===============================" + newline + newline);
            buf.append(set).append(newline);
        }
        buf.append(newline);

        return buf.toString();
    }

    public void addQuestionSet(QuestionSet questionSet)
            throws WdkModelException {
        if (questionSetList != null) questionSetList.add(questionSet);
        else addSet(questionSet, questionSets);
    }

    public void addRecordClassSet(RecordClassSet recordClassSet)
            throws WdkModelException {
        if (recordClassSetList != null) recordClassSetList.add(recordClassSet);
        else addSet(recordClassSet, recordClassSets);
    }

    public void addQuerySet(QuerySet querySet) throws WdkModelException {
        if (querySetList != null) querySetList.add(querySet);
        else addSet(querySet, querySets);
    }

    public void addParamSet(ParamSet paramSet) throws WdkModelException {
        if (paramSetList != null) paramSetList.add(paramSet);
        else addSet(paramSet, paramSets);
    }

    public void addGroupSet(GroupSet groupSet) throws WdkModelException {
        if (groupSetList != null) groupSetList.add(groupSet);
        else addSet(groupSet, groupSets);
    }

    public void addXmlQuestionSet(XmlQuestionSet questionSet)
            throws WdkModelException {
        if (xmlQuestionSetList != null) xmlQuestionSetList.add(questionSet);
        else addSet(questionSet, xmlQuestionSets);
    }

    public void addXmlRecordClassSet(XmlRecordClassSet recordClassSet)
            throws WdkModelException {
        if (xmlRecordClassSetList != null) xmlRecordClassSetList.add(recordClassSet);
        else addSet(recordClassSet, xmlRecordClassSets);
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

    public void addCategory(SearchCategory category) {
        this.categoryList.add(category);
    }

    public Map<String, SearchCategory> getCategories() {
        return new LinkedHashMap<String, SearchCategory>(categoryMap);
    }

    public Map<String, SearchCategory> getRooCategories(String usedBy) {
        Map<String, SearchCategory> roots = new LinkedHashMap<String, SearchCategory>();
        for (SearchCategory root : rootCategoryMap.values()) {
            String cusedBy = root.getUsedBy();
            if (cusedBy == null || usedBy == null
                    || cusedBy.equalsIgnoreCase(usedBy))
                roots.put(root.getName(), root);
        }
        return roots;
    }

    public void addMacroDeclaration(MacroDeclaration macro) {
        macroList.add(macro);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        logger.debug("Model unloaded.");
    }

    public String queryParamDisplayName(String paramName) {
        for (String paramSetName : paramSets.keySet()) {
            ParamSet paramSet = (ParamSet) paramSets.get(paramSetName);
            for (Param param : paramSet.getParams()) {
                if (param.getName().equals(paramName))
                    return param.getPrompt();
            }
        }
        return paramName;
    }

    public String getSecretKey() throws NoSuchAlgorithmException,
            WdkModelException, IOException {
        if (secretKey == null) {
            // load secret key file & read contents
            String secretKeyFileLoc = modelConfig.getSecretKeyFile();
            if (secretKeyFileLoc == null) return null;

            File file = new File(secretKeyFileLoc);
            if (!file.exists()) return null;

            InputStream fis = new FileInputStream(secretKeyFileLoc);
            StringBuffer contents = new StringBuffer();
            int chr;
            while ((chr = fis.read()) != -1) {
                contents.append((char) chr);
            }
            fis.close();
            this.secretKey = UserFactory.md5(contents.toString());
        }
        return secretKey;
    }

    public boolean getUseWeights() {
	return modelConfig.getUseWeights();
    }

    public User getSystemUser() throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException {
        if (systemUser == null) systemUser = userFactory.createGuestUser();
        return systemUser;
    }

    public BasketFactory getBasketFactory() {
        return basketFactory;
    }

    public FavoriteFactory getFavoriteFactory() {
        return favoriteFactory;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * @return the queryMonitor
     */
    public QueryMonitor getQueryMonitor() {
        return modelConfig.getQueryMonitor();
    }
}
