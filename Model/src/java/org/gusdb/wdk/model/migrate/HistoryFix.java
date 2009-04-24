package org.gusdb.wdk.model.migrate;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AnswerParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 *         the fix converts all previous incompatible params to the latest
 *         WDK-1.18 compatible format in the history table.
 */
public class HistoryFix {

    /**
     * @param args
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public static void main(String[] args) throws NoSuchAlgorithmException,
            WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        if (args.length != 2
                || !args[0].equals("-" + Utilities.ARGUMENT_PROJECT_ID)) {
            System.err.println("Usage: historyFix -"
                    + Utilities.ARGUMENT_PROJECT_ID + " <project_id>");
            System.exit(-1);
        }
        String projectId = args[1];
        HistoryFix fixer = new HistoryFix(projectId);
        fixer.invoke();
    }

    private WdkModel wdkModel;
    private PreparedStatement psUpate;

    private HistoryFix(String projectId) throws NoSuchAlgorithmException,
            WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        // load model
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        wdkModel = WdkModel.construct(projectId, gusHome);

        String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
        String sql = "UPDATE " + userSchema + "histories SET answer_id = ?, "
                + " display_params = ?, is_valid = 1 "
                + "WHERE user_id = ? AND history_id = ?";
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        psUpate = SqlUtils.getPreparedStatement(dataSource, sql);
    }

    private void invoke() throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, JSONException {
        List<User> users = getUsers();
        for (User user : users) {
            System.out.println("fixing user #" + user.getUserId() + "....");
            Map<String, Integer> answerHistories = new LinkedHashMap<String, Integer>();
            Map<Integer, String> answerParams = new LinkedHashMap<Integer, String>();
            Map<Integer, History> histories = getHistories(user, answerParams,
                    answerHistories);

            // need to do it in ascending order of history ids, so that the
            // boolean can be recovered properly.
            Integer[] ids = new Integer[histories.size()];
            histories.keySet().toArray(ids);
            Arrays.sort(ids);
            for (Integer historyId : ids) {
                History history = histories.get(historyId);

                // check if the history has valid params content
                String valueString = history.getParamValuesString();
                if (history.isBoolean() && valueString != null
                        && valueString.startsWith("{")) continue;

                String answerParam = answerParams.get(historyId);
                // invalid history may either have invalid question names,
                // or incompatible param values
                recoverHistory(user, history, answerParam, answerHistories);
            }
        }
    }

    /**
     * get a list of all registered users
     * 
     * @return
     * @throws SQLException
     * @throws WdkUserException
     */
    private List<User> getUsers() throws SQLException, WdkUserException {
        List<User> users = new ArrayList<User>();
        String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
        String sql = "SELECT * FROM " + userSchema + "users WHERE is_guest = 0";
        UserFactory userFactory = wdkModel.getUserFactory();
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        ResultSet resultSet = null;
        try {
            resultSet = SqlUtils.executeQuery(dataSource, sql);
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String email = resultSet.getString("email");
                String signature = resultSet.getString("signature");
                User user = new User(wdkModel, userId, email, signature);
                user.setGuest(resultSet.getBoolean("is_guest"));
                user.setLastName(resultSet.getString("last_name"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setMiddleName(resultSet.getString("middle_name"));
                user.setTitle(resultSet.getString("title"));
                user.setOrganization(resultSet.getString("organization"));
                user.setDepartment(resultSet.getString("department"));
                user.setAddress(resultSet.getString("address"));
                user.setCity(resultSet.getString("city"));
                user.setState(resultSet.getString("state"));
                user.setZipCode(resultSet.getString("zip_code"));
                user.setPhoneNumber(resultSet.getString("phone_number"));
                user.setCountry(resultSet.getString("country"));

                // load the user's roles
                // userFactory.loadUserRoles(user);

                // load user's preferences
                // userFactory.loadPreferences(user);

                users.add(user);
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
        System.out.println(users.size() + " users loaded.");
        return users;
    }

    private Map<Integer, History> getHistories(User user,
            Map<Integer, String> answerParams,
            Map<String, Integer> answerHistories) throws SQLException,
            WdkUserException, NoSuchAlgorithmException, JSONException,
            WdkModelException {
        Map<Integer, History> histories = new LinkedHashMap<Integer, History>();
        String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
        String wdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
        String sql = "SELECT h.*, a.question_name, a.answer_checksum, "
                + " a.params AS answer_params " + " FROM " + userSchema
                + "histories h, " + wdkSchema + "answer a "
                + " WHERE h.answer_id = a.answer_id " + " AND a.project_id = '"
                + wdkModel.getProjectId() + "' AND h.user_id = "
                + user.getUserId() + " ORDER BY h.history_id ASC";
        UserFactory userFactory = wdkModel.getUserFactory();
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        ResultSet resultSet = null;
        try {
            resultSet = SqlUtils.executeQuery(dataSource, sql);
            while (resultSet.next()) {
                int historyId = resultSet.getInt("history_id");
                History history = userFactory.loadHistory(user, historyId,
                        resultSet);
                String answerParam = wdkModel.getUserPlatform().getClobData(
                        resultSet, "answer_params");
                answerParams.put(historyId, answerParam);
                String checksum = history.getAnswerChecksum();
                if (!answerHistories.containsKey(checksum))
                    answerHistories.put(checksum, historyId);
                histories.put(historyId, history);
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
        System.out.println(histories.size() + " histories loaded.");
        return histories;

    }

    private boolean recoverHistory(User user, History history,
            String answerParam, Map<String, Integer> answerHistories) {
        try {
            // validate question name
            Map<String, String> paramValues = new LinkedHashMap<String, String>();
            String paramValueString = history.getParamValuesString();
            if (paramValueString == null || paramValueString.length() == 0)
                paramValueString = answerParam;

            if (history.isBoolean()) {
                if (paramValueString.startsWith("Boolean")) {
                    String[] parts = paramValueString.split("--WDK_DATA_DIVIDER--");
                    paramValueString = parts[parts.length - 1];
                }
                recoverBoolean(user, history, paramValueString);
            } else {
                JSONObject jsParams = new JSONObject(paramValueString);
                for (String paramName : JSONObject.getNames(jsParams)) {
                    String paramValue = jsParams.getString(paramName);
                    paramValues.put(paramName, paramValue);
                }
                recoverNonBoolean(user, history, paramValues, answerHistories);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void recoverBoolean(User user, History oldHistory, String expression)
            throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException, JSONException {
        // try to create a new history, and use the info there
        History newHistory = user.combineHistory(expression,
                oldHistory.isUseBooleanFilter());
        try {
            QueryInstance instance = newHistory.getAnswer().getIdsQueryInstance();
            String valueString = instance.getDependentParamJSONObject().toString();
            psUpate.setInt(1, newHistory.getAnswerId());
            wdkModel.getUserPlatform().updateClobData(psUpate, 2, valueString,
                    false);
            psUpate.setInt(3, user.getUserId());
            psUpate.setInt(4, oldHistory.getHistoryId());
            psUpate.execute();
        } finally {
            user.deleteHistory(newHistory.getHistoryId());
        }
    }

    private void recoverNonBoolean(User user, History oldHistory,
            Map<String, String> oldValues, Map<String, Integer> answerHistories)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        Question question = oldHistory.getQuestion();
        Map<String, Param> params = question.getParamMap();
        Map<String, Object> newValues = new LinkedHashMap<String, Object>();
        for (String paramName : oldValues.keySet()) {
            String oldValue = oldValues.get(paramName);

            // map old history param to new history param
            if (paramName.equals("historyId")
                    || paramName.equals("geneHistoryId"))
                paramName = "gene_result";

            Param param = params.get(paramName);
            String newValue = oldValue;
            if (param instanceof AnswerParam) {
                String[] parts = oldValue.split(":");
                String checksum = parts[0].trim();
                int historyId = 0;
                if (user.getSignature().equals(checksum)) {
                    historyId = Integer.parseInt(parts[1]);
                } else {
                    historyId = answerHistories.get(checksum);
                }
                newValue = user.getSignature() + ":" + historyId;
            }
            newValues.put(paramName, newValue);
        }
        String[] paramNames = new String[newValues.size()];
        newValues.keySet().toArray(paramNames);
        Arrays.sort(paramNames);
        JSONObject jsParams = new JSONObject();
        for (String paramName : paramNames) {
            Object paramValue = newValues.get(paramName);
            jsParams.put(paramName, paramValue);
        }
        String valueString = jsParams.toString();
        
        String oldValueString = oldHistory.getParamValuesString();
        // nothing's changed, don't need to update anything
        if (oldValueString != null && valueString.equals(oldValueString))
            return;

        psUpate.setInt(1, oldHistory.getAnswerId());
        wdkModel.getUserPlatform().updateClobData(psUpate, 2, valueString,
                false);
        psUpate.setInt(3, user.getUserId());
        psUpate.setInt(4, oldHistory.getHistoryId());
        psUpate.execute();
    }
}
