/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class UserTest {

    private WdkModel wdkModel;
    private UserFactory userFactory;

    public UserTest() throws Exception {
        this.wdkModel = UnitTestHelper.getModel();
        this.userFactory = wdkModel.getUserFactory();
    }

    @Test
    public void testCreateGuest() throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException {
        User guest1 = userFactory.createGuestUser();
        Assert.assertTrue("guest1", guest1.isGuest());

        // try another, different guest
        User guest2 = userFactory.createGuestUser();
        Assert.assertTrue("guest2", guest2.isGuest());

        Assert.assertTrue("Different guest ids",
                guest1.getUserId() != guest2.getUserId());
        Assert.assertTrue("Different guest signatures",
                guest1.getSignature() != guest2.getSignature());
    }

    @Test
    public void testCreateRegisteredUser() throws WdkUserException,
            WdkModelException, SQLException {
        String email = "wdk-test@email";
        String firstName = "Test";
        String lastName = "User";
        User user = userFactory.getUserByEmail(email);
        if (user != null) {
          // user exists, delete first
          userFactory.deleteUser(user.getEmail());
        }

        user = userFactory.createUser(email, lastName, firstName, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null);

        Assert.assertFalse("not guest", user.isGuest());
        Assert.assertEquals("email", email, user.getEmail());
        Assert.assertEquals("first name", firstName, user.getFirstName());
        Assert.assertEquals("last name", lastName, user.getLastName());
        Assert.assertTrue("user id", user.getUserId() > 0);
    }

    @Test
    public void testGetUser() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();

        User user1 = userFactory.getUser(user.getUserId());
        Assert.assertEquals("get user by id", user, user1);

        User user2 = userFactory.getUserByEmail(user.getEmail());
        Assert.assertEquals("get user by email", user, user2);

        User user3 = userFactory.getUser(user.getSignature());
        Assert.assertEquals("get user by signature", user, user3);
    }

    @Test
    public void testDeleteUser() throws WdkUserException, WdkModelException,
            SQLException {
        String email = "wdk-test@email";
        User user = userFactory.getUserByEmail(email);
        if (user == null) {
          // user doesn't exist, create it
          user = userFactory.createUser(email, "Test", "User", null, null, null,
              null, null, null, null, null, null, null, null, null, null);
        }

        userFactory.deleteUser(user.getEmail());

        // make sure the user is gone
        try {
            userFactory.getUserByEmail(email);
            Assert.assertFalse("User still exists", true);
        }
        catch (WdkModelException ex) {
            // expected, user, doesn't exist
        }
    }

    @Test
    public void testAddSortingAttributes() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        Question question = UnitTestHelper.getNormalQuestion();
        String questionName = question.getFullName();
        AttributeField[] attributes = question.getRecordClass().getAttributeFields();
        Map<String, Boolean> columns = user.getSortingAttributes(questionName);
        
        int length = Math.min(Utilities.SORTING_LEVEL, attributes.length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            String attrName = attributes[i].getName();
            boolean order = random.nextBoolean();
            user.addSortingAttribute(questionName, attrName, order);
            columns = user.getSortingAttributes(questionName);
            Assert.assertTrue(columns.size() > i);
            Assert.assertEquals(attrName, columns.keySet().iterator().next());
            Assert.assertEquals(order, columns.get(attrName));
        }

        for (int i = length -1 ; i >=0; i--) {
            String attrName = attributes[i].getName();
            boolean order = random.nextBoolean();
            user.addSortingAttribute(questionName, attrName, order);
            columns = user.getSortingAttributes(questionName);
            Assert.assertEquals(length, columns.size());
            Assert.assertEquals(attrName, columns.keySet().iterator().next());
            Assert.assertEquals(order, columns.get(attrName));
        }
    }
}
