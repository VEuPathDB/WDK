/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.User;
import org.gusdb.wdk.model.UserAnswer;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * @author Jerric
 * @created Aug 29, 2005
 */
public class JUnitUserTest extends TestCase {

    private static final int NUM_USERS = 2;

    private TestUtility utility;
    private WdkModel wdkModel;
    private SanityModel sanityModel;
    private Answer[] answers;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JUnitUserTest.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnitUserTest("testGetUserID"));
        suite.addTest(new JUnitUserTest("testAddAnswer"));
        suite.addTest(new JUnitUserTest("testDeleteAnswer"));
        suite.addTest(new JUnitUserTest("testClearAnswers"));
        suite.addTest(new JUnitUserTest("testGetAnswers"));
        suite.addTest(new JUnitUserTest("testGetAnswerByID"));
        suite.addTest(new JUnitUserTest("testGetAnswerByName"));
        suite.addTest(new JUnitUserTest("testGetAnswerByAnswer"));
        suite.addTest(new JUnitUserTest("testRenameAnswer"));
        suite.addTest(new JUnitUserTest("testCombineAnswersIntIntString"));
        suite.addTest(new JUnitUserTest("testCombineAnswersString"));
        return suite;
    }

    /**
     * 
     */
    public JUnitUserTest() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param name
     */
    public JUnitUserTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        utility = TestUtility.getInstance();
        wdkModel = utility.getWdkModel();
        sanityModel = utility.getSanityModel();

        answers = createAnswers(sanityModel);

        // test must first create some users, and then get the ID of that user;
        // assuming add user function has been tested in model junit test
        for (int i = 0; i < NUM_USERS; i++) {
            String userID = "user_" + i;
            wdkModel.createUser(userID);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getUserID()'
     */
    public void testGetUserID() {
        // get existing users
        for (int i = 0; i < NUM_USERS; i++) {
            String userID = "user_" + i;
            User user = wdkModel.getUser(userID);
            assertNotNull(user);
            // System.out.println(user);
        }

        // get non-existing users
        for (int i = NUM_USERS + 1; i < NUM_USERS * 2; i++) {
            String userID = "user_" + i;
            User user = wdkModel.getUser(userID);
            assertNull(user);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.addAnswer(Answer)'
     */
    public void testAddAnswer() {
        // create answers for those questions
        try {
            // now all users ask all questions
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearAnswers();

                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getAnswerByAnswer(answer);
                    assertNotNull(userAnswer);
                }

                // test on duplicates situation
                UserAnswer uans = user.getAnswers()[0];
                user.addAnswer(uans.getAnswer());
                UserAnswer uansnew = user.getAnswerByAnswer(uans.getAnswer());
                assertEquals(uans, uansnew);

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);
                // System.out.println(user);
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.deleteAnswer(int)'
     */
    public void testDeleteAnswer() {
        // first add answers to the user
        try {
            // now all users ask all questions
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearAnswers();

                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getAnswerByAnswer(answer);
                    assertNotNull(userAnswer);

                    answerIDs.add(userAnswer.getAnswerID());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);

                // now try to delete answers one by one
                for (int answerID : answerIDs) {
                    user.deleteAnswer(answerID);
                }
                // all answers should be deleted
                assertEquals(0, user.getAnswers().length);

                // now try on invalid answers
                for (int answerID : answerIDs) {
                    try {
                        user.deleteAnswer(answerID);
                        assertTrue(false);
                    } catch (WdkUserException ex) {
                        // TODO Auto-generated catch block
                        // ex.printStackTrace();
                        // System.err.println(ex);
                    }
                }
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }

        // delete all answers from all users
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.clearAnswers()'
     */
    public void testClearAnswers() {
        try {
            // clear all answers from all users
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearAnswers();

                // add answers into history
                for (Answer answer : answers)
                    user.addAnswer(answer);

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);

                user.clearAnswers();
                // all answers should be deleted
                assertEquals(0, user.getAnswers().length);
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswers()'
     */
    public void testGetAnswers() {
        try {
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearAnswers();

                // add answers into history
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswerByID(int)'
     */
    public void testGetAnswerByID() {
        try {
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearAnswers();

                // add answers into history
                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getAnswerByAnswer(answer);
                    answerIDs.add(userAnswer.getAnswerID());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);

                for (int answerID : answerIDs) {
                    UserAnswer userAnswer = user.getAnswerByID(answerID);
                    assertNotNull(userAnswer);
                }

                // now test on invalid situations
                user.clearAnswers();
                for (int answerID : answerIDs) {
                    try {
                        user.getAnswerByID(answerID);
                        assertTrue(false);
                    } catch (WdkUserException ex) {
                        // TODO Auto-generated catch block
                        // ex.printStackTrace();
                        // System.err.println(ex);
                    }
                }
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswerByName(String)'
     */
    public void testGetAnswerByName() {
        try {
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearAnswers();

                // add answers into history
                List<String> answerNames = new ArrayList<String>();
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getAnswerByAnswer(answer);
                    answerNames.add(userAnswer.getName());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);

                for (String name : answerNames) {
                    UserAnswer userAnswer = user.getAnswerByName(name);
                    assertNotNull(userAnswer);
                }

                // now test on invalid situations
                user.clearAnswers();
                for (String name : answerNames) {
                    try {
                        user.getAnswerByName(name);
                        assertTrue(false);
                    } catch (WdkUserException ex) {
                        // TODO Auto-generated catch block
                        // ex.printStackTrace();
                        // System.err.println(ex);
                    }
                }
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswerByName(String)'
     */
    public void testGetAnswerByAnswer() {
        try {
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearAnswers();

                // add answers into history
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);

                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.getAnswerByAnswer(answer);
                    assertNotNull(userAnswer);
                }

                // now test on invalid situations
                user.clearAnswers();
                for (Answer answer : answers) {
                    try {
                        user.getAnswerByAnswer(answer);
                        assertTrue(false);
                    } catch (WdkUserException ex) {
                        // TODO Auto-generated catch block
                        // ex.printStackTrace();
                        // System.err.println(ex);
                    }
                }
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.renameAnswer(int, String)'
     */
    public void testRenameAnswer() {
        try {
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearAnswers();

                // add answers into history
                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getAnswerByAnswer(answer);
                    answerIDs.add(userAnswer.getAnswerID());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);

                // now test on renaming answers
                int idx = 1;
                for (int answerID : answerIDs) {
                    String newName = "answer_" + idx;
                    UserAnswer oldAnswer = user.getAnswerByID(answerID);
                    user.renameAnswer(answerID, newName);
                    UserAnswer newAnswer = user.getAnswerByName(newName);

                    assertEquals(oldAnswer, newAnswer);
                    // System.out.println(newAnswer);
                    idx++;
                }

                // now test on duplicates situations
                idx = 1;
                for (int answerID : answerIDs) {
                    String newName = "answer_" + (answerIDs.size() - idx + 1);
                    // make sure I don't change a name of its own
                    if (idx == (answerIDs.size() - idx + 1)) continue;
                    try {
                        user.renameAnswer(answerID, newName);
                        assertTrue(false);
                    } catch (WdkUserException ex) {
                        // ex.printStackTrace();
                    }
                    idx++;
                }

                // now test on non-existing answers
                user.clearAnswers();
                idx = 1;
                for (int answerID : answerIDs) {
                    String newName = "answer_" + idx;
                    try {
                        user.renameAnswer(answerID, newName);
                        assertTrue(false);
                    } catch (WdkUserException ex) {
                        // ex.printStackTrace();
                    }
                    idx++;
                }
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.combineAnswers(int, int,
     * String)'
     */
    public void testCombineAnswersIntIntString() {
        try {
            // get the user
            String userID = "user_0";
            User user = wdkModel.getUser(userID);

            assertNotNull(user);
            user.clearAnswers();

            // add answers into history
            List<Integer> answerIDs = new ArrayList<Integer>();
            for (Answer answer : answers) {
                user.addAnswer(answer);
                UserAnswer userAnswer = user.getAnswerByAnswer(answer);
                answerIDs.add(userAnswer.getAnswerID());
            }

            // the answer list should be full now
            assertEquals(answers.length, user.getAnswers().length);

            // try to combine answers of the same type
            for (int firstID : answerIDs) {
                for (int secondID : answerIDs) {
                    UserAnswer firstAnswer = user.getAnswerByID(firstID);
                    UserAnswer secondAnswer = user.getAnswerByID(secondID);
                    String firstType = firstAnswer.getType();
                    String secondType = secondAnswer.getType();
                    if (firstType.equalsIgnoreCase(secondType)) {
                        // test union
                        UserAnswer userAnswer = user.combineAnswers(firstID,
                                secondID, "union", 1, 20);
                        assertNotNull(userAnswer);
                        // System.out.println(userAnswer);
                        // test intersect
                        userAnswer = user.combineAnswers(firstID, secondID,
                                "intersect", 1, 20);
                        assertNotNull(userAnswer);
                        // System.out.println(userAnswer);
                        // test except
                        userAnswer = user.combineAnswers(firstID, secondID,
                                "minus", 1, 20);
                        assertNotNull(userAnswer);
                        // System.out.println(userAnswer);
                    } else { // test on invalid combinations
                        try {
                            user.combineAnswers(firstID, secondID, "union", 1,
                                    20);
                            assertTrue(false);
                        } catch (WdkUserException ex) {
                            // ex.printStackTrace();
                        } catch (WdkModelException ex) {
                            // ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.combineAnswers(String)'
     */
    public void testCombineAnswersString() {
        try {
            // get the user
            String userID = "user_0";
            User user = wdkModel.getUser(userID);

            assertNotNull(user);
            user.clearAnswers();

            // add answers into history, and also store IDs by type
            Map<String, List<Integer>> groups = new HashMap<String, List<Integer>>();
            for (Answer answer : answers) {
                user.addAnswer(answer);
                UserAnswer userAnswer = user.getAnswerByAnswer(answer);

                // store them by type
                String type = userAnswer.getType();
                if (!groups.containsKey(type))
                    groups.put(type, new ArrayList<Integer>());
                groups.get(type).add(userAnswer.getAnswerID());
            }

            // the answer list should be full now
            assertEquals(answers.length, user.getAnswers().length);

            // now get the list of largest number of answers
            int max = 0;
            List<Integer> answerIDs = null;
            for (List<Integer> group : groups.values()) {
                if (group.size() > max) {
                    answerIDs = group;
                    max = group.size();
                }
            }
            assertTrue(max > 0);

            // now change the answer name to be "ans_x", where x is the id of it
            String[] name = new String[answerIDs.size()];
            String[] id = new String[answerIDs.size()];
            for (int i = 0; i < answerIDs.size(); i++) {
                int answerID = answerIDs.get(i);
                id[i] = "#" + answerID;
                name[i] = "ans_" + answerID;
                user.renameAnswer(answerID, name[i]);
            }

            // TEST
            //System.out.println("Testbed Size: " + answerIDs.size());

            // write the test cases for it
            UserAnswer result;
            if (answerIDs.size() >= 2) {
                result = user.combineAnswers(id[0] + " UNION " + id[1], 1, 20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineAnswers("\"" + name[0] + "\" UNION \""
                        + name[1] + "\"", 1, 20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineAnswers("\"" + name[0] + "\" UNION "
                        + id[1], 1, 20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineAnswers(name[0] + " UNION " + name[1], 1,
                        20);
                assertNotNull(result);
                // System.out.println(result);
            }
            if (answerIDs.size() >= 3) {
                result = user.combineAnswers(id[0] + " UNION " + id[1]
                        + " INTERSECT " + id[2], 1, 20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineAnswers(id[0] + " MINUS (\"" + name[1]
                        + "\" INTERSECT " + id[2] + ")", 1, 20);
                assertNotNull(result);
                // System.out.println(result);
            }
            if (answerIDs.size() >= 4) {
                result = user.combineAnswers("(" + id[0] + " INTERSECT "
                        + id[1] + ") INTERSECT (" + id[2] + " INTERSECT "
                        + id[3] + ")", 1, 20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineAnswers("((" + id[0] + " INTERSECT "
                        + id[1] + ") INTERSECT " + id[2] + ") INTERSECT "
                        + id[3], 1, 20);
                assertNotNull(result);
                // System.out.println(result);
            }

            // now test some of invalid expressions
            try { // miss the other part of parenthese
                result = user.combineAnswers("(#1 UNION #2) MINUS #3)", 1, 20);
                assertTrue(false);
            } catch (WdkUserException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            } catch (WdkModelException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            }
            try { // miss the other part of double quote
                result = user.combineAnswers("\"ans_1\" UNION \"ans_2", 1, 20);
                assertTrue(false);
            } catch (WdkUserException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            } catch (WdkModelException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            }
            try { // question not found
                result = user.combineAnswers("\"invalid_ans\" UNION ans_2", 1,
                        20);
                assertTrue(false);
            } catch (WdkUserException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            } catch (WdkModelException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    private Answer[] createAnswers(SanityModel sanityModel)
            throws WdkUserException, WdkModelException {
        // get sanity questions
        SanityQuestion[] questions = sanityModel.getAllSanityQuestions();

        assertTrue(questions.length > 0);
        Answer[] answers = new Answer[questions.length];
        for (int i = 0; i < questions.length; i++) {
            // get model question from sanity question
            Reference questionRef = new Reference(questions[i].getRef());
            QuestionSet questionSet = wdkModel.getQuestionSet(questionRef.getSetName());
            Question question = questionSet.getQuestion(questionRef.getElementName());

            // run question
            answers[i] = question.makeAnswer(questions[i].getParamHash(),
                    questions[i].getPageStart(), questions[i].getPageEnd());
        }
        return answers;
    }
}