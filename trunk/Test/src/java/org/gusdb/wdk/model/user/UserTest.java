/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
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
        try {
            User user = userFactory.getUserByEmail(email);
            // user exists, delete first
            userFactory.deleteUser(user.getEmail());
        } catch (WdkUserException ex) {
            // user doesn't exist do nothing
        }

        User user = userFactory.createUser(email, lastName, firstName, null,
                null, null, null, null, null, null, null, null, null, null,
                null);

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
        User user;
        try {
            user = userFactory.getUserByEmail(email);
        } catch (WdkUserException ex) {
            // user doesn't exist, create it
            user = userFactory.createUser(email, "Test", "User", null, null,
                    null, null, null, null, null, null, null, null, null, null);
        }

        userFactory.deleteUser(user.getEmail());

        // make sure the user is gone
        try {
            userFactory.getUserByEmail(email);
            Assert.assertFalse("User still exists", true);
        } catch (WdkUserException ex) {
            // expected, user, doesn't exist
        }
    }
    
    @Test
    public void testGlobalPreference() {
        
        
        
        Assert.assertTrue(false);
    }
    
    @Test
    public void testProjectPreference() {
        
        Assert.assertTrue(false);
    }
}
