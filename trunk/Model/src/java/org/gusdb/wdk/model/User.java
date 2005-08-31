package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A wdk model user. The WdkModel has a list of Users, and maintains unique
 * identifiers for them. These will likely be the session id for non-persistent
 * users.
 */
public class User {

    private static final String STUB_PREFIX = "__STUB__";

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

    public UserAnswer addAnswer(Answer answer) {
        answerIndex++;
        UserAnswer userAnswer = new UserAnswer(userID, answerIndex, answer);

        // initialize userAnswers map
        if (userAnswers == null)
            userAnswers = new HashMap<Integer, UserAnswer>();
        userAnswers.put(answerIndex, userAnswer);
        return userAnswer;
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
            String operation) throws WdkUserException, WdkModelException {
        // check if the answers exist
        if (userAnswers == null || !userAnswers.containsKey(firstAnswerID)
                || !userAnswers.containsKey(secondAnswerID))
            throw new WdkUserException(
                    "The answer specified by the given ID doesn't exist!");

        // check if the answers have the same type
        UserAnswer firstAnswer = userAnswers.get(firstAnswerID);
        UserAnswer secondAnswer = userAnswers.get(secondAnswerID);
        if (!firstAnswer.getType().equalsIgnoreCase(secondAnswer.getType()))
            throw new WdkUserException(
                    "The answers can't be combined with differen types");

        // now combine the answers using booleanQuestionNode
        BooleanQuestionNode firstChild = firstAnswer.getLeafQuestion();
        BooleanQuestionNode secondChild = secondAnswer.getLeafQuestion();

        // generate new answer for the combine questions
        BooleanQuestionNode root = BooleanQuestionNode.combine(firstChild,
                secondChild, operation, model);
        Answer answer = root.makeAnswer();

        // create a new UserAnswer
        return addAnswer(answer);
    }

    public UserAnswer combineAnswers(String expression)
            throws WdkUserException, WdkModelException {
        // replace the literals in the expression
        Map<String, String> replace = new HashMap<String, String>();
        String exp = replaceLiterals(expression, replace).trim();

        // build the BooleanQuestionNode tree
        BooleanQuestionNode root = parseBlock(exp, replace);

        // make answer
        Answer answer = root.makeAnswer();
        return addAnswer(answer);
    }

    private String replaceLiterals(String expression,
            Map<String, String> replace) throws WdkUserException {
        // literals are marked by double quotes
        StringBuffer sb = new StringBuffer();
        int mark = 0; // the first char position of current non-literals
        int stubID = 0;
        for (int i = 0; i < expression.length(); i++) {
            // check if we meet the opening double quote
            if (expression.charAt(i) == '"') {
                // output previous non-literal part
                if (i != 0) sb.append(expression.substring(mark, i));

                int start = i;
                int end = start;
                // seek for the closing double quote
                while (start == end) {
                    i++;
                    if (i >= expression.length())
                        throw new WdkUserException(
                                "The format of boolean expression is invalid: "
                                        + expression);
                    // check if it's quote
                    if (expression.charAt(i) == '"') {
                        // check if it should be escaped
                        if (i < expression.length() - 1
                                && expression.charAt(i + 1) == '"') {
                            // escaped
                            i++;
                        } else {// it's an ending of quote
                            end = i;
                        }
                    }
                }
                // now output the stub
                stubID++;
                String stub = STUB_PREFIX + Integer.toString(stubID) + "__";
                String literal = expression.substring(start, end + 1);
                replace.put(stub, literal);
                sb.append(" " + stub + " ");
                mark = end + 1;
            }
        }
        if (mark < expression.length()) sb.append(expression.substring(mark));
        return sb.toString();
    }

    private BooleanQuestionNode parseBlock(String block,
            Map<String, String> replace) throws WdkUserException,
            WdkModelException {
        // check if the expression can be divided further
        // to do so, just need to check if there're spaces or parenthese
        int spaces = block.indexOf(" ");
        int parenthese = block.indexOf("(");
        if (spaces < 0 && parenthese < 0) {
            // can't be divided further; the block must be an id or a name of
            // the Answer; id starts with '#'
            UserAnswer answer;
            if (block.charAt(0) == '#') { // an answer id
                int answerID = Integer.parseInt(block.substring(1));
                answer = getAnswerByID(answerID);
            } else { // a name of an answer, but being replaced
                String name = replace.get(block);
                assert (name != null) : block;
                answer = getAnswerByName(name);
            }
            return answer.getLeafQuestion();
        }
        // otherwise, need to divide further
        // check the root operation
        int pos;
        if (block.charAt(0) == '(') {
            int openBlock = 1;
            pos = 1;
            // find the paired closing parenthese
            while (pos < block.length() && openBlock > 0) {
                if (block.charAt(pos) == '(') openBlock++;
                else if (block.charAt(pos) == ')') openBlock--;
                pos++;
            }
            if (openBlock > 0)
                throw new WdkUserException(
                        "The format of boolean expression is invalid!");
        } else { // no parenthese, then must be separated with space
            pos = block.indexOf(" ");
        }
        // grab the left piece
        String leftPiece = block.substring(0, pos).trim();
        // remove parenthese is necessary
        int bound = leftPiece.length() - 1;
        if (leftPiece.charAt(0) == '(' && leftPiece.charAt(bound) == ')')
            leftPiece = leftPiece.substring(1, bound).trim();

        // grab operation
        String remain = block.substring(pos + 1).trim();
        int end = remain.indexOf(" ");
        String operation = remain.substring(0, end).trim();

        // grab right piece
        String rightPiece = remain.substring(end + 1).trim();
        // remove parenthese is necessary
        bound = rightPiece.length() - 1;
        if (rightPiece.charAt(0) == '(' && rightPiece.charAt(bound) == ')')
            rightPiece = rightPiece.substring(1, bound).trim();

        // create BooleanQuestioNode for each piece
        BooleanQuestionNode firstNode = parseBlock(leftPiece, replace);
        BooleanQuestionNode secondNode = parseBlock(rightPiece, replace);

        // combine left & right sub-tree to form a new tree
        return BooleanQuestionNode.combine(firstNode, secondNode, operation,
                model);
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