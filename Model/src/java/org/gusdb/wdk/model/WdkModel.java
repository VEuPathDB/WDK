package org.gusdb.wdk.model;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.w3c.dom.Document;

// why is this in impl?

/**
 * @author
 * @modified Jan 6, 2006 - Jerric Add a historyFactory in the model
 */
public class WdkModel {

    // used to
    public static final String PARAM_DIVIDER = "--WDK_PARAM_DIVIDER--";

    public static final int TRUNCATE_DEFAULT = 100;

    public static WdkModel INSTANCE = new WdkModel();

    protected RDBMSPlatformI platform;
    private RDBMSPlatformI authenPlatform;

    Map<String, QuerySet> querySets = new LinkedHashMap<String, QuerySet>();
    Map<String, ParamSet> paramSets = new LinkedHashMap<String, ParamSet>();
    Map<String, RecordClassSet> recordClassSets = new LinkedHashMap<String, RecordClassSet>();
    Map<String, ReferenceList> referenceLists = new LinkedHashMap<String, ReferenceList>();
    Map<String, QuestionSet> questionSets = new LinkedHashMap<String, QuestionSet>();
    Map<String, ModelSetI> allModelSets = new LinkedHashMap<String, ModelSetI>();
    Map<String, GroupSet> groupSets = new LinkedHashMap< String, GroupSet >();
    
    private String name;
    String displayName;
    private String version = "1.0"; // use default version
    String introduction;
    String historyDatasetLink;
    String historyDatasetColumnName;
    ResultFactory resultFactory;
    Map<String, String> properties;

    private EnumParam booleanOps;
    private Document document;
    String webServiceUrl;

    /**
     * xmlSchemaURL is used by the XmlQuestions. This is the only place where
     * XmlQuestion can find it.
     */
    private URL xmlSchemaURL;

    private Map<String, XmlQuestionSet> xmlQuestionSets;

    private Map<String, XmlRecordClassSet> xmlRecordClassSets;

    private File xmlDataDir;

    private DatasetFactory datasetFactory;

    /**
     * Default constructor
     */
    public WdkModel() {
        INSTANCE = this;
        xmlQuestionSets = new LinkedHashMap<String, XmlQuestionSet>();
        xmlRecordClassSets = new LinkedHashMap<String, XmlRecordClassSet>();
    }

    /**
     * Convenience method for constructing a model from the configuration
     * information
     */
    public static WdkModel construct(String modelName) throws WdkModelException {
        File configDir = new File(System.getProperties().getProperty(
                "configDir"));

        File modelConfigXmlFile = new File(configDir, modelName + "-config.xml");
        File modelXmlFile = new File(configDir, modelName + ".xml");
        File modelPropFile = new File(configDir, modelName + ".prop");
        File schemaFile = new File(System.getProperty("schemaFile"));
        File xmlSchemaFile = new File(System.getProperty("xmlSchemaFile"));
        File xmlDataDir = new File(System.getProperty("xmlDataDir"));

        try {
            WdkModel model = ModelXmlParser.parseXmlFile(
                    modelXmlFile.toURI().toURL(),
                    modelPropFile.toURI().toURL(), schemaFile.toURI().toURL(),
                    xmlSchemaFile.toURI().toURL(),
                    modelConfigXmlFile.toURI().toURL());
            model.setXmlDataDir(xmlDataDir);
            
            // set the exception header
            WdkModelException.modelName = model.getProjectId();
            WdkUserException.modelName = model.getProjectId();

            return model;
        } catch (java.net.MalformedURLException e) {
            throw new WdkModelException(e);
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

    public void setName(String name) {
        this.name = name;
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

    /**
     * @param version
     *            The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getIntroduction() {
        return introduction;
    }

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setHistoryDataset(String historyDatasetLink) {
        this.historyDatasetLink = historyDatasetLink;
    }

    public String getHistoryDatasetLink() {
        return historyDatasetLink;
    }

    public void setHistoryDatasetColumnName(String historyDatasetColumnName) {
        this.historyDatasetColumnName = historyDatasetColumnName;
    }

    public String getHistoryDatasetColumnName() {
        return historyDatasetColumnName;
    }

    // RecordClass Sets
    public void addRecordClassSet(RecordClassSet recordClassSet)
            throws WdkModelException {
        addSet(recordClassSet, recordClassSets);
    }

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
        Iterator keys = recordClassSets.keySet().iterator();
        int counter = 0;
        while (keys.hasNext()) {
            String name = (String) keys.next();
            RecordClassSet nextRecordClassSet = (RecordClassSet) recordClassSets.get(name);
            sets[counter] = nextRecordClassSet;
            counter++;
        }
        return sets;
    }

    // Query Sets
    public void addQuerySet(QuerySet querySet) throws WdkModelException {
        addSet(querySet, querySets);
    }

    public QuerySet getQuerySet(String setName) throws WdkUserException {
        if (!querySets.containsKey(setName)) {
            String err = "WDK Model " + name
                    + " does not contain a query set with name " + setName;
            throw new WdkUserException(err);
        }
        return (QuerySet) querySets.get(setName);
    }

    public boolean hasQuerySet(String setName) {
        return querySets.containsKey(setName);
    }

    public QuerySet[] getAllQuerySets() {

        QuerySet sets[] = new QuerySet[querySets.size()];
        Iterator keys = querySets.keySet().iterator();
        int counter = 0;
        while (keys.hasNext()) {
            String name = (String) keys.next();
            sets[counter] = (QuerySet) querySets.get(name);
            counter++;
        }
        return sets;
    }

    public QuestionSet[] getAllQuestionSets() {

        QuestionSet sets[] = new QuestionSet[questionSets.size()];
        Iterator keys = questionSets.keySet().iterator();
        int counter = 0;
        while (keys.hasNext()) {
            String name = (String) keys.next();
            sets[counter] = (QuestionSet) questionSets.get(name);
            counter++;
        }
        return sets;
    }

    // Question Sets
    public void addQuestionSet(QuestionSet questionSet)
            throws WdkModelException {
        addSet(questionSet, questionSets);
    }

    public QuestionSet getQuestionSet(String setName) throws WdkUserException {
        if (!questionSets.containsKey(setName)) {
            String err = "WDK Model " + name
                    + " does not contain a Question set with name " + setName;
            throw new WdkUserException(err);
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

    // ReferenceLists
    public void addReferenceList(ReferenceList referenceList)
            throws WdkModelException {
        addSet(referenceList, referenceLists);
    }

    public ReferenceList getReferenceList(String referenceListName)
            throws WdkUserException {

        if (!referenceLists.containsKey(referenceListName)) {
            String err = "WDK Model " + name
                    + " does not contain a  query set with name "
                    + referenceListName;
            throw new WdkUserException(err);
        }
        return (ReferenceList) referenceLists.get(referenceListName);
    }

    public ReferenceList[] getAllReferenceLists() {

        ReferenceList lists[] = new ReferenceList[referenceLists.size()];
        Iterator keys = referenceLists.keySet().iterator();
        int counter = 0;
        while (keys.hasNext()) {
            String name = (String) keys.next();
            ReferenceList nextReferenceList = (ReferenceList) referenceLists.get(name);
            lists[counter] = nextReferenceList;
            counter++;
        }
        return lists;
    }

    public Question makeBooleanQuestion(RecordClass rc) {

        Question q = new Question();
        q.setName(BooleanQuestionNode.BOOLEAN_QUESTION_NAME);
        q.setRecordClass(rc);
        BooleanQuery bq = makeBooleanQuery();
        q.setQuery(bq);
        return q;
    }

    public BooleanQuery makeBooleanQuery() {
        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.setResultFactory(resultFactory);
        booleanQuery.setRDBMSPlatform(platform);
        return booleanQuery;
    }

    public BooleanQueryInstance makeBooleanQueryInstance() {

        BooleanQuery booleanQuery = new BooleanQuery();
        BooleanQueryInstance bqi = new BooleanQueryInstance(booleanQuery);
        return bqi;
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

        makeBooleanOps();

        Iterator modelSets = allModelSets.values().iterator();
        while (modelSets.hasNext()) {
            ModelSetI modelSet = (ModelSetI) modelSets.next();
            modelSet.setResources(this);
        }
    }

    public void configure(URL modelConfigXmlFileURL) throws Exception {

        ModelConfig modelConfig = ModelConfigParser.parseXmlFile(modelConfigXmlFileURL);
        String fileName = modelConfigXmlFileURL.getFile();
        String connectionUrl = modelConfig.getConnectionUrl();
        String login = modelConfig.getLogin();
        String password = modelConfig.getPassword();
        String platformClass = modelConfig.getPlatformClass();
        Integer maxIdle = modelConfig.getMaxIdle();
        Integer minIdle = modelConfig.getMinIdle();
        Integer maxWait = modelConfig.getMaxWait();
        Integer maxActive = modelConfig.getMaxActive();
        Integer initialSize = modelConfig.getInitialSize();

        RDBMSPlatformI platform = (RDBMSPlatformI) Class.forName(platformClass).newInstance();

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
            authenPlatform = (RDBMSPlatformI) Class.forName(authenPlatformClass).newInstance();
            authenPlatform.init(authenConnection, authenLogin, authenPassword,
                    minIdle, maxIdle, maxWait, maxActive / 2, initialSize, fileName);
            UserFactory.initialize(this, projectId, authenPlatform,
                    loginSchema, defaultRole, smtpServer, registerEmail,
                    emailSubject, emailContent);
        } else {
            UserFactory.initialize(this, projectId, null, null, null, null,
                    null, null, null);
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
    }

    public void configure(File modelConfigXmlFile) throws Exception {
        configure(modelConfigXmlFile.toURI().toURL());
    }

    public RDBMSPlatformI getRDBMSPlatform() {
        return platform;
    }

    public UserFactory getUserFactory() throws WdkUserException {
        return UserFactory.getInstance();
    }

    public String getWebServiceUrl() {
        return webServiceUrl;
    }

    public Object resolveReference(String twoPartName, String refererName,
            String refererClassName, String refererAttributeName)
            throws WdkModelException {
        String s = "Invalid reference in " + refererClassName + " '"
                + refererName + "' at " + refererAttributeName + "=\""
                + twoPartName + "\".";

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
    public void resolveReferences() throws WdkModelException {
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
            groupSet.resolveReferences( this );
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

    // Param Sets
    public void addQuerySet(ParamSet paramSet) throws WdkModelException {
        addSet(paramSet, paramSets);
    }

    public void addParamSet(ParamSet paramSet) throws WdkModelException {
        addSet(paramSet, paramSets);
    }
    
    public void addGroupSet(GroupSet groupSet) throws WdkModelException {
        addSet( groupSet, groupSets );
    }

    // /////////////////////////////////////////////////////////////////
    // ///// Protected methods
    // /////////////////////////////////////////////////////////////////

    void checkName(String setName) throws WdkModelException {
    // TODO What's supposed to be here?
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public EnumParam getBooleanOps() {
        return this.booleanOps;
    }

    private void makeBooleanOps() {

        EnumItem union = new EnumItem();
        union.setTerm("Union With");
        union.setInternal("union");

        EnumItem intersect = new EnumItem();
        intersect.setTerm("Intersect With");
        intersect.setInternal("intersect");

        EnumItem subtract = new EnumItem();
        subtract.setTerm("Subtract");
        subtract.setInternal("minus");

        EnumParam booleanOpsEnum = new EnumParam();
        booleanOpsEnum.addItem(union);
        booleanOpsEnum.addItem(intersect);
        booleanOpsEnum.addItem(subtract);

        booleanOpsEnum.setMultiPick(new Boolean(false));

        this.booleanOps = booleanOpsEnum;
    }

    // =========================================================================
    // Xml data source related methods
    // =========================================================================

    public void addXmlQuestionSet(XmlQuestionSet questionSet)
            throws WdkModelException {
        addSet(questionSet, xmlQuestionSets);
    }

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

    public void addXmlRecordClassSet(XmlRecordClassSet recordClassSet)
            throws WdkModelException {
        addSet(recordClassSet, xmlRecordClassSets);
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

    public String getProjectId() {
        return this.name;
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
}
