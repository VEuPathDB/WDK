/**
 * 
 */
package org.gusdb.wdk.model.migrate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 */
public class Migrator1_18To1_19 implements Migrator {

  private static final String ARG_OLD_USER_SCHEMA = "oldUserSchema";
  private static final String ARG_OLD_WDK_SCHEMA = "oldWdkSchema";

  private static class UserProject {
    public int userId;
    public String projectId;

    public UserProject(int userId, String projectId) {
      this.userId = userId;
      this.projectId = projectId;
    }

    public int hashCode() {
      return (userId + projectId).hashCode();
    }

    public boolean equals(Object obj) {
      if (obj != null && obj instanceof UserProject) {
        UserProject up = (UserProject) obj;
        return this.userId == up.userId
            && this.projectId.equalsIgnoreCase(up.projectId);
      } else
        return false;
    }
  }

  public class HistoryInfo implements Comparable<HistoryInfo> {
    public int userId;
    public String projectId;
    public int historyId;
    public Date createTime;
    public Date runTime;
    public int estimateSize;
    public String answerFilter;
    public String customName;
    public boolean isBoolean;
    public boolean isDeleted;
    public String paramClob;
    public String answerParamClob;
    public int answerId;
    public String questionName;

    public boolean isValid = true;
    public Map<Integer, HistoryInfo> parents = new LinkedHashMap<Integer, HistoryInfo>();

    public boolean ancesterOf(HistoryInfo history) {
      if (history.userId != userId || history.historyId == historyId
          || !history.projectId.equals(projectId))
        return false;
      for (HistoryInfo parent : history.parents.values()) {
        if (parent.historyId == historyId)
          return true;
        if (ancesterOf(parent))
          return true;
      }
      return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(HistoryInfo history) {
      if (ancesterOf(history))
        return -1;
      else if (history.ancesterOf(this))
        return 1;
      else
        return historyId - history.historyId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return (userId + projectId + historyId).hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof HistoryInfo) {
        HistoryInfo history = (HistoryInfo) obj;
        return userId == history.userId && historyId == history.historyId
            && projectId.equals(history.projectId);
      } else
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "User: " + userId + ", Project: " + projectId + ", History: "
          + historyId;
    }
  }

  private static final Logger logger = Logger.getLogger(Migrator1_18To1_19.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.migrate.Migrator#migrate()
   */
  @Override
  public void migrate(WdkModel wdkModel, CommandLine commandLine)
      throws WdkModelException, WdkUserException, NoSuchAlgorithmException,
      SQLException, JSONException {
    DBPlatform platform = wdkModel.getUserPlatform();
    int migrateId = platform.getNextId("apidb", "migration");

    String oldUserSchema = commandLine.getOptionValue(ARG_OLD_USER_SCHEMA);
    String oldWdkSchema = commandLine.getOptionValue(ARG_OLD_WDK_SCHEMA);

    copyClobValues(wdkModel, oldWdkSchema, migrateId);
    copyDatasetIndices(wdkModel, oldUserSchema, oldWdkSchema, migrateId);
    copyDatasetValues(wdkModel, oldWdkSchema, migrateId);
    copyUserDatasets(wdkModel, oldUserSchema, oldWdkSchema, migrateId);
    copyAnswers(wdkModel, oldUserSchema, oldWdkSchema, migrateId);
    copySteps(wdkModel, oldUserSchema, oldWdkSchema, migrateId);
  }

  /**
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   * 
   */
  private void copyClobValues(WdkModel wdkModel, String oldWdkSchema,
      int migrateId) throws SQLException, WdkUserException, WdkModelException {
    logger.debug("Copying clob values...");
    String newWdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
    String cvo = oldWdkSchema + "clob_values";
    String cvn = newWdkSchema + "clob_values";
    StringBuffer sql = new StringBuffer("INSERT INTO " + cvn);
    sql.append("  (clob_checksum, clob_value, migration_id) ");
    sql.append("SELECT cvo.clob_checksum, cvo.clob_value, " + migrateId);
    sql.append(" FROM " + cvo + " cvo, ");
    sql.append("  (SELECT clob_checksum FROM " + cvo);
    sql.append("   MINUS");
    sql.append("   SELECT clob_checksum FROM " + cvn + ") cvm ");
    sql.append("WHERE cvo.clob_checksum = cvm.clob_checksum ");

    DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
    int count = SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
        "wdk-migrate-clob");
    logger.debug(count + " clob_value rows inserted");
  }

  /**
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   * 
   */
  private void copyDatasetIndices(WdkModel wdkModel, String oldUserSchema,
      String oldWdkSchema, int migrateId) throws SQLException,
      WdkUserException, WdkModelException {
    logger.debug("Copying dataset indices...");
    String newWdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
    String dio = oldWdkSchema + "dataset_indices";
    String din = newWdkSchema + "dataset_indices";
    StringBuffer sql = new StringBuffer("INSERT INTO " + din);
    sql.append("  (dataset_id, dataset_checksum, summary, ");
    sql.append("   dataset_size, prev_dataset_id, migration_id) ");
    sql.append("SELECT ").append(newWdkSchema);
    sql.append("  dataset_indices_pkseq.nextval as dataset_id, ");
    sql.append("  dataset_checksum, summary, dataset_size, ");
    sql.append("  prev_dataset_id, ").append(migrateId);
    sql.append(" FROM (SELECT DISTINCT d.dataset_checksum, d.summary, ");
    sql.append("        d.dataset_size, d.dataset_id AS prev_dataset_id ");
    sql.append("      FROM ").append(dio).append(" d, ");
    sql.append(oldUserSchema).append("user_datasets ud, ");
    sql.append("         (SELECT dataset_checksum FROM ").append(dio);
    sql.append("          MINUS ");
    sql.append("          SELECT dataset_checksum FROM ").append(din).append(
        ") dm ");
    sql.append("      WHERE d.dataset_id = ud.dataset_id ");
    sql.append("        AND d.dataset_checksum = dm.dataset_checksum)");

    DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
    int count = SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
        "wdk-migrate-dataset-index");
    logger.debug(count + " dataset index rows inserted");
  }

  /**
   * 
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  private void copyDatasetValues(WdkModel wdkModel, String oldWdkSchema,
      int migrateId) throws SQLException, WdkUserException, WdkModelException {
    logger.debug("Copying dataset values...");
    String newWdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
    String dvo = oldWdkSchema + "dataset_values";
    String dvn = newWdkSchema + "dataset_values";
    StringBuffer sql = new StringBuffer("INSERT INTO " + dvn);
    sql.append("  (dataset_id, dataset_value, migration_id) ");
    sql.append("SELECT di.dataset_id, dv.dataset_value, " + migrateId);
    sql.append(" FROM ").append(dvo).append(" dv, ");
    sql.append(newWdkSchema).append("dataset_indices di ");
    sql.append("WHERE dv.dataset_id = di.prev_dataset_id ");
    sql.append("  AND di.dataset_id NOT IN ");
    sql.append("    (SELECT dataset_id FROM ").append(dvn).append(")");

    DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
    int count = SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
        "wdk-migrate-dataset-value");
    logger.debug(count + " dataset value rows inserted");
  }

  /**
   * 
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  private void copyAnswers(WdkModel wdkModel, String oldUserSchema,
      String oldWdkSchema, int migrateId) throws SQLException,
      WdkUserException, WdkModelException {
    logger.debug("Copying answers...");
    String newWdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
    DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
    String ao = oldWdkSchema + "answer";
    String an = newWdkSchema + "answers";
    StringBuffer sql = new StringBuffer("INSERT INTO " + an);
    sql.append("  (answer_id, project_id, answer_checksum, ");
    sql.append("   project_version, question_name, query_checksum, ");
    sql.append("   prev_answer_id, params, result_message, migration_id) ");
    sql.append("SELECT ");
    sql.append(newWdkSchema).append("answers_pkseq.nextval, ");
    sql.append("  DECODE(a.project_id, 'ApiDB', 'EuPathDB', a.project_id) AS project_id, ");
    sql.append("  a.answer_checksum, a.project_version, a.question_name, ");
    sql.append("  a.query_checksum, a.answer_id AS prev_answer_id, ");
    sql.append("  a.params, a.result_message, " + migrateId);
    sql.append(" FROM wdkstorage.answer a, ");
    sql.append("  (SELECT a.answer_id FROM ").append(ao).append(" a, ");
    sql.append(oldUserSchema).append("users u, ");
    sql.append(oldUserSchema).append("histories h ");
    sql.append("   WHERE a.answer_id = h.answer_id ");
    sql.append("     AND h.user_id = u.user_id AND u.is_guest = 0");
    sql.append("   MINUS ");
    sql.append("   SELECT ao.answer_id FROM ");
    sql.append(ao).append(" ao, ").append(an).append(" an ");
    sql.append("   WHERE DECODE(ao.project_id, 'ApiDB', 'EuPathDB', ao.project_id) = an.project_id ");
    sql.append("     AND ao.answer_checksum = an.answer_checksum) af ");
    sql.append("WHERE a.answer_id = af.answer_id ");

    int count = SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
        "wdk-migrate-answers");
    logger.debug(count + " answer rows inserted");
  }

  /**
   * 
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  private void copyUserDatasets(WdkModel wdkModel, String oldUserSchema,
      String oldWdkSchema, int migrateId) throws SQLException,
      WdkUserException, WdkModelException {
    logger.debug("Copying user datasets...");
    ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
    String newWdkSchema = userDB.getWdkEngineSchema();
    String newUserSchema = userDB.getUserSchema();
    String udo = oldUserSchema + "user_datasets";
    String udn = newUserSchema + "user_datasets2";
    String dio = oldWdkSchema + "dataset_indices";
    String din = newWdkSchema + "dataset_indices";
    StringBuffer sql = new StringBuffer("INSERT INTO " + udn);
    sql.append("  (user_dataset_id, dataset_id, user_id, ");
    sql.append("   create_time, upload_file, migration_id) ");
    sql.append("SELECT ").append(newUserSchema).append(
        "user_datasets2_pkseq.nextval, ");
    sql.append("  udf.dataset_id, udf.user_id, udo.create_time, udo.upload_file, "
        + migrateId);
    sql.append(" FROM ").append(din).append(" din, ");
    sql.append(dio).append(" dio, ").append(udo).append(" udo, ");
    sql.append("  (SELECT din.dataset_id, u.user_id ");
    sql.append("   FROM ").append(newUserSchema).append("users u, ");
    sql.append(din).append(" din, ").append(dio).append(" dio, ");
    sql.append(udo).append(" udo ");
    sql.append("   WHERE u.user_id = udo.user_id ");
    sql.append("     AND udo.dataset_id = dio.dataset_id ");
    sql.append("     AND dio.dataset_checksum = din.dataset_checksum ");
    sql.append("   MINUS ");
    sql.append("   SELECT dataset_id, user_id FROM ").append(udn);
    sql.append("  ) udf ");
    sql.append("WHERE udf.user_id = udo.user_id ");
    sql.append("  AND udf.dataset_id = din.dataset_id ");
    sql.append("  AND din.dataset_checksum = dio.dataset_checksum ");
    sql.append("  AND dio.dataset_id = udo.dataset_id ");

    DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
    int count = SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
        "wdk-migrate-user-dataset");
    logger.debug(count + " user dataset rows inserted");
  }

  /**
   * 
   * @throws SQLException
   * @throws SQLException
   * @throws JSONException
   * @throws WdkUserException
   * @throws WdkModelException
   * @throws NoSuchAlgorithmException
   */
  private void copySteps(WdkModel wdkModel, String oldUserSchema,
      String oldWdkSchema, int migrateId) throws SQLException, JSONException,
      NoSuchAlgorithmException, WdkModelException, WdkUserException {
    logger.debug("Copying steps...");
    ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
    String newWdkSchema = userDB.getWdkEngineSchema();
    String newUserSchema = userDB.getUserSchema();
    String newProject = wdkModel.getProjectId();
    String oldProject = (newProject.equals("EuPathDB")) ? "ApiDB" : newProject;
    StringBuffer sql = new StringBuffer("SELECT h.history_id, ");
    sql.append(" u.user_id, h.create_time, h.last_run_time, ");
    sql.append(" h.estimate_size, h.answer_filter, h.custom_name, ");
    sql.append(" h.is_boolean, h.is_deleted, h.display_params, an.params, ");
    sql.append(" an.project_id, an.answer_id, an.question_name ");
    sql.append("FROM ").append(oldUserSchema).append("histories h, ");
    sql.append(newUserSchema).append("users u, ");
    sql.append(oldWdkSchema).append("answer ao, ");
    sql.append(newWdkSchema).append("answers an ");
    sql.append("WHERE h.user_id = u.user_id AND u.is_guest = 0 ");
    sql.append(" AND h.answer_id = ao.answer_id ");
    sql.append(" AND h.migration_id IS NULL ");
    sql.append(" AND ao.answer_checksum = an.answer_checksum ");
    sql.append(" AND ao.project_id = '").append(oldProject).append("' ");
    sql.append(" AND an.project_id = '").append(newProject).append("' ");

    Map<UserProject, Map<Integer, HistoryInfo>> users = new LinkedHashMap<UserProject, Map<Integer, HistoryInfo>>();
    DBPlatform platform = wdkModel.getUserPlatform();
    DataSource dataSource = platform.getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql.toString(),
          "wdk-migrate-steps");
      int count = 0;
      while (resultSet.next()) {
        HistoryInfo info = new HistoryInfo();
        info.historyId = resultSet.getInt("history_id");
        info.userId = resultSet.getInt("user_id");
        info.createTime = resultSet.getDate("create_time");
        info.runTime = resultSet.getDate("last_run_time");
        info.estimateSize = resultSet.getInt("estimate_size");
        info.answerFilter = resultSet.getString("answer_filter");
        info.customName = resultSet.getString("custom_name");
        info.isBoolean = resultSet.getBoolean("is_boolean");
        info.isDeleted = resultSet.getBoolean("is_deleted");
        info.paramClob = platform.getClobData(resultSet, "display_params");
        info.answerParamClob = platform.getClobData(resultSet, "params");
        info.projectId = resultSet.getString("project_id");
        info.answerId = resultSet.getInt("answer_id");
        info.questionName = resultSet.getString("question_name");

        UserProject user = new UserProject(info.userId, info.projectId);
        Map<Integer, HistoryInfo> histories = users.get(user);
        if (histories == null) {
          histories = new LinkedHashMap<Integer, HistoryInfo>();
          users.put(user, histories);
        }
        histories.put(info.historyId, info);

        count++;
        if (count % 100 == 0)
          logger.debug(count + " histories read.");
      }
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
    logger.info("Totally read " + users.size() + " users");
    int count = 0;
    for (UserProject user : users.keySet()) {
      Map<Integer, HistoryInfo> histories = users.get(user);
      createSteps(wdkModel, oldUserSchema, oldWdkSchema, migrateId, user,
          histories);

      count++;
      if (count % 100 == 0)
        logger.debug(count + "/" + users.size() + " users' steps copied.");
    }
  }

  private void createSteps(WdkModel wdkModel, String oldUserSchema,
      String oldWdkSchema, int migrateId, UserProject user,
      Map<Integer, HistoryInfo> histories) throws JSONException, SQLException,
      WdkUserException, WdkModelException {
    for (HistoryInfo history : histories.values()) {
      normalizeParams(history);
      Set<Integer> parents = parseParents(wdkModel, oldUserSchema,
          oldWdkSchema, history);
      for (int parentId : parents) {
        HistoryInfo parent = histories.get(parentId);
        if (parent == null) {
          history.isValid = false;
          break;
        } else
          history.parents.put(parentId, parent);
      }
    }
    HistoryInfo[] array = new HistoryInfo[histories.size()];
    histories.values().toArray(array);
    Arrays.sort(array);
    Map<Integer, Integer> stepMap = loadStepMap(wdkModel, user);
    for (HistoryInfo history : array) {
      try {
        int stepId = addStep(wdkModel, history, stepMap);
        stepMap.put(history.historyId, stepId);

        // mark the history to be done
        DataSource source = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, source, "UPDATE " + oldUserSchema
            + "histories SET migration_id = " + migrateId + " WHERE user_id = "
            + history.userId + " AND history_id = " + history.historyId,
            "wdk-migrate-update-id");

        String newSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
        SqlUtils.executeUpdate(wdkModel, source, "UPDATE " + newSchema
            + "steps " + " SET prev_step_id = " + history.historyId
            + ", migration_id = " + migrateId + " WHERE user_id = "
            + history.userId + " AND display_id = " + stepId,
            "wdk-migrate-update-prev-step");
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private Map<Integer, Integer> loadStepMap(WdkModel wdkModel, UserProject user)
      throws SQLException, WdkUserException, WdkModelException {
    ResultSet resultSet = null;
    DataSource source = wdkModel.getUserPlatform().getDataSource();
    ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
    Map<Integer, Integer> stepMap = new LinkedHashMap<Integer, Integer>();
    try {
      resultSet = SqlUtils.executeQuery(wdkModel, source, "SELECT "
          + "    s.display_id, s.prev_step_id  FROM " + userDB.getUserSchema()
          + "steps s, " + userDB.getWdkEngineSchema() + "answers a "
          + " WHERE s.answer_id = a.answer_id "
          + "  AND s.prev_step_id IS NOT NULL " + "  AND a.project_id = '"
          + user.projectId + "' AND s.user_id = " + user.userId,
          "wdk-migrate-select-step-id");
      while (resultSet.next()) {
        int stepId = resultSet.getInt("display_id");
        int historyId = resultSet.getInt("prev_step_id");
        stepMap.put(historyId, stepId);
      }
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
    return stepMap;
  }

  private void normalizeParams(HistoryInfo history) throws JSONException {
    String paramClob = history.paramClob;
    if (paramClob == null || paramClob.length() < 4)
      paramClob = history.answerParamClob;

    // convert the older clob format
    if (paramClob.indexOf("--WDK_DATA_DIVIDER--") >= 0) {
      String[] parts = paramClob.split("--WDK_DATA_DIVIDER--");
      if (parts[0].equals("BooleanQuerySet.BooleanQuery")) {

        paramClob = (parts.length < 6) ? parts[1] : parts[5];
      } else {
        JSONObject jsParams = new JSONObject();
        for (int i = 1; i < parts.length; i += 2) {
          String paramName = parts[i].trim();
          String paramValue = parts[i + 1].trim();
          jsParams.put(paramName, paramValue);
        }
        paramClob = jsParams.toString();
      }
    }
    history.paramClob = paramClob;
  }

  private Set<Integer> parseParents(WdkModel wdkModel, String oldUserSchema,
      String oldWdkSchema, HistoryInfo history) throws JSONException,
      SQLException, WdkUserException {
    Set<Integer> parents = new LinkedHashSet<Integer>();
    String paramClob = history.paramClob;
    try {
      if (paramClob.startsWith("{")) {
        Question question = (Question) wdkModel.resolveReference(history.questionName);
        JSONObject jsParams = new JSONObject(paramClob);
        DataSource source = wdkModel.getUserPlatform().getDataSource();
        for (Param param : question.getParams()) {
          if (!(param instanceof AnswerParam))
            continue;
          String parent = (String) jsParams.get(param.getName());
          int pos = parent.indexOf(":");
          if (pos == 0)
            parent = parent.substring(pos + 1);
          int parentId;
          if (parent.matches("^\\d+$")) {
            parentId = Integer.parseInt(parent);
          } else { // history is an answer checksum
            BigInteger result = (BigInteger) SqlUtils.executeScalar(wdkModel,
                source, "SELECT history_id FROM " + oldUserSchema
                    + "histories h, " + oldWdkSchema + "answer a WHERE "
                    + "  a.answer_checksum = '" + parent
                    + "' AND h.answer_id = a.answer_id",
                "wdk-migrate-select-history-id-by-checksum");
            parentId = result.intValue();
          }
          parents.add(parentId);
          jsParams.put(param.getName(), parentId);
        }
        history.paramClob = jsParams.toString();
      } else {
        Matcher matcher = Pattern.compile("\\b\\d+\\b").matcher(paramClob);
        while (matcher.find()) {
          String parent = paramClob.substring(matcher.start(), matcher.end());
          parents.add(Integer.parseInt(parent));
        }
      }
    } catch (WdkModelException ex) {
      logger.info(history + " invalid");
      logger.info(ex);
      history.isValid = false;
    }

    return parents;
  }

  private int addStep(WdkModel wdkModel, HistoryInfo history,
      Map<Integer, Integer> stepMap) throws WdkModelException, WdkUserException {
    String paramClob = prepareParams(wdkModel, history, stepMap);
    int stepId;
    if (paramClob.startsWith("{")) {
      stepId = insertRawStep(wdkModel, history, paramClob);
    } else {
      User user = wdkModel.getUserFactory().getUser(history.userId);
      Step step = user.combineStep(paramClob, false, history.isDeleted);
      stepId = step.getDisplayId();
    }
    return stepId;
  }

  private String prepareParams(WdkModel wdkModel, HistoryInfo history,
      Map<Integer, Integer> stepMap) throws WdkModelException, WdkUserException {
    try {
      String paramClob = history.paramClob;
      if (paramClob.startsWith("{")) {
        Question question = (Question) wdkModel.resolveReference(history.questionName);
        JSONObject jsOld = new JSONObject(paramClob);
        JSONObject jsNew = new JSONObject();
        for (Param param : question.getParams()) {
          if (!jsOld.has(param.getName()))
            continue;
          String paramValue = (String) jsOld.get(param.getName());
          if (param instanceof AnswerParam) {
            int newId = stepMap.get(Integer.parseInt(paramValue));
            paramValue = Integer.toString(newId);
          } else if (param instanceof DatasetParam) {
            int userDatasetId = getUserDatasetId(wdkModel, history.userId,
                (DatasetParam) param, paramClob);
            paramValue = Integer.toString(userDatasetId);
          } else if (!paramValue.startsWith(Utilities.PARAM_COMPRESSE_PREFIX)) {
            paramValue = param.compressValue(paramValue);
          }
          jsNew.put(param.getName(), paramValue);
        }
        paramClob = jsNew.toString();
      } else {
        for (int parentId : history.parents.keySet()) {
          if (stepMap.containsKey(parentId)) {
            String newId = stepMap.get(parentId).toString();
            paramClob = paramClob.replaceAll("\\b" + parentId + "\\b", newId);
          } else {
            history.isValid = false;
          }
        }
      }
      return paramClob;
    } catch (JSONException e) {
      throw new WdkModelException("Could not prepare params.", e);
    }
  }

  private int getUserDatasetId(WdkModel wdkModel, int userId,
      DatasetParam param, String value) throws WdkModelException {
    User user = wdkModel.getUserFactory().getUser(userId);
    if (value.length() == 65 || value.length() == 32) {
      // the value is a dataset_checksum, or a combined checksum
      String checksum = value;
      if (value.length() == 65)
        checksum = value.substring(value.indexOf(':') + 1);
      DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
      ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
      String userSchema = userDB.getUserSchema();
      String wdkSchema = userDB.getWdkEngineSchema();
      Object result = SqlUtils.executeScalar(wdkModel, dataSource,
          "SELECT user_dataset_id                              " + "FROM "
              + userSchema + "  user_datasets2 ud, " + wdkSchema
              + "  dataset_indices di "
              + "WHERE ud.dataset_id = di.dataset_id "
              + "  AND di.dataset_checksum = '" + checksum + "'",
          "wdk-migrate-select-user-dataset");
      return (Integer) result;
    } else { // the value is raw value, create a dataset from it.
      RecordClass recordClass = param.getRecordClass();
      Dataset dataset = user.createDataset(recordClass, null, value);
      return dataset.getUserDatasetId();
    }
  }

  private int insertRawStep(WdkModel wdkModel, HistoryInfo history,
      String paramClob) throws WdkModelException, WdkUserException {
    String schema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    String sql = "INSERT INTO " + schema + "steps (step_id, display_id, "
        + "user_id, answer_id, left_child_id, right_child_id, "
        + "create_time, last_run_time, estimate_size, answer_filter, "
        + "custom_name, is_deleted, is_collapsible, " + "display_params) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?)";

    Integer leftChild = null, rightChild = null;
    try {
      JSONObject jsParams = new JSONObject(paramClob);
      Question question = (Question) wdkModel.resolveReference(history.questionName);
      Query query = question.getQuery();
      if (query.isBoolean()) {
        BooleanQuery booleanQuery = (BooleanQuery) query;
        Object left = jsParams.get(booleanQuery.getLeftOperandParam().getName());
        Object right = jsParams.get(booleanQuery.getRightOperandParam().getName());
        if (left != null)
          leftChild = Integer.parseInt((String) left);
        if (right != null)
          rightChild = Integer.parseInt((String) right);
      } else if (query.isTransform()) {
        for (Param param : query.getParams()) {
          if (param instanceof AnswerParam) {
            Object left = jsParams.get(param.getName());
            if (left != null) {
              leftChild = Integer.parseInt((String) left);
              break;
            }
          }
        }
      }
    } catch (WdkModelException ex) {
      // question name doesn't exist
      history.isValid = false;
      ex.printStackTrace();
    } catch (JSONException e) {
      throw new WdkModelException("Could not insert raw step.", e);
    }

    DBPlatform platform = wdkModel.getUserPlatform();
    DataSource dataSource = platform.getDataSource();
    PreparedStatement ps = null;

    try {
      ps = SqlUtils.getPreparedStatement(dataSource, sql);
      int stepId = platform.getNextId(schema, "steps");
      BigDecimal display = (BigDecimal) SqlUtils.executeScalar(wdkModel,
          dataSource, "" + "SELECT max(display_id) + 1 FROM " + schema
              + "steps WHERE user_id = " + history.userId,
          "wdk-migrate-max-step-id");
      int displayId = (display == null) ? 1 : display.intValue();

      ps.setInt(1, stepId);
      ps.setInt(2, displayId);
      ps.setInt(3, history.userId);
      ps.setInt(4, history.answerId);
      ps.setObject(5, leftChild);
      ps.setObject(6, rightChild);
      ps.setDate(7, history.createTime);
      ps.setDate(8, history.runTime);
      ps.setInt(9, history.estimateSize);
      ps.setString(10, history.answerFilter);
      ps.setString(11, history.customName);
      ps.setBoolean(12, history.isDeleted);
      platform.setClobData(ps, 13, paramClob, false);
      ps.executeUpdate();

      return displayId;
    } catch (SQLException e) {
      throw new WdkModelException("Could not insert raw step.", e);
    } finally {
      SqlUtils.closeStatement(ps);
    }
  }

  @Override
  public void declareOptions(Options options) {
    Option option = new Option(ARG_OLD_USER_SCHEMA, true,
        "the old user login schema, where the user data is migrated from");
    option.setRequired(true);
    option.setArgName(ARG_OLD_USER_SCHEMA);
    options.addOption(option);

    option = new Option(ARG_OLD_WDK_SCHEMA, true,
        "the old wdk storage schema, where the wdk data is migrated from");
    option.setRequired(true);
    option.setArgName(ARG_OLD_WDK_SCHEMA);
    options.addOption(option);
  }
}
