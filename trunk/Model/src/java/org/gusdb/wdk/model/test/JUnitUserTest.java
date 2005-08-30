/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

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

    private static final int NUM_USERS = 10;

    private TestUtility utility;
    private WdkModel wdkModel;
    private SanityModel sanityModel;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JUnitUserTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        utility = TestUtility.getInstance();
        wdkModel = utility.getWdkModel();
        sanityModel = utility.getSanityModel();

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
            Answer[] answers = createAnswers(sanityModel);

            // now all users ask all questions
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);

                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.addAnswer(answer);

                    assertNotNull(userAnswer);
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);
                System.out.println(user);
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
            Answer[] answers = createAnswers(sanityModel);

            // now all users ask all questions
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);

                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.addAnswer(answer);

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
            Answer[] answers = createAnswers(sanityModel);

            // clear all answers from all users
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);

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
            Answer[] answers = createAnswers(sanityModel);
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);

                // the answer list should be empty at the beginning
                assertEquals(0, user.getAnswers().length);

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
            Answer[] answers = createAnswers(sanityModel);
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);

                // the answer list should be empty at the beginning
                assertEquals(0, user.getAnswers().length);

                // add answers into history
                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.addAnswer(answer);
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
                    UserAnswer userAnswer = user.getAnswerByID(answerID);
                    assertNull(userAnswer);
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
            Answer[] answers = createAnswers(sanityModel);
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);

                // the answer list should be empty at the beginning
                assertEquals(0, user.getAnswers().length);

                // add answers into history
                List<String> answerNames = new ArrayList<String>();
                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.addAnswer(answer);
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
                    UserAnswer userAnswer = user.getAnswerByName(name);
                    assertNull(userAnswer);
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
            Answer[] answers = createAnswers(sanityModel);

            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);

                // the answer list should be empty at the beginning
                assertEquals(0, user.getAnswers().length);

                // add answers into history
                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.addAnswer(answer);
                    answerIDs.add(userAnswer.getAnswerID());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);

                // now test on renaming answers
                int idx = 1;
                for (int answerID : answerIDs) {
                    String newName = "answer_" + idx;
                    user.renameAnswer(answerID, newName);
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
            Answer[] answers = createAnswers(sanityModel);

            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNotNull(user);

                // the answer list should be empty at the beginning
                assertEquals(0, user.getAnswers().length);

                // add answers into history
                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.addAnswer(answer);
                    answerIDs.add(userAnswer.getAnswerID());
                }

                // the answer list should be full now
                assertEquals(answers.length, user.getAnswers().length);

                // try to combine answers of the same type
                for (int firstID : answerIDs) {
                    for (int secondID : answerIDs) {
                        String firstType = user.getAnswerByID(firstID).getType();
                        String secondType = user.getAnswerByID(secondID).getType();
                        if (firstType.equalsIgnoreCase(secondType)) {
                            // test union
                            UserAnswer userAnswer = user.combineAnswers(
                                    firstID, secondID, "union");
                            assertNotNull(userAnswer);
                            // test intersect
                            userAnswer = user.combineAnswers(firstID, secondID,
                                    "intersect");
                            assertNotNull(userAnswer);
                            // test intersect
                            userAnswer = user.combineAnswers(firstID, secondID,
                                    "intersect");
                            assertNotNull(userAnswer);
                            // test except
                            userAnswer = user.combineAnswers(firstID, secondID,
                                    "except");
                            assertNotNull(userAnswer);
                            System.out.println(userAnswer);
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
            Answer[] answers = createAnswers(sanityModel);

            // get the user
            String userID = "user_0";
            User user = wdkModel.getUser(userID);

            assertNotNull(user);

            // the answer list should be empty at the beginning
            assertEquals(0, user.getAnswers().length);

            // add answers into history, and also store IDs by type
            Map<String, List<Integer>> groups = new HashMap<String, List<Integer>>();
            for (Answer answer : answers) {
                UserAnswer userAnswer = user.addAnswer(answer);

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
            for (int answerID : answerIDs) {
                String newName = "ans_" + answerID;
                user.renameAnswer(answerID, newName);
            }

            // write the test cases for it
            UserAnswer result;
            if (answerIDs.size() >= 2) {
                result = user.combineAnswers("#1 UNION #2");
                assertNotNull(result);
                result = user.combineAnswers("#1 UNION #2");
                assertNotNull(result);
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
