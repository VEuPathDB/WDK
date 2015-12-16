package org.gusdb.wdk.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.QueryLogger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.runtime.InstanceManager;
import org.gusdb.fgputil.runtime.Manageable;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigAppDB;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.config.QueryMonitor;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dbms.ConnectionContainer;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.filter.FilterSet;
import org.gusdb.wdk.model.ontology.Ontology;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.question.SearchCategory;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.FavoriteFactory;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactoryImpl;
import org.gusdb.wdk.model.user.analysis.UnconfiguredStepAnalysisFactory;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.xml.sax.SAXException;

/**
 * The top level WdkModel object provides a facade to access all the resources and functionalities provided by
 * WDK. Furthermore, it is also an in-memory representation of the whole WKK model.
 * 
 * 
 * @author
 * @modified Jan 6, 2006 - Jerric Add a stepFactory in the model
 */
public class WdkModel implements ConnectionContainer, Manageable<WdkModel> {

  public static final String WDK_VERSION = "2.9.0";

  public static final String USER_SCHEMA_VERSION = "5";

  public static final String DB_INSTANCE_APP = "APP";
  public static final String DB_INSTANCE_USER = "USER";

  public static final String INDENT = "  ";

  private static final Logger logger = Logger.getLogger(WdkModel.class);

  private static final String NL = System.getProperty("line.separator");

  /**
   * Convenience method for constructing a model from the configuration information.
   * 
   * @throws WdkModelException
   *           if unable to construct model
   */
  public static WdkModel construct(String projectId, String gusHome) throws WdkModelException {
    return InstanceManager.getInstance(WdkModel.class, gusHome, projectId);
  }

  private String gusHome;
  private ModelConfig modelConfig;
  private String projectId;

  private DatabaseInstance appDb;
  private DatabaseInstance userDb;

  private List<QuerySet> querySetList = new ArrayList<>();
  private Map<String, QuerySet> querySets = new LinkedHashMap<>();

  private List<ParamSet> paramSetList = new ArrayList<>();
  private Map<String, ParamSet> paramSets = new LinkedHashMap<>();

  private List<RecordClassSet> recordClassSetList = new ArrayList<>();
  private Map<String, RecordClassSet> recordClassSets = new LinkedHashMap<>();

  private List<QuestionSet> questionSetList = new ArrayList<>();
  private Map<String, QuestionSet> questionSets = new LinkedHashMap<>();

  private Map<String, ModelSetI<? extends WdkModelBase>> allModelSets = new LinkedHashMap<>();

  private List<GroupSet> groupSetList = new ArrayList<GroupSet>();
  private Map<String, GroupSet> groupSets = new LinkedHashMap<>();

  private List<XmlQuestionSet> xmlQuestionSetList = new ArrayList<>();
  private Map<String, XmlQuestionSet> xmlQuestionSets = new LinkedHashMap<>();

  private List<XmlRecordClassSet> xmlRecordClassSetList = new ArrayList<>();
  private Map<String, XmlRecordClassSet> xmlRecordClassSets = new LinkedHashMap<>();

  private List<FilterSet> filterSetList = new ArrayList<>();
  private Map<String, FilterSet> filterSets = new LinkedHashMap<>();

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

  private Map<String, String> properties;

  private UIConfig uiConfig = new UIConfig();

  private ExampleStratsAuthor exampleStratsAuthor;

  private StepAnalysisPlugins stepAnalysisPlugins;

  /**
   * xmlSchemaURL is used by the XmlQuestions. This is the only place where XmlQuestion can find it.
   */
  private URL xmlSchemaURL;

  private File xmlDataDir;

  private UserFactory userFactory;
  private StepFactory stepFactory;
  private DatasetFactory datasetFactory;
  private BasketFactory basketFactory;
  private FavoriteFactory favoriteFactory;
  private StepAnalysisFactory stepAnalysisFactory;

  private List<PropertyList> defaultPropertyLists = new ArrayList<PropertyList>();
  private Map<String, String[]> defaultPropertyListMap = new LinkedHashMap<String, String[]>();

  private List<SearchCategory> categoryList = new ArrayList<SearchCategory>();
  private Map<String, SearchCategory> categoryMap = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> rootCategoryMap = new LinkedHashMap<String, SearchCategory>();

  private List<Ontology> ontologyList = new ArrayList<Ontology>();
  private Map<String, Ontology> ontologyMap = new LinkedHashMap<String, Ontology>();

  
  private String secretKey;

  private ReentrantLock systemUserLock = new ReentrantLock();
  private User systemUser;

  private String buildNumber;

  private ThreadMonitor _myThreadMonitor;

  public WdkModel() {
    // add default sets
    try {
      addFilterSet(FilterSet.getWdkFilterSet());
    }
    catch (WdkModelException ex) {
      throw new WdkRuntimeException(ex);
    }
  }

  @Override
  public WdkModel getInstance(String projectId, String gusHome) throws WdkModelException {
    logger.info("Constructing WDK Model for " + projectId + " with GUS_HOME=" + gusHome);
    logger.info("WDK Model constructed by class: " + getCallingClass());

    Events.init();
    try {
      ModelXmlParser parser = new ModelXmlParser(gusHome);
      WdkModel wdkModel = parser.parseModel(projectId);
      wdkModel.doAdditionalStartup();
      logger.info("WDK Model construction complete.");
      return wdkModel;
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw new WdkModelException(ex);
    }
  }

  private static String getCallingClass() {
    final int stacktraceOffset = 6;
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if (stackTrace.length == 0) return "unknown";
    int callIndex = (stackTrace.length <= stacktraceOffset ?
        stackTrace.length - 1 : stacktraceOffset);
    return stackTrace[callIndex].getClassName();
  }

  private void doAdditionalStartup() throws WdkModelException {
    // verify the user schema
    modelConfig.getUserDB().checkSchema(this);

    // start up thread monitor and save reference
    _myThreadMonitor = ThreadMonitor.start(this);
  }

  public static ModelConfig getModelConfig(String projectId, String gusHome) throws WdkModelException {
    try {
      ModelXmlParser parser = new ModelXmlParser(gusHome);
      return parser.getModelConfig(projectId);
    }
    catch (IOException | SAXException e) {
      throw new WdkModelException("Unable to read model config for gusHome '" + gusHome + "', projectId '" +
          projectId + "'", e);
    }
  }

  /**
   * @param initRecordClassList
   * @return
   */
  public Question getQuestion(String questionFullName) throws WdkModelException {
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

  public RecordClass getRecordClass(String recordClassReference) throws WdkModelException {
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

  public void setProperties(Map<String, String> properties, Set<String> replacedMacros)
      throws WdkModelException {
    // make sure all the declared model macros are present
    for (String macro : modelMacroSet) {
      // macro not provided, error
      if (!properties.containsKey(macro))
        throw new WdkModelException("Required model macro '" + macro +
            "' is not defined in the model.prop file");
      // macro provided but not used, warning, but not error
      if (!replacedMacros.contains(macro))
        logger.warn("The model macro '" + macro + "' is never used in" + " the model xml files.");
    }
    // make sure all the declared jsp macros are present
    for (String macro : jspMacroSet) {
      if (!properties.containsKey(macro))
        throw new WdkModelException("Required jsp macro '" + macro +
            "' is not defined in the model.prop file");
    }
    // make sure all the declared perl macros are present
    for (String macro : perlMacroSet) {
      if (!properties.containsKey(macro))
        throw new WdkModelException("Required perl macro '" + macro +
            "' is not defined in the model.prop file");
    }
    this.properties = properties;
  }

  // RecordClass Sets

  public RecordClassSet getRecordClassSet(String recordClassSetName) throws WdkModelException {

    if (!recordClassSets.containsKey(recordClassSetName)) {
      String err = "WDK Model " + projectId + " does not contain a recordClass set with name " +
          recordClassSetName;

      throw new WdkModelException(err);
    }
    return recordClassSets.get(recordClassSetName);
  }

  public RecordClassSet[] getAllRecordClassSets() {
    RecordClassSet sets[] = new RecordClassSet[recordClassSets.size()];
    recordClassSets.values().toArray(sets);
    return sets;
  }

  // Query Sets

  public QuerySet getQuerySet(String setName) throws WdkModelException {
    if (!querySets.containsKey(setName)) {
      String err = "WDK Model " + projectId + " does not contain a query set with name " + setName;
      throw new WdkModelException(err);
    }
    return querySets.get(setName);
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
      String err = "WDK Model " + projectId + " does not contain a Question set with name " + setName;
      throw new WdkModelException(err);
    }
    return questionSets.get(setName);
  }

  public boolean hasQuestionSet(String setName) {
    return questionSets.containsKey(setName);
  }

  public Map<String, QuestionSet> getQuestionSets() {
    Map<String, QuestionSet> sets = new LinkedHashMap<String, QuestionSet>();
    for (String setName : questionSets.keySet()) {
      sets.put(setName, questionSets.get(setName));
    }
    return sets;
  }

  public ParamSet getParamSet(String setName) throws WdkModelException {
    if (!paramSets.containsKey(setName)) {
      String err = "WDK Model " + projectId + " does not contain a param set with name " + setName;
      throw new WdkModelException(err);
    }
    return paramSets.get(setName);
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
      throw new WdkModelException("The Model does not " + "have a groupSet named " + setName);
    return groupSet;
  }

  public FilterSet[] getAllFilterSets() {
    FilterSet[] array = new FilterSet[filterSets.size()];
    filterSets.values().toArray(array);
    return array;
  }

  public FilterSet getFilterSet(String setName) throws WdkModelException {
    FilterSet filterSet = filterSets.get(setName);
    if (filterSet == null)
      throw new WdkModelException("The Model does not " + "have a filterSet named " + setName);
    return filterSet;
  }

  public Question getBooleanQuestion(RecordClass recordClass) throws WdkModelException {
    // check if the boolean question already exists
    String qname = Question.BOOLEAN_QUESTION_PREFIX + recordClass.getFullName().replace('.', '_');
    QuestionSet internalSet = getQuestionSet(Utilities.INTERNAL_QUESTION_SET);

    Question booleanQuestion;
    if (internalSet.contains(qname)) {
      booleanQuestion = internalSet.getQuestion(qname);
    }
    else {
      booleanQuestion = new Question();
      booleanQuestion.setName(qname);
      booleanQuestion.setDisplayName("Combine " + recordClass.getDisplayName() + " results");
      booleanQuestion.setRecordClassRef(recordClass.getFullName());
      BooleanQuery booleanQuery = getBooleanQuery(recordClass);
      booleanQuestion.setQueryRef(booleanQuery.getFullName());
      booleanQuestion.excludeResources(projectId);
      booleanQuestion.resolveReferences(this);

      internalSet.addQuestion(booleanQuestion);
    }
    return booleanQuestion;
  }

  public BooleanQuery getBooleanQuery(RecordClass recordClass) throws WdkModelException {
    // check if the boolean query already exists
    String queryName = BooleanQuery.getQueryName(recordClass);
    QuerySet internalQuerySet = getQuerySet(Utilities.INTERNAL_QUERY_SET);

    BooleanQuery booleanQuery;
    if (internalQuerySet.contains(queryName)) {
      booleanQuery = (BooleanQuery) internalQuerySet.getQuery(queryName);
    }
    else {
      booleanQuery = recordClass.getBooleanQuery();

      // make sure we create index on primary keys
      booleanQuery.setIndexColumns(recordClass.getIndexColumns());

      internalQuerySet.addQuery(booleanQuery);

      booleanQuery.excludeResources(projectId);
      booleanQuery.resolveReferences(this);
      booleanQuery.setDoNotTest(true);
      booleanQuery.setIsCacheable(true); // cache the boolean query
    }
    return booleanQuery;
  }

  // ModelSetI's
  private <T extends ModelSetI<? extends WdkModelBase>> void addSet(T set, Map<String, T> setMap)
      throws WdkModelException {
    String setName = set.getName();
    if (allModelSets.containsKey(setName)) {
      String err = "WDK Model " + projectId + " already contains a set with name " + setName;

      throw new WdkModelException(err);
    }
    setMap.put(setName, set);
    allModelSets.put(setName, set);
  }

  /**
   * Set whatever resources the model needs. It will pass them to its kids
   */
  public void setResources() throws WdkModelException {
    for (ModelSetI<? extends WdkModelBase> modelSet : allModelSets.values()) {
      modelSet.setResources(this);
    }
  }

  /**
   * This method should happen after the resolveReferences, since projectId is set by this method from
   * modelConfig
   */
  public void configure(ModelConfig modelConfig) throws WdkModelException {

    // assign projectId
    String projectId = modelConfig.getProjectId().trim();
    if (projectId.length() == 0 || projectId.indexOf('\'') >= 0)
      throw new WdkModelException("The projectId/modelName cannot be " +
          "empty, and cannot have single quote in it: " + projectId);
    this.projectId = projectId;
    this.modelConfig = modelConfig;
    ModelConfigAppDB appDbConfig = modelConfig.getAppDB();
    ModelConfigUserDB userDbConfig = modelConfig.getUserDB();
    QueryLogger.initialize(modelConfig.getQueryMonitor());

    appDb = new DatabaseInstance(appDbConfig, DB_INSTANCE_APP, true);
    userDb = new DatabaseInstance(userDbConfig, DB_INSTANCE_USER, true);

    resultFactory = new ResultFactory(this);
    userFactory = new UserFactory(this);
    stepFactory = new StepFactory(this);
    datasetFactory = new DatasetFactory(this);
    basketFactory = new BasketFactory(this);
    favoriteFactory = new FavoriteFactory(this);
    stepAnalysisFactory = (stepAnalysisPlugins == null ? new UnconfiguredStepAnalysisFactory(this)
        : new StepAnalysisFactoryImpl(this));

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

    // further validation
    validateUrls();

  }

  public void releaseResources() {
    logger.info("Releasing WDK Model resources...");
    stepAnalysisFactory.shutDown();
    releaseDb(appDb);
    releaseDb(userDb);
    ThreadMonitor.shutDown(_myThreadMonitor);
    Events.shutDown();
    logger.info("WDK Model resources released.");
  }

  private static void releaseDb(DatabaseInstance db) {
    try {
      logger.info("Releasing database resources for DB: " + db.getIdentifier());
      db.close();
    }
    catch (Exception e) {
      logger.error("Exception caught while trying to shut down DB instance " + "with name '" + db.getIdentifier() +
          "'.  Ignoring.", e);
    }
  }

  private void addBasketReferences() throws WdkModelException {
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

  public DatabaseInstance getAppDb() {
    return appDb;
  }

  public DatabaseInstance getUserDb() {
    return userDb;
  }

  public UserFactory getUserFactory() {
    return userFactory;
  }

  public StepFactory getStepFactory() {
    return stepFactory;
  }

  public StepAnalysisFactory getStepAnalysisFactory() {
    return stepAnalysisFactory;
  }

  public Object resolveReference(String twoPartName) throws WdkModelException {
    String s = "Invalid reference '" + twoPartName + "'. ";

    // ensures <code>twoPartName</code> is formatted correctly
    Reference reference = new Reference(twoPartName);

    String setName = reference.getSetName();
    String elementName = reference.getElementName();

    ModelSetI<? extends WdkModelBase> set = allModelSets.get(setName);

    if (set == null) {
      String s3 = s + " There is no set called '" + setName + "'";
      throw new WdkModelException(s3);
    }
    Object element = set.getElement(elementName);
    if (element == null) {
      String s4 = s + " Set '" + setName + "' returned null for '" + elementName + "'";
      String s5 = s4 + "\n\nIf you are modifying or trying to access a strategy, your attempt failed because the strategy contains at least a step with an *obsolete search*. Please contact us with the name of the strategy or the link you tried to follow (should be available on your browser's location bar).";
      throw new WdkModelException(s5);
    }
    return element;
  }

  /**
   * Some elements within the set may refer to others by name. Resolve those references into real object
   * references.
   */
  private void resolveReferences() throws WdkModelException {
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
    for (FilterSet filterSet : filterSets.values()) {
      filterSet.resolveReferences(this);
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

    for (Ontology ontology: this.ontologyMap.values()) {
      ontology.resolveReferences(this);
    }
  }

  private void excludeResources() throws WdkModelException {
    // decide model name, display name, and version
    boolean hasModelName = false;
    for (WdkModelName wdkModelName : wdkModelNames) {
      if (wdkModelName.include(projectId)) {
        if (hasModelName) {
          throw new WdkModelException("The model has more than one " + "<modelName> for project " + projectId);
        }
        else {
          this.displayName = wdkModelName.getDisplayName();
          this.version = wdkModelName.getVersion();
          this.releaseDate = wdkModelName.getReleaseDate();
          this.buildNumber = wdkModelName.getBuildNumber();
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
          throw new WdkModelException("The model has more than one " + "<introduction> for project " +
              projectId);
        }
        else {
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
          throw new WdkModelException("The model has more than one " + "defaultPropertyList \"" + listName +
              "\" for project " + projectId);
        }
        else {
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

    // remove filter sets
    for (FilterSet filterSet : filterSetList) {
      if (filterSet.include(projectId)) {
        filterSet.excludeResources(projectId);
        addSet(filterSet, filterSets);
      }
    }
    filterSetList = null;

    // exclude categories
    for (SearchCategory category : this.categoryList) {
      if (category.include(projectId)) {
        String name = category.getName();
        if (categoryMap.containsKey(name))
          throw new WdkModelException("The category name '" + name + "' is duplicated");
        category.excludeResources(projectId);
        categoryMap.put(name, category);
      }
    }
    categoryList = null;

    // exclude ontologies
    for (Ontology ontology : this.ontologyList) {
      if (ontology.include(projectId)) {
        String name = ontology.getName();
        if (ontologyMap.containsKey(name))
          throw new WdkModelException("The ontology name '" + name + "' is duplicated");
        ontology.excludeResources(projectId);
        ontologyMap.put(name, ontology);
      }
    }
    ontologyList = null;
    
    // exclude categories
    for (MacroDeclaration macro : macroList) {
      if (macro.include(projectId)) {
        String name = macro.getName();
        macro.excludeResources(projectId);
        if (macro.isUsedByModel()) {
          if (modelMacroSet.contains(name))
            throw new WdkModelException("More than one model " + "macros '" + name + "' are defined");
          modelMacroSet.add(name);
        }
        if (macro.isUsedByJsp()) {
          if (jspMacroSet.contains(name))
            throw new WdkModelException("More than one jsp " + "macros '" + name + "' are defined");
          jspMacroSet.add(name);
        }
        if (macro.isUsedByPerl()) {
          if (perlMacroSet.contains(name))
            throw new WdkModelException("More than one perl " + "macros '" + name + "' are defined");
          perlMacroSet.add(name);
        }
      }
    }
    macroList = null;

    if (stepAnalysisPlugins != null) {
      stepAnalysisPlugins.excludeResources(projectId);
    }
  }

  /**
   * this method has be to called after the excluding, but before resolving.
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

  private void createBooleanQuestions() throws WdkModelException {
    for (RecordClassSet recordClassSet : getAllRecordClassSets()) {
      for (RecordClass recordClass : recordClassSet.getRecordClasses()) {
        getBooleanQuestion(recordClass);
      }
    }
  }

  @Override
  public String toString() {
    return new StringBuilder("WdkModel: ").append("projectId='").append(projectId).append("'").append(NL).append(
        "displayName='").append(displayName).append("'").append(NL).append("introduction='").append(
        introduction).append("'").append(NL).append(NL).append(uiConfig.toString()).append(
        showSet("Param", paramSets)).append(showSet("Query", querySets)).append(
        showSet("RecordClass", recordClassSets)).append(showSet("XmlRecordClass", xmlRecordClassSets)).append(
        showSet("Question", questionSets)).append(showSet("XmlQuestion", xmlQuestionSets)).toString();
  }

  protected String showSet(String setType, Map<String, ? extends ModelSetI<? extends WdkModelBase>> setMap) {
    StringBuilder buf = new StringBuilder(NL).append(
        "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo").append(NL).append(
        "ooooooooooooooooooooooooooooo ").append(setType).append(" Sets oooooooooooooooooooooooooo").append(
        NL).append("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo").append(NL).append(
        NL);
    for (ModelSetI<? extends WdkModelBase> set : setMap.values()) {
      buf.append("=========================== ").append(set.getName()).append(
          " ===============================").append(NL).append(NL).append(set).append(NL);
    }
    return buf.append(NL).toString();
  }

  public void addQuestionSet(QuestionSet questionSet) throws WdkModelException {
    if (questionSetList != null)
      questionSetList.add(questionSet);
    else
      addSet(questionSet, questionSets);
  }

  public void addRecordClassSet(RecordClassSet recordClassSet) throws WdkModelException {
    if (recordClassSetList != null)
      recordClassSetList.add(recordClassSet);
    else
      addSet(recordClassSet, recordClassSets);
  }

  public void addQuerySet(QuerySet querySet) throws WdkModelException {
    if (querySetList != null)
      querySetList.add(querySet);
    else
      addSet(querySet, querySets);
  }

  public void addParamSet(ParamSet paramSet) throws WdkModelException {
    if (paramSetList != null)
      paramSetList.add(paramSet);
    else
      addSet(paramSet, paramSets);
  }

  public void addGroupSet(GroupSet groupSet) throws WdkModelException {
    if (groupSetList != null)
      groupSetList.add(groupSet);
    else
      addSet(groupSet, groupSets);
  }

  public void addFilterSet(FilterSet filterSet) throws WdkModelException {
    if (filterSetList != null)
      filterSetList.add(filterSet);
    else
      addSet(filterSet, filterSets);
  }

  public void addXmlQuestionSet(XmlQuestionSet questionSet) throws WdkModelException {
    if (xmlQuestionSetList != null)
      xmlQuestionSetList.add(questionSet);
    else
      addSet(questionSet, xmlQuestionSets);
  }

  public void addXmlRecordClassSet(XmlRecordClassSet recordClassSet) throws WdkModelException {
    if (xmlRecordClassSetList != null)
      xmlRecordClassSetList.add(recordClassSet);
    else
      addSet(recordClassSet, xmlRecordClassSets);
  }

  // =========================================================================
  // Xml data source related methods
  // =========================================================================

  public XmlQuestionSet[] getXmlQuestionSets() {
    XmlQuestionSet[] qsets = new XmlQuestionSet[xmlQuestionSets.size()];
    xmlQuestionSets.values().toArray(qsets);
    return qsets;
  }

  public XmlQuestionSet getXmlQuestionSet(String setName) throws WdkModelException {
    XmlQuestionSet qset = xmlQuestionSets.get(setName);
    if (qset == null)
      throw new WdkModelException("WDK Model " + projectId +
          " does not contain an Xml Question set with name " + setName);
    return qset;
  }

  public XmlRecordClassSet[] getXmlRecordClassSets() {
    XmlRecordClassSet[] rcsets = new XmlRecordClassSet[xmlRecordClassSets.size()];
    xmlRecordClassSets.values().toArray(rcsets);
    return rcsets;
  }

  public XmlRecordClassSet getXmlRecordClassSet(String setName) throws WdkModelException {
    XmlRecordClassSet rcset = xmlRecordClassSets.get(setName);
    if (rcset == null)
      throw new WdkModelException("WDK Model " + projectId +
          " does not contain an Xml Record Class set with name " + setName);
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

  public String getProjectId() {
    return projectId;
  }

  public String getQuestionDisplayName(String questionFullName) {
    try {
      Question question = (Question) resolveReference(questionFullName);
      return question.getDisplayName();
    }
    catch (WdkModelException ex) {
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
   * if the property list of the given name doesn't exist, an empty string array will be returned.
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
      propLists.put(plName, Arrays.copyOf(values, values.length));
    }
    return propLists;
  }

  public void addCategory(SearchCategory category) {
    this.categoryList.add(category);
  }

  public Map<String, SearchCategory> getCategories() {
    return getCategories(null);
  }

  public Map<String, SearchCategory> getCategories(String usedBy) {
    return getCategories(usedBy, false);
  }

  public Map<String, SearchCategory> getCategories(String usedBy, boolean strict) {
    Map<String, SearchCategory> categories = new LinkedHashMap<>();
    for (String name : categoryMap.keySet()) {
      SearchCategory category = categoryMap.get(name);
      if (category.isUsedBy(usedBy, strict))
        categories.put(name, category);
    }
    return categories;
  }

  public Map<String, SearchCategory> getRootCategories(String usedBy) {
    Map<String, SearchCategory> roots = new LinkedHashMap<String, SearchCategory>();
    for (SearchCategory root : rootCategoryMap.values()) {
      String cusedBy = root.getUsedBy();
      if (root.isUsedBy(cusedBy))
        roots.put(root.getName(), root);
    }
    return roots;
  }
  
  public void addOntology(Ontology ontology) {
    this.ontologyList.add(ontology);
  }

  public Ontology getOntology(String name) {
    return ontologyMap.get(name);
  }

  public Collection<Ontology> getOntologies() {
    return Collections.unmodifiableCollection(ontologyMap.values());
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
      ParamSet paramSet = paramSets.get(paramSetName);
      for (Param param : paramSet.getParams()) {
        if (param.getName().equals(paramName))
          return param.getPrompt();
      }
    }
    return paramName;
  }

  public String getSecretKey() throws WdkModelException {
    try {
      if (secretKey == null) {
        // load secret key file & read contents
        String secretKeyFileLoc = modelConfig.getSecretKeyFile();
        if (secretKeyFileLoc == null)
          return null;

        File file = new File(secretKeyFileLoc);
        if (!file.exists())
          return null;

        InputStream fis = new FileInputStream(secretKeyFileLoc);
        StringBuilder contents = new StringBuilder();
        int chr;
        while ((chr = fis.read()) != -1) {
          contents.append((char) chr);
        }
        fis.close();
        this.secretKey = UserFactory.md5(contents.toString());
      }
      return secretKey;
    }
    catch (IOException e) {
      throw new WdkModelException("Unable to retrieve secret key from file.", e);
    }
  }

  public boolean getUseWeights() {
    return modelConfig.getUseWeights();
  }

  public User getSystemUser() throws WdkModelException {
    if (systemUser == null) {
      try {
        // ideally would synchronize on systemUser but cannot sync on null so use lock
        systemUserLock.lock();
        if (systemUser == null) {
          systemUser = userFactory.createSystemUser();
        }
      }
      finally {
        systemUserLock.unlock();
      }
    }
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

  /**
   * @return the buildNumber
   */
  public String getBuildNumber() {
    return buildNumber;
  }

  /**
   * @param buildNumber
   *          the buildNumber to set
   */
  public void setBuildNumber(String buildNumber) {
    this.buildNumber = buildNumber;
  }

  public String getGusHome() {
    return gusHome;
  }

  public void setGusHome(String gusHome) {
    this.gusHome = gusHome;
  }

  public void setUIConfig(UIConfig uiConfig) {
    this.uiConfig = uiConfig;
  }

  public UIConfig getUIConfig() {
    return uiConfig;
  }

  public StepAnalysisPlugins getStepAnalysisPlugins() {
    return stepAnalysisPlugins;
  }

  public void setStepAnalysisPlugins(StepAnalysisPlugins stepAnalysisPlugins) {
    this.stepAnalysisPlugins = stepAnalysisPlugins;
  }

  public ExampleStratsAuthor getExampleStratsAuthor() {
    return exampleStratsAuthor;
  }

  public void setExampleStratsAuthor(ExampleStratsAuthor exampleStratsAuthor) {
    this.exampleStratsAuthor = exampleStratsAuthor;
  }

  @Override
  public Connection getConnection(String key) throws WdkModelException, SQLException {
    switch (key) {
      case DB_INSTANCE_APP:
        return appDb.getDataSource().getConnection();
      case DB_INSTANCE_USER:
        return userDb.getDataSource().getConnection();
      default: // unknown
        throw new WdkModelException("Invalid DB Connection key.");
    }
  }

  public void logStepAnalysisPlugins() {
    StringBuilder sb = new StringBuilder().append("*******************************************\n").append(
        "Included Step Analysis Plugin Configuration:\n").append(stepAnalysisPlugins.toString()).append(
        "*******************************************\n").append("Step Analysis Plugins per Question:\n");
    for (QuestionSet questionSet : getQuestionSets().values()) {
      for (Question question : questionSet.getQuestions()) {
        Map<String, StepAnalysis> sas = question.getStepAnalyses();
        if (!sas.isEmpty()) {
          sb.append("Plugins for Question:" + question.getFullName() + "\n");
          for (StepAnalysis sa : sas.values()) {
            sb.append(sa);
          }
        }
      }
    }
    logger.info(sb.toString());
  }

  public String getDependencyTree() throws WdkModelException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    writer.println("<wdkModel>");

    // print questions
    String[] setNames = questionSets.keySet().toArray(new String[0]);
    Arrays.sort(setNames);
    for (String setName : setNames) {
      writer.println(INDENT + "<questionSet name=\"" + setName + "\">");
      Map<String, Question> questions = questionSets.get(setName).getQuestionMap();
      String[] questionNames = questions.keySet().toArray(new String[0]);
      for (String questionName : questionNames) {
        questions.get(questionName).printDependency(writer, INDENT + INDENT);
      }
      writer.println(INDENT + "</questionSet>");
    }

    // print record classes
    setNames = recordClassSets.keySet().toArray(new String[0]);
    Arrays.sort(setNames);
    for (String setName : setNames) {
      writer.println(INDENT + "<recordClassSet name=\"" + setName + "\">");
      Map<String, RecordClass> recordClasses = recordClassSets.get(setName).getRecordClassMap();
      String[] rcNames = recordClasses.keySet().toArray(new String[0]);
      for (String rcName : rcNames) {
        recordClasses.get(rcName).printDependency(writer, INDENT + INDENT);
      }
      writer.println(INDENT + "</recordClassSet>");
    }

    writer.println("</wdkModel>");
    return stringWriter.toString();
  }
  
  private void validateUrls() throws WdkModelException {
    
    // validate unique urlPaths
    Set<String> rcUrlPaths = new HashSet<String>();

    // slugs should be unique across all recordClasses
    for (RecordClassSet set : recordClassSets.values()) {
      for (RecordClass rc : set.getRecordClasses()) {
        String urlPath = rc.getUrlPath();
        if (rcUrlPaths.contains(urlPath))
          throw new WdkModelException("Duplicate urlPath found in recordClass " +
              rc.getFullName() + ": [" + urlPath + "].");
        rcUrlPaths.add(urlPath);
      }
    }

    Map<RecordClass, Set<String>> questionUrlPathsByRecordClass = new HashMap<RecordClass, Set<String>>();
    
    // urlPaths should be unique per questionSet
    for (QuestionSet set : questionSets.values()) {
      for (Question q : set.getQuestions()) {
        if (!questionUrlPathsByRecordClass.containsKey(q.getRecordClass())) {
          questionUrlPathsByRecordClass.put(q.getRecordClass(), new HashSet<String>());
        }
        Set<String> qUrlPaths = questionUrlPathsByRecordClass.get(q.getRecordClass());
        String urlPath = q.getUrlPath();
        if (qUrlPaths.contains(urlPath)) {
          logger.info("urlPath validation for question " + q.getFullName() + " with recordClass " + q.getRecordClass());
          throw new WdkModelException("Duplicate question urlPath found in question " +
              q.getFullName() + " with the recordClass " + q.getRecordClass().getName() +
              ": [" + urlPath + "].");
        }
        qUrlPaths.add(urlPath);
      }
    }

  }
}
