package org.gusdb.wdk.model;

import static java.util.Objects.isNull;
import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.AutoCloseableList;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.events.Event;
import org.gusdb.fgputil.events.EventListener;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.events.ListenerExceptionEvent;
import org.gusdb.fgputil.functional.FunctionalInterfaces.SupplierWithException;
import org.gusdb.fgputil.runtime.InstanceManager;
import org.gusdb.fgputil.runtime.Manageable;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins;
import org.gusdb.wdk.model.answer.single.SingleRecordQuestion;
import org.gusdb.wdk.model.columntool.ColumnToolBundleMap;
import org.gusdb.wdk.model.columntool.DefaultColumnToolBundleRef;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigAppDB;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.config.ModelConfigUserDatasetStore;
import org.gusdb.wdk.model.config.QueryMonitor;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dbms.ConnectionContainer;
import org.gusdb.wdk.model.filter.FilterSet;
import org.gusdb.wdk.model.ontology.Ontology;
import org.gusdb.wdk.model.ontology.OntologyFactory;
import org.gusdb.wdk.model.ontology.OntologyFactoryImpl;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.AbstractDependentParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.question.BooleanQuestion;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.question.SearchCategory;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.FavoriteFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactoryImpl;
import org.gusdb.wdk.model.user.analysis.UnconfiguredStepAnalysisFactory;
import org.gusdb.wdk.model.user.dataset.UserDatasetFactory;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.xml.sax.SAXException;

/**
 * The top level WdkModel object provides a facade to access all the resources and functionalities provided by
 * WDK. Furthermore, it is also an in-memory representation of the whole WDK model.
 */
public class WdkModel implements ConnectionContainer, Manageable<WdkModel>, AutoCloseable {

  private static final Logger LOG = Logger.getLogger(WdkModel.class);

  public static final String WDK_VERSION = "2.9.0";
  public static final String USER_SCHEMA_VERSION = "5";

  public static final String DB_INSTANCE_APP = "APP";
  public static final String DB_INSTANCE_USER = "USER";

  public static final String INDENT = "  ";

  /**
   * Convenience method for constructing a model from the configuration information.
   *
   * @throws WdkModelException
   *           if unable to construct model
   */
  public static WdkModel construct(String projectId, String gusHome) throws WdkModelException {
    return InstanceManager.getInstance(WdkModel.class, gusHome, projectId);
  }

  private String _gusHome;
  private ModelConfig _modelConfig;
  private String _projectId;
  private long _startupTime;

  private DatabaseInstance appDb;
  private DatabaseInstance userDb;

  private Optional<UserDatasetStore> _userDatasetStore;

  private List<QuerySet> querySetList = new ArrayList<>();
  private Map<String, QuerySet> querySets = new LinkedHashMap<>();

  private List<ParamSet> paramSetList = new ArrayList<>();
  private Map<String, ParamSet> paramSets = new LinkedHashMap<>();

  private List<RecordClassSet> recordClassSetList = new ArrayList<>();
  private Map<String, RecordClassSet> recordClassSets = new LinkedHashMap<>();

  private List<QuestionSet> questionSetList = new ArrayList<>();
  private Map<String, QuestionSet> questionSets = new LinkedHashMap<>();

  private Map<String, ModelSetI<? extends WdkModelBase>> allModelSets = new LinkedHashMap<>();

  private List<GroupSet> groupSetList = new ArrayList<>();
  private Map<String, GroupSet> groupSets = new LinkedHashMap<>();

  private List<XmlQuestionSet> xmlQuestionSetList = new ArrayList<>();
  private Map<String, XmlQuestionSet> xmlQuestionSets = new LinkedHashMap<>();

  private List<XmlRecordClassSet> xmlRecordClassSetList = new ArrayList<>();
  private Map<String, XmlRecordClassSet> xmlRecordClassSets = new LinkedHashMap<>();

  private List<FilterSet> filterSetList = new ArrayList<>();
  private Map<String, FilterSet> filterSets = new LinkedHashMap<>();

  private Map<String, String> _recordClassUrlSegmentMap = new HashMap<>();

  private List<WdkModelName> wdkModelNames = new ArrayList<>();
  private AutoCloseableList<AutoCloseable> managedCloseables = new AutoCloseableList<>();

  private String displayName;
  private String version; // use default version
  private String releaseDate;

  private List<WdkModelText> introductions = new ArrayList<>();
  private String _introduction;

  private List<MacroDeclaration> macroList = new ArrayList<>();
  private Set<String> modelMacroSet = new LinkedHashSet<>();
  private Set<String> jspMacroSet = new LinkedHashSet<>();
  private Set<String> perlMacroSet = new LinkedHashSet<>();

  private Map<String, String> properties;

  private UIConfig uiConfig = new UIConfig();

  private ExampleStratsAuthor exampleStratsAuthor;

  private StepAnalysisPlugins stepAnalysisPlugins;

  /**
   * xmlSchemaURL is used by the XmlQuestions. This is the only place where XmlQuestion can find it.
   */
  private URL xmlSchemaURL;

  private File xmlDataDir;

  private DatasetFactory datasetFactory;
  private BasketFactory basketFactory;
  private FavoriteFactory favoriteFactory;
  private StepAnalysisFactory stepAnalysisFactory;
  private Optional<UserDatasetFactory> userDatasetFactory;

  private List<PropertyList> defaultPropertyLists = new ArrayList<>();
  private Map<String, String[]> defaultPropertyListMap = new LinkedHashMap<>();

  private List<SearchCategory> categoryList = new ArrayList<>();
  private Map<String, SearchCategory> categoryMap = new LinkedHashMap<>();
  private Map<String, SearchCategory> rootCategoryMap = new LinkedHashMap<>();

  private List<OntologyFactoryImpl> ontologyFactoryList = new ArrayList<>();
  private Map<String, OntologyFactory> ontologyFactoryMap = new LinkedHashMap<>();
  private String categoriesOntologyName;

  // 4/3/20 Cache ontologies on the server
  private Map<String, Ontology> _ontologyCache = new ConcurrentHashMap<>();

  private ColumnToolBundleMap columnToolBundleMap = new ColumnToolBundleMap();
  private String defaultColumnToolBundleRef;

  private TwoTuple<ValidatedToken,User> systemUser;

  private String buildNumber;

  // unfortunately this must be public to fit in Manageable framework
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
    Date now = new Date();
    LOG.info("Constructing WDK Model for " + projectId + " with GUS_HOME=" + gusHome);
    LOG.info("WDK Model constructor called by class: " + getCallingClass());
    LOG.info("Startup date " + now + " [" + now.getTime() + "]");

    startEvents();
    try {
      ModelXmlParser parser = new ModelXmlParser(gusHome);
      WdkModel wdkModel = parser.parseModel(projectId);
      wdkModel.setStartupTime(now.getTime());
      wdkModel.checkSchema();
      LOG.info("WDK Model construction complete.");
      return wdkModel;
    }
    catch (Exception ex) {
      LOG.error("Exception occurred while loading model.", ex);
      throw new WdkModelException(ex);
    }
  }

  /**
   * Starts events framework and listens for exceptions thrown by event handlers, logging them
   */
  private void startEvents() {
    Events.init();
    Events.subscribe(new EventListener() {
      @Override
      public void eventTriggered(Event event) throws Exception {
        ListenerExceptionEvent errorEvent = (ListenerExceptionEvent)event;
        LOG.error("Error while processing event: " +
            errorEvent.getEvent().getClass().getName(), errorEvent.getException());
      }
    }, ListenerExceptionEvent.class);
  }

  private static String getCallingClass() {
    final int stacktraceOffset = 6;
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if (stackTrace.length == 0) return "unknown";
    int callIndex = (stackTrace.length <= stacktraceOffset ?
        stackTrace.length - 1 : stacktraceOffset);
    return stackTrace[callIndex].getClassName();
  }

  private void checkSchema() throws WdkModelException {
    // verify the user schema
    _modelConfig.getUserDB().checkSchema(this);
  }

  public static ModelConfig getModelConfig(String projectId, String gusHome) throws WdkModelException {
    try {
      ModelXmlParser parser = new ModelXmlParser(gusHome);
      return parser.getModelConfig(projectId).build();
    }
    catch (IOException | SAXException e) {
      throw new WdkModelException("Unable to read model config for gusHome '" + gusHome + "', projectId '" +
          projectId + "'", e);
    }
  }

  /**
   * @param questionFullName question's full name (two-part name)
   * @return question with the passed name
   */
  public Optional<Question> getQuestionByFullName(String questionFullName) {
    try {
      Reference r = new Reference(questionFullName);
      return Optional.ofNullable(questionSets.get(r.getSetName()))
          .flatMap(set -> set.getQuestion(r.getElementName()));
    }
    catch (WdkModelException e) {
      return Optional.empty();
    }
  }

  public Optional<Question> getQuestionByName(String name) {
    for (QuestionSet questionSet : questionSets.values()) {
      for (Question question : questionSet.getQuestions()) {
        if (question.getName().equals(name)) {
          return Optional.of(question);
        }
      }
    }
    return Optional.empty();
  }

  public Question[] getQuestions(RecordClass recordClass) {
    String rcName = recordClass.getFullName();
    List<Question> questions = new ArrayList<>();
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

  public Optional<RecordClass> getRecordClassByFullName(String recordClassReference) {
    try {
      Reference r = new Reference(recordClassReference);
      return Optional.ofNullable(recordClassSets.get(r.getSetName()))
          .flatMap(set -> set.getRecordClass(r.getElementName()));
    }
    catch (WdkModelException e) {
      return Optional.empty();
    }
  }

  /**
   * Tries to find a configured recordclass by URL segment
   *
   * @param urlSegment of a record class
   * @return optional containing record class if found, or empty optional if not
   */
  public Optional<RecordClass> getRecordClassByUrlSegment(String urlSegment) {
    return Optional.ofNullable(_recordClassUrlSegmentMap.get(urlSegment)).map(name -> getRecordClassByFullName(name).get());
  }

  public Optional<RecordClass> getRecordClassByNameOrUrlSegment(String nameOrSegment) {
    Optional<RecordClass> rc = getRecordClassByUrlSegment(nameOrSegment);
    return rc.isPresent() ? rc : getRecordClassByFullName(nameOrSegment);
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

  private void setStartupTime(long startupTime) {
    _startupTime = startupTime;
  }

  public long getStartupTime() {
    return _startupTime;
  }

  public void addIntroduction(WdkModelText introduction) {
    introductions.add(introduction);
  }

  public String getIntroduction() {
    return _introduction;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public String getCategoriesOntologyName() {
    return categoriesOntologyName;
  }

  public void setDefaultColumnToolBundleRef(DefaultColumnToolBundleRef ref) {
    defaultColumnToolBundleRef = ref.getRef();
  }

  public String getDefaultColumnToolBundleRef() {
    return defaultColumnToolBundleRef;
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
        LOG.warn("The model macro '" + macro + "' is never used in" + " the model xml files.");
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

  public RecordClassSet[] getAllRecordClassSets() {
    RecordClassSet sets[] = new RecordClassSet[recordClassSets.size()];
    recordClassSets.values().toArray(sets);
    return sets;
  }

  // Query Sets

  public QuerySet getQuerySet(String setName) throws WdkModelException {
    if (!querySets.containsKey(setName)) {
      String err = "WDK Model " + _projectId + " does not contain a query set with name " + setName;
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
  public Optional<QuestionSet> getQuestionSet(String setName) {
    return Optional.ofNullable(questionSets.get(setName));
  }

  public boolean hasQuestionSet(String setName) {
    return questionSets.containsKey(setName);
  }

  public Map<String, QuestionSet> getQuestionSetsMap() {
    Map<String, QuestionSet> sets = new LinkedHashMap<>();
    for (String setName : questionSets.keySet()) {
      sets.put(setName, questionSets.get(setName));
    }
    return sets;
  }

  public ParamSet getParamSet(String setName) throws WdkModelException {
    if (!paramSets.containsKey(setName)) {
      String err = "WDK Model " + _projectId + " does not contain a param set with name " + setName;
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

  // ModelSetI's
  private <T extends ModelSetI<? extends WdkModelBase>> void addSet(T set, Map<String, T> setMap)
      throws WdkModelException {
    String setName = set.getName();
    if (allModelSets.containsKey(setName)) {
      String err = "WDK Model " + _projectId + " already contains a set with name " + setName;

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
   * modelConfig &lt;-- I am a horrible comment, explore later (resolveReferences is called in this method)
   */
  public void configure(ModelConfig modelConfig) throws WdkModelException {

    // assign projectId
    String projectId = modelConfig.getProjectId().trim();
    if (projectId.length() == 0 || projectId.indexOf('\'') >= 0)
      throw new WdkModelException("The projectId/modelName cannot be " +
          "empty, and cannot have single quote in it: " + projectId);

    _projectId = projectId;
    _modelConfig = modelConfig;

    ModelConfigAppDB appDbConfig = modelConfig.getAppDB();
    ModelConfigUserDB userDbConfig = modelConfig.getUserDB();
    ModelConfigUserDatasetStore udsConfig = modelConfig.getUserDatasetStoreConfig();

    QueryLogger.initialize(modelConfig.getQueryMonitor());

    appDb = new DatabaseInstance(appDbConfig, DB_INSTANCE_APP, true);
    userDb = new DatabaseInstance(userDbConfig, DB_INSTANCE_USER, true);

    // set true to avoid a broken dev irods at build time
    if ( udsConfig == null ) {
      _userDatasetStore = Optional.empty();
    }
    else {
      _userDatasetStore = Optional.of(udsConfig.getUserDatasetStore(modelConfig.getWdkTempDir()));
    }

    datasetFactory = new DatasetFactory(this);
    basketFactory = new BasketFactory(this);
    favoriteFactory = new FavoriteFactory(this);
    userDatasetFactory = _userDatasetStore.isPresent() ? Optional.of(new UserDatasetFactory(this)) : Optional.empty();

    // exclude resources that are not used by this project
    excludeResources();

    // internal sets will be created if author hasn't define them
    createInternalSets();

    // it has to be called after internal sets are created, but before
    // recordClass references are resolved.
    addBasketReferences();

    // resolve references in the model objects
    resolveReferences();

    // create generated questions (boolean and single-record)
    addGeneratedQuestions();

    validateDependentParams();

    // create step analysis factory - wait until the end since it spawns a thread
    //   that would have to be cleaned up if error occurs during reference resolving)
    stepAnalysisFactory = (stepAnalysisPlugins == null ?
        new UnconfiguredStepAnalysisFactory(this) :
        new StepAnalysisFactoryImpl(this));

    systemUser = new UserFactory(this).createUnregisteredUser();

    LOG.info("WDK Model configured.");
  }

  private void validateDependentParams() throws WdkModelException {

    // find names of all queries that are not owned by a parameter.  these are our "root" queries
    Set<String> nonRootQueryNames = new HashSet<>();
    for (ParamSet paramSet : paramSets.values()) {
      for (Param param : paramSet.getParams()) {
        if (param instanceof AbstractDependentParam) {
          nonRootQueryNames.addAll(((AbstractDependentParam)param).getContainedQueryFullNames());
        }
      }
    }

    // gather all root queries (those that are not contained by a param);
    // also gather all queries for dependent param resolution
    List<Query> allQueries = new ArrayList<>();
    Set<Query> rootQueries = new HashSet<>();
    Set<String> rootQueryNames = new HashSet<>();

    for (QuerySet querySet : querySets.values()) {
      for (Query query : querySet.getQueries()) {
        allQueries.add(query);
        if (!nonRootQueryNames.contains(query.getFullName())) {
          rootQueries.add(query);
          rootQueryNames.add(query.getFullName());
        }
      }
    }

    // for each root query, put the names of its immediate parameters into a "context"
    // then recurse down through its param-query tree, and validate that all queries use only params
    // found in the context
    for (Query rootQuery : rootQueries) {
      rootQuery.validateDependentParams();
    }

    // finally, resolve depended param refs in all queries' parameters
    for (Query query : allQueries) {
      for (Param param : query.getParams()) {
        param.resolveDependedParamRefs();
      }
    }
  }

  /**
   * Register a closeable with the WDKModel, ceding control of the closeable's lifecycle to this instance of the WDKModel.
   * The registered closeable will be closed when the WDKModel is closed.
   *
   * @param c Closeable for which control should be ceded to WDKModel.
   */
  public void registerClosable(AutoCloseable c) {
    managedCloseables.add(c);
  }

  @Override
  public void close() {
    LOG.info("Releasing WDK Model resources...");
    stepAnalysisFactory.shutDown();
    releaseDb(appDb);
    releaseDb(userDb);
    Events.shutDown();
    managedCloseables.close();
    LOG.info("WDK Model resources released.");
  }

  private static void releaseDb(DatabaseInstance db) {
    try {
      LOG.info("Releasing database resources for DB: " + db.getIdentifier());
      db.close();
    }
    catch (Exception e) {
      LOG.error("Exception caught while trying to shut down DB instance " + "with name '" + db.getIdentifier() +
          "'.  Ignoring.", e);
    }
  }

  private void addBasketReferences() throws WdkModelException {
    for (RecordClassSet rcSet : recordClassSets.values()) {
      for (RecordClass recordClass : rcSet.getRecordClasses()) {
        if (recordClass.isUseBasket()) {
          basketFactory.createRealtimeBasketQuestion(recordClass);
          basketFactory.createSnapshotBasketQuestion(recordClass);
        }
      }
    }
  }

  public ModelConfig getModelConfig() {
    return _modelConfig;
  }

  public DatabaseInstance getAppDb() {
    return appDb;
  }

  public DatabaseInstance getUserDb() {
    return userDb;
  }

  public UserFactory getUserFactory() {
    return new UserFactory(this);
  }

  public StepAnalysisFactory getStepAnalysisFactory() {
    return stepAnalysisFactory;
  }

  public Optional<UserDatasetFactory> getUserDatasetFactory() {
    return userDatasetFactory;
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

    LOG.info("Resolving references...");

    // NOTE: the order of set resolution matters; this sequence has been well
    // thought out and is important

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

    columnToolBundleMap.resolveReferences(this);
    if (!isNull(defaultColumnToolBundleRef)) {
      // make sure default (if present) refers to a registered tool bundle
      columnToolBundleMap.getToolBundle(defaultColumnToolBundleRef);
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

    // ensure question names are not duplicated
    Set<String> names = new HashSet<>();
    for (QuestionSet questionSet : questionSets.values()) {
      for (Question question : questionSet.getQuestions()) {
        if (!names.add(question.getName())) {
          throw new WdkModelException("Duplicate question name '" + question.getName() + "' found in model XML.");
        }
      }
    }

    LOG.info("Loading ontology...");
    Timer ontologyTime = new Timer();
    // resolve ontology references and determine WDK Categories ontology
    OntologyFactoryImpl ontologyFactory;
    switch (this.ontologyFactoryMap.size()) {
      case 0:
        break;
      case 1:
        ontologyFactory = (OntologyFactoryImpl)this.ontologyFactoryMap.values().iterator().next();
        ontologyFactory.resolveReferences(this);
        ontologyFactory.setUseAsWdkCategories(true);
        this.categoriesOntologyName = ontologyFactory.getName();
        break;
      default: // more than one ontology
        String wdkCategoriesOntologyName = null;
        for (OntologyFactory ontology: this.ontologyFactoryMap.values()) {
          // cast as (known) implementation
          ontologyFactory = (OntologyFactoryImpl)ontology;
          ontologyFactory.resolveReferences(this);
          // make sure only one ontology is set to be the WDK categories ontology
          if (ontologyFactory.getUseAsWdkCategories()) {
            if (wdkCategoriesOntologyName == null) {
              wdkCategoriesOntologyName = ontologyFactory.getName();
            }
            else {
              throw new WdkModelException("More than one ontology [" +
                  wdkCategoriesOntologyName + ", " + ontologyFactory.getName() +
                  "] is specified as the WDK Categories Ontology.  Only one can be used.");
            }
          }
        }
        this.categoriesOntologyName = wdkCategoriesOntologyName;
    }

    LOG.info("Total ontology load time: " + ontologyTime.getElapsedString());
  }

  private void excludeResources() throws WdkModelException {
    LOG.info("Excluding model resources...");
    // decide model name, display name, and version
    boolean hasModelName = false;
    for (WdkModelName wdkModelName : wdkModelNames) {
      if (wdkModelName.include(_projectId)) {
        if (hasModelName) {
          throw new WdkModelException("The model has more than one " + "<modelName> for project " + _projectId);
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
      if (intro.include(_projectId)) {
        if (hasIntroduction) {
          throw new WdkModelException("The model has more than one " + "<introduction> for project " +
              _projectId);
        }
        else {
          _introduction = intro.getText();
          hasIntroduction = true;
        }
      }
    }
    introductions = null;

    // exclude the property list
    for (PropertyList propList : defaultPropertyLists) {
      if (propList.include(_projectId)) {
        String listName = propList.getName();
        if (defaultPropertyListMap.containsKey(listName)) {
          throw new WdkModelException("The model has more than one " + "defaultPropertyList \"" + listName +
              "\" for project " + _projectId);
        }
        else {
          propList.excludeResources(_projectId);
          defaultPropertyListMap.put(listName, propList.getValues());
        }
      }
    }
    defaultPropertyLists = null;

    // remove question sets
    for (QuestionSet questionSet : questionSetList) {
      if (questionSet.include(_projectId)) {
        questionSet.excludeResources(_projectId);
        addSet(questionSet, questionSets);
      }
    }
    questionSetList = null;

    // remove param sets
    for (ParamSet paramSet : paramSetList) {
      if (paramSet.include(_projectId)) {
        paramSet.excludeResources(_projectId);
        addSet(paramSet, paramSets);
      }
    }
    paramSetList = null;

    // remove query sets
    for (QuerySet querySet : querySetList) {
      if (querySet.include(_projectId)) {
        querySet.excludeResources(_projectId, _modelConfig.getAppDB().getPlatformEnum());
        addSet(querySet, querySets);
      }
    }
    querySetList = null;

    // remove record class sets
    for (RecordClassSet recordClassSet : recordClassSetList) {
      if (recordClassSet.include(_projectId)) {
        recordClassSet.excludeResources(_projectId);
        addSet(recordClassSet, recordClassSets);
      }
    }
    recordClassSetList = null;

    // remove group sets
    for (GroupSet groupSet : groupSetList) {
      if (groupSet.include(_projectId)) {
        groupSet.excludeResources(_projectId);
        addSet(groupSet, groupSets);
      }
    }
    groupSetList = null;

    // remove xml question sets
    for (XmlQuestionSet xmlQSet : xmlQuestionSetList) {
      if (xmlQSet.include(_projectId)) {
        xmlQSet.excludeResources(_projectId);
        addSet(xmlQSet, xmlQuestionSets);
      }
    }
    xmlQuestionSetList = null;

    // remove xml record class sets
    for (XmlRecordClassSet xmlRSet : xmlRecordClassSetList) {
      if (xmlRSet.include(_projectId)) {
        xmlRSet.excludeResources(_projectId);
        addSet(xmlRSet, xmlRecordClassSets);
      }
    }
    xmlRecordClassSetList = null;

    // remove filter sets
    for (FilterSet filterSet : filterSetList) {
      if (filterSet.include(_projectId)) {
        filterSet.excludeResources(_projectId);
        addSet(filterSet, filterSets);
      }
    }
    filterSetList = null;

    // exclude categories
    for (SearchCategory category : this.categoryList) {
      if (category.include(_projectId)) {
        String name = category.getName();
        if (categoryMap.containsKey(name))
          throw new WdkModelException("The category name '" + name + "' is duplicated");
        category.excludeResources(_projectId);
        categoryMap.put(name, category);
      }
    }
    categoryList = null;

    // exclude ontologies
    for (OntologyFactoryImpl ontology : this.ontologyFactoryList) {
      if (ontology.include(_projectId)) {
        String name = ontology.getName();
        if (ontologyFactoryMap.containsKey(name))
          throw new WdkModelException("The ontology name '" + name + "' is duplicated");
        ontology.excludeResources(_projectId);
        ontologyFactoryMap.put(name, ontology);
      }
    }
    ontologyFactoryList = null;

    // exclude categories
    for (MacroDeclaration macro : macroList) {
      if (macro.include(_projectId)) {
        String name = macro.getName();
        macro.excludeResources(_projectId);
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
      stepAnalysisPlugins.excludeResources(_projectId);
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
      internalParamSet.excludeResources(_projectId);
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
      internalQuerySet.excludeResources(_projectId, _modelConfig.getAppDB().getPlatformEnum());
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
      internalQuestionSet.excludeResources(_projectId);
    }
  }

  private void addGeneratedQuestions() throws WdkModelException {
    for (RecordClassSet recordClassSet : getAllRecordClassSets()) {
      for (RecordClass recordClass : recordClassSet.getRecordClasses()) {
        addGeneratedQuestion(
          BooleanQuestion.getQuestionName(recordClass),
          () -> new BooleanQuestion(recordClass));
        addGeneratedQuestion(
          SingleRecordQuestion.getQuestionName(recordClass),
          () -> new SingleRecordQuestion(recordClass));
      }
    }
  }

  public void addGeneratedQuestion(String questionName, SupplierWithException<Question> questionSupplier) throws WdkModelException {
    
    // check if question already exists
    QuestionSet internalSet = getQuestionSet(Utilities.INTERNAL_QUESTION_SET).get();
    if (internalSet.getQuestion(questionName).isPresent()) {
      throw new WdkModelException("Generated questions should be created only " +
          "once and their names cannot be used in WDK Model XML. Found " +
          questionName + " in call to create it.");
    }

    // not yet added; add
    try {
      internalSet.addQuestion(questionSupplier.get());
    }
    catch (Exception e) {
      throw WdkModelException.translateFrom(e);
    }
  }

  @Override
  public String toString() {
    String userDatasetStoreStr = _userDatasetStore.isEmpty()
        ? "No user dataset store configured"
        : _userDatasetStore.get().toString();
    return new StringBuilder("WdkModel: ").append("projectId='").append(_projectId).append("'").append(NL).append(
        "displayName='").append(displayName).append("'").append(NL).append("introduction='").append(
        _introduction).append("'").append(NL).append(NL).append(userDatasetStoreStr).append(NL).append(uiConfig.toString()).append(
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

  public void setColumnToolLibrary(ColumnToolBundleMap columnToolLibrary) {
    this.columnToolBundleMap = columnToolLibrary;
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

  public Map<String, XmlQuestionSet> getXmlQuestionSetsMap() {
    return xmlQuestionSets;
  }

  public XmlQuestionSet getXmlQuestionSet(String setName) throws WdkModelException {
    XmlQuestionSet qset = xmlQuestionSets.get(setName);
    if (qset == null)
      throw new WdkModelException("WDK Model " + _projectId +
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
      throw new WdkModelException("WDK Model " + _projectId +
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

  public Optional<UserDatasetStore> getUserDatasetStore() {
    return _userDatasetStore;
  }

  public DatasetFactory getDatasetFactory() {
    return datasetFactory;
  }

  public String getProjectId() {
    return _projectId;
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
    Map<String, String[]> propLists = new LinkedHashMap<>();
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

  public Map<String, SearchCategory> getRootCategories() {

    Map<String, SearchCategory> roots = new LinkedHashMap<>();
    for (SearchCategory root : rootCategoryMap.values()) {
      String cusedBy = root.getUsedBy();
      if (root.isUsedBy(cusedBy))
        roots.put(root.getName(), root);
    }
    return roots;
  }

  public void addOntology(OntologyFactoryImpl ontologyFactory) {
    this.ontologyFactoryList.add(ontologyFactory);
  }

  public Set<String> getOntologyNames() {
    return Collections.unmodifiableSet(ontologyFactoryMap.keySet());
  }

  private OntologyFactory getOntologyFactory(String name) {
    return ontologyFactoryMap.get(name);
  }

  public Ontology getOntology(String name) throws WdkModelException {
    Ontology ontology = _ontologyCache.get(name);
    if (ontology == null) {
      OntologyFactory factory = getOntologyFactory(name);
      if (factory != null) {
        ontology = factory.getOntology(this);
        _ontologyCache.put(name, ontology);
      }
    }
    // may still be null if no factory exists for this name
    return ontology;
  }

  public void addMacroDeclaration(MacroDeclaration macro) {
    macroList.add(macro);
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

  public ValidatedToken getSystemUserToken() {
    return systemUser.getFirst();
  }

  public User getSystemUser() {
    return systemUser.getSecond();
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
    return _modelConfig.getQueryMonitor();
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
    return _gusHome;
  }

  public void setGusHome(String gusHome) {
    _gusHome = gusHome;
  }

  public void setUiConfig(UIConfig uiConfig) {
    this.uiConfig = uiConfig;
  }

  public UIConfig getUiConfig() {
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
    StringBuilder sb = new StringBuilder()
        .append("*******************************************\n")
        .append("Included Step Analysis Plugin Configuration:\n")
        .append(stepAnalysisPlugins == null ?
            "No step analysis plugins were configured in this WDK Model.\n" :
            stepAnalysisPlugins.toString())
        .append("*******************************************\n");
    if (stepAnalysisPlugins != null) {
      sb.append("Step Analysis Plugins per Question:\n");
      for (QuestionSet questionSet : getQuestionSetsMap().values()) {
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
    }
    LOG.info(sb.toString());
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

  public void registerRecordClassUrlSegment(String urlSegment, String rcFullName) throws WdkModelException {
    if (_recordClassUrlSegmentMap.containsKey(urlSegment) &&
        !_recordClassUrlSegmentMap.get(urlSegment).equals(rcFullName)) { // protects from duplicate identical calls
      throw new WdkModelException("Duplicate RecordClass URL segment specified [" + urlSegment + "]");
    }
    _recordClassUrlSegmentMap.put(urlSegment, rcFullName);
  }

  public static AutoCloseableList<WdkModel> loadMultipleModels(String gusHome, String[] projects) throws WdkModelException {
    AutoCloseableList<WdkModel> models = new AutoCloseableList<>();
    try {
      for (String projectId : projects) {
        models.add(WdkModel.construct(projectId, gusHome));
      }
      return models;
    }
    catch (Exception e) {
      models.close();
      throw e;
    }
  }

  /**
   * Identifies and returns an xml question by its full name.
   * @param xmlQuestionFullName
   * @return
   * @throws WdkModelException
   */
  public XmlQuestion getXmlQuestionByFullName(String xmlQuestionFullName) throws WdkModelException {
	Reference reference = new Reference(xmlQuestionFullName);
    XmlQuestionSet xmlQuestionSet = getXmlQuestionSet(reference.getSetName());
    if(xmlQuestionSet == null) throw new WdkModelException("Cannot find xml question set for " + xmlQuestionFullName);
    XmlQuestion xmlQuestion = xmlQuestionSet.getQuestion(reference.getElementName());
    if(xmlQuestion == null) throw new WdkModelException("Cannot find xml question with the name " + xmlQuestionFullName);
    return xmlQuestion;
  }

  public List<Question> getAllQuestions() {
    List<Question> questions = new ArrayList<>();
    for (QuestionSet qSet : getAllQuestionSets()) {
      questions.addAll(Arrays.asList(qSet.getQuestions()));
    }
    return questions;
  }

  public List<RecordClass> getAllRecordClasses() {
    return Arrays.stream(getAllRecordClassSets())
        .flatMap(rcs -> Arrays.stream(rcs.getRecordClasses()))
        .collect(Collectors.toList());
  }

  /**
   * Checks for a valid question name and throws WdkUserException if param is
   * not valid.  For now we simply check that it is a valid two-part name
   * (i.e. \S+\.\S+), so we will still get a WdkModelException down the line
   * if the question name is the correct format but does not actually exist.
   * We do this because sometimes developers change question names in one
   * place but not another and if so, then we want to know about it.  If we
   * mask this mistake with a WdkUserException, we might see bad consequences
   * down the line.
   *
   * @param qFullName potential question name
   * @throws WdkUserException if name is not in format *.*
   */
  public void validateQuestionFullName(String qFullName) throws WdkUserException {
    String message = "The search '" + qFullName + "' is not or is no longer available.";
    try {
      // First check to see if this is a 'regular' question; if not, check XML questions
      if (qFullName == null || getQuestionByFullName(qFullName) == null) {
        throw new WdkModelException("Question name is null or resulting question is null");
      }
    }
    catch (WdkModelException e) {
      try {
        // exception will be thrown below; will mean that name is neither 'regular' question nor XML
        Reference r = new Reference(qFullName);
        XmlQuestionSet xqs = getXmlQuestionSet(r.getSetName());
        xqs.getQuestion(r.getElementName());
      }
      catch (WdkModelException e2) {
        throw new WdkUserException(message, e2);
      }
    }
  }

  /**
   * Checks for a valid record class name and throws WdkUserException if param
   * is not valid.  For now we simply check that it is a valid two-part name
   * (i.e. \S+\.\S+), so we will still get a WdkModelException down the line
   * if the record class name is the correct format but does not actually
   * exist.  We do this because sometimes developers change record class names
   * in one place but not another and if so, then we want to know about it.
   * If we mask this mistake with a WdkUserException, we might see bad
   * consequences down the line.
   *
   * @param recordClassName potential record class name
   * @throws WdkUserException if name is not in format *.*
   */
  public void validateRecordClassName(String recordClassName) throws WdkUserException {
    String message = "The record type '" + recordClassName + "' is not or is no longer available.";
    try {
      // First check to see if this is a 'regular' record class; if not, check XML record classes
      if (recordClassName == null || getRecordClassByFullName(recordClassName) == null) {
        throw new WdkModelException("RecordClass name is null or resulting RecordClass is null");
      }
    }
    catch (WdkModelException e) {
      try {
        // exception will be thrown below; will mean that name is neither 'regular' RC nor XML
        Reference r = new Reference(recordClassName);
        XmlRecordClassSet xrcs = getXmlRecordClassSet(r.getSetName());
        xrcs.getRecordClass(r.getElementName());
      }
      catch (WdkModelException e2) {
        throw new WdkUserException(message, e2);
      }
    }
  }

  public ColumnToolBundleMap getColumnToolBundleMap() {
    return columnToolBundleMap;
  }

  // TODO: cache at model creation time
  public Map<String, List<Question>> getRecordClassQuestionMap() {
    Map<String, List<Question>> recordClassQuestionMap = new HashMap<>();
    for (Question q : getAllQuestions()) {
      String rcName = q.getRecordClass().getFullName();
      if (!recordClassQuestionMap.containsKey(rcName)) {
        recordClassQuestionMap.put(rcName, new ArrayList<>());
      }
      recordClassQuestionMap.get(rcName).add(q);
    }
    return recordClassQuestionMap;
  }
}
