/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.ModelConfig;
import org.gusdb.wdk.model.ModelConfigUserDB;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class UserFactory {
    public static final String GUEST_USER_PREFIX = "WDK_GUEST_";

    public static final String GLOBAL_PREFERENCE_KEY = "[Global]";

    private static Logger logger = Logger.getLogger(UserFactory.class);

    // -------------------------------------------------------------------------
    // data base table and column definitions
    // -------------------------------------------------------------------------
    static final String TABLE_USER = "users";

    static final String COLUMN_USER_ID = "user_id";
    static final String COLUMN_SIGNATURE = "signature";

    private final String COLUMN_EMAIL = "email";

    // -------------------------------------------------------------------------
    // the macros used by the registration email
    // -------------------------------------------------------------------------
    private static final String EMAIL_MACRO_USER_NAME = "USER_NAME";
    private static final String EMAIL_MACRO_EMAIL = "EMAIL";
    private static final String EMAIL_MACRO_PASSWORD = "PASSWORD";

    public static String encrypt(String str) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] encrypted = digest.digest(str.getBytes());
        // convert each byte into hex format
        StringBuffer buffer = new StringBuffer();
        for (byte code : encrypted) {
            buffer.append(Integer.toHexString(code & 0xFF));
        }
        return buffer.toString();
    }

    /**
     * md5 checksum algorithm. encrypt(String) drops leading zeros of hex codes
     * so is not compatible with md5
     **/
    public static String md5(String str) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] encrypted = digest.digest(str.getBytes());
        StringBuffer buffer = new StringBuffer();
        for (byte code : encrypted) {
            buffer.append(Integer.toString((code & 0xff) + 0x100, 16).substring(
                    1));
        }
        return buffer.toString();
    }

    // -------------------------------------------------------------------------
    // member variables
    // -------------------------------------------------------------------------
    private DBPlatform platform;
    private DataSource dataSource;

    private String userSchema;
    private String defaultRole;

    private String projectId;

    // WdkModel is used by the legacy code, may consider to be removed
    private WdkModel wdkModel;

    public UserFactory(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
        this.platform = wdkModel.getUserPlatform();
        this.dataSource = platform.getDataSource();
        this.projectId = wdkModel.getProjectId();

        ModelConfig modelConfig = wdkModel.getModelConfig();
        ModelConfigUserDB userDB = modelConfig.getUserDB();
        this.userSchema = userDB.getUserSchema();
        this.defaultRole = modelConfig.getDefaultRole();
    }

    /**
     * @return Returns the userRole.
     */
    public String getDefaultRole() {
        return defaultRole;
    }

    public String getProjectId() {
        return projectId;
    }

    public User createUser(String email, String lastName, String firstName,
            String middleName, String title, String organization,
            String department, String address, String city, String state,
            String zipCode, String phoneNumber, String country,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences) throws WdkUserException,
            WdkModelException {
        return createUser(email, lastName, firstName, middleName, title,
                organization, department, address, city, state, zipCode,
                phoneNumber, country, globalPreferences, projectPreferences,
                true);
    }

    User createUser(String email, String lastName, String firstName,
            String middleName, String title, String organization,
            String department, String address, String city, String state,
            String zipCode, String phoneNumber, String country,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences, boolean resetPwd)
            throws WdkUserException, WdkModelException {
        if (email == null)
            throw new WdkUserException("The user's email cannot be empty.");
        // format the info
        email = email.trim();
        if (email.length() == 0)
            throw new WdkUserException("The user's email cannot be empty.");

        PreparedStatement psUser = null;
        try {
            // check whether the user exist in the database already exist.
            // if loginId exists, the operation failed
            if (isExist(email))
                throw new WdkUserException("The email '" + email
                        + "' has already been registered. "
                        + "Please choose another one.");

            // get a new userId
            int userId = platform.getNextId(userSchema, "users");
            String signature = encrypt(userId + "_" + email);
            Date registerTime = new Date();

            psUser = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + userSchema + TABLE_USER + " (" + COLUMN_USER_ID + ", "
                    + COLUMN_EMAIL + ", passwd, is_guest, "
                    + "register_time, last_name, first_name, "
                    + "middle_name, title, organization, department, address, "
                    + "city, state, zip_code, phone_number, country,signature)"
                    + " VALUES (?, ?, ' ', ?, ?, ?, ?, ?, ?, ?, ?,"
                    + "?, ?, ?, ?, ?, ?, ?)");
            psUser.setInt(1, userId);
            psUser.setString(2, email);
            psUser.setBoolean(3, false);
            psUser.setDate(4, new java.sql.Date(registerTime.getTime()));
            psUser.setString(5, lastName);
            psUser.setString(6, firstName);
            psUser.setString(7, middleName);
            psUser.setString(8, title);
            psUser.setString(9, organization);
            psUser.setString(10, department);
            psUser.setString(11, address);
            psUser.setString(12, city);
            psUser.setString(13, state);
            psUser.setString(14, zipCode);
            psUser.setString(15, phoneNumber);
            psUser.setString(16, country);
            psUser.setString(17, signature);
            psUser.execute();

            // create user object
            User user = new User(wdkModel, userId, email, signature);
            user.setLastName(lastName);
            user.setFirstName(firstName);
            user.setMiddleName(middleName);
            user.setTitle(title);
            user.setOrganization(organization);
            user.setDepartment(department);
            user.setAddress(address);
            user.setCity(city);
            user.setState(state);
            user.setZipCode(zipCode);
            user.setPhoneNumber(phoneNumber);
            user.setCountry(country);
            user.addUserRole(defaultRole);
            user.setGuest(false);

            // save user's roles
            saveUserRoles(user);

            // save preferences
            if (globalPreferences == null)
                globalPreferences = new LinkedHashMap<String, String>();
            for (String param : globalPreferences.keySet()) {
                user.setGlobalPreference(param, globalPreferences.get(param));
            }
            if (projectPreferences == null)
                projectPreferences = new LinkedHashMap<String, String>();
            for (String param : projectPreferences.keySet()) {
                user.setProjectPreference(param, projectPreferences.get(param));
            }
            savePreferences(user);

            // generate a random password, and send to the user via email
            if (resetPwd) resetPassword(user);

            return user;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeStatement(psUser);
        }
    }

    public User createGuestUser() throws WdkUserException, WdkModelException,
            SQLException, NoSuchAlgorithmException {
        PreparedStatement psUser = null;
        try {
            // get a new user id
            int userId = platform.getNextId(userSchema, "users");
            String email = GUEST_USER_PREFIX + userId;
            Date registerTime = new Date();
            Date lastActiveTime = new Date();
            String signature = encrypt(userId + "_" + email);
            String firstName = "Guest #" + userId;
            psUser = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + userSchema + "users (user_id, email, passwd, is_guest, "
                    + "register_time, last_active, first_name, signature) "
                    + "VALUES (?, ?, ' ', ?, ?, ?, ?, ?)");
            psUser.setInt(1, userId);
            psUser.setString(2, email);
            psUser.setBoolean(3, true);
            psUser.setDate(4, new java.sql.Date(registerTime.getTime()));
            psUser.setDate(5, new java.sql.Date(lastActiveTime.getTime()));
            psUser.setString(6, firstName);
            psUser.setString(7, signature);
            psUser.executeUpdate();

            User user = new User(wdkModel, userId, email, signature);
            user.setFirstName(firstName);
            user.addUserRole(defaultRole);
            user.setGuest(true);

            // save user's roles
            saveUserRoles(user);

            logger.info("Guest user #" + userId + " created.");

            return user;
        } finally {
            SqlUtils.closeStatement(psUser);
        }
    }

    public User login(User guest, String email, String password)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        // make sure the guest is really a guest
        if (!guest.isGuest())
            throw new WdkUserException("User has been logged in.");

        // authenticate the user; if fails, a WdkUserException will be thrown
        // out
        User user = authenticate(email, password);

        // merge the history of the guest into the user
        user.mergeUser(guest);

        // update user active timestamp
        updateUser(user);

        return user;
    }

    private User authenticate(String email, String password)
            throws WdkUserException, WdkModelException {
        // convert email to lower case
        email = email.trim();
        ResultSet rs = null;
        try {
            // encrypt password
            password = encrypt(password);

            // query on the database to see if the pair match
            PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
                    "SELECT user_id " + "FROM " + userSchema + "users WHERE "
                            + "email = ? AND passwd = ?");
            ps.setString(1, email);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (!rs.next())
                throw new WdkUserException("Invalid email or password.");
            int userId = rs.getInt("user_id");

            // passed validation, load user information
            return getUser(userId);
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeResultSet(rs);
        }
    }

    /**
     * Only load the basic information of the user
     * 
     * @param email
     * @return
     * @throws WdkUserException
     * @throws SQLException
     * @throws WdkModelException
     */
    public User getUserByEmail(String email) throws WdkUserException,
            SQLException {
        email = email.trim();

        ResultSet rsUser = null;
        try {
            // get user information
            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT " + COLUMN_USER_ID + " FROM "
                            + userSchema + TABLE_USER + " WHERE email = ?");
            psUser.setString(1, email);
            rsUser = psUser.executeQuery();
            if (!rsUser.next())
                throw new WdkUserException("The user with email '" + email
                        + "' doesn't exist.");

            // read user info
            int userId = rsUser.getInt("user_id");
            return getUser(userId);
        } finally {
            SqlUtils.closeResultSet(rsUser);
        }
    }

    public User getUser(String signature) throws WdkUserException,
            WdkModelException {
        ResultSet rsUser = null;
        try {
            // get user information
            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT user_id FROM " + userSchema
                            + "users WHERE signature = ?");
            psUser.setString(1, signature);
            rsUser = psUser.executeQuery();
            if (!rsUser.next())
                throw new WdkUserException("The user with signature '"
                        + signature + "' doesn't exist.");

            // read user info
            int userId = rsUser.getInt("user_id");
            return getUser(userId);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeResultSet(rsUser);
        }
    }

    public User getUser(int userId) throws WdkUserException, SQLException {
        ResultSet rsUser = null;
        try {
            // get user information
            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource,
                    "SELECT email, signature, is_guest, last_name, "
                            + "first_name, middle_name, title, organization, "
                            + "department, address, city, state, zip_code, "
                            + "phone_number, country FROM " + userSchema
                            + "users WHERE user_id = ?");
            psUser.setInt(1, userId);
            rsUser = psUser.executeQuery();
            if (!rsUser.next())
                throw new WdkUserException("The user with id " + userId
                        + " doesn't exist.");

            // read user info
            String email = rsUser.getString("email");
            String signature = rsUser.getString("signature");
            User user = new User(wdkModel, userId, email, signature);
            user.setGuest(rsUser.getBoolean("is_guest"));
            user.setLastName(rsUser.getString("last_name"));
            user.setFirstName(rsUser.getString("first_name"));
            user.setMiddleName(rsUser.getString("middle_name"));
            user.setTitle(rsUser.getString("title"));
            user.setOrganization(rsUser.getString("organization"));
            user.setDepartment(rsUser.getString("department"));
            user.setAddress(rsUser.getString("address"));
            user.setCity(rsUser.getString("city"));
            user.setState(rsUser.getString("state"));
            user.setZipCode(rsUser.getString("zip_code"));
            user.setPhoneNumber(rsUser.getString("phone_number"));
            user.setCountry(rsUser.getString("country"));

            // load the user's roles
            loadUserRoles(user);

            // load user's preferences
            loadPreferences(user);

            return user;
        } finally {
            SqlUtils.closeResultSet(rsUser);
        }
    }

    public User[] queryUsers(String emailPattern) throws WdkUserException,
            WdkModelException {
        String sql = "SELECT user_id, email FROM " + userSchema + "users";

        if (emailPattern != null && emailPattern.length() > 0) {
            emailPattern = emailPattern.replace('*', '%');
            emailPattern = emailPattern.replaceAll("'", "");
            sql += " WHERE email LIKE '" + emailPattern + "'";
        }
        sql += " ORDER BY email";
        List<User> users = new ArrayList<User>();
        ResultSet rs = null;
        try {
            rs = SqlUtils.executeQuery(wdkModel, dataSource, sql);
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                User user = getUser(userId);
                users.add(user);
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeResultSet(rs);
        }
        User[] array = new User[users.size()];
        users.toArray(array);
        return array;
    }

    public void checkConsistancy() throws WdkUserException, WdkModelException {
        String sql = "SELECT user_id, email FROM " + userSchema + "users "
                + "where signature is null";

        ResultSet rs = null;
        PreparedStatement psUser = null;
        try {
            // update user's register time
            int count = SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE "
                    + userSchema + "users SET register_time = last_active "
                    + "WHERE register_time is null");
            System.out.println(count + " users with empty register_time have "
                    + "been updated");

            // update history's is_delete field
            count = SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE "
                    + userSchema
                    + "histories SET is_deleted = 0 WHERE is_deleted is null");
            System.out.println(count + " histories with empty is_deleted have "
                    + "been updated");

            // update user's signature
            psUser = SqlUtils.getPreparedStatement(dataSource, "Update "
                    + userSchema + "users SET signature = ? WHERE user_id = ?");

            rs = SqlUtils.executeQuery(wdkModel, dataSource, sql);
            while (rs.next()) {
                int userId = rs.getInt("user_id");

                String email = rs.getString("email");
                String signature = encrypt(userId + "_" + email);
                psUser.setString(1, signature);
                psUser.setInt(2, userId);
                psUser.executeUpdate();
                System.out.println("User [" + userId + "] " + email
                        + "'s signature is updated");
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeResultSet(rs);
            SqlUtils.closeStatement(psUser);
        }
    }

    private void loadUserRoles(User user) throws WdkUserException {
        ResultSet rsRole = null;
        try {
            // load the user's roles
            PreparedStatement psRole = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT " + "user_role from " + userSchema
                            + "user_roles " + "WHERE user_id = ?");
            psRole.setInt(1, user.getUserId());
            rsRole = psRole.executeQuery();
            while (rsRole.next()) {
                user.addUserRole(rsRole.getString("user_role"));
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeResultSet(rsRole);
        }
    }

    private void saveUserRoles(User user) throws WdkUserException,
            WdkModelException {
        int userId = user.getUserId();
        PreparedStatement psRoleDelete = null;
        PreparedStatement psRoleInsert = null;
        try {
            // before that, remove the records first
            SqlUtils.executeUpdate(wdkModel, dataSource, "DELETE FROM "
                    + userSchema + "user_roles WHERE user_id = " + userId);

            // Then get a prepared statement to do the insertion
            psRoleInsert = SqlUtils.getPreparedStatement(dataSource, "INSERT "
                    + "INTO " + userSchema + "user_roles (user_id, user_role)"
                    + " VALUES (?, ?)");
            String[] roles = user.getUserRoles();
            for (String role : roles) {
                psRoleInsert.setInt(1, userId);
                psRoleInsert.setString(2, role);
                psRoleInsert.execute();
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeStatement(psRoleDelete);
            SqlUtils.closeStatement(psRoleInsert);
        }
    }

    /**
     * Save the basic information of a user
     * 
     * @param user
     * @throws WdkUserException
     */
    void saveUser(User user) throws WdkUserException {
        int userId = user.getUserId();
        // check if user exists in the database. if not, fail and ask to create
        // the user first
        PreparedStatement psUser = null;
        PreparedStatement psRoleDelete = null;
        PreparedStatement psRoleInsert = null;
        try {
            Date lastActiveTime = new Date();

            // save the user's basic information
            psUser = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + userSchema + "users SET is_guest = ?, "
                    + "last_active = ?, last_name = ?, first_name = ?, "
                    + "middle_name = ?, organization = ?, department = ?, "
                    + "title = ?,  address = ?, city = ?, state = ?, "
                    + "zip_code = ?, phone_number = ?, country = ?, "
                    + "email = ? " + "WHERE user_id = ?");
            psUser.setBoolean(1, user.isGuest());
            psUser.setDate(2, new java.sql.Date(lastActiveTime.getTime()));
            psUser.setString(3, user.getLastName());
            psUser.setString(4, user.getFirstName());
            psUser.setString(5, user.getMiddleName());
            psUser.setString(6, user.getOrganization());
            psUser.setString(7, user.getDepartment());
            psUser.setString(8, user.getTitle());
            psUser.setString(9, user.getAddress());
            psUser.setString(10, user.getCity());
            psUser.setString(11, user.getState());
            psUser.setString(12, user.getZipCode());
            psUser.setString(13, user.getPhoneNumber());
            psUser.setString(14, user.getCountry());
            psUser.setString(15, user.getEmail());
            psUser.setInt(16, userId);
            psUser.execute();

            // save user's roles
            // saveUserRoles(user);

            // save preference
            savePreferences(user);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeStatement(psUser);
            SqlUtils.closeStatement(psRoleDelete);
            SqlUtils.closeStatement(psRoleInsert);
        }
    }

    /**
     * update the time stamp of the activity
     * 
     * @param user
     * @throws WdkUserException
     */
    private void updateUser(User user) throws WdkUserException {
        PreparedStatement psUser = null;
        try {
            Date lastActiveTime = new Date();
            psUser = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + userSchema + "users SET last_active = ?"
                    + " WHERE user_id = ?");
            psUser.setDate(1, new java.sql.Date(lastActiveTime.getTime()));
            psUser.setInt(2, user.getUserId());
            int result = psUser.executeUpdate();
            if (result == 0)
                throw new WdkUserException("User " + user.getEmail()
                        + " cannot be found.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeStatement(psUser);
        }
    }

    public void deleteExpiredUsers(int hoursSinceActive)
            throws WdkUserException, WdkModelException {
        ResultSet rsUser = null;
        try {
            // construct time
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -hoursSinceActive);
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());

            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT email FROM " + userSchema + "users "
                            + "WHERE email " + "LIKE '" + GUEST_USER_PREFIX
                            + "%' AND last_active < ?");
            psUser.setTimestamp(1, timestamp);
            rsUser = psUser.executeQuery();
            int count = 0;
            while (rsUser.next()) {
                deleteUser(rsUser.getString("email"));
                count++;
            }
            System.out.println("Deleted " + count + " expired users.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeResultSet(rsUser);
        }
    }

    private void savePreferences(User user) throws WdkUserException {
        int userId = user.getUserId();
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;
        try {
            // delete preferences
            psDelete = SqlUtils.getPreparedStatement(dataSource, "DELETE FROM "
                    + userSchema + "preferences WHERE user_id = ? "
                    + "AND project_id = ?");
            psDelete.setInt(1, userId);
            psDelete.setString(2, GLOBAL_PREFERENCE_KEY);
            psDelete.execute();
            psDelete.setInt(1, userId);
            psDelete.setString(2, projectId);
            psDelete.execute();

            // insert preferences
            psInsert = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + userSchema + "preferences (user_id, project_id, "
                    + "preference_name, preference_value) "
                    + "VALUES (?, ?, ?, ?)");
            Map<String, String> global = user.getGlobalPreferences();
            for (String prefName : global.keySet()) {
                String prefValue = global.get(prefName);
                psInsert.setInt(1, userId);
                psInsert.setString(2, GLOBAL_PREFERENCE_KEY);
                psInsert.setString(3, prefName);
                psInsert.setString(4, prefValue);
                psInsert.execute();
            }
            Map<String, String> project = user.getProjectPreferences();
            for (String prefName : project.keySet()) {
                String prefValue = project.get(prefName);
                psInsert.setInt(1, userId);
                psInsert.setString(2, projectId);
                psInsert.setString(3, prefName);
                psInsert.setString(4, prefValue);
                psInsert.execute();
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeStatement(psDelete);
            SqlUtils.closeStatement(psInsert);
        }
    }

    private void loadPreferences(User user) throws WdkUserException {
        int userId = user.getUserId();
        PreparedStatement psGlobal = null, psProject = null;
        ResultSet rsGlobal = null, rsProject = null;
        try {
            // load global preferences
            psGlobal = SqlUtils.getPreparedStatement(dataSource, "SELECT "
                    + "preference_name, preference_value FROM " + userSchema
                    + "preferences WHERE user_id = ? AND project_id = '"
                    + GLOBAL_PREFERENCE_KEY + "'");
            psGlobal.setInt(1, userId);
            rsGlobal = psGlobal.executeQuery();
            while (rsGlobal.next()) {
                String prefName = rsGlobal.getString("preference_name");
                String prefValue = rsGlobal.getString("preference_value");
                user.setGlobalPreference(prefName, prefValue);
            }

            // load project specific preferences
            psProject = SqlUtils.getPreparedStatement(dataSource, "SELECT "
                    + "preference_name, preference_value FROM " + userSchema
                    + "preferences WHERE user_id = ? AND project_id = ?");
            psProject.setInt(1, userId);
            psProject.setString(2, projectId);
            rsProject = psProject.executeQuery();
            while (rsProject.next()) {
                String prefName = rsProject.getString("preference_name");
                String prefValue = rsProject.getString("preference_value");
                user.setProjectPreference(prefName, prefValue);

            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeResultSet(rsGlobal);
            SqlUtils.closeResultSet(rsProject);
        }
    }

    public void resetPassword(String email) throws WdkUserException,
            WdkModelException, SQLException {
        User user = getUserByEmail(email);
        resetPassword(user);
    }

    private void resetPassword(User user) throws WdkUserException,
            WdkModelException {
        String email = user.getEmail();

        // generate a random password of 8 characters long, the range will be
        // [0-9A-Za-z]
        StringBuffer buffer = new StringBuffer();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) {
            int value = rand.nextInt(36);
            if (value < 10) { // number
                buffer.append(value);
            } else { // lower case letters
                buffer.append((char) ('a' + value - 10));
            }
        }
        String password = buffer.toString();

        savePassword(email, password);

        ModelConfig modelConfig = wdkModel.getModelConfig();
        String emailContent = modelConfig.getEmailContent();
        String supportEmail = modelConfig.getSupportEmail();
        String emailSubject = modelConfig.getEmailSubject();

        // send an email to the user
        String pattern = "\\$\\$" + EMAIL_MACRO_USER_NAME + "\\$\\$";
        String name = user.getFirstName() + " " + user.getLastName();
        String message = emailContent.replaceAll(pattern,
                Matcher.quoteReplacement(name));

        pattern = "\\$\\$" + EMAIL_MACRO_EMAIL + "\\$\\$";
        message = message.replaceAll(pattern, Matcher.quoteReplacement(email));

        pattern = "\\$\\$" + EMAIL_MACRO_PASSWORD + "\\$\\$";
        message = message.replaceAll(pattern,
                Matcher.quoteReplacement(password));

        Utilities.sendEmail(wdkModel, user.getEmail(), supportEmail,
                emailSubject, message);
    }

    void changePassword(String email, String oldPassword, String newPassword,
            String confirmPassword) throws WdkUserException {
        email = email.trim();

        if (newPassword == null || newPassword.trim().length() == 0)
            throw new WdkUserException("The new password cannot be empty.");

        // check if the new password matches
        if (!newPassword.equals(confirmPassword))
            throw new WdkUserException("The new password doesn't match, "
                    + "please type them again. It's case sensitive.");

        // encrypt password
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            oldPassword = encrypt(oldPassword);

            // check if the old password matches
            ps = SqlUtils.getPreparedStatement(dataSource, "SELECT count(*) "
                    + "FROM " + userSchema + "users WHERE email =? "
                    + "AND passwd = ?");
            ps.setString(1, email);
            ps.setString(2, oldPassword);
            rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count <= 0)
                throw new WdkUserException("The current password is incorrect.");

            // passed check, then save the new password
            savePassword(email, newPassword);
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeResultSet(rs);
            // SqlUtils.closeStatement(ps);
        }

    }

    public void savePassword(String email, String password)
            throws WdkUserException {
        email = email.trim();
        PreparedStatement ps = null;
        try {
            // encrypt the password, and save it
            String encrypted = encrypt(password);
            ps = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + userSchema + "users SET passwd = ? " + "WHERE email = ?");
            ps.setString(1, encrypted);
            ps.setString(2, email);
            ps.execute();
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeStatement(ps);
        }
    }

    private boolean isExist(String email) throws WdkUserException {
        email = email.trim();
        // check if user exists in the database. if not, fail and ask to create
        // the user first
        ResultSet rs = null;
        try {
            PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
                    "SELECT count(*) " + "FROM " + userSchema
                            + "users WHERE email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            return (count > 0);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            SqlUtils.closeResultSet(rs);
        }
    }

    public void deleteUser(String email) throws WdkUserException,
            WdkModelException, SQLException {
        // get user id
        User user = getUserByEmail(email);

        // delete strategies and steps from all projects
        user.deleteStrategies(true);
        user.deleteSteps(true);

        String where = " WHERE user_id = " + user.getUserId();
        try {
            // delete preference
            String sql = "DELETE FROM " + userSchema + "preferences" + where;
            SqlUtils.executeUpdate(wdkModel, dataSource, sql);

            // delete user roles
            sql = "DELETE FROM " + userSchema + "user_roles" + where;
            SqlUtils.executeUpdate(wdkModel, dataSource, sql);

            // delete user
            sql = "DELETE FROM " + userSchema + "users" + where;
            SqlUtils.executeUpdate(wdkModel, dataSource, sql);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        }
    }

    public WdkModel getWdkModel() {
        return wdkModel;
    }
}
