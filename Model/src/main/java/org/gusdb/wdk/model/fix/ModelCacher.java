package org.gusdb.wdk.model.fix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;

/**
 * @author xingao
 * 
 *         The code load model info into local tables, and will be used to validate steps.
 */
@Deprecated
public class ModelCacher extends BaseCLI {

	// option to keep the apicomm local question/params cache that is used to check for invalid steps
	// (it is not the WDK cache, though it could make sense to add another flag for it)
  public static final String ARG_KEEP_CACHE = "keepCache";

  private static final Logger logger = Logger.getLogger(ModelCacher.class);

  public static void main(String[] args) {
    String cmdName = System.getProperty("cmdName");
    ModelCacher cacher = new ModelCacher(cmdName);
    try {
      cacher.invoke(args);
      logger.info("model cacher done.");
      System.exit(0);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * @param command
   * @param description
   */
  public ModelCacher(String command) {
    super((command != null) ? command : "wdkCacheModel", "store model information into database");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "A comma-separated"
        + " list of ProjectIds, which should match the directory name"
        + " under $GUS_HOME, where model-config.xml is stored.");
    addNonValueOption("create", false, "create the cache tables to store "
        + "model definition. It affects all projects.");
    addNonValueOption("drop", false, "drop the local cache table for storing "
        + "model definition. It affects all projects.");
    addNonValueOption("expand", false, "load the model definition into " + "the cache tables.");
    addNonValueOption("skipParams", false, "if expanding, just load question table, and skip param table");
    addSingleValueOption("schema", false, null, "optional. the name of the"
        + " schema where the tables will be created/dropped/used.");
    addNonValueOption(ARG_KEEP_CACHE, false, "option. if this flag is present, the question data from"
        + " previous run will be kept in the local cache tables, and the question in the model will be skipped.");
    addGroup(true, "create", "drop", "expand");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#execute()
   */
  @Override
  protected void execute() throws Exception {

    String gusHome = GusHome.getGusHome();
    String strProject = (String) getOptionValue(ARG_PROJECT_ID);
    String[] projects = strProject.split(",");

    boolean create = (Boolean) getOptionValue("create");
    boolean drop = (Boolean) getOptionValue("drop");
    boolean expand = (Boolean) getOptionValue("expand");
    boolean skipParams = (Boolean) getOptionValue("skipParams");
    String schema = (String) getOptionValue("schema");
    if (schema == null)
      schema = "";
    schema = schema.trim();
    if (schema.length() > 0 && !schema.endsWith("."))
      schema += ".";

    boolean keepCache = (Boolean) getOptionValue(ARG_KEEP_CACHE);

    int exitCode = 0;
    if (drop || create) {
      try (WdkModel wdkModel = WdkModel.construct(projects[0], gusHome)) {
        if (drop) {
          dropTables(wdkModel, schema);
          logger.info("dropped model cache tables");
        }
        if (create) {
          createTables(wdkModel, schema);
          logger.info("created model cache tables");
        }
      }
    }
    else if (expand) {
      for (String projectId : projects) {
        logger.info("Expanding model for project " + projectId);
        try (WdkModel wdkModel = WdkModel.construct(projectId, gusHome)) {
          exitCode = expand(wdkModel, schema, keepCache, skipParams);
        }
        logger.info("=========================== done ============================");
      }
    }
    else {
      logger.error("No valid operation specified");
      throw new WdkModelException("No valid operation specified");
    }
    // no fatal errors; exit with exitCode
    System.exit(exitCode);
  }

  public int expand(WdkModel wdkModel, String schema, boolean keepCache, boolean skipParams) throws SQLException, WdkModelException {
    // need to reset the cache first
    wdkModel.getResultFactory().getCacheFactory().resetCache(false, true);

    // recreate the cache for the queries
    CacheFactory cacheFactory = wdkModel.getResultFactory().getCacheFactory();
    cacheFactory.recreateCache(true, true);

    DatabaseInstance database = wdkModel.getUserDb();
    DataSource dataSource = database.getDataSource();
    
    if (schema.length() == 0)
      schema = database.getDefaultSchema();

    String projectId = wdkModel.getProjectId();
    if (!keepCache)
      deleteCache(dataSource, projectId, schema);

    Connection connection = dataSource.getConnection();
    connection.setAutoCommit(false);
    PreparedStatement psQuestion = null, psParam = null, psEnum = null, psSelect = null;
    Map<String, Exception> errorMap = new LinkedHashMap<>();
    int questionsWritten = 0;
    int questionsAlreadyPresent = 0;
    try {
      String sql = "INSERT INTO " + schema + "wdk_questions " + "(question_id, question_name, project_id, " +
          " question_checksum, query_checksum, record_class) " + "VALUES (?, ?, ?, ?, ?, ?)";
      psQuestion = connection.prepareStatement(sql);

      sql = "INSERT INTO " + schema + "wdk_params (param_id, " + " question_id, param_name, param_type) " +
          "VALUES (?, ?, ?, ?)";
      psParam = connection.prepareStatement(sql);

      sql = "INSERT INTO " + schema + "wdk_enum_params " + "(param_id, param_value) VALUES (?, ?)";
      psEnum = connection.prepareStatement(sql);

      // check if question exists
      sql = "SELECT count(*) FROM " + schema + "wdk_questions WHERE question_name = ? AND project_id = ?";
      psSelect = connection.prepareStatement(sql);

      int length = schema.length();
      String schemaWithoutDot = (length == 0) ? null : schema.substring(0, length - 1);

      for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
        for (Question question : questionSet.getQuestions()) {
          try {
            if (questionExists(question, psSelect)) {
              questionsAlreadyPresent++;
              continue;
            }
            saveQuestion(question, psQuestion, psParam, psEnum, schemaWithoutDot, skipParams);
            connection.commit();
            questionsWritten++;
          }
          catch (Exception e) {
            logger.error("Failed to write " + question.getFullName(), e);
            errorMap.put(question.getFullName(), e);
            connection.rollback();
          }
        }
      }
    }
    finally {
      connection.setAutoCommit(true);
      SqlUtils.closeStatement(psQuestion);
      SqlUtils.closeStatement(psParam);
      SqlUtils.closeStatement(psEnum);
      SqlUtils.closeStatement(psSelect);
    }
    // log stats
    logger.info(questionsAlreadyPresent + " questions already cached in DB.");
    logger.info(questionsWritten + " questions successfully written.");
    logger.info(errorMap.size() + " questions had errors. See below for summary, above for details.");
    for (String qName : errorMap.keySet()) {
      logger.error(qName + ": " + errorMap.get(qName).toString());
    }
    return (errorMap.isEmpty() ? 0 : 1);
  }

  public void dropTables(WdkModel wdkModel, String schema) throws SQLException {
    DatabaseInstance database = wdkModel.getUserDb();
    DataSource dataSource = database.getDataSource();
    DBPlatform platform = database.getPlatform();

    if (schema.length() == 0)
      schema = database.getDefaultSchema();

    String[] sequences = new String[] { "wdk_questions_pkseq", "wdk_params_pkseq" };
    for (String sequence : sequences) {
      try {
        SqlUtils.executeUpdate(dataSource, "DROP SEQUENCE " + schema + sequence, "wdk-drop-sequence");
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    String[] tables = new String[] { "wdk_enum_params", "wdk_params", "wdk_questions" };
    for (String table : tables) {
      if (platform.checkTableExists(dataSource, schema, table)) {
        SqlUtils.executeUpdate(dataSource, "DROP TABLE " + schema + table, "wdk-drop-table");
      }
    }
  }

  public void createTables(WdkModel wdkModel, String schema) throws SQLException {
    DatabaseInstance database = wdkModel.getUserDb();
    DBPlatform platform = database.getPlatform();
    DataSource dataSource = database.getDataSource();

    if (schema.length() == 0)
      schema = database.getDefaultSchema();

    if (!platform.checkTableExists(dataSource, schema, "wdk_questions"))
      createQuestionTable(dataSource, schema);
    if (!platform.checkTableExists(dataSource, schema, "wdk_params"))
      createParamTable(dataSource, schema);
    if (!platform.checkTableExists(dataSource, schema, "wdk_enum_params"))
      createEnumParamTable(dataSource, schema);
  }

  private void createQuestionTable(DataSource dataSource, String schema)
      throws SQLException {
    // create sequence
    String sql = "CREATE SEQUENCE " + schema + "wdk_questions_pkseq " + "INCREMENT BY 1 START WITH 1";
    SqlUtils.executeUpdate(dataSource, sql, "wdk-create-sequence");

    // create table
    sql = "CREATE TABLE " + schema + "wdk_questions " + "(question_id NUMBER(12) NOT NULL, " +
        "question_name VARCHAR(200) NOT NULL, " + "project_id VARCHAR(50) NOT NULL, " +
        "question_checksum  VARCHAR(40) NOT NULL, " + "query_checksum  VARCHAR(40) NOT NULL, " +
        "record_class  VARCHAR(200) NOT NULL, " + "CONSTRAINT wdk_questions_pk PRIMARY KEY (question_id), " +
        "CONSTRAINT wdk_questions_uq1 " + "  UNIQUE (project_id, question_name) )";
    SqlUtils.executeUpdate(dataSource, sql, "wdk-create-table");

    // create index
    sql = "CREATE INDEX " + schema + "wdk_questions_idx01 " + "ON wdk_questions (question_checksum)";
    SqlUtils.executeUpdate(dataSource, sql, "wdk-create-index");
  }

  private void createParamTable(DataSource dataSource, String schema) throws SQLException {
    // create sequence
    String sql = "CREATE SEQUENCE " + schema + "wdk_params_pkseq " + "INCREMENT BY 1 START WITH 1";
    SqlUtils.executeUpdate(dataSource, sql, "wdk-create-sequence");

    // create table
    sql = "CREATE TABLE " + schema + "wdk_params ( " + "param_id NUMBER(12) NOT NULL, " +
        "question_id NUMBER(12) NOT NULL, " + "param_name VARCHAR(200) NOT NULL, " +
        "param_type VARCHAR(200) NOT NULL, " + "CONSTRAINT wdk_params_pk PRIMARY KEY (param_id), " +
        "CONSTRAINT wdk_params_question_id_fk FOREIGN KEY (question_id) " +
        "  REFERENCES wdk_questions (question_id), " +
        "CONSTRAINT wdk_params_uq1 UNIQUE (question_id, param_name) )";
    SqlUtils.executeUpdate(dataSource, sql, "wdk-create-table");

    // create index
    sql = "CREATE INDEX " + schema + "wdk_params_idx01 " + "ON wdk_params (param_type)";
    SqlUtils.executeUpdate(dataSource, sql, "wdk-create-index");
  }

  private void createEnumParamTable(DataSource dataSource, String schema)
      throws SQLException {
    // create table
    String sql = "CREATE TABLE " + schema + "wdk_enum_params ( " + "param_id NUMBER(12) NOT NULL, " +
        "param_value  VARCHAR(1000) NOT NULL, " + "CONSTRAINT wdk_enum_params_fk01 FOREIGN KEY (param_id) " +
        "   REFERENCES wdk_params (param_id) )";
    SqlUtils.executeUpdate(dataSource, sql, "wdk-create-table");

    // create index
    sql = "CREATE INDEX " + schema + "wdk_enum_params_idx01 " + "ON wdk_enum_params (param_id, param_value)";
    SqlUtils.executeUpdate(dataSource, sql, "wdk-create-index");
  }

  private void deleteCache(DataSource dataSource, String projectId, String schema) throws SQLException {
    // delete enum_param values
    PreparedStatement psEnums = null, psParams = null, psQuestions = null;
    try {
      // delete enum params
      String sql = "DELETE FROM " + schema + "wdk_enum_params " + "WHERE param_id IN " +
          "(SELECT param_id FROM wdk_params p, wdk_questions q " + " WHERE p.question_id = q.question_id " +
          " AND project_id = ?)";
      psEnums = SqlUtils.getPreparedStatement(dataSource, sql);
      psEnums.setString(1, projectId);
      psEnums.executeUpdate();

      // delete params
      sql = "DELETE FROM " + schema + "wdk_params WHERE question_id IN " +
          "(SELECT question_id FROM wdk_questions " + "WHERE project_id = ?)";
      psParams = SqlUtils.getPreparedStatement(dataSource, sql);
      psParams.setString(1, projectId);
      psParams.executeUpdate();

      // delete questions
      sql = "DELETE FROM " + schema + "wdk_questions " + "WHERE project_id = ?";
      psQuestions = SqlUtils.getPreparedStatement(dataSource, sql);
      psQuestions.setString(1, projectId);
      psQuestions.executeUpdate();
    }
    finally {
      SqlUtils.closeStatement(psEnums);
      SqlUtils.closeStatement(psParams);
      SqlUtils.closeStatement(psQuestions);
    }
  }

  private boolean questionExists(Question question, PreparedStatement psSelect) throws SQLException {
    psSelect.setString(1, question.getFullName());
    psSelect.setString(2, question.getWdkModel().getProjectId());
    ResultSet resultSet = null;
    try {
      resultSet = psSelect.executeQuery();
      resultSet.next();
      int count = resultSet.getInt(1);
      return (count > 0);
    }
    finally {
      if (resultSet != null) resultSet.close();
    }

  }

  private void saveQuestion(Question question, PreparedStatement psQuestion, PreparedStatement psParam,
      PreparedStatement psEnum, String schemaWithoutDot, boolean skipParams) throws WdkModelException, SQLException {
    logger.debug("Caching question [" + question.getFullName() + "]...");
    WdkModel wdkModel = question.getWdkModel();
    DatabaseInstance userDb = wdkModel.getUserDb();
    DBPlatform platform = userDb.getPlatform();

    long questionId = platform.getNextId(userDb.getDataSource(), schemaWithoutDot, "wdk_questions");
    psQuestion.setLong(1, questionId);
    psQuestion.setString(2, question.getFullName());
    psQuestion.setString(3, wdkModel.getProjectId());
    psQuestion.setString(4, question.getQuery().getChecksum(false));
    psQuestion.setString(5, question.getQuery().getChecksum(true));
    psQuestion.setString(6, question.getRecordClass().getFullName());
    psQuestion.executeUpdate();

    if (!skipParams) {
      // save the params
      for (Param param : question.getParams()) {
        saveParam(wdkModel, param, questionId, psParam, psEnum, schemaWithoutDot);
      }
    }
  }

  private void saveParam(WdkModel wdkModel, Param param, long questionId,
      PreparedStatement psParam, PreparedStatement psEnum, String schemaWithoutDot) throws SQLException,
      WdkModelException {
    DatabaseInstance database = wdkModel.getUserDb();

    String type = param.getClass().getSimpleName();
    if (param instanceof AbstractEnumParam &&
        ((AbstractEnumParam)param).getDisplayType().equals(AbstractEnumParam.DISPLAY_TYPEAHEAD)) {
      type += "-TypeAhead";
    }

    long paramId = database.getPlatform().getNextId(database.getDataSource(), schemaWithoutDot, "wdk_params");
    psParam.setLong(1, paramId);
    psParam.setLong(2, questionId);
    psParam.setString(3, param.getName());
    psParam.setString(4, type);
    psParam.executeUpdate();

    if (param instanceof AbstractEnumParam && !type.endsWith("-TypeAhead")) {
      saveEnums((AbstractEnumParam) param, paramId, psEnum);
    }
  }

  /**
   * 
   * @param param
   * @param paramId
   * @param psEnum
   * @throws WdkModelException
   * @throws SQLException
   */
  private void saveEnums(AbstractEnumParam param, long paramId, PreparedStatement psEnum)
      throws WdkModelException, SQLException {
    // need to handle dependent params
    Set<String> paramValues = Collections.emptySet(); //.getAllValues();
    for (String paramValue : paramValues) {
      psEnum.setLong(1, paramId);
      psEnum.setString(2, paramValue);
      psEnum.addBatch();
    }
    psEnum.executeBatch();
  }
}
