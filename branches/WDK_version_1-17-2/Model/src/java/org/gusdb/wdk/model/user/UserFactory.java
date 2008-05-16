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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;

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

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.*;
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

    static class HistoryKey {

        public int userId;
        public int historyId;

        public HistoryKey(int userId, int historyId) {
            this.userId = userId;
            this.historyId = historyId;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HistoryKey) {
                HistoryKey hkey = (HistoryKey) obj;
                return ((this.userId == hkey.userId) && (this.historyId == hkey.historyId));
            } else return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return (userId + (userId ^ historyId));
        }
    }

    public static final String GUEST_USER_PREFIX = "WDK_GUEST_";

    public static final String GLOBAL_PREFERENCE_KEY = "[Global]";

    private static Logger logger = Logger.getLogger(UserFactory.class);

    private RDBMSPlatformI platform;
    private DataSource dataSource;

    private String loginSchema;
    private String defaultRole;
    private String smtpServer;

    private String projectId;

    // WdkModel is used by the legacy code, may consider to be removed
    private WdkModel wdkModel;

    // the information for registration email
    private String supportEmail;
    private String emailSubject;
    private String emailContent;

    public UserFactory(WdkModel wdkModel, String projectId,
            RDBMSPlatformI platform, String loginSchema, String defaultRole,
            String smtpServer, String supportEmail, String emailSubject,
            String emailContent) {
        this.platform = platform;
        this.dataSource = platform.getDataSource();
        this.wdkModel = wdkModel;
        this.projectId = projectId;
        this.loginSchema = loginSchema;
        this.defaultRole = defaultRole;
        this.smtpServer = smtpServer;
        this.supportEmail = supportEmail;
        this.emailContent = emailContent;
        this.emailSubject = emailSubject;
    }

    public WdkModel getWdkModel() {
        return wdkModel;
    }

    public void sendEmail(String email, String reply, String subject,
            String content) throws WdkUserException {
        logger.debug("Sending message to: " + email + ", reply: " + reply
                + ", using SMPT: " + smtpServer);

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
            String signature = encrypt(userId + "_" + email);

            psUser = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + loginSchema + "users (user_id, email, passwd, is_guest, "
                    + "register_time, last_active, last_name, first_name, "
                    + "middle_name, title, organization, department, address, "
                    + "city, state, zip_code, phone_number, country,signature)"
                    + " VALUES (?, ?, ' ', ?, "
                    + platform.getCurrentDateFunction() + ", "
                    + platform.getCurrentDateFunction() + ", ?, ?, ?, ?, ?,"
                    + "?, ?, ?, ?, ?, ?, ?, ?)");
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
            psUser.setString(16, signature);
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
        } catch (NoSuchAlgorithmException ex) {
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
            String signature = encrypt(userId + "_" + email);
            String firstName = "Guest #" + userId;
            psUser = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + loginSchema + "users (user_id, email, passwd, is_guest, "
                    + "register_time, last_active, first_name, signature) "
                    + "VALUES (?, ?, ' ', 1, "
                    + platform.getCurrentDateFunction() + ", "
                    + platform.getCurrentDateFunction() + ", ?, ?)");
            psUser.setInt(1, userId);
            psUser.setString(2, email);
            psUser.setString(3, firstName);
            psUser.setString(4, signature);
            psUser.executeUpdate();

            User user = new User(wdkModel, userId, email, signature);
            user.setFirstName(firstName);
            user.addUserRole(defaultRole);
            user.setGuest(true);

            // save user's roles
            saveUserRoles(user);

            logger.info("Guest user #" + userId + " created.");

            return user;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } catch (NoSuchAlgorithmException ex) {
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
                    "SELECT user_id " + "FROM " + loginSchema + "users WHERE "
                            + "email = ? AND passwd = ?");
            ps.setString(1, email);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (!rs.next())
                throw new WdkUserException("Invalid email or password.");
            int userId = rs.getInt("user_id");

            // passed validation, load user information
            return loadUser(userId);
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

    /**
     * Only load the basic information of the user
     * 
     * @param email
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public User loadUser(String email) throws WdkUserException {
        email = email.trim();

        ResultSet rsUser = null;
        try {
            // get user information
            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT user_id FROM " + loginSchema
                            + "users WHERE email = ?");
            psUser.setString(1, email);
            rsUser = psUser.executeQuery();
            if (!rsUser.next())
                throw new WdkUserException("The user with email '" + email
                        + "' doesn't exist.");

            // read user info
            int userId = rsUser.getInt("user_id");
            return loadUser(userId);
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

    public User loadUserBySignature(String signature) throws WdkUserException,
            WdkModelException {
        ResultSet rsUser = null;
        try {
            // get user information
            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT user_id FROM " + loginSchema
                            + "users WHERE signature = ?");
            psUser.setString(1, signature);
            rsUser = psUser.executeQuery();
            if (!rsUser.next())
                throw new WdkUserException("The user with signature '"
                        + signature + "' doesn't exist.");

            // read user info
            int userId = rsUser.getInt("user_id");
            return loadUser(userId);
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

    public User loadUser(int userId) throws WdkUserException {
        ResultSet rsUser = null;
        try {
            // get user information
            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource,
                    "SELECT email, signature, is_guest, last_name, "
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

            // load history count
            int historyCount = getHistoryCount(user);
            user.setHistoryCount(historyCount);

            // update user active timestamp
            updateUser(user);

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
        String sql = "SELECT user_id, email FROM " + loginSchema + "users";

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
                int userId = rs.getInt("user_id");
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

    public void checkConsistancy() throws WdkUserException {
        String sql = "SELECT user_id, email FROM " + loginSchema + "users "
                + "where signature is null";

        ResultSet rs = null;
        PreparedStatement psUser = null;
        try {
            // update user's register time
            int count = SqlUtils.executeUpdate(dataSource, "UPDATE "
                    + loginSchema + "users SET register_time = last_active "
                    + "WHERE register_time is null");
            System.out.println(count + " users with empty register_time have "
                    + "been updated");

            // update history's is_delete field
            count = SqlUtils.executeUpdate(dataSource, "UPDATE " + loginSchema
                    + "histories SET is_deleted = 0 WHERE is_deleted is null");
            System.out.println(count + " histories with empty is_deleted have "
                    + "been updated");

            // update user's signature
            psUser = SqlUtils.getPreparedStatement(dataSource, "Update "
                    + loginSchema + "users SET signature = ? WHERE user_id = ?");

            rs = SqlUtils.getResultSet(dataSource, sql);
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
            try {
                SqlUtils.closeResultSet(rs);
                SqlUtils.closeStatement(psUser);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
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
                    + "last_active = " + platform.getCurrentDateFunction()
                    + ", last_name = ?, first_name = ?, "
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
    private void updateUser(User user) throws WdkUserException {
        PreparedStatement psUser = null;
        try {
            psUser = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "users SET last_active = "
                    + platform.getCurrentDateFunction() + " WHERE user_id = ?");
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

    public void deleteExpiredUsers(int hoursSinceActive)
            throws WdkUserException, WdkModelException {
        ResultSet rsUser = null;
        try {
            // construct time
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -hoursSinceActive);
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());

            PreparedStatement psUser = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT email FROM " + loginSchema + "users "
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
            try {
                SqlUtils.closeResultSet(rsUser);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    Map<Integer, History> loadHistories(User user,
            Map<Integer, History> invalidHistories) throws WdkUserException,
            WdkModelException {
        Map<Integer, History> histories = new LinkedHashMap<Integer, History>();

        ResultSet rsHistory = null;
        try {
            PreparedStatement psHistory = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT history_id, question_name, create_time"
                            + ", last_run_time, custom_name, estimate_size, "
                            + "is_boolean, is_deleted, params FROM "
                            + loginSchema + "histories WHERE user_id = ? "
                            + "AND project_id = ? ORDER BY last_run_time DESC");
            psHistory.setInt(1, user.getUserId());
            psHistory.setString(2, projectId);
            rsHistory = psHistory.executeQuery();

            while (rsHistory.next()) {
                // load history info
                int historyId = rsHistory.getInt("history_id");

                History history = loadHistory(user, historyId, rsHistory);

                if (history.isValid()) histories.put(historyId, history);
                else invalidHistories.put(historyId, history);
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

    History loadHistory(User user, int historyId) throws WdkUserException {
        ResultSet rsHistory = null;
        try {
            PreparedStatement psHistory = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT question_name, create_time, "
                            + "last_run_time, custom_name, estimate_size, "
                            + "is_boolean, is_deleted, params FROM "
                            + loginSchema + "histories WHERE user_id = ? "
                            + "AND project_id = ? AND history_id = ? "
                            + "ORDER BY last_run_time DESC");
            psHistory.setInt(1, user.getUserId());
            psHistory.setString(2, projectId);
            psHistory.setInt(3, historyId);
            rsHistory = psHistory.executeQuery();
            if (!rsHistory.next())
                throw new SQLException("The history #" + historyId
                        + " of user " + user.getEmail() + " doesn't exist.");

            return loadHistory(user, historyId, rsHistory);
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

    private History loadHistory(User user, int historyId, ResultSet rsHistory)
            throws SQLException {
        History history = new History(this, user, historyId);

        // load history info
        Timestamp createTime = rsHistory.getTimestamp("create_time");
        Timestamp lastRunTime = rsHistory.getTimestamp("last_run_time");

        history.setCreatedTime(new Date(createTime.getTime()));
        history.setLastRunTime(new Date(lastRunTime.getTime()));
        history.setCustomName(rsHistory.getString("custom_name"));
        history.setEstimateSize(rsHistory.getInt("estimate_size"));
        history.setBoolean(rsHistory.getBoolean("is_boolean"));
        history.setDeleted(rsHistory.getBoolean("is_deleted"));

        String instanceContent = platform.getClobData(rsHistory, "params");
        history.setQuestionName(rsHistory.getString("question_name"));

        // re-construct the answer
        try {
            constructAnswer(history, instanceContent);
        } catch (WdkModelException ex) {
            history.setValid(false);
        } catch (WdkUserException ex) {
            history.setValid(false);
        }

        return history;
    }

    private Map<String, Object> parseParams(boolean isBoolean,
            String paramsClob, Object[] subTypeInfo) {
        String[] parts = paramsClob.split(Utilities.DATA_DIVIDER);
        int base = 1;

        // the first element is query name, ignored;
        // the second could be subtype label or the first param-value pair
        // if the second is subtype label then:
        // * the third is subtype value, and
        // * the fourth is expand flag, and
        // * param-value pair starts from the fifth item
        if (parts[1].equals(Utilities.SUB_TYPE)) {
            base = 4;
            subTypeInfo[0] = parts[2];
            subTypeInfo[1] = Boolean.parseBoolean(parts[3]);
        }

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        if (isBoolean) {// the last part for boolean is a boolean expression
            params.put(Utilities.BOOLEAN_EXPRESSION_PARAM_KEY, parts[base]);
        } else {
            for (int i = base; i < parts.length; i++) {
                String pvPair = parts[i];
                int index = pvPair.indexOf('=');
                String paramName = pvPair.substring(0, index).trim();
                String value = pvPair.substring(index + 1).trim();
                params.put(paramName, value);
            }
        }
        return params;
    }

    private void constructAnswer(History history, String instanceContent)
            throws WdkModelException, WdkUserException {
        User user = history.getUser();
        Object[] subTypeInfo = { null, false };
        Map<String, Object> params = parseParams(history.isBoolean(),
                instanceContent, subTypeInfo);
        Answer answer;
        if (history.isBoolean()) {
            String expression = (String) params.get(Utilities.BOOLEAN_EXPRESSION_PARAM_KEY);
            history.setBooleanExpression(expression);

            BooleanExpression exp = new BooleanExpression(user);
            Map<String, String> operatorMap = getWdkModel().getBooleanOperators();
            BooleanQuestionNode root = exp.parseExpression(expression,
                    operatorMap);
            answer = root.makeAnswer(1, user.getItemsPerPage());
            answer.setSubTypeValue(subTypeInfo[0]);
            answer.setExpandSubType((Boolean) subTypeInfo[1]);
        } else {
            // obtain the question with full name
            String questionName = history.getQuestionName();
            Question question = (Question) wdkModel.resolveReference(questionName);

            // get the user's preferences
            answer = question.makeAnswer(params, 1, user.getItemsPerPage(),
                    user.getSortingAttributes(questionName), subTypeInfo[0]);
            String[] summaryAttributes = user.getSummaryAttributes(questionName);
            answer.setSumaryAttributes(summaryAttributes);
        }
        history.setParams(params);
        history.setAnswer(answer);
    }

    History createHistory(User user, Answer answer, String booleanExpression,
            boolean deleted) throws WdkUserException, WdkModelException {
        int userId = user.getUserId();
        String questionName = answer.getQuestion().getFullName();

        boolean isBoolean = answer.getIsBoolean();
        // String customName = (isBoolean) ? booleanExpression : null;
        // if (customName != null && customName.length() > 4000)
        // customName = customName.substring(0, 4000);
        String customName = null;

        int estimateSize = answer.getResultSize();
        QueryInstance qinstance = answer.getIdsQueryInstance();
        String qiChecksum = qinstance.getChecksum();
        String signature = qinstance.getQuery().getSignature();
        String instanceContent = qinstance.getQueryInstanceContent(!isBoolean);
        if (isBoolean)
            instanceContent += Utilities.DATA_DIVIDER + booleanExpression;

        // check whether the answer exist or not
        ResultSet rsHistory = null;
        PreparedStatement psHistory = null;
        ResultSet rsMax = null;

        Connection connection = null;
        try {
            PreparedStatement psCheck = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT history_id FROM " + loginSchema
                            + "histories WHERE user_id = ? AND project_id = ? "
                            + "AND query_instance_checksum = ? "
                            + "AND is_deleted = ?");
            psCheck.setInt(1, userId);
            psCheck.setString(2, projectId);
            psCheck.setString(3, qiChecksum);
            psCheck.setBoolean(4, deleted);
            rsHistory = psCheck.executeQuery();

            if (rsHistory.next()) {
                // get existing history
                int historyId = rsHistory.getInt("history_id");
                History history = loadHistory(user, historyId);

                // update the history time stamp
                history.update();
                return history;
            }

            // no existing ones matched, get a new history id
            // int historyId = getMaxHistoryId( userId ) + 1;
            // instead of getting a new history id, we start a transaction,
            // insert a new history with generated id, and read it back
            Date createTime = new Date();
            Date lastRunTime = new Date(createTime.getTime());

            int historyId = 1;

            connection = dataSource.getConnection();
            synchronized (connection) {

                connection.setAutoCommit(false);

                psHistory = connection.prepareStatement("INSERT INTO "
                        + loginSchema + "histories (history_id, "
                        + "user_id, project_id, question_name, create_time, "
                        + "last_run_time, custom_name, estimate_size, "
                        + "query_instance_checksum, query_signature, "
                        + "is_boolean, is_deleted, params) VALUES "
                        + "((SELECT max(max_id) + 1  FROM "
                        + "(SELECT max(history_id) AS max_id FROM "
                        + loginSchema + "histories WHERE user_id = " + userId
                        + " UNION SELECT count(*) AS max_id FROM "
                        + loginSchema + "histories WHERE user_id = ?) f), "
                        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "0, ?)");
                psHistory.setInt(1, userId);
                psHistory.setInt(2, userId);
                psHistory.setString(3, projectId);
                psHistory.setString(4, questionName);
                psHistory.setTimestamp(5, new Timestamp(createTime.getTime()));
                psHistory.setTimestamp(6, new Timestamp(lastRunTime.getTime()));
                psHistory.setString(7, customName);
                psHistory.setInt(8, estimateSize);
                psHistory.setString(9, qiChecksum);
                psHistory.setString(10, signature);
                psHistory.setBoolean(11, isBoolean);
                // the platform set clob, and run the statement
                platform.updateClobData(psHistory, 12, instanceContent, false);
                psHistory.executeUpdate();

                // query to get the new history id
                PreparedStatement psMax = connection.prepareStatement("SELECT"
                        + " max(history_id) AS max_id FROM " + loginSchema
                        + "histories WHERE user_id = ?");
                psMax.setInt(1, userId);
                rsMax = psMax.executeQuery();
                if (rsMax.next()) historyId = rsMax.getInt("max_id");

                connection.commit();
            }
            // create the History
            History history = new History(this, user, historyId);
            history.setAnswer(answer);
            history.setCreatedTime(createTime);
            history.setLastRunTime(lastRunTime);
            history.setCustomName(customName);
            history.setEstimateSize(estimateSize);
            history.setBoolean(answer.getIsBoolean());
            history.setQuestionName(questionName);

            // update the user's history count
            int historyCount = getHistoryCount(user);
            user.setHistoryCount(historyCount);

            return history;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
                SqlUtils.closeStatement(psHistory);
                SqlUtils.closeResultSet(rsHistory);
                SqlUtils.closeResultSet(rsMax);
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

        // TEST
        logger.debug("Save custom name: '" + history.getBaseCustomName() + "'");

        // update custom name
        Date lastRunTime = (updateTime) ? new Date() : history.getLastRunTime();
        PreparedStatement psHistory = null;
        try {
            psHistory = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "histories SET custom_name = ?, "
                    + "last_run_time = ?, is_deleted = ?, estimate_size = ?"
                    + "WHERE user_id = ? AND project_id = ? AND history_id = ?");
            psHistory.setString(1, history.getBaseCustomName());
            psHistory.setTimestamp(2, new Timestamp(lastRunTime.getTime()));
            psHistory.setBoolean(3, history.isDeleted());
            psHistory.setInt(4, history.getEstimateSize());
            psHistory.setInt(5, user.getUserId());
            psHistory.setString(6, projectId);
            psHistory.setInt(7, history.getHistoryId());
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

    void deleteHistories(User user, boolean allProjects)
            throws WdkUserException {
        PreparedStatement psHistory = null;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("DELETE FROM " + loginSchema + "histories "
                    + "WHERE user_id = ? ");
            if (!allProjects) sql.append("AND project_id = ?");
            psHistory = SqlUtils.getPreparedStatement(dataSource,
                    sql.toString());

            psHistory.setInt(1, user.getUserId());
            if (!allProjects) psHistory.setString(2, projectId);
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

    public void deleteInvalidHistories(User user) throws WdkUserException,
            WdkModelException {
        // get invalid histories
        Map<Integer, History> invalidHistories = new LinkedHashMap<Integer, History>();
        loadHistories(user, invalidHistories);
        for (int historyId : invalidHistories.keySet()) {
            deleteHistory(user, historyId);
        }
    }

    public void deleteInvalidHistories(Map<String, String> signatures)
            throws WdkUserException {
        ResultSet rsHistory = null;
        PreparedStatement psDelete = null;
        try {
            // get invalid histories
            PreparedStatement psHistory = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT user_id, history_id, question_name, "
                            + "is_boolean, signature FROM " + loginSchema
                            + "histories where project_id = ?");
            psHistory.setString(1, projectId);
            rsHistory = psHistory.executeQuery();
            Set<HistoryKey> histories = new LinkedHashSet<HistoryKey>();
            while (rsHistory.next()) {
                int userId = rsHistory.getInt("user_id");
                int historyId = rsHistory.getInt("history_id");
                String questionName = rsHistory.getString("question_name");
                boolean isBoolean = rsHistory.getBoolean("is_boolean");
                String signature = rsHistory.getString("signature");
                if (!isBoolean) {
                    if (!signatures.containsKey(questionName)) {
                        // check if the question still exists
                        histories.add(new HistoryKey(userId, historyId));
                        continue;
                    } else if (!signature.equals(signatures.get(questionName))) {
                        // check if the parameter names/number has been changed
                        histories.add(new HistoryKey(userId, historyId));
                        continue;
                    }
                }
                // check if the history has valid parameter values by trying
                // to make an answer
                try {
                    User user = loadUser(userId);
                    History history = loadHistory(user, historyId);
                    // remove deleted, undepended histories
                    if (history.isDeleted() && !history.isDepended())
                        histories.add(new HistoryKey(userId, historyId));
                } catch (WdkModelException ex) {
                    histories.add(new HistoryKey(userId, historyId));
                }

            }

            System.out.print(histories.size()
                    + " invalid Histories found. Deleting them...");

            // delete invalid histories
            psDelete = SqlUtils.getPreparedStatement(dataSource, "DELETE FROM "
                    + loginSchema + "histories WHERE user_id = ? AND "
                    + "project_id = ? AND history_id = ?");
            for (HistoryKey history : histories) {
                psDelete.setInt(1, history.userId);
                psDelete.setString(2, projectId);
                psDelete.setInt(3, history.historyId);
                psDelete.executeUpdate();
            }
            System.out.println("done.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsHistory);
                SqlUtils.closeStatement(psDelete);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private int getHistoryCount(User user) throws WdkUserException {
        ResultSet rsHistory = null;
        try {
            PreparedStatement psHistory = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT count(*) AS num FROM " + loginSchema
                            + "histories WHERE user_id = ? AND project_id = ? "
                            + "AND is_deleted = 0");
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
                Matcher.quoteReplacement(user.getFirstName()));
        message = message.replaceAll("\\$\\$EMAIL\\$\\$",
                Matcher.quoteReplacement(email));
        message = message.replaceAll("\\$\\$PASSWORD\\$\\$",
                Matcher.quoteReplacement(password));
        sendEmail(user.getEmail(), supportEmail, emailSubject, message);
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
                    + "FROM " + loginSchema + "users WHERE email =? "
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

    String encrypt(String str) throws NoSuchAlgorithmException {
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

        // delete history from all projects
        user.deleteHistories(true);

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

    public String getLoginSchema() {
        return loginSchema;
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
