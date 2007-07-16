package org.gusdb.wdk.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.ValidationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.QueryFactory;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.xml.sax.SAXException;

// why is this in impl?

/**
 * @author
 * @modified Jan 6, 2006 - Jerric Add a historyFactory in the model
 */
public class WdkModel {

    public static final String WDK_VERSION = "1.13";

    public static final int TRUNCATE_DEFAULT = 100;

    @Deprecated
    public static WdkModel INSTANCE = new WdkModel();

    private String projectId;

    private RDBMSPlatformI platform;
    private RDBMSPlatformI authenPlatform;

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
    private String name;
    private String displayName;
    private String version; // use default version

    private List<WdkModelText> introductions = new ArrayList<WdkModelText>();
    private String introduction;
    private ResultFactory resultFactory;

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

    /**
     * Default constructor
     */
    public WdkModel() {
        INSTANCE = this;
    }

    /**
     * Convenience method for constructing a model from the configuration
     * information
     */
    public static WdkModel construct(String modelName) throws WdkModelException {
        String gusHome = System.getProperty(ModelXmlParser.GUS_HOME);

        try {
            ModelXmlParser parser = new ModelXmlParser(gusHome);
            WdkModel model = parser.parseModel(modelName);

            return model;
        } catch (SAXException ex) {
            throw new WdkModelException(ex);
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        } catch (ValidationException ex) {
            throw new WdkModelException(ex);
        } catch (ParserConfigurationException ex) {
            throw new WdkModelException(ex);
        } catch (TransformerFactoryConfigurationError ex) {
            throw new WdkModelException(ex);
        } catch (TransformerException ex) {
            throw new WdkModelException(ex);
        }
    }

    /**
     * @param initRecordClassList
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public Question getQuestion(String initRecordClassList)
            throws WdkUserException, WdkModelException {
        Reference r = new Reference(initRecordClassList);
        QuestionSet ss = getQuestionSet(r.getSetName());
        return ss.getQuestion(r.getElementName());
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

    public String getName() {
        return name;
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
    public Map getProperties() {
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
            String err = "WDK Model " + name
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
            String err = "WDK Model " + name
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
            String err = "WDK Model " + name
                    + " does not contain a Question set with name " + setName;
            throw new WdkModelException(err);
        }
        return (QuestionSet) questionSets.get(setName);
    }

    public boolean hasQuestionSet(String setName) {
        return questionSets.containsKey(setName);
    }

    public Map<String, QuestionSet> getQuestionSets() {
        return new LinkedHashMap<String, QuestionSet>(questionSets);
    }

    public Map<String, Map<String, Question[]>> getQuestionsByCategory() {
        QuestionSet[] qSets = getAllQuestionSets();

        Map<String, Map<String, Vector<Question>>> qVecByCat = new LinkedHashMap<String, Map<String, Vector<Question>>>();
        for (QuestionSet qSet : qSets) {
            if (true == qSet.getInternal()) continue;
            Question[] questions = qSet.getQuestions();
            for (Question q : questions) {
                String recType = q.getRecordClass().getFullName();
                String cat = q.getCategory();
                if (null == cat) cat = "";

                if (null == qVecByCat.get(recType)) {
                    qVecByCat.put(recType,
                            new LinkedHashMap<String, Vector<Question>>());
                }

                if (null == qVecByCat.get(recType).get(cat)) {
                    qVecByCat.get(recType).put(cat, new Vector<Question>());
                }

                qVecByCat.get(recType).get(cat).add(q);
            }
        }

        Map<String, Map<String, Question[]>> qArrayByCat = new LinkedHashMap<String, Map<String, Question[]>>();
        Iterator recI = qVecByCat.keySet().iterator();
        while (recI.hasNext()) {
            String recType = (String) recI.next();
            Map<String, Vector<Question>> recMap = qVecByCat.get(recType);
            Iterator catI = recMap.keySet().iterator();
            while (catI.hasNext()) {
                String cat = (String) catI.next();
                Vector<Question> qVec = recMap.get(cat);
                Question[] qArray = new Question[qVec.size()];
                qVec.toArray(qArray);

                if (null == qArrayByCat.get(recType)) {
                    qArrayByCat.put(recType,
                            new LinkedHashMap<String, Question[]>());
                }

                qArrayByCat.get(recType).put(cat, qArray);
            }
        }

        return qArrayByCat;
    }

    public ParamSet getParamSet(String setName) throws WdkModelException {
        if (!paramSets.containsKey(setName)) {
            String err = "WDK Model " + name
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
        GroupSet groupSet = groupSets.get(setName);
        if (groupSet == null)
            throw new WdkModelException("The Model does not "
                    + "have a groupSet named " + setName);
        return groupSet;
    }

    public Question makeBooleanQuestion(RecordClass rc)
            throws WdkModelException {

        Question q = new Question();
        q.setName(BooleanQuestionNode.BOOLEAN_QUESTION_NAME);
        q.setRecordClass(rc);

        // can't call resolve references, since the underlying query is invalid
        // yet
        // q.resolveReferences( this );
        q.setResources(this);

        BooleanQuery bq = makeBooleanQuery();
        q.setQuery(bq);
        return q;
    }

    public BooleanQuery makeBooleanQuery() throws WdkModelException {
        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.resolveReferences(this);
        booleanQuery.setResources(this);
        return booleanQuery;
    }

    // ModelSetI's
    private void addSet(ModelSetI set, Map setMap) throws WdkModelException {
        String setName = set.getName();
        if (allModelSets.containsKey(setName)) {
            String err = "WDK Model " + name
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
        Iterator modelSets = allModelSets.values().iterator();
        while (modelSets.hasNext()) {
            ModelSetI modelSet = (ModelSetI) modelSets.next();
            modelSet.setResources(this);
        }
    }

    /**
     * This method should happen after the resolveReferences, since projectId is
     * set by this method from modelConfig
     * 
     * @param gusHome
     * @throws WdkModelException
     */
    public void configure(String gusHome, String modelName)
            throws WdkModelException {
        try {
            ModelConfigParser parser = new ModelConfigParser(gusHome);
            ModelConfig modelConfig = parser.parseConfig(modelName);

            // assign projectId
            this.projectId = modelConfig.getProjectId();

            String fileName = gusHome + "/config/" + modelName + "-config.xml";

            String connectionUrl = modelConfig.getConnectionUrl();
            String login = modelConfig.getLogin();
            String password = modelConfig.getPassword();
            String platformClass = modelConfig.getPlatformClass();
            Integer maxIdle = modelConfig.getMaxIdle();
            Integer minIdle = modelConfig.getMinIdle();
            Integer maxWait = modelConfig.getMaxWait();
            Integer maxActive = modelConfig.getMaxActive();
            Integer initialSize = modelConfig.getInitialSize();

            RDBMSPlatformI platform = (RDBMSPlatformI) Class.forName(
                    platformClass).newInstance();

            // also load the connection info for authentication database
            String authenPlatformClass = modelConfig.getAuthenticationPlatformClass();
            String authenLogin = modelConfig.getAuthenticationLogin();
            String authenPassword = modelConfig.getAuthenticationPassword();
            String authenConnection = modelConfig.getAuthenticationConnectionUrl();

            String loginSchema = modelConfig.getLoginSchema();

            String defaultRole = modelConfig.getDefaultRole();
            String smtpServer = modelConfig.getSmtpServer();
            String registerEmail = modelConfig.getRegisterEmail();
            String emailSubject = modelConfig.getEmailSubject();
            String emailContent = modelConfig.getEmailContent();

            boolean enableQueryLogger = modelConfig.isEnableQueryLogger();
            String queryLoggerFile = modelConfig.getQueryLoggerFile();

            String projectId = getProjectId();

            // initialize authentication factory
            // set the max active as half of the model's configuration
            if (authenPlatformClass != null && !"".equals(authenPlatformClass)) {
                authenPlatform = (RDBMSPlatformI) Class.forName(
                        authenPlatformClass).newInstance();
                authenPlatform.init(authenConnection, authenLogin,
                        authenPassword, minIdle, maxIdle, maxWait,
                        maxActive / 2, initialSize, fileName);
                userFactory = new UserFactory(this, projectId, authenPlatform,
                        loginSchema, defaultRole, smtpServer, registerEmail,
                        emailSubject, emailContent);
            } else {
                userFactory = new UserFactory(this, projectId, null, null,
                        null, null, null, null, null);
            }

            platform.init(connectionUrl, login, password, minIdle, maxIdle,
                    maxWait, maxActive, initialSize, fileName);
            ResultFactory resultFactory = new ResultFactory(platform, login,
                    enableQueryLogger, queryLoggerFile);
            this.platform = platform;
            this.webServiceUrl = modelConfig.getWebServiceUrl();
            this.resultFactory = resultFactory;

            // initialize dataset factory with the login preferences
            datasetFactory = new DatasetFactory(authenPlatform, loginSchema);

            // initialize QueryFactory in user schema too
            queryFactory = new QueryFactory(authenPlatform, loginSchema);

            // resolve references in the model objects
            resolveReferences();
        } catch (InstantiationException ex) {
            throw new WdkModelException(ex);
        } catch (IllegalAccessException ex) {
            throw new WdkModelException(ex);
        } catch (ClassNotFoundException ex) {
            throw new WdkModelException(ex);
        } catch (SAXException ex) {
            throw new WdkModelException(ex);
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        } catch (ValidationException ex) {
            throw new WdkModelException(ex);
        }
    }

    public RDBMSPlatformI getRDBMSPlatform() {
        return platform;
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
     */
    private void resolveReferences() throws WdkModelException {
        // set the exception header
        WdkModelException.modelName = getProjectId();
        WdkUserException.modelName = getProjectId();

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
        for (GroupSet groupSet : groupSets.values()) {
            groupSet.resolveReferences(this);
        }
        Iterator itQuerySets = querySets.values().iterator();
        while (itQuerySets.hasNext()) {
            QuerySet querySet = (QuerySet) itQuerySets.next();
            querySet.resolveReferences(this);
        }
        Iterator itParamSets = paramSets.values().iterator();
        while (itParamSets.hasNext()) {
            ParamSet paramSet = (ParamSet) itParamSets.next();
            paramSet.resolveReferences(this);
        }
        Iterator itRecordSets = recordClassSets.values().iterator();
        while (itRecordSets.hasNext()) {
            RecordClassSet recordClassSet = (RecordClassSet) itRecordSets.next();
            recordClassSet.resolveReferences(this);
        }
        Iterator itQuestionSets = questionSets.values().iterator();
        while (itQuestionSets.hasNext()) {
            QuestionSet questionSet = (QuestionSet) itQuestionSets.next();
            questionSet.resolveReferences(this);
        }
        // resolve references for xml record classes and questions
        for (XmlRecordClassSet rcSet : xmlRecordClassSets.values()) {
            rcSet.resolveReferences(this);
        }
        for (XmlQuestionSet qSet : xmlQuestionSets.values()) {
            qSet.resolveReferences(this);
        }
    }

    private void excludeResources() throws WdkModelException {
        // decide model name, display name, and version
        for (WdkModelName wdkModelName : wdkModelNames) {
            if (wdkModelName.include(projectId)) {
                this.name = wdkModelName.getName();
                this.displayName = wdkModelName.getDisplayName();
                this.version = wdkModelName.getVersion();
                break;
            }
        }
        wdkModelNames = null; // no more use of modelNames

        // decide the introduction
        for (WdkModelText intro : introductions) {
            if (intro.include(projectId)) {
                this.introduction = intro.getText();
                break;
            }
        }
        introductions = null;

        // exclude the property list
        for (PropertyList propList : defaultPropertyLists) {
            if (propList.include(projectId)) {
                propList.excludeResources(projectId);
                defaultPropertyListMap.put(propList.getName(),
                        propList.getValues());
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
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer("WdkModel: name='" + name + "'"
                + newline + "displayName='" + displayName + "'" + newline
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
        Iterator setIterator = setMap.values().iterator();
        while (setIterator.hasNext()) {
            ModelSetI set = (ModelSetI) setIterator.next();
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
        XmlQuestionSet qset = xmlQuestionSets.get(setName);
        if (qset == null)
            throw new WdkModelException("WDK Model " + name
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
        XmlRecordClassSet rcset = xmlRecordClassSets.get(setName);
        if (rcset == null)
            throw new WdkModelException("WDK Model " + name
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

    public RDBMSPlatformI getPlatform() {
        return platform;
    }

    public RDBMSPlatformI getAuthenticationPlatform() {
        return authenPlatform;
    }

    public DatasetFactory getDatasetFactory() {
        return datasetFactory;
    }

    public QueryFactory getQueryFactory() {
        return queryFactory;
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

        String minus = platform.getMinus();
        map.put("minus", minus);
        map.put("MINUS", minus);
        map.put("not", minus);
        map.put("NOT", minus);
        map.put("except", minus);
        map.put("EXCEPT", minus);
        map.put("-", minus);

        return map;
    }

    public String getParamDisplayName(String paramName) {
        for (ParamSet paramset : paramSets.values()) {
            Object object = paramset.getElement(paramName);
            if (object == null) continue;
            Param param = (Param) object;
            return param.getPrompt();
        }
        return null;
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
}
