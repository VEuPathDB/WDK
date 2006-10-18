/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * @author Jerric
 * @created Aug 29, 2005
 */
public class JUnitUserTest extends TestCase {

    private TestUtility utility;
    private WdkModel wdkModel;
    private SanityModel sanityModel;
    private UserFactory userFactory;
    private DatasetFactory datasetFactory;
    private Answer[] answers;
    private User user;
    private Random rand;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JUnitUserTest.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnitUserTest("testCreateHistory"));
        suite.addTest(new JUnitUserTest("testLoadHistory"));
        suite.addTest(new JUnitUserTest("testUpdateHistory"));
        suite.addTest(new JUnitUserTest("testDeleteHistory"));
        suite.addTest(new JUnitUserTest("testCombineHistory"));
        suite.addTest(new JUnitUserTest("testCreateDataset"));
        suite.addTest(new JUnitUserTest("testLoadDataset"));
        suite.addTest(new JUnitUserTest("testUpdateDataset"));
        suite.addTest(new JUnitUserTest("testDeleteDataset"));
        return suite;
    }

    /**
     * 
     */
    public JUnitUserTest() {
        super();
    }

    /**
     * @param name
     */
    public JUnitUserTest(String name) {
        super(name);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        rand = new Random(System.currentTimeMillis());
        utility = TestUtility.getInstance();
        if (wdkModel == null) {
            wdkModel = utility.getWdkModel();
            sanityModel = utility.getSanityModel();
            userFactory = wdkModel.getUserFactory();
            datasetFactory = wdkModel.getDatasetFactory();

            user = userFactory.loadUser(1);
            // user = userFactory.createGuestUser();
            // System.out.println("New user: #" + user.getUserId());
            // System.exit(0);

            answers = createAnswers(sanityModel);
        }
    }

    public void testCreateHistory() {
    // Set<Integer> ids = new HashSet<Integer>();
    // for (int i = 0; i < answers.length; i++) {
    // String qname = answers[i].getQuestion().getName();
    // try {
    // History history = user.createHistory(answers[i]);
    //
    // // print history
    // printHistory(history);
    //
    // int historyId = history.getHistoryId();
    // assertFalse(ids.contains(historyId));
    // ids.add(historyId);
    // } catch (WdkUserException ex) {
    // ex.printStackTrace();
    // assertTrue(false);
    // } catch (WdkModelException ex) {
    // System.err.println("Question failed: " + qname);
    // ex.printStackTrace();
    // assertTrue(false);
    // }
    // }
    }

    public void testLoadHistory() {
    // try {
    // // test to load all histories
    // History[] histories = user.getHistories();
    // int[] histIds = new int[histories.length];
    //
    // for (int i = 0; i < histories.length; i++) {
    // History history = histories[i];
    //
    // // print history
    // printHistory(history);
    //
    // histIds[i] = history.getHistoryId();
    // }
    //
    // // test to load history one by one
    // for (int i = 0; i < histIds.length; i++) {
    // // int index = rand.nextInt(histIds.length);
    // int index = i;
    // int histId = histIds[index];
    // History history = user.getHistory(histId);
    //
    // printHistory(history);
    //
    // assertEquals(histId, history.getHistoryId());
    // }
    // } catch (WdkUserException ex) {
    // ex.printStackTrace();
    // assertTrue(false);
    // } catch (WdkModelException ex) {
    // ex.printStackTrace();
    // assertTrue(false);
    // }
    }

    public void testUpdateHistory() {
    // try {
    // History[] histories = user.getHistories();
    //
    // // update the history names
    // for (History history : histories) {
    // int histId = history.getHistoryId();
    // String name = "History_" + histId + "_" + rand.nextInt(10000);
    // history.setCustomName(name);
    // history.update();
    //
    // // then load the history from the database and print it
    // history = user.getHistory(histId);
    // printHistory(history);
    // }
    // } catch (WdkUserException ex) {
    // ex.printStackTrace();
    // assertTrue(false);
    // } catch (WdkModelException ex) {
    // ex.printStackTrace();
    // assertTrue(false);
    // }
    }

    public void testCombineHistory() {
//        try {
//            History hist1 = user.getHistory(1);
//            History hist2 = user.getHistory(2);
//            History hist9 = user.getHistory(9);
//
//            // print the base history
//            printHistory(hist1);
//            printHistory(hist2);
//            printHistory(hist9);
//
//            History history = user.combineHistory("(1 OR 2) OR 9");
//            printHistory(history);
//
//        } catch (WdkUserException ex) {
//            ex.printStackTrace();
//            assertTrue(false);
//        } catch (WdkModelException ex) {
//            ex.printStackTrace();
//            assertTrue(false);
//        }
    }

    public void testDeleteHistory() {
        int historyId = 14;
        try {
            user.deleteHistory(historyId);
        } catch (WdkUserException ex) {
            ex.printStackTrace();
            assertTrue(false);
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
        
        // the history shouldn't be found
        try {
            History history = user.getHistory(historyId);
            assertNull(history);
        } catch (WdkUserException ex) {
            // ex.printStackTrace();
            System.out.println("Expected: " + ex.toString());
            assertTrue(true);
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public void testCreateDataset() {

    }

    public void testLoadDataset() {

    }

    public void testUpdateDataset() {

    }

    public void testDeleteDataset() {

    }

    private Answer[] createAnswers(SanityModel sanityModel)
            throws WdkUserException, WdkModelException {
        // TEST
        System.out.println("Creating answers....");

        // get sanity questions
        SanityQuestion[] questions = sanityModel.getAllSanityQuestions();

        assertTrue(questions.length > 0);
        List<Answer> answers = new ArrayList<Answer>();
        // choose a subset of questions only
        for (int i = 0; i < 10; i++) {
            // int index = rand.nextInt(questions.length);
            int index = i;

            // get model question from sanity question
            Reference questionRef = new Reference(questions[index].getRef());
            QuestionSet questionSet = wdkModel.getQuestionSet(questionRef.getSetName());
            Question question = questionSet.getQuestion(questionRef.getElementName());

            // run question
            Answer answer = question.makeAnswer(
                    questions[index].getParamHash(),
                    questions[index].getPageStart(),
                    questions[index].getPageEnd());
            answers.add(answer);
        }
        Answer[] array = new Answer[answers.size()];
        answers.toArray(array);

        // TEST
        System.out.println(array.length + " answers are created.");

        return array;
    }

    private void printHistory(History history) throws WdkModelException,
            WdkUserException {
        assertNotNull(history);

        DateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

        // print out the history info
        System.out.println("-----------------------------------------");
        System.out.println("History #" + history.getHistoryId() + " - '"
                + history.getCustomName() + "' ("
                + format.format(history.getLastRunTime()) + ") EST="
                + history.getEstimateSize());

        // print out the answer
        Answer answer = history.getAnswer();
        assertNotNull(answer);
        System.out.println(answer.printAsTable());
    }
}
