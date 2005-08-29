/**
 * 
 */
package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.WdkModel;

import junit.framework.TestCase;

/**
 * @author Jerric
 * @created Aug 29, 2005
 */
public class JUnitUserTest extends TestCase {

    private WdkModel model;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JUnitUserTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        // load WdkModel the first time
        if (model == null) model = WdkModel.construct("toyModel");
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getUserID()'
     */
    public void testGetUserID() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.addAnswer(Answer)'
     */
    public void testAddAnswer() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.deleteAnswer(int)'
     */
    public void testDeleteAnswer() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswers()'
     */
    public void testGetAnswers() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswerByID(int)'
     */
    public void testGetAnswerByID() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswerByName(String)'
     */
    public void testGetAnswerByName() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.renameAnswer(int, String)'
     */
    public void testRenameAnswer() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.combineAnswers(int, int,
     * String)'
     */
    public void testCombineAnswersIntIntString() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.combineAnswers(String)'
     */
    public void testCombineAnswersString() {
    // TODO Auto-generated method stub

    }

}
