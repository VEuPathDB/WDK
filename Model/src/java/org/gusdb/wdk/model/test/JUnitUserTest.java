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
        suite.addTest(new JUnitUserTest("testDeleteUserAnswer"));
        suite.addTest(new JUnitUserTest("testClearUserAnswers"));
        suite.addTest(new JUnitUserTest("testGetUserAnswers"));
        suite.addTest(new JUnitUserTest("testGetUserAnswerByID"));
        suite.addTest(new JUnitUserTest("testGetUserAnswerByName"));
        suite.addTest(new JUnitUserTest("testGetUserAnswerByAnswer"));
        suite.addTest(new JUnitUserTest("testRenameUserAnswer"));
        suite.addTest(new JUnitUserTest("testCombineUserAnswersIntIntString"));
        suite.addTest(new JUnitUserTest("testCombineUserAnswersString"));
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
                user.clearUserAnswers();

                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getUserAnswerByAnswer(answer);
                    assertNotNull(userAnswer);
                }

                // test on duplicates situation
                UserAnswer uans = user.getUserAnswers()[0];
                user.addAnswer(uans.getAnswer());
                UserAnswer uansnew = user.getUserAnswerByAnswer(uans.getAnswer());
                assertEquals(uans, uansnew);

                // the answer list should be full now
                assertEquals(answers.length, user.getUserAnswers().length);
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
    public void testDeleteUserAnswer() {
        // first add answers to the user
        try {
            // now all users ask all questions
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearUserAnswers();

                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getUserAnswerByAnswer(answer);
                    assertNotNull(userAnswer);

                    answerIDs.add(userAnswer.getAnswerID());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getUserAnswers().length);

                // now try to delete answers one by one
                for (int answerID : answerIDs) {
                    user.deleteUserAnswer(answerID);
                }
                // all answers should be deleted
                assertEquals(0, user.getUserAnswers().length);

                // now try on invalid answers
                for (int answerID : answerIDs) {
                    try {
                        user.deleteUserAnswer(answerID);
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
    public void testClearUserAnswers() {
        try {
            // clear all answers from all users
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearUserAnswers();

                // add answers into history
                for (Answer answer : answers)
                    user.addAnswer(answer);

                // the answer list should be full now
                assertEquals(answers.length, user.getUserAnswers().length);

                user.clearUserAnswers();
                // all answers should be deleted
                assertEquals(0, user.getUserAnswers().length);
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getUserAnswers()'
     */
    public void testGetUserAnswers() {
        try {
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearUserAnswers();

                // add answers into history
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getUserAnswers().length);
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
    public void testGetUserAnswerByID() {
        try {
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearUserAnswers();

                // add answers into history
                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getUserAnswerByAnswer(answer);
                    answerIDs.add(userAnswer.getAnswerID());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getUserAnswers().length);

                for (int answerID : answerIDs) {
                    UserAnswer userAnswer = user.getUserAnswerByID(answerID);
                    assertNotNull(userAnswer);
                }

                // now test on invalid situations
                user.clearUserAnswers();
                for (int answerID : answerIDs) {
                    try {
                        user.getUserAnswerByID(answerID);
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
    public void testGetUserAnswerByName() {
        try {
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearUserAnswers();

                // add answers into history
                List<String> answerNames = new ArrayList<String>();
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getUserAnswerByAnswer(answer);
                    answerNames.add(userAnswer.getName());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getUserAnswers().length);

                for (String name : answerNames) {
                    UserAnswer userAnswer = user.getUserAnswerByName(name);
                    assertNotNull(userAnswer);
                }

                // now test on invalid situations
                user.clearUserAnswers();
                for (String name : answerNames) {
                    try {
                        user.getUserAnswerByName(name);
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
    public void testGetUserAnswerByAnswer() {
        try {
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearUserAnswers();

                // add answers into history
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getUserAnswers().length);

                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.getUserAnswerByAnswer(answer);
                    assertNotNull(userAnswer);
                }

                // now test on invalid situations
                user.clearUserAnswers();
                for (Answer answer : answers) {
                    try {
                        user.getUserAnswerByAnswer(answer);
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
    public void testRenameUserAnswer() {
        try {
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);
                user.clearUserAnswers();

                // add answers into history
                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    user.addAnswer(answer);
                    UserAnswer userAnswer = user.getUserAnswerByAnswer(answer);
                    answerIDs.add(userAnswer.getAnswerID());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getUserAnswers().length);

                // now test on renaming answers
                int idx = 1;
                for (int answerID : answerIDs) {
                    String newName = "answer_" + idx;
                    UserAnswer oldAnswer = user.getUserAnswerByID(answerID);
                    user.renameUserAnswer(answerID, newName);
                    UserAnswer newAnswer = user.getUserAnswerByName(newName);

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
                        user.renameUserAnswer(answerID, newName);
                        assertTrue(false);
                    } catch (WdkUserException ex) {
                        // ex.printStackTrace();
                    }
                    idx++;
                }

                // now test on non-existing answers
                user.clearUserAnswers();
                idx = 1;
                for (int answerID : answerIDs) {
                    String newName = "answer_" + idx;
                    try {
                        user.renameUserAnswer(answerID, newName);
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
    public void testCombineUserAnswersIntIntString() {
        try {
            // get the user
            String userID = "user_0";
            User user = wdkModel.getUser(userID);

            assertNotNull(user);
            user.clearUserAnswers();

            // add answers into history
            List<Integer> answerIDs = new ArrayList<Integer>();
            for (Answer answer : answers) {
                user.addAnswer(answer);
                UserAnswer userAnswer = user.getUserAnswerByAnswer(answer);
                answerIDs.add(userAnswer.getAnswerID());
            }

            // the answer list should be full now
            assertEquals(answers.length, user.getUserAnswers().length);

            // try to combine answers of the same type
            for (int firstID : answerIDs) {
                for (int secondID : answerIDs) {
                    UserAnswer firstAnswer = user.getUserAnswerByID(firstID);
                    UserAnswer secondAnswer = user.getUserAnswerByID(secondID);
                    String firstType = firstAnswer.getType();
                    String secondType = secondAnswer.getType();
                    if (firstType.equalsIgnoreCase(secondType)) {
                        // test union
                        UserAnswer userAnswer = user.combineUserAnswers(
                                firstID, secondID, "union", 1, 20);
                        assertNotNull(userAnswer);
                        // System.out.println(userAnswer);
                        // test intersect
                        userAnswer = user.combineUserAnswers(firstID, secondID,
                                "intersect", 1, 20);
                        assertNotNull(userAnswer);
                        // System.out.println(userAnswer);
                        // test except
                        userAnswer = user.combineUserAnswers(firstID, secondID,
                                "minus", 1, 20);
                        assertNotNull(userAnswer);
                        // System.out.println(userAnswer);
                    } else { // test on invalid combinations
                        try {
                            user.combineUserAnswers(firstID, secondID, "union",
                                    1, 20);
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
    public void testCombineUserAnswersString() {
        try {
            // get the user
            String userID = "user_0";
            User user = wdkModel.getUser(userID);

            assertNotNull(user);
            user.clearUserAnswers();

            // add answers into history, and also store IDs by type
            Map<String, List<Integer>> groups = new HashMap<String, List<Integer>>();
            for (Answer answer : answers) {
                user.addAnswer(answer);
                UserAnswer userAnswer = user.getUserAnswerByAnswer(answer);

                // store them by type
                String type = userAnswer.getType();
                if (!groups.containsKey(type))
                    groups.put(type, new ArrayList<Integer>());
                groups.get(type).add(userAnswer.getAnswerID());
            }

            // the answer list should be full now
            assertEquals(answers.length, user.getUserAnswers().length);

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
                user.renameUserAnswer(answerID, name[i]);
            }

            // TEST
            // System.out.println("Testbed Size: " + answerIDs.size());

            // write the test cases for it
            UserAnswer result;
            if (answerIDs.size() >= 2) {
                result = user.combineUserAnswers(id[0] + " UNION " + id[1], 1,
                        20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineUserAnswers("\"" + name[0] + "\" UNION \""
                        + name[1] + "\"", 1, 20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineUserAnswers("\"" + name[0] + "\" UNION "
                        + id[1], 1, 20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineUserAnswers(name[0] + " UNION " + name[1],
                        1, 20);
                assertNotNull(result);
                // System.out.println(result);
            }
            if (answerIDs.size() >= 3) {
                result = user.combineUserAnswers(id[0] + " UNION " + id[1]
                        + " INTERSECT " + id[2], 1, 20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineUserAnswers(id[0] + " MINUS (\"" + name[1]
                        + "\" INTERSECT " + id[2] + ")", 1, 20);
                assertNotNull(result);
                // System.out.println(result);
            }
            if (answerIDs.size() >= 4) {
                result = user.combineUserAnswers("(" + id[0] + " INTERSECT "
                        + id[1] + ") INTERSECT (" + id[2] + " INTERSECT "
                        + id[3] + ")", 1, 20);
                assertNotNull(result);
                // System.out.println(result);

                result = user.combineUserAnswers("((" + id[0] + " INTERSECT "
                        + id[1] + ") INTERSECT " + id[2] + ") INTERSECT "
                        + id[3], 1, 20);
                assertNotNull(result);
                // System.out.println(result);
            }

            // now test some of invalid expressions
            try { // miss the other part of parenthese
                result = user.combineUserAnswers("(#1 UNION #2) MINUS #3)", 1,
                        20);
                assertTrue(false);
            } catch (WdkUserException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            } catch (WdkModelException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            }
            try { // miss the other part of double quote
                result = user.combineUserAnswers("\"ans_1\" UNION \"ans_2", 1,
                        20);
                assertTrue(false);
            } catch (WdkUserException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            } catch (WdkModelException ex) {
                // ex.printStackTrace();
                // System.err.println(ex);
            }
            try { // question not found
                result = user.combineUserAnswers("\"invalid_ans\" UNION ans_2",
                        1, 20);
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