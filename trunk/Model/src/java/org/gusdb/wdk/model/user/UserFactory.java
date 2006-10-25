/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author xingao
 * 
 */
public class UserFactory {

    /*
     * Inner class to act as a JAF datasource to send HTML e-mail content
     */
    static class HTMLDataSource implements javax.activation.DataSource {

        private String html;

        public HTMLDataSource(String htmlString) {
            html = htmlString;
        }

        // Return html string in an InputStream.
        // A new stream must be returned each time.
        public InputStream getInputStream() throws IOException {
            if (html == null) throw new IOException("Null HTML");
            return new ByteArrayInputStream(html.getBytes());
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        public String getContentType() {
            return "text/html";
        }

        public String getName() {
            return "JAF text/html dataSource to send e-mail only";
        }
    }

    public static final String GUEST_USER_PREFIX = "WDK_GUEST_";

    public static final String GLOBAL_PREFERENCE_KEY = "[Global]";

    private static UserFactory factory;

    private RDBMSPlatformI platform;
    private DataSource dataSource;

    private String loginSchema;
    private String defaultRole;
    private String smtpServer;

    private String projectId;

    // WdkModel is used by the legacy code, may consider to be removed
    private WdkModel wdkModel;

    // the information for registration email
    private String registerEmail;
    private String emailSubject;
    private String emailContent;

    public static UserFactory getInstance() throws WdkUserException {
        if (factory == null) {
            throw new WdkUserException(
                    "UserFactory is not initialized properly. Please Initialize WdkModel first.");
        }
        return factory;
    }

    public static void initialize(WdkModel wdkModel, String projectId,
            RDBMSPlatformI platform, String loginSchema, String defaultRole,
            String smtpServer, String registerEmail, String emailSubject,
            String emailContent) {
        factory = new UserFactory(platform);
        factory.wdkModel = wdkModel;
        factory.projectId = projectId;
        factory.loginSchema = loginSchema;
        factory.defaultRole = defaultRole;
        factory.smtpServer = smtpServer;
        factory.registerEmail = registerEmail;
        factory.emailContent = emailContent;
        factory.emailSubject = emailSubject;
    }

    private UserFactory(RDBMSPlatformI platform) {
        this.platform = platform;
        this.dataSource = platform.getDataSource();
    }

    public WdkModel getWdkModel() {
        return wdkModel;
    }

    public void sendEmail(String email, String reply, String subject,
            String content) throws WdkUserException {
        // create properties and get the session
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.debug", "true");
        Session session = Session.getInstance(props);

        // instantiate a message
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(reply));
            message.setReplyTo(new Address[] { new InternetAddress(reply) });
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(
                    email));
            message.setSubject(subject);
            message.setSentDate(new Date());
            // set html content
            message.setDataHandler(new DataHandler(new HTMLDataSource(content)));

            // send email
            Transport.send(message);
        } catch (AddressException ex) {
            throw new WdkUserException(ex);
        } catch (MessagingException ex) {
            throw new WdkUserException(ex);
        }
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

    /**
     * @return Returns the smtpServer.
     */
    public String getSmtpServer() {
        return smtpServer;
    }

    /**
     * @param smtpServer
     *            The smtpServer to set.
     */
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
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
                        + "' has been registered. Please choose another one.");

            // get a new userId
            int userId = Integer.parseInt(platform.getNextId(loginSchema,
                    "users"));

            psUser = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + loginSchema + "users (user_id, email, passwd, is_guest, "
                    + "last_active, last_name, first_name, middle_name, title,"
                    + " organization, department, address, city, state, "
                    + "zip_code, phone_number, country) VALUES (?, ?, ' ', ?, "
                    + "SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            psUser.setInt(1, userId);
            psUser.setString(2, email);
            psUser.setBoolean(3, false);
            psUser.setString(4, lastName);
            psUser.setString(5, firstName);
            psUser.setString(6, middleName);
            psUser.setString(7, title);
            psUser.setString(8, organization);
            psUser.setString(9, department);
            psUser.setString(10, address);
            psUser.setString(11, city);
            psUser.setString(12, state);
            psUser.setString(13, zipCode);
            psUser.setString(14, phoneNumber);
            psUser.setString(15, country);
            psUser.execute();

            // create user object
            User user = new User(wdkModel, userId, email);
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
            for (String param : globalPreferences.keySet()) {
                user.setGlobalPreference(param, globalPreferences.get(param));
            }
            for (String param : projectPreferences.keySet()) {
                user.setProjectPreference(param, projectPreferences.get(param));
            }
            savePreferences(user);

            // generate a random password, and send to the user via email
            if (resetPwd) resetPassword(user);

            return user;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psUser);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    public User createGuestUser() throws WdkUserException, WdkModelException {
        PreparedStatement psUser = null;
        try {
            // get a new user id
            int userId = Integer.parseInt(platform.getNextId(loginSchema,
                    "users"));
            String email = GUEST_USER_PREFIX + userId;
            String firstName = "Guest #" + userId;
            psUser = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + loginSchema + "users (user_id, email, passwd, is_guest, "
                    + "last_active, first_name) "
                    + "VALUES (?, ?, ' ', 1, SYSDATE, ?)");
            psUser.setInt(1, userId);
            psUser.setString(2, email);
            psUser.setString(3, firstName);
            psUser.executeUpdate();

            User user = new User(wdkModel, userId, email);
            user.setFirstName(firstName);
            user.addUserRole(defaultRole);
            user.setGuest(true);

            // save user's roles
            saveUserRoles(user);

            return user;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psUser);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    public User login(User guest, String email, String password)
            throws WdkUserException, WdkModelException {
        // make sure the guest is really a guest
        if (!guest.isGuest())
            throw new WdkUserException("User has been logged in.");

        // authenticate the user; if fails, a WdkUserException will be thrown
        // out
        User user = authenticate(email, password);

        // merge the history of the guest into the user
        user.mergeUser(guest);
        user.update();
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
                    "SELECT count(*) " + "FROM " + loginSchema + "users WHERE "
                            + "email = ? AND passwd = ?");
            ps.setString(1, email);
            ps.setString(2, password);
            rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count != 1)
                throw new WdkUserException("Invalid email or password.");

            // passed validation, load user information
            return loadUser(email);
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rs);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private void markActive(User user) throws WdkUserException {
        PreparedStatement psUser = null;
        try {
            psUser = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "users SET last_active = SYSDATE WHERE "
                    + "user_id = ?");
            psUser.setInt(1, user.getUserId());
            psUser.executeUpdate();
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psUser);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    /**
     * Only load the basic information of the user
     * 
     * @param email
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public User loadUser(String email) throws WdkUserException,
            WdkModelException {
        // convert to lower case
        email = email.trim();

        ResultSet rsUser = null;
        try {
            // get user information
            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT user_id, is_guest, last_name, "
                            + "first_name, middle_name, title, organization, "
                            + "department, address, city, state, zip_code, "
                            + "phone_number, country FROM " + loginSchema
                            + "users WHERE email = ?");
            psUser.setString(1, email);
            rsUser = psUser.executeQuery();
            if (!rsUser.next())
                throw new WdkUserException("The user with email '" + email
                        + "' doesn't exist.");

            // read user info
            int userId = rsUser.getInt("user_id");
            User user = new User(wdkModel, userId, email);
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

            // update user active timestamp
            markActive(user);

            return user;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsUser);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    public User loadUser(int userId) throws WdkUserException, WdkModelException {
        ResultSet rsUser = null;
        try {
            // get user information
            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT email, is_guest, last_name, "
                            + "first_name, middle_name, title, organization, "
                            + "department, address, city, state, zip_code, "
                            + "phone_number, country FROM " + loginSchema
                            + "users WHERE user_id = ?");
            psUser.setInt(1, userId);
            rsUser = psUser.executeQuery();
            if (!rsUser.next())
                throw new WdkUserException("The user with id " + userId
                        + " doesn't exist.");

            // read user info
            String email = rsUser.getString("email");
            User user = new User(wdkModel, userId, email);
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

            // update user active timestamp
            markActive(user);

            return user;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsUser);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    public User[] queryUsers(String emailPattern) throws WdkUserException,
            WdkModelException {
        String sql = "SELECT user_id FROM " + loginSchema + "users";
        ;
        if (emailPattern != null && emailPattern.length() > 0) {
            emailPattern = emailPattern.replace('*', '%');
            emailPattern = emailPattern.replaceAll("'", "");
            sql += " WHERE email LIKE '" + emailPattern + "'";
        }
        sql += " ORDER BY email";
        List<User> users = new ArrayList<User>();
        ResultSet rs = null;
        try {
            rs = SqlUtils.getResultSet(dataSource, sql);
            while (rs.next()) {
                int userId = rs.getInt("userId");
                User user = loadUser(userId);
                users.add(user);
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rs);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
        User[] array = new User[users.size()];
        users.toArray(array);
        return array;
    }

    private void loadUserRoles(User user) throws WdkUserException {
        ResultSet rsRole = null;
        try {
            // load the user's roles
            PreparedStatement psRole = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT " + "user_role from " + loginSchema
                            + "user_roles " + "WHERE user_id = ?");
            psRole.setInt(1, user.getUserId());
            rsRole = psRole.executeQuery();
            while (rsRole.next()) {
                user.addUserRole(rsRole.getString("user_role"));
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsRole);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private void saveUserRoles(User user) throws WdkUserException {
        int userId = user.getUserId();
        PreparedStatement psRoleDelete = null;
        PreparedStatement psRoleInsert = null;
        try {
            // before that, remove the records first
            psRoleDelete = SqlUtils.getPreparedStatement(dataSource, "DELETE "
                    + "FROM " + loginSchema + "user_roles WHERE user_id = ?");
            psRoleDelete.setInt(1, userId);
            psRoleDelete.execute();

            // Then get a prepared statement to do the insertion
            psRoleInsert = SqlUtils.getPreparedStatement(dataSource, "INSERT "
                    + "INTO " + loginSchema + "user_roles (user_id, user_role)"
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
            try {
                SqlUtils.closeStatement(psRoleDelete);
                SqlUtils.closeStatement(psRoleInsert);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    /**
     * Save the basic information of a user
     * 
     * @param user
     * @throws WdkUserException
     */
    void saveUser(User user) throws WdkUserException {
        String email = user.getEmail().trim();
        int userId = user.getUserId();
        // check if user exists in the database. if not, fail and ask to create
        // the user first
        PreparedStatement psUser = null;
        PreparedStatement psRoleDelete = null;
        PreparedStatement psRoleInsert = null;
        try {
            if (!isExist(email))
                throw new WdkUserException("The user with email " + email
                        + " doesn't exist. Save operation cancelled.");

            // save the user's basic information
            psUser = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "users SET is_guest = ?, "
                    + "last_active = SYSDATE, last_name = ?, first_name = ?, "
                    + "middle_name = ?, organization = ?, department = ?, "
                    + "title = ?,  address = ?, city = ?, state = ?, "
                    + "zip_code = ?, phone_number = ?, country = ? "
                    + "WHERE user_id = ?");
            psUser.setBoolean(1, user.isGuest());
            psUser.setString(2, user.getLastName());
            psUser.setString(3, user.getFirstName());
            psUser.setString(4, user.getMiddleName());
            psUser.setString(5, user.getOrganization());
            psUser.setString(6, user.getDepartment());
            psUser.setString(7, user.getTitle());
            psUser.setString(8, user.getAddress());
            psUser.setString(9, user.getCity());
            psUser.setString(10, user.getState());
            psUser.setString(11, user.getZipCode());
            psUser.setString(12, user.getPhoneNumber());
            psUser.setString(13, user.getCountry());
            psUser.setInt(14, userId);
            psUser.execute();

            // save user's roles
            saveUserRoles(user);

            // save preference
            savePreferences(user);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psUser);
                SqlUtils.closeStatement(psRoleDelete);
                SqlUtils.closeStatement(psRoleInsert);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    /**
     * update the time stamp of the activity
     * 
     * @param user
     * @throws WdkUserException
     */
    void updateUser(User user) throws WdkUserException {
        PreparedStatement psUser = null;
        try {
            psUser = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "users SET last_active = SYSDATE "
                    + "WHERE user_id = ?");
            psUser.setInt(1, user.getUserId());
            int result = psUser.executeUpdate();
            if (result == 0)
                throw new WdkUserException("User " + user.getEmail()
                        + " cannot be found.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psUser);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private int getMaxHistoryId(int userId) throws WdkUserException {
        int maxId = 0;
        // get the max id from the history storage
        ResultSet rsMax = null;
        try {
            PreparedStatement psMax = SqlUtils.getPreparedStatement(dataSource,
                    "SELECT max(history_id) AS maxId FROM " + loginSchema
                            + "histories WHERE user_id = ? AND project_id = ?");
            psMax.setInt(1, userId);
            psMax.setString(2, projectId);
            rsMax = psMax.executeQuery();
            if (rsMax.next()) maxId = rsMax.getInt("maxId");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsMax);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
        return maxId;
    }

    Map<Integer, History> loadHistories(User user) throws WdkUserException,
            WdkModelException {
        Map<Integer, History> histories = new LinkedHashMap<Integer, History>();

        ResultSet rsHistory = null;
        try {
            PreparedStatement psHistory = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT history_id, question_name, create_time"
                            + ", last_run_time, custom_name, estimate_size, "
                            + "is_boolean, params FROM " + loginSchema
                            + "histories WHERE user_id = ? AND project_id = ? "
                            + "ORDER BY last_run_time DESC");
            psHistory.setInt(1, user.getUserId());
            psHistory.setString(2, projectId);
            rsHistory = psHistory.executeQuery();

            while (rsHistory.next()) {
                // load history info
                int historyId = rsHistory.getInt("history_id");
                Timestamp createTime = rsHistory.getTimestamp("create_time");
                Timestamp lastRunTime = rsHistory.getTimestamp("last_run_time");

                History history = new History(this, user, historyId);
                history.setCreatedTime(new Date(createTime.getTime()));
                history.setLastRunTime(new Date(lastRunTime.getTime()));
                history.setCustomName(rsHistory.getString("custom_name"));
                history.setEstimateSize(rsHistory.getInt("estimate_size"));
                history.setBoolean(rsHistory.getBoolean("is_boolean"));

                Clob clob = rsHistory.getClob("params");
                String paramsClob = clob.getSubString(1, (int) clob.length());

                // re-construct the answer
                Answer answer;
                if (history.isBoolean()) {
                    answer = constructBooleanAnswer(user, paramsClob);
                    history.setBooleanExpression(paramsClob);
                } else {
                    String questionName = rsHistory.getString("question_name");
                    answer = constructAnswer(user, questionName, paramsClob);
                }
                history.setAnswer(answer);
                histories.put(historyId, history);
            }
            // now compute the dependencies of the histories
            History[] array = new History[histories.size()];
            histories.values().toArray(array);
            for (History history : histories.values()) {
                history.computeDependencies(array);
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsHistory);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
        return histories;
    }

    History loadHistory(User user, int historyId) throws WdkUserException,
            WdkModelException {
        ResultSet rsHistory = null;
        try {
            PreparedStatement psHistory = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT question_name, create_time, "
                            + "last_run_time, custom_name, estimate_size, "
                            + "is_boolean, params FROM " + loginSchema
                            + "histories WHERE user_id = ? AND project_id = ? "
                            + "AND history_id = ? ORDER BY last_run_time DESC");
            psHistory.setInt(1, user.getUserId());
            psHistory.setString(2, projectId);
            psHistory.setInt(3, historyId);
            rsHistory = psHistory.executeQuery();
            if (!rsHistory.next())
                throw new WdkUserException("The history #" + historyId
                        + " of user " + user.getEmail() + " doesn't exist.");

            // load history info
            Timestamp createTime = rsHistory.getTimestamp("create_time");
            Timestamp lastRunTime = rsHistory.getTimestamp("last_run_time");

            History history = new History(this, user, historyId);
            history.setCreatedTime(new Date(createTime.getTime()));
            history.setLastRunTime(new Date(lastRunTime.getTime()));
            history.setCustomName(rsHistory.getString("custom_name"));
            history.setEstimateSize(rsHistory.getInt("estimate_size"));
            history.setBoolean(rsHistory.getBoolean("is_boolean"));

            Clob clob = rsHistory.getClob("params");
            String paramsClob = clob.getSubString(1, (int) clob.length());

            // re-construct the answer
            Answer answer;
            if (history.isBoolean()) {
                answer = constructBooleanAnswer(user, paramsClob);
                history.setBooleanExpression(paramsClob);
            } else {
                String questionName = rsHistory.getString("question_name");
                answer = constructAnswer(user, questionName, paramsClob);
            }
            history.setAnswer(answer);

            return history;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsHistory);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private Answer constructAnswer(User user, String questionName,
            String paramsClob) throws WdkModelException, WdkUserException {
        // obtain the question with full name
        Question question = (Question) wdkModel.resolveReference(questionName,
                "UserFactory", "UserFactory", "question_name");

        String[] parts = paramsClob.split(WdkModel.PARAM_DIVIDER);
        // the first element is query name, ignored
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        for (int i = 1; i < parts.length; i++) {
            String pvPair = parts[i];
            int index = pvPair.indexOf('=');
            String paramName = pvPair.substring(0, index).trim();
            String value = pvPair.substring(index + 1).trim();
            params.put(paramName, value);
        }

        // get the user's preference of items per page
        return question.makeAnswer(params, 0, user.getItemsPerPage() - 1);
    }

    private Answer constructBooleanAnswer(User user, String expression)
            throws WdkUserException, WdkModelException {
        BooleanExpression exp = new BooleanExpression(user);
        Map<String, String> operatorMap = getWdkModel().getBooleanOperators();
        BooleanQuestionNode root = exp.parseExpression(expression, operatorMap);

        return root.makeAnswer(0, user.getItemsPerPage());
    }

    History createHistory(User user, Answer answer, String booleanExpression)
            throws WdkUserException, WdkModelException {
        int userId = user.getUserId();
        String questionName = answer.getQuestion().getFullName();

        boolean isBoolean = answer.getIsBoolean();
        String customName = (isBoolean) ? booleanExpression : null;
        if (customName != null && customName.length() > 4000)
            customName = customName.substring(0, 4000);

        int estimateSize = answer.getResultSize();
        QueryInstance qinstance = answer.getIdsQueryInstance();
        String checksum = qinstance.getChecksum();
        String signature = qinstance.getQuery().getSignature();
        String params = (isBoolean) ? booleanExpression
                : qinstance.getClobContent();

        // check whether the asnwer exist or not
        ResultSet rsHistory = null;
        PreparedStatement psHistory = null;
        try {
            PreparedStatement psCheck = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT history_id FROM " + loginSchema
                            + "histories WHERE user_id = ? AND project_id = ? "
                            + "AND checksum = ?");
            psCheck.setInt(1, userId);
            psCheck.setString(2, projectId);
            psCheck.setString(3, checksum);
            rsHistory = psCheck.executeQuery();

            if (rsHistory.next()) {
                // get existing history
                int historyId = rsHistory.getInt("history_id");
                History history = loadHistory(user, historyId);

                // update the history time stamp
                history.update();
                return history;
            }

            // new existing ones matched, get a new history id
            int historyId = getMaxHistoryId(userId) + 1;
            Date createTime = new Date();
            Date lastRunTime = new Date(createTime.getTime());
            psHistory = SqlUtils.getPreparedStatement(dataSource, "INSERT "
                    + "INTO " + loginSchema + "histories (history_id, user_id,"
                    + " project_id, question_name, create_time, last_run_time,"
                    + " custom_name, estimate_size, checksum, signature, "
                    + "is_boolean, params) VALUES (?, ?, ?, ?, ?, ?, ?, ?, "
                    + "?, ?, ?, ?)");
            psHistory.setInt(1, historyId);
            psHistory.setInt(2, userId);
            psHistory.setString(3, projectId);
            psHistory.setString(4, questionName);
            psHistory.setTimestamp(5, new Timestamp(createTime.getTime()));
            psHistory.setTimestamp(6, new Timestamp(lastRunTime.getTime()));
            psHistory.setString(7, customName);
            psHistory.setInt(8, estimateSize);
            psHistory.setString(9, checksum);
            psHistory.setString(10, signature);
            psHistory.setBoolean(11, isBoolean);
            // the platform set clob, and run the statement
            platform.updateClobData(psHistory, 12, params);

            // create the History
            History history = new History(this, user, historyId);
            history.setAnswer(answer);
            history.setCreatedTime(createTime);
            history.setLastRunTime(lastRunTime);
            history.setCustomName(customName);
            history.setEstimateSize(estimateSize);
            history.setBoolean(answer.getIsBoolean());

            return history;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psHistory);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    /**
     * This method only update the custom name, the time stamp of last running
     * 
     * @param user
     * @param history
     * @throws WdkUserException
     */
    void updateHistory(User user, History history, boolean updateTime)
            throws WdkUserException {
        // check email existence
        if (!isExist(user.getEmail()))
            throw new WdkUserException("The user " + user.getEmail()
                    + " doesn't exist. Updating operation cancelled.");

        // update custom name
        Date lastRunTime = (updateTime) ? new Date() : history.getLastRunTime();
        PreparedStatement psHistory = null;
        try {
            psHistory = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "histories SET custom_name = ?, "
                    + "last_run_time = ? WHERE user_id = ? "
                    + "AND project_id = ? AND history_id = ?");
            psHistory.setString(1, history.getCustomName());
            psHistory.setTimestamp(2, new Timestamp(lastRunTime.getTime()));
            psHistory.setInt(3, user.getUserId());
            psHistory.setString(4, projectId);
            psHistory.setInt(5, history.getHistoryId());
            int result = psHistory.executeUpdate();
            if (result == 0)
                throw new WdkUserException("The history #"
                        + history.getHistoryId() + " of user "
                        + user.getEmail() + " cannot be found.");

            // update the last run stamp
            history.setLastRunTime(lastRunTime);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psHistory);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    void deleteHistory(User user, int historyId) throws WdkUserException {
        PreparedStatement psHistory = null;
        try {
            // remove history
            psHistory = SqlUtils.getPreparedStatement(dataSource, "DELETE "
                    + "FROM " + loginSchema + "histories WHERE user_id = ? "
                    + "AND project_id = ? AND history_id = ?");
            psHistory.setInt(1, user.getUserId());
            psHistory.setString(2, projectId);
            psHistory.setInt(3, historyId);
            int result = psHistory.executeUpdate();
            if (result == 0)
                throw new WdkUserException("The history #" + historyId
                        + " of user " + user.getEmail() + " cannot be found.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psHistory);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    void deleteHistories(User user) throws WdkUserException {
        PreparedStatement psHistory = null;
        try {
            psHistory = SqlUtils.getPreparedStatement(dataSource, "DELETE "
                    + "FROM " + loginSchema + "histories WHERE user_id = ? "
                    + "AND project_id = ?");
            psHistory.setInt(1, user.getUserId());
            psHistory.setString(2, projectId);
            psHistory.executeUpdate();
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psHistory);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    int getHistoryCount(User user) throws WdkUserException {
        ResultSet rsHistory = null;
        try {
            PreparedStatement psHistory = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT count(*) AS num FROM " + loginSchema
                            + "histories WHERE user_id = ? AND project_id = ?");
            psHistory.setInt(1, user.getUserId());
            psHistory.setString(2, projectId);
            rsHistory = psHistory.executeQuery();
            rsHistory.next();
            return rsHistory.getInt("num");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsHistory);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private void savePreferences(User user) throws WdkUserException {
        int userId = user.getUserId();
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;
        try {
            // delete preferences
            psDelete = SqlUtils.getPreparedStatement(dataSource, "DELETE FROM "
                    + loginSchema + "preferences WHERE user_id = ? "
                    + "AND project_id = ?");
            psDelete.setInt(1, userId);
            psDelete.setString(2, GLOBAL_PREFERENCE_KEY);
            psDelete.execute();
            psDelete.setInt(1, userId);
            psDelete.setString(2, projectId);
            psDelete.execute();

            // insert preferences
            psInsert = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + loginSchema + "preferences (user_id, project_id, "
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
            try {
                SqlUtils.closeStatement(psDelete);
                SqlUtils.closeStatement(psInsert);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private void loadPreferences(User user) throws WdkUserException {
        int userId = user.getUserId();
        PreparedStatement psGlobal = null, psProject = null;
        ResultSet rsGlobal = null, rsProject = null;
        try {
            // load global preferences
            psGlobal = SqlUtils.getPreparedStatement(dataSource, "SELECT "
                    + "preference_name, preference_value FROM " + loginSchema
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
                    + "preference_name, preference_value FROM " + loginSchema
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
            try {
                SqlUtils.closeResultSet(rsGlobal);
                SqlUtils.closeResultSet(rsProject);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    public void resetPassword(String email) throws WdkUserException,
            WdkModelException {
        User user = loadUser(email);
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

        // send an email to the user
        String message = emailContent.replaceAll("\\$\\$FIRST_NAME\\$\\$",
                user.getFirstName());
        message = message.replaceAll("\\$\\$EMAIL\\$\\$", email);
        message = message.replaceAll("\\$\\$PASSWORD\\$\\$", password);
        sendEmail(user.getEmail(), registerEmail, emailSubject, message);
    }

    void changePassword(String email, String oldPassword, String newPassword,
            String confirmPassword) throws WdkUserException {
        email = email.trim();

        // encrypt password
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            oldPassword = encrypt(oldPassword);

            // check if the old password matches
            ps = SqlUtils.getPreparedStatement(dataSource, "SELECT count(*) "
                    + "FROM " + loginSchema + "users WHERE email =? "
                    + "AND passwd = ?");
            ps.setString(1, email);
            ps.setString(2, oldPassword);
            rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count <= 0)
                throw new WdkUserException("The current password is incorrect.");

            // check if the new password matches
            if (!newPassword.equals(confirmPassword))
                throw new WdkUserException("The new password doesn't match, "
                        + "please type them again. It's case sensitive.");

            // passed check, then save the new password
            savePassword(email, newPassword);
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rs);
                // SqlUtils.closeStatement(ps);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
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
                    + loginSchema + "users SET passwd = ? " + "WHERE email = ?");
            ps.setString(1, encrypted);
            ps.setString(2, email);
            ps.execute();
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(ps);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private boolean isExist(String email) throws WdkUserException {
        email = email.trim();
        // check if user exists in the database. if not, fail and ask to create
        // the user first
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = SqlUtils.getPreparedStatement(dataSource, "SELECT count(*) "
                    + "FROM " + loginSchema + "users WHERE email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            return (count > 0);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rs);
                // SqlUtils.closeStatement(ps);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private String encrypt(String str) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] encrypted = digest.digest(str.getBytes());
        // convert each byte into hex format
        StringBuffer buffer = new StringBuffer();
        for (byte code : encrypted) {
            buffer.append(Integer.toHexString(code & 0xFF));
        }
        return buffer.toString();
    }

    public void deleteUser(String email) throws WdkUserException,
            WdkModelException {
        // get user id
        User user = loadUser(email);

        // delete history
        user.deleteHistories();

        // delete datasets
        user.deleteDatasets();

        String where = " WHERE user_id = " + user.getUserId();
        try {
            // delete preference
            String sql = "DELETE FROM " + loginSchema + "preferences" + where;
            SqlUtils.executeUpdate(dataSource, sql);

            // delete user roles
            sql = "DELETE FROM " + loginSchema + "user_roles" + where;
            SqlUtils.executeUpdate(dataSource, sql);

            // delete user
            sql = "DELETE FROM " + loginSchema + "users" + where;
            SqlUtils.executeUpdate(dataSource, sql);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        }

    }

    public static void main(String[] args) {
        StringBuffer buffer = new StringBuffer();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) {
            int value = rand.nextInt(36);
            if (value < 10) { // number
                buffer.append(value);
                // } else if (value < 36) { // upper case letters
                // buffer.append((char) ('A' + value - 10));
            } else { // lower case letters
                buffer.append((char) ('a' + value - 10));
            }
        }
        String password = buffer.toString();

        System.out.println("random passwd: " + password);
    }
}
