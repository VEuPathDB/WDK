package org.gusdb.wdk.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * A wdk model user. The WdkModel has a list of Users, and maintains unique
 * identifiers for them. These will likely be the session id for non-persistent
 * users.
 */
public class User {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.User");

    private String userID;
    private Map<Integer, UserAnswer> userAnswers;
    private int answerIndex;
    /**
     * the WdkModel is used when growing BooleanQuestionNode
     */
    private WdkModel model;

    public User(String userID, WdkModel model) {
        this.model = model;
        this.userID = userID;
        this.answerIndex = 0;
        // don't create the userAnswers map by default, since there may be many
        // users at the same time, and it would consume too many memory; so I
        // only create it when it's used.
    }

    public String getUserID() {
        return this.userID;
    }

    public void addAnswer(Answer answer) throws WdkUserException {
        try {
            getUserAnswerByAnswer(answer, false);
            // answer exists, return
            return;
        } catch (WdkUserException ex) {
            // TODO Auto-generated catch block
            // ex.printStackTrace();
            // System.err.println(ex);
        }
        insertAnswer(answer);
    }

    public void addAnswerFuzzy(Answer answer) throws WdkUserException {
        try {
            getUserAnswerByAnswer(answer, true);
            // answer exists, return
            return;
        } catch (WdkUserException ex) {
            // TODO Auto-generated catch block
            // ex.printStackTrace();
            // System.err.println(ex);
        }
        insertAnswer(answer);
    }

    private void insertAnswer(Answer answer) throws WdkUserException {
        answerIndex++;
        UserAnswer userAnswer = new UserAnswer(userID, answerIndex, answer);

        // initialize userAnswers map
        if (userAnswers == null)
            userAnswers = new HashMap<Integer, UserAnswer>();
        userAnswers.put(answerIndex, userAnswer);

        // cache the history
        try {
            saveHistory(userAnswer);
        } catch (WdkModelException ex) {
            throw new WdkUserException(ex);
        }
    }

    public void deleteUserAnswer(int answerId) throws WdkUserException {
        if (userAnswers == null)
            throw new WdkUserException(
                    "The answer specified by the given ID doesn't exist!");
        UserAnswer answer = userAnswers.remove(answerId);
        if (answer == null)
            throw new WdkUserException(
                    "The answer specified by the given ID doesn't exist!");
        if (userAnswers.isEmpty()) userAnswers = null;

        // also delete the history record in the database
        deleteHistory(answerId);
    }

    private synchronized void deleteHistory(int historyID)
            throws WdkUserException {
        String historyTableName = model.resultFactory.getHistoryTableName();
        DataSource dataSource = model.platform.getDataSource();

        StringBuffer sb = new StringBuffer("DELETE FROM ");
        sb.append(historyTableName);
        sb.append(" WHERE ");
        sb.append(ResultFactory.FIELD_USER_ID);
        sb.append("='");
        sb.append(userID);
        sb.append(" AND ");
        sb.append(ResultFactory.FIELD_HISTORY_ID);
        sb.append("=");
        sb.append(historyID);

        // execute the deletion
        try {
            SqlUtils.executeUpdate(dataSource, sb.toString());
        } catch (SQLException ex) {
            logger.finest("Got an SQLException: " + ex.toString());
            throw new WdkUserException(ex);
        }
    }

    public void clearUserAnswers() throws WdkUserException {
        if (userAnswers != null) userAnswers.clear();
        userAnswers = null;

        // clear the history cache, too
        String historyTableName = model.resultFactory.getHistoryTableName();
        DataSource dataSource = model.platform.getDataSource();

        StringBuffer sb = new StringBuffer("DELETE FROM ");
        sb.append(historyTableName);
        sb.append(" WHERE ");
        sb.append(ResultFactory.FIELD_USER_ID);
        sb.append("='");
        sb.append(userID);
        sb.append("'");
        try {
            SqlUtils.execute(dataSource, sb.toString());
        } catch (SQLException ex) {
            logger.finest("Got an SQLException: " + ex.toString());
            throw new WdkUserException(ex);
        }
    }

    public UserAnswer[] getUserAnswers() {
        if (userAnswers == null || userAnswers.size() == 0)
            return new UserAnswer[0];
        UserAnswer[] answers = new UserAnswer[userAnswers.size()];
        userAnswers.values().toArray(answers);
        return answers;
    }

    public Map getRecordAnswerMap() {
        Map<String, Map<Integer, UserAnswer>> recAnsMapMap = new HashMap<String, Map<Integer, UserAnswer>>();
        if (userAnswers == null || userAnswers.size() == 0)
            return recAnsMapMap;

        for (int ansID : userAnswers.keySet()) {
            UserAnswer usrAns = userAnswers.get(new Integer(ansID));
            String rec = usrAns.getAnswer().getQuestion().getRecordClass().getFullName();
            if (recAnsMapMap.get(rec) == null) {
                recAnsMapMap.put(rec, new HashMap<Integer, UserAnswer>());
            }
            Map<Integer, UserAnswer> recAnsMapMap1 = recAnsMapMap.get(rec);
            recAnsMapMap1.put(new Integer(ansID), usrAns);
        }

        // wants answers in sorted arrays
        Map recAnsMap = new HashMap<String, UserAnswer[]>();
        for (Object r : recAnsMapMap.keySet()) {
            String rec = (String) r;
            Map<Integer, UserAnswer> recAnsMapMap1 = recAnsMapMap.get(rec);
            List ansIDList = Arrays.asList(recAnsMapMap1.keySet().toArray());
            Collections.sort(ansIDList);
            Collections.reverse(ansIDList);
            Object[] sortedAnsIDs = ansIDList.toArray();
            Vector v = new Vector();
            for (int i = 0; i < sortedAnsIDs.length; i++) {
                v.add(recAnsMapMap1.get((Integer) sortedAnsIDs[i]));
            }
            UserAnswer[] sortedUsrAns = new UserAnswer[v.size()];
            v.copyInto(sortedUsrAns);

            recAnsMap.put(rec, sortedUsrAns);
        }

        return recAnsMap;
    }

    public UserAnswer getUserAnswerByID(int answerID) throws WdkUserException {
        if (userAnswers == null || !userAnswers.containsKey(answerID))
            throw new WdkUserException("The answer of ID " + answerID
                    + " does not exist!");
        return userAnswers.get(answerID);
    }

    public UserAnswer getUserAnswerByName(String name) throws WdkUserException {
        if (userAnswers != null) {
            for (UserAnswer answer : userAnswers.values()) {
                if (answer.getName().equalsIgnoreCase(name)) return answer;
            }
        }
        throw new WdkUserException("The answer of name " + name
                + " does not exist!");
    }

    public UserAnswer getUserAnswerByAnswerFuzzy(Answer answer)
            throws WdkUserException {
        return getUserAnswerByAnswer(answer, true);
    }

    public UserAnswer getUserAnswerByAnswer(Answer answer)
            throws WdkUserException {
        return getUserAnswerByAnswer(answer, false);
    }

    private UserAnswer getUserAnswerByAnswer(Answer answer, boolean ignorePage)
            throws WdkUserException {
        if (userAnswers != null) {
            // check if the answer exists or not
            for (UserAnswer uans : userAnswers.values()) {
                Answer ans = uans.getAnswer();
                // check question name
                String qname = ans.getQuestion().getFullName();
                if (!qname.equalsIgnoreCase(answer.getQuestion().getFullName()))
                    continue;

                // check paging number
                if (!ignorePage
                        && (ans.startRecordInstanceI != answer.startRecordInstanceI || ans.endRecordInstanceI != answer.endRecordInstanceI))
                    continue;

                // check parameters
                Map params = ans.getParams();
                Map pchecks = answer.getParams();
                Iterator it = params.keySet().iterator();
                boolean equal = true;
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String value = params.get(key).toString();
                    // check on the input answer
                    if (pchecks.containsKey(key)) {
                        String vcheck = pchecks.get(key).toString();
                        if (!value.equalsIgnoreCase(vcheck)) {
                            equal = false;
                            break;
                        }
                    } else {
                        equal = false;
                        break;
                    }
                }
                // check if two answers are the same
                if (equal) return uans;
            }
        }
        throw new WdkUserException(
                "The UserAnswer specified by the given answer doesn't exist!");
    }

    public void renameUserAnswer(int answerID, String name)
            throws WdkUserException {
        // check if the answer exists
        if (userAnswers == null || !userAnswers.containsKey(answerID))
            throw new WdkUserException(
                    "The answer specified by the given ID doesn't exist!");

        // check if the answer name is unique
        for (int ansID : userAnswers.keySet()) {
            if (ansID != answerID) {
                UserAnswer answer = userAnswers.get(ansID);
                if (answer.getName().equalsIgnoreCase(name))
                    throw new WdkUserException(
                            "Duplicated name of the answer for this user");
            }
        }
        // name is unique in user's session scope
        UserAnswer answer = userAnswers.get(answerID);
        answer.setName(name);
    }

    public UserAnswer combineUserAnswers(int firstAnswerID, int secondAnswerID,
            String operation, int startIndex, int endIndex,
            Map<String, String> operatorMap)
            throws WdkModelException, WdkUserException {
        // construct operand map
        Map<String, Answer> operandMap = buildOperandMap();

        // construct the expression
        StringBuffer sb = new StringBuffer();
        sb.append('#');
        sb.append(firstAnswerID);
        sb.append(' ');
        sb.append(operation);
        sb.append(" #");
        sb.append(secondAnswerID);

        // construct BooleanQuestionNode
        BooleanExpression be = new BooleanExpression(model);
        BooleanQuestionNode root = be.parseExpression(sb.toString(),
                operandMap, operatorMap);

        // create a new UserAnswer
        Answer answer = root.makeAnswer(startIndex, endIndex);
        addAnswer(answer);
        // set user answer as combined
        UserAnswer userAnswer = getUserAnswerByAnswer(answer);
        userAnswer.setCombinedAnswer(true);
        userAnswer.setName(sb.toString());
        return userAnswer;
    }

    public UserAnswer combineUserAnswers(String expression, int startIndex,
            int endIndex, Map<String, String> operatorMap)
            throws WdkUserException, WdkModelException {
        // construct operand map
        Map<String, Answer> operandMap = buildOperandMap();

        // construct BooleanQuestionNode
        BooleanExpression be = new BooleanExpression(model);
        BooleanQuestionNode root = be.parseExpression(expression, operandMap,
                operatorMap);

        // make answer
        Answer answer = root.makeAnswer(startIndex, endIndex);
        addAnswer(answer);
        // set user answer as combined
        UserAnswer userAnswer = getUserAnswerByAnswer(answer);
        userAnswer.setCombinedAnswer(true);
        userAnswer.setName(expression);
        return userAnswer;
    }

    public String validateExpression(String expression, int startIndex,
            int endIndex, Map<String, String> operatorMap)
            throws WdkModelException {
        // construct operand map
        Map<String, Answer> operandMap = buildOperandMap();

        // construct BooleanQuestionNode
        BooleanExpression be = new BooleanExpression(model);
        try {
            be.parseExpression(expression, operandMap, operatorMap);
        } catch (WdkUserException ue) {
            return ue.getMessage();
        }
        return null;
    }

    private Map<String, Answer> buildOperandMap() {
        Map<String, Answer> operandMap = new HashMap<String, Answer>();
        for (int answerID : userAnswers.keySet()) {
            UserAnswer userAnswer = userAnswers.get(answerID);
            operandMap.put("#" + answerID, userAnswer.getAnswer());
            operandMap.put(userAnswer.getName(), userAnswer.getAnswer());
        }
        return operandMap;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();

        sb.append("==================================");
        sb.append(newline);
        sb.append("UserID=" + userID);
        int size = (userAnswers != null) ? userAnswers.size() : 0;
        sb.append("\t#Answers=" + size);
        sb.append(newline);
        if (userAnswers != null) {
            sb.append("----------------------------------");
            sb.append(newline);
            sb.append("ID\tType\t\t\tName");
            sb.append(newline);
            for (UserAnswer answer : userAnswers.values()) {
                sb.append(answer.getAnswerID());
                sb.append("\t" + answer.getType());
                sb.append("\t" + answer.getName());
                sb.append(newline);
            }
        }
        sb.append(newline);
        return sb.toString();
    }

    private void saveHistory(UserAnswer userAnswer)
            throws WdkModelException, WdkUserException {
        int historyID = userAnswer.getAnswerID();
        Integer tempID = userAnswer.getAnswer().getDatasetId();
        try {
            if (tempID == null) {
                // read the record to make sure the id is initialized
                userAnswer.getAnswer().getResultSize();
                tempID = userAnswer.getAnswer().getDatasetId();
            }
            int datasetID = tempID.intValue();

            String historyTableName = model.resultFactory.getHistoryTableName();
            DataSource dataSource = model.platform.getDataSource();

            // check if the same history ID has been used; if so, replace the
            // old
            // one; otherwise, insert a new record
            StringBuffer sb = new StringBuffer("SELECT * FROM ");
            sb.append(historyTableName);
            sb.append(" WHERE ");
            sb.append(ResultFactory.FIELD_USER_ID);
            sb.append("='");
            sb.append(userID);
            sb.append("' AND ");
            sb.append(ResultFactory.FIELD_HISTORY_ID);
            sb.append("=");
            sb.append(historyID);

            ResultSet rs = SqlUtils.getResultSet(dataSource, sb.toString());
            sb.delete(0, sb.length());
            if (rs.next()) { // has existing history, replace old one
                sb.append("UPDATE ");
                sb.append(historyTableName);
                sb.append(" SET ");
                sb.append(ResultFactory.FIELD_DATASET_ID);
                sb.append("=");
                sb.append(datasetID);
                sb.append(" WHERE ");
                sb.append(ResultFactory.FIELD_USER_ID);
                sb.append("='");
                sb.append(userID);
                sb.append("' AND ");
                sb.append(ResultFactory.FIELD_HISTORY_ID);
                sb.append("=");
                sb.append(historyID);
            } else { // no matched history, insert a new one
                sb.append("INSERT INTO ");
                sb.append(historyTableName);
                sb.append(" (");
                sb.append(ResultFactory.FIELD_USER_ID);
                sb.append(", ");
                sb.append(ResultFactory.FIELD_HISTORY_ID);
                sb.append(", ");
                sb.append(ResultFactory.FIELD_DATASET_ID);
                sb.append(") VALUES ('");
                sb.append(userID);
                sb.append("', ");
                sb.append(historyID);
                sb.append(", ");
                sb.append(datasetID);
                sb.append(")");
            }
            // execute update/insert
            SqlUtils.executeUpdate(dataSource, sb.toString());
        } catch (SQLException ex) {
            logger.finest("Got an SQLException: " + ex.toString());
            throw new WdkUserException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        // in this place the user object removes the query histories
        clearUserAnswers();
        super.finalize();
    }
}
