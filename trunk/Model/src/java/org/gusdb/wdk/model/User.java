package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * A wdk model user. The WdkModel has a list of Users, and maintains unique
 * identifiers for them. These will likely be the session id for non-persistent
 * users.
 */
public class User {

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

    public void addAnswer(Answer answer) {

        try {
            getAnswerByAnswerFuzzy(answer);
            // answer exists, return
            return;
        } catch (WdkUserException ex) {
            // TODO Auto-generated catch block
            // ex.printStackTrace();
            // System.err.println(ex);
        }

        answerIndex++;
        UserAnswer userAnswer = new UserAnswer(userID, answerIndex, answer);

        // initialize userAnswers map
        if (userAnswers == null)
            userAnswers = new HashMap<Integer, UserAnswer>();
        userAnswers.put(answerIndex, userAnswer);
    }

    public void deleteAnswer(int answerId) throws WdkUserException {
        if (userAnswers == null)
            throw new WdkUserException(
                    "The answer specified by the given ID doesn't exist!");
        UserAnswer answer = userAnswers.remove(answerId);
        if (answer == null)
            throw new WdkUserException(
                    "The answer specified by the given ID doesn't exist!");
        if (userAnswers.isEmpty()) userAnswers = null;
    }

    public void clearAnswers() {
        if (userAnswers != null) userAnswers.clear();
        userAnswers = null;
    }

    public UserAnswer[] getAnswers() {
        if (userAnswers == null || userAnswers.size() == 0)
            return new UserAnswer[0];
        UserAnswer[] answers = new UserAnswer[userAnswers.size()];
        userAnswers.values().toArray(answers);
        return answers;
    }

    public Map getRecordAnswerMap() {
	Map recAnsMapMap = new HashMap<String, Map>();
	if (userAnswers == null || userAnswers.size() == 0)
            return recAnsMapMap;

	for (int ansID : userAnswers.keySet()) {
	    UserAnswer usrAns = userAnswers.get(new Integer(ansID));
	    String rec = usrAns.getAnswer().getQuestion().getRecordClass().getFullName();
	    if (recAnsMapMap.get(rec) == null) {
		recAnsMapMap.put(rec, new HashMap<Integer, UserAnswer>());
	    }
	    Map recAnsMapMap1 = (Map)recAnsMapMap.get(rec);
	    recAnsMapMap1.put(new Integer(ansID), usrAns);
	}

	//wants answers in sorted arrays
	Map recAnsMap = new HashMap<String, UserAnswer[]>();
	for (Object r : recAnsMapMap.keySet()) {
	    String rec = (String)r;
	    Map recAnsMapMap1 = (Map)recAnsMapMap.get(rec);
	    List ansIDList = Arrays.asList(recAnsMapMap1.keySet().toArray());
	    Collections.sort(ansIDList);
	    Collections.reverse(ansIDList);
	    Object[] sortedAnsIDs = ansIDList.toArray();
	    Vector v = new Vector();
	    for (int i=0; i<sortedAnsIDs.length; i++) {
		v.add(recAnsMapMap1.get((Integer)sortedAnsIDs[i]));
	    }
	    UserAnswer[] sortedUsrAns = new UserAnswer[v.size()];
	    v.copyInto(sortedUsrAns);

	    recAnsMap.put(rec, sortedUsrAns);
	}

	return recAnsMap;
    }

    public UserAnswer getAnswerByID(int answerID) throws WdkUserException {
        if (userAnswers == null || !userAnswers.containsKey(answerID))
            throw new WdkUserException("The answer of ID " + answerID
                    + " does not exist!");
        return userAnswers.get(answerID);
    }

    public UserAnswer getAnswerByName(String name) throws WdkUserException {
        if (userAnswers != null) {
            for (UserAnswer answer : userAnswers.values()) {
                if (answer.getName().equalsIgnoreCase(name)) return answer;
            }
        }
        throw new WdkUserException("The answer of name " + name
                + " does not exist!");
    }

    public UserAnswer getAnswerByAnswerFuzzy(Answer answer) throws WdkUserException {
	return getAnswerByAnswer(answer, true);
    }
    public UserAnswer getAnswerByAnswer(Answer answer) throws WdkUserException {
	return getAnswerByAnswer(answer, false);
    }
    private UserAnswer getAnswerByAnswer(Answer answer, boolean ignorePage) throws WdkUserException {
        if (userAnswers != null) {
            // check if the answer exists or not
            for (UserAnswer uans : userAnswers.values()) {
                Answer ans = uans.getAnswer();
                // check question name
                String qname = ans.getQuestion().getFullName();
                if (!qname.equalsIgnoreCase(answer.getQuestion().getFullName()))
                    continue;

                // check paging number
                if (!ignorePage && (ans.startRecordInstanceI != answer.startRecordInstanceI
				    || ans.endRecordInstanceI != answer.endRecordInstanceI))
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

    public void renameAnswer(int answerID, String name) throws WdkUserException {
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

    public UserAnswer combineAnswers(int firstAnswerID, int secondAnswerID,
            String operation, int startIndex, int endIndex)
            throws  WdkModelException, WdkUserException {
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
        BooleanQuestionNode root = be.combineAnswers(sb.toString(), operandMap);

        // create a new UserAnswer
        Answer answer = root.makeAnswer(startIndex, endIndex);
        addAnswer(answer);
        return getAnswerByAnswer(answer);
    }

    public UserAnswer combineAnswers(String expression, int startIndex,
            int endIndex) throws WdkUserException, WdkModelException {
        // construct operand map
        Map<String, Answer> operandMap = buildOperandMap();

        // construct BooleanQuestionNode
        BooleanExpression be = new BooleanExpression(model);
        BooleanQuestionNode root = be.combineAnswers(expression, operandMap);

        // make answer
        Answer answer = root.makeAnswer(startIndex, endIndex);
        addAnswer(answer);
        return getAnswerByAnswer(answer);
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
}
