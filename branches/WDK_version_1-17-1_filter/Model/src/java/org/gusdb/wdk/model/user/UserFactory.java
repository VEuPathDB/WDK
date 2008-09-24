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


    private Answer createAnswer(User user, AnswerValue result, String booleanExpression)
 	throws WdkUserException, WdkModelException {
	//int userId = user.getUserId();
        String questionName = result.getQuestion().getFullName();

        boolean isBoolean = result.getIsBoolean();

        int estimateSize = result.getResultSize();
        QueryInstance qinstance = result.getIdsQueryInstance();
        String qiChecksum = qinstance.getChecksum();
        String signature = qinstance.getQuery().getSignature();
        String params = booleanExpression;
        if (!isBoolean)
            params = qinstance.getQuery().getFullName()
                    + qinstance.getParamsContent();
	else {
	    // change booleanExpression to use answer ids
	    int stepId = Integer.parseInt(params.substring(0, params.indexOf(" ")));
	    Step step = loadStep(user, stepId);
	    params = step.getAnswer().getAnswerId() + params.substring(params.indexOf(" "), params.length());

	    stepId = Integer.parseInt(params.substring(params.lastIndexOf(" ") + 1, params.length()));
	    step = loadStep(user, stepId);
	    params = params.substring(0, params.lastIndexOf(" ") + 1) + step.getAnswer().getAnswerId();
	    logger.debug("Original boolean expression: " + booleanExpression);
	    logger.debug("Translated boolean expression: " + params);
	}
        // check whether the Answer exists or not
	PreparedStatement psAnswer = null;
	PreparedStatement psCheck = null;
	PreparedStatement psMax = null;
        ResultSet rsAnswer = null;
        ResultSet rsMax = null;

        Connection connection = null;
        try {	    
	    psCheck = SqlUtils.getPreparedStatement(dataSource,
		    "SELECT answer_id FROM " + loginSchema
		    + "answers WHERE project_id = ? "
		    + "AND query_instance_checksum = ?");
	    psCheck.setString(1, projectId);
	    psCheck.setString(2, qiChecksum);
	    rsAnswer = psCheck.executeQuery();

            if (rsAnswer.next()) {
                // get existing Answer
                int answerId = rsAnswer.getInt("answer_id");
		String leftChildId = null;
		String rightChildId = null;
		if (isBoolean) {
		    leftChildId = booleanExpression.substring(0, booleanExpression.indexOf(" "));
		    rightChildId = booleanExpression.substring(booleanExpression.lastIndexOf(" ") + 1, booleanExpression.length());
		}
                Answer answer = loadAnswer(user, answerId, leftChildId, rightChildId);

		return answer;
            }

	    // No existing Answer; create a new Answer w/ new answer id
            Date createTime = new Date();
            Date lastRunTime = new Date(createTime.getTime());

            int answerId = Integer.parseInt(platform.getNextId(loginSchema,
                    "answers"));

            connection = dataSource.getConnection();
            synchronized (connection) {

                connection.setAutoCommit(false);

                psAnswer = connection.prepareStatement("INSERT INTO "
                        + loginSchema + "answers (answer_id, "
                        + "project_id, question_name, estimate_size, "
                        + "query_instance_checksum, query_signature, "
                        + "is_boolean, params) VALUES "
                        + "(?, ?, ?, ?, ?, ?, ?, ?)");
                psAnswer.setInt(1, answerId);
                psAnswer.setString(2, projectId);
                psAnswer.setString(3, questionName);
		psAnswer.setInt(4, estimateSize);
                psAnswer.setString(5, qiChecksum);
                psAnswer.setString(6, signature);
                psAnswer.setBoolean(7, isBoolean);
                // the platform set clob, and run the statement
                platform.updateClobData(psAnswer, 8, params, false);
                psAnswer.executeUpdate();

                connection.commit();
            }

            // create the Answer
            Answer answer = new Answer(this, user, answerId);
            answer.setAnswerValue(result);
	    answer.setEstimateSize(estimateSize);
            answer.setBoolean(result.getIsBoolean());
            answer.setQuestionName(questionName);

            return answer;
        } catch (SQLException ex) {
	    System.out.println(ex.getCause());
	    System.out.println(ex.getMessage());
	    ex.printStackTrace();
            throw new WdkUserException(ex);
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
		SqlUtils.closeStatement(psCheck);
		SqlUtils.closeStatement(psAnswer);
		SqlUtils.closeResultSet(rsMax);
                SqlUtils.closeResultSet(rsAnswer);
	    } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    private Answer loadAnswer(User user, int answerId, String leftChildId, String rightChildId) 
	throws WdkUserException {
	ResultSet rsHistory = null;
	PreparedStatement psHistory = null;
	try {
	    psHistory = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT question_name, estimate_size,"
		    + "is_boolean, params FROM "
		    + loginSchema + "answers WHERE answer_id = ?");
	    psHistory.setInt(1, answerId);
	    rsHistory = psHistory.executeQuery();
	    if (!rsHistory.next()) 
		throw new SQLException("The global history #" + answerId + " does not exist.");

	    Answer answer = new Answer(this, user, answerId);
            answer.setEstimateSize(rsHistory.getInt("estimate_size"));
            answer.setBoolean(rsHistory.getBoolean("is_boolean"));
            String paramsClob = platform.getClobData(rsHistory, "params");
            String questionName = rsHistory.getString("question_name");
            answer.setQuestionName(questionName);

            // get the params
            Map<String, Object> params;
            if (answer.isBoolean()) {
                params = new LinkedHashMap<String, Object>();
                params.put("Boolean Expression", paramsClob);
            } else {
                params = parseParams(paramsClob);
            }
            answer.setParams(params);

            try {
                AnswerValue result;
                if (answer.isBoolean()) {
		    // need to translate back to user ids in boolean expression
		    // in order to correctly reconstruct answer value
		    String boolExp = leftChildId + paramsClob.substring(paramsClob.indexOf(" "), paramsClob.lastIndexOf(" ") + 1)
			+ rightChildId;
                    result = constructBooleanAnswerValue(user, boolExp);
                    answer.setBooleanExpression(paramsClob);
                } else {
                    result = constructAnswerValue(user, questionName, params);
                }
                answer.setAnswerValue(result);
            } catch (WdkModelException ex) {
		System.out.println("Answer has no AnswerValue!");
                answer.setValid(false);
            } catch (WdkUserException ex) {
		System.out.println("Answer has no AnswerValue!");
                answer.setValid(false);
	    }
	    
	    return answer;
	} catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
		SqlUtils.closeStatement(psHistory);
                SqlUtils.closeResultSet(rsHistory);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    // TODO:  Rewrite for Steps?
    Map<Integer, Step> loadSteps(User user, Map<Integer,Step> invalidUserAnswers)
	throws WdkUserException, WdkModelException {
        Map<Integer, Step> userAnswers = new LinkedHashMap<Integer, Step>();

        ResultSet rsUserAnswer = null;
	PreparedStatement psUserAnswer = null;
        try {
            psUserAnswer = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT step_id, display_id, question_name, create_time"
                            + ", last_run_time, custom_name, estimate_size, "
                            + "is_boolean, is_deleted, params, "
		            + "ga.answer_id AS answer_id FROM "
                            + loginSchema + "steps ua, " + loginSchema
		            + "answers ga WHERE ua.user_id = ? "
                            + "AND ua.project_id = ? AND ga.answer_id = ua.answer_id "
		            + "AND ga.project_id = ua.project_id "
		            + "ORDER BY last_run_time DESC");
            psUserAnswer.setInt(1, user.getUserId());
            psUserAnswer.setString(2, projectId);
            rsUserAnswer = psUserAnswer.executeQuery();

            while (rsUserAnswer.next()) {
                // load history info
                int stepId = rsUserAnswer.getInt("display_id");
		int internalId = rsUserAnswer.getInt("step_id");
		int answerId = rsUserAnswer.getInt("answer_id");
                Timestamp createTime = rsUserAnswer.getTimestamp("create_time");
                Timestamp lastRunTime = rsUserAnswer.getTimestamp("last_run_time");

                Step userAnswer = new Step(this, user, stepId, internalId);
		Answer answer = new Answer(this, user, answerId);
		userAnswer.setAnswer(answer);
                userAnswer.setCreatedTime(new Date(createTime.getTime()));
                userAnswer.setLastRunTime(new Date(lastRunTime.getTime()));
                userAnswer.setCustomName(rsUserAnswer.getString("custom_name"));
                userAnswer.setEstimateSize(rsUserAnswer.getInt("estimate_size"));
                userAnswer.setBoolean(rsUserAnswer.getBoolean("is_boolean"));
                userAnswer.setDeleted(rsUserAnswer.getBoolean("is_deleted"));

                String paramsClob = platform.getClobData(rsUserAnswer, "params");
                String questionName = rsUserAnswer.getString("question_name");
                userAnswer.setQuestionName(questionName);

                // re-construct the result
                AnswerValue result;

                // get the params
                Map<String, Object> params;
                if (userAnswer.isBoolean()) {
                    params = new LinkedHashMap<String, Object>();
                    params.put("Boolean Expression", paramsClob);
                } else {
                    params = parseParams(paramsClob);
                }
                userAnswer.setParams(params);

                // construct answer of the history
                try {
                    if (userAnswer.isBoolean()) {
                        result = constructBooleanAnswerValue(user, paramsClob);
                        userAnswer.setBooleanExpression(paramsClob);
                    } else {
                        result = constructAnswerValue(user, questionName, params);
                    }
                    userAnswer.setAnswerValue(result);
                    userAnswers.put(stepId, userAnswer);
                } catch (WdkModelException ex) {
                    // invalid userAnswer
                    userAnswer.setValid(false);
                    invalidUserAnswers.put(stepId, userAnswer);
                } catch (WdkUserException ex) {
                    // invalid userAnswer
                    userAnswer.setValid(false);
                    invalidUserAnswers.put(stepId, userAnswer);
                }
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
		SqlUtils.closeStatement(psUserAnswer);
                SqlUtils.closeResultSet(rsUserAnswer);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
        return userAnswers;
    }

    // get left child id, right child id in here
    Step loadStep(User user, int stepId) throws WdkUserException {
        ResultSet rsStep = null;
	PreparedStatement psStep = null;
        try {
            psStep = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT step_id, answer_id, create_time, last_run_time, "
                            + "custom_name, is_deleted, left_child_id, right_child_id FROM "
                            + loginSchema + "steps WHERE user_id = ? "
                            + "AND project_id = ? AND display_id = ? "
                            + "ORDER BY last_run_time DESC");
            psStep.setInt(1, user.getUserId());
            psStep.setString(2, projectId);
            psStep.setInt(3, stepId);
            rsStep = psStep.executeQuery();
            if (!rsStep.next())
                throw new SQLException("The history #" + stepId
                        + " of user " + user.getEmail() + " doesn't exist.");

            // load Step info
            Timestamp createTime = rsStep.getTimestamp("create_time");
            Timestamp lastRunTime = rsStep.getTimestamp("last_run_time");
	    int internalId = rsStep.getInt("step_id");

            Step step = new Step(this, user, stepId, internalId);
            step.setCreatedTime(new Date(createTime.getTime()));
            step.setLastRunTime(new Date(lastRunTime.getTime()));
            step.setCustomName(rsStep.getString("custom_name"));
            step.setDeleted(rsStep.getBoolean("is_deleted"));

	    // load Answer
	    int answerId = rsStep.getInt("answer_id");
	    String leftChildId = rsStep.getString("left_child_id");
	    String rightChildId = rsStep.getString("right_child_id");
	    Answer answer = loadAnswer(user, answerId, leftChildId, rightChildId);

	    step.setAnswer(answer);

            return step;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
		SqlUtils.closeStatement(psStep);
                SqlUtils.closeResultSet(rsStep);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    // parse boolexp to pass left_child_id, right_child_id to loadAnswer
    Step createStep(User user, AnswerValue result, String booleanExpression, boolean deleted)
	throws WdkUserException, WdkModelException { 
	int userId = user.getUserId();
        String questionName = result.getQuestion().getFullName();

        boolean isBoolean = result.getIsBoolean();
        String customName = null;

        int estimateSize = result.getResultSize();
        QueryInstance qinstance = result.getIdsQueryInstance();
        String qiChecksum = qinstance.getChecksum();
        String signature = qinstance.getQuery().getSignature();
        String params = booleanExpression;
        if (!isBoolean)
            params = qinstance.getQuery().getFullName()
                    + qinstance.getParamsContent();

	PreparedStatement psAnswer = null;
	PreparedStatement psCheck = null;
	PreparedStatement psUserAnswer = null;
	PreparedStatement psMax = null;
	ResultSet rsAnswer = null;
        ResultSet rsUserAnswer = null;
        ResultSet rsMax = null;

        Connection connection = null;
	
	Answer answer = null;

	try {
	    // check whether the Answer exists or not
	    psCheck = SqlUtils.getPreparedStatement(
       	            dataSource, "SELECT answer_id FROM " + loginSchema
		    + "answers WHERE project_id = ? "
		    + "AND query_instance_checksum = ?");
	    psCheck.setString(1, projectId);
	    psCheck.setString(2, qiChecksum);

	    rsAnswer = psCheck.executeQuery();

	    // Load Answer if exists;  if not, create it
	    if (rsAnswer.next()) {
		int answerId = rsAnswer.getInt("answer_id");
		String leftChildId = null;
		String rightChildId = null;
		if (isBoolean) {
		    leftChildId = booleanExpression.substring(0, booleanExpression.indexOf(" "));
		    rightChildId = booleanExpression.substring(booleanExpression.lastIndexOf(" ") + 1, booleanExpression.length());
		}
		answer = loadAnswer(user, answerId, leftChildId, rightChildId);
		System.out.println("Answer id: " + answerId);
	    }
	    else {
		answer = createAnswer(user, result, booleanExpression);
	    }
	
	    // Now that we have the Answer, create the UserAnswer
            Date createTime = new Date();
            Date lastRunTime = new Date(createTime.getTime());
	    
            int stepId = -1;
            int internalId = Integer.parseInt(platform.getNextId(loginSchema,
                    "steps"));
            connection = dataSource.getConnection();
            synchronized (connection) {
		
                connection.setAutoCommit(false);
		
                psUserAnswer = connection.prepareStatement("INSERT INTO "
			+ loginSchema + "steps (display_id, step_id,"
                        + "user_id, answer_id, project_id, create_time, "
                        + "last_run_time, custom_name, is_deleted) VALUES "
                        + "((SELECT max(max_id) + 1  FROM "
                        + "(SELECT max(display_id) AS max_id FROM "
                        + loginSchema + "steps WHERE user_id = " + userId
                        + " UNION SELECT count(*) AS max_id FROM "
                        + loginSchema + "steps WHERE user_id = ?) f), "
                        + "?, ?, ?, ?, ?, ?, ?, 0)");
                psUserAnswer.setInt(1, userId);
		psUserAnswer.setInt(2, internalId);
                psUserAnswer.setInt(3, userId);
		psUserAnswer.setInt(4, answer.getAnswerId());
                psUserAnswer.setString(5, projectId);
		psUserAnswer.setTimestamp(6, new Timestamp(createTime.getTime()));
                psUserAnswer.setTimestamp(7, new Timestamp(lastRunTime.getTime()));
                psUserAnswer.setString(8, customName);
		psUserAnswer.executeUpdate();

                // query to get the new history id
                psMax = connection.prepareStatement("SELECT"
                        + " max(display_id) AS max_id FROM " + loginSchema
                        + "steps WHERE user_id = ?");
                psMax.setInt(1, userId);
                rsMax = psMax.executeQuery();
                if (rsMax.next()) stepId = rsMax.getInt("max_id");

                connection.commit();
            }
	    if (stepId < 0) {
		throw new WdkModelException("An unknown error occurred while creating step.");
	    }
            // create the History
            Step userAnswer = new Step(this, user, stepId, internalId);
	    userAnswer.setAnswer(answer);
            userAnswer.setAnswerValue(result);
            userAnswer.setCreatedTime(createTime);
            userAnswer.setLastRunTime(lastRunTime);
            userAnswer.setCustomName(customName);
	    
	    // Need to insert children into steps table
	    // NOTE:  This will continue to assume we're working in simple,
	    // two-operand booleans!  Need to find a way to not do this!
	    if (isBoolean) {
		updateUserAnswerTree(user, userAnswer, booleanExpression);
	    }
	    else if (userAnswer.isTransform()) {
		updateUserAnswerTree(user, userAnswer, null);
	    }

            // update the user's history count
	    // change to update user's answer count?
            int historyCount = getHistoryCount(user);
            user.setHistoryCount(historyCount);

            return userAnswer;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
		SqlUtils.closeStatement(psAnswer);
		SqlUtils.closeStatement(psCheck);
		SqlUtils.closeStatement(psUserAnswer);
                SqlUtils.closeResultSet(rsAnswer);
		SqlUtils.closeResultSet(rsUserAnswer);
		SqlUtils.closeResultSet(rsMax);
	    } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    // Note:  this function still assumes two-operand booleans.  will need
    // to be rewritten?
    private void updateUserAnswerTree(User user, Step userAnswer, String booleanExpression) 
	throws WdkUserException {
	PreparedStatement psUpdateAnswerTree = null;

	try {
	    int leftChildId;
	    int rightChildId;
	    if (booleanExpression != null && booleanExpression.length() != 0) {

		psUpdateAnswerTree = SqlUtils.getPreparedStatement(
	            dataSource, "UPDATE " + loginSchema +
		    "steps SET left_child_id = ?, "
		    + "right_child_id = ? "
		    + "WHERE step_id = ?");
		System.out.println("Updating answer tree: " + booleanExpression);
		leftChildId = Integer.parseInt(booleanExpression.substring(0, booleanExpression.indexOf(" ")));
		System.out.println("Left: " + leftChildId);
		rightChildId = Integer.parseInt(booleanExpression.substring(booleanExpression.lastIndexOf(" ") + 1,
									    booleanExpression.length()));
		System.out.println("Right: " + rightChildId);
		psUpdateAnswerTree.setInt(1, leftChildId);
		psUpdateAnswerTree.setInt(2, rightChildId);
		psUpdateAnswerTree.setInt(3, userAnswer.getInternalId());
	    }
	    else {
		psUpdateAnswerTree = SqlUtils.getPreparedStatement(dataSource, "UPDATE " + loginSchema +
								   "steps SET left_child_id = ? "
								   + "WHERE step_id = ?");
		Param[] params = userAnswer.getAnswerValue().getQuestion().getParams();
		HistoryParam histParam = null;
		for ( Param param : params ) {
		    if ( param instanceof HistoryParam ) {
			histParam = (HistoryParam)param;
		    }
		}

		if (histParam == null) 
		    throw new WdkUserException("Transform query has no HistoryParam.");

		Map<String, Object> paramVals = userAnswer.getAnswerValue().getParams();
		leftChildId = Integer.parseInt((String) paramVals.get(histParam.getName()));
		psUpdateAnswerTree.setInt(1, leftChildId);
		psUpdateAnswerTree.setInt(2, userAnswer.getInternalId());
	    }
	    psUpdateAnswerTree.executeUpdate();
	} catch (SQLException ex) {
	    throw new WdkUserException(ex);
	} finally {
	    try {
		SqlUtils.closeStatement(psUpdateAnswerTree);
	    } catch (SQLException ex) {
		throw new WdkUserException(ex);
	    }
	}
    }

    /**
     * This method only update the custom name, the time stamp of last running
     * 
     * @param user
     * @param userAnswer
     * @throws WdkUserException
     */
    void updateStep(User user, Step userAnswer, boolean updateTime)
            throws WdkUserException {
        // check email existence
        if (!isExist(user.getEmail()))
            throw new WdkUserException("The user " + user.getEmail()
                    + " doesn't exist. Updating operation cancelled.");

        // TEST
        logger.info("Save custom name: '" + userAnswer.getBaseCustomName() + "'");

        // update custom name
        Date lastRunTime = (updateTime) ? new Date() : userAnswer.getLastRunTime();
        PreparedStatement psUserAnswer = null;
        try {
            psUserAnswer = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "steps SET custom_name = ?, "
                    + " last_run_time = ?, is_deleted = ?"
                    + "WHERE step_id = ?");
            psUserAnswer.setString(1, userAnswer.getBaseCustomName());
            psUserAnswer.setTimestamp(2, new Timestamp(lastRunTime.getTime()));
            psUserAnswer.setBoolean(3, userAnswer.isDeleted());
            psUserAnswer.setInt(4, userAnswer.getInternalId());
            int result = psUserAnswer.executeUpdate();
            if (result == 0)
                throw new WdkUserException("The history #"
                        + userAnswer.getStepId() + " of user "
                        + user.getEmail() + " cannot be found.");

            // update the last run stamp
            userAnswer.setLastRunTime(lastRunTime);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psUserAnswer);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    // Not sure about this one...maybe I'll just go the lazy route and select all strategy ids for the
    // user, then load them w/ loadStrategy...don't think there's a clever join to do this in one
    // sitting
    Map<Integer, Strategy> loadStrategies(User user, Map<Integer, Strategy> invalidStrategies) 
	throws WdkUserException, WdkModelException {
        Map<Integer, Strategy> userStrategies = new LinkedHashMap<Integer, Strategy>();

	PreparedStatement psStrategyIds = null;
	ResultSet rsStrategyIds = null;

	try {
	    psStrategyIds = SqlUtils.getPreparedStatement(
		     dataSource, "SELECT display_id FROM "
		     + loginSchema + "strategies WHERE "
		     + "user_id = ? AND project_id = ?");
	    psStrategyIds.setInt(1, user.getUserId());
	    psStrategyIds.setString(2, projectId);
	    rsStrategyIds = psStrategyIds.executeQuery();

	    Strategy strategy;
	    int strategyId;
	    while (rsStrategyIds.next()) {
		strategyId = rsStrategyIds.getInt("display_id");
		strategy = loadStrategy(user, strategyId);
		userStrategies.put(new Integer(strategyId), strategy);
	    }

	    return userStrategies;
	} catch (SQLException ex) {
	    throw new WdkUserException(ex);
	} finally {
	    try {
		SqlUtils.closeStatement(psStrategyIds);
		SqlUtils.closeResultSet(rsStrategyIds);
	    } catch (SQLException ex) {
		throw new WdkUserException(ex);
	    }
	}
    }

    Strategy importStrategyBySignature(User user, String signature)
	throws WdkUserException, WdkModelException {
	ResultSet rsStrategy = null;
	try {
	    // Get user_id, strategy_id from strategies by signature
	    PreparedStatement psStrategy = SqlUtils.getPreparedStatement(
		     dataSource, "SELECT user_id, strategy_id FROM "
		     + loginSchema + "strategies WHERE "
		     + "signature = ?");
	    psStrategy.setString(1, signature);
	    rsStrategy = psStrategy.executeQuery();
	    if (!rsStrategy.next())
		throw new WdkUserException("The strategy with signature '" + signature + "' doesn't exist.");

	    int userId = rsStrategy.getInt("user_id");
	    int strategyId = rsStrategy.getInt("strategy_id");

	    return importStrategy(user, userId, strategyId);
	} catch (SQLException ex) {
	    throw new WdkUserException(ex);
	} finally {
	    try {
		SqlUtils.closeResultSet(rsStrategy);
	    } catch (SQLException ex) {
		throw new WdkUserException(ex);
	    }
	}
    }

    Strategy importStrategyByGlobalId(User user, int globalId)
	throws WdkUserException, WdkModelException {
	ResultSet rsStrategy = null;
	try {
	    // Get user_id, strategy_id from strategies by signature
	    PreparedStatement psStrategy = SqlUtils.getPreparedStatement(
		     dataSource, "SELECT user_id, display_id FROM "
		     + loginSchema + "strategies WHERE "
		     + "strategy_id = ?");
	    psStrategy.setInt(1, globalId);
	    rsStrategy = psStrategy.executeQuery();
	    if (!rsStrategy.next())
		throw new WdkUserException("The strategy with global id " + globalId + " doesn't exist.");

	    int userId = rsStrategy.getInt("user_id");
	    int strategyId = rsStrategy.getInt("display_id");

	    return importStrategy(user, userId, strategyId);
	} catch (SQLException ex) {
	    throw new WdkUserException(ex);
	} finally {
	    try {
		SqlUtils.closeResultSet(rsStrategy);
	    } catch (SQLException ex) {
		throw new WdkUserException(ex);
	    }
	}
    }

    private Strategy importStrategy(User user, int exportUserId, int strategyId)
	throws WdkUserException, WdkModelException {
	
	// Load export User by user_id
	User exportUser = loadUser(exportUserId);
	// Load Strategy by export User, strategy_id
	Strategy exportStrat = loadStrategy(exportUser, strategyId);

	Step importLatest = importStep(user, exportStrat.getLatestStep());
      
	return createStrategy(user, importLatest, exportStrat.getName(), exportStrat.getIsSaved());
    }

    private Step importStep(User user, Step exportStep)
	throws WdkUserException, WdkModelException {
	Step importStep;
	String booleanExpression = null;

	if (exportStep.getUser() == user) {
	    return exportStep;
	}

	// Is this step a boolean?  Load depended steps first.
	if (exportStep.isBoolean()) {
	    Step leftChild = importStep(user, exportStep.getPreviousStep());
	    Step rightChild = importStep(user, exportStep.getChildStep());
	    booleanExpression = leftChild.getStepId() + " " + exportStep.getOperation() + " " + rightChild.getStepId();
	    return createStep(user, exportStep.getAnswerValue(), booleanExpression, exportStep.isDeleted());
	}
	// Is this step a transform?  Load depended step first.
	else if (exportStep.isTransform()) {
	    throw new WdkModelException("Not implemented yet!!!");
	}
	else {
	    return createStep(user, exportStep.getAnswerValue(), booleanExpression, exportStep.isDeleted());
	}
    }

    // TODO: SQL needs to be changed to work w/ new steps table
    Strategy loadStrategy(User user, int userStrategyId) throws WdkUserException {
	// Get name, saved, latest step_id, and all latest step data
	// FROM strategy table JOIN step table
	// Create strategy object
	// Create latest UserAnswer/Step objects
	// while we get have a non-empty parent stack:
	//   remove latest parent from stack

	//   select parent_id, child_id, child step data FROM
	//   step_tree JOIN step WHERE parent_id = latest parent
	
	//   for each row returned, create Step, add Step to strategy, put child_id on parent stack

	PreparedStatement psStrategy = null;
	PreparedStatement psAnswerTree = null;
	ResultSet rsStrategy = null;
	ResultSet rsAnswerTree = null;
	try {
	    psStrategy = SqlUtils.getPreparedStatement(
		     dataSource, "SELECT strategy_id, name, is_saved, root_step_id "
		     + "FROM " + loginSchema + "strategies WHERE "
		     + "user_id = ? AND display_id = ? "
		     + "AND project_id = ?");
	    psStrategy.setInt(1, user.getUserId());
	    psStrategy.setInt(2, userStrategyId);
	    psStrategy.setString(3, projectId);
	    rsStrategy = psStrategy.executeQuery();
	    if (!rsStrategy.next()) {
		throw new WdkUserException("The strategy " + userStrategyId + " does not exist "
					   + "for user " + user.getEmail());
	    }
	    
	    int internalId = rsStrategy.getInt("strategy_id");

	    Strategy strategy = new Strategy(this, user, userStrategyId, internalId, rsStrategy.getString("name"));
	    strategy.setIsSaved(rsStrategy.getBoolean("is_saved"));

	    // Now add step_id to a stack, and go into while loop
	    int currentStepId = rsStrategy.getInt("root_step_id");
	    Integer currentStepIdObj = new Integer(currentStepId);
	    Step currentStep = loadStep(user, currentStepId);
	    
	    strategy.addStep(currentStep);

	    Stack<Integer> answerTree = new Stack<Integer>();
	    answerTree.push(currentStepIdObj);

	    HashMap<Integer, Step> steps = new HashMap<Integer, Step>();
	    steps.put(currentStepIdObj, currentStep);
	    
	    Integer parentAnswerId;
	    Step parentStep;

	    psAnswerTree = SqlUtils.getPreparedStatement(
	        dataSource, "SELECT left_child_id, right_child_id FROM "
		+ loginSchema + "steps WHERE user_id = ? AND "
		+ "display_id = ? AND project_id = ?");

	    while (!answerTree.empty()) {
		parentAnswerId = answerTree.pop();
		
		psAnswerTree.setInt(1, user.getUserId());
		psAnswerTree.setInt(2, parentAnswerId.intValue());
		psAnswerTree.setString(3, projectId);

		rsAnswerTree = psAnswerTree.executeQuery();
		
		if (rsAnswerTree.next()) {
		    parentStep = steps.get(parentAnswerId);
		    
		    // TODO:  need to check if currentStepId < 1?
		    // left child
		    currentStepId = rsAnswerTree.getInt("left_child_id");
		    if (currentStepId >= 1) {
			currentStepIdObj = new Integer(currentStepId);
			currentStep = loadStep(user, currentStepId);
			answerTree.push(currentStepIdObj);
			steps.put(currentStepIdObj, currentStep);
			
			parentStep.setPreviousStep(currentStep);
			currentStep.setNextStep(parentStep);
		    }
		    // right child
		    currentStepId = rsAnswerTree.getInt("right_child_id");
		    if (currentStepId >= 1) {
			currentStepIdObj = new Integer(currentStepId);
			currentStep = loadStep(user, currentStepId);
			answerTree.push(currentStepIdObj);
			steps.put(currentStepIdObj, currentStep);
		    
			parentStep.setChildStep(currentStep);
		    }
		}
	    }
	    
	    return strategy;
	} catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psStrategy);
                SqlUtils.closeStatement(psAnswerTree);
                SqlUtils.closeResultSet(rsStrategy);
                SqlUtils.closeResultSet(rsAnswerTree);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    // This function only updates the strategies table
    void updateStrategy(User user, Strategy strategy, boolean overwrite)
	throws WdkUserException, WdkModelException {
	if (!isExist(user.getEmail()))
            throw new WdkUserException("The user " + user.getEmail()
                    + " doesn't exist. Updating operation cancelled.");

        // update strategy name, saved, step_id
	PreparedStatement psStrategy = null;
	ResultSet rsStrategy = null;

	int userId = user.getUserId();

        try {
	    if (strategy.getIsSaved()) {
		if (!overwrite) {
		    Strategy newStrat = createStrategy(user, strategy.getLatestStep(),
							       strategy.getName(), false);
		    strategy.setStrategyId(newStrat.getStrategyId());
		    strategy.setInternalId(newStrat.getInternalId());
		    strategy.setIsSaved(newStrat.getIsSaved());
		    return;
		}
		else {
		    PreparedStatement psCheck = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT strategy_id, display_id FROM " + loginSchema
                            + "strategies WHERE user_id = ? AND project_id = ? "
                            + "AND name = ? AND saved = ?");
		    psCheck.setInt(1, userId);
		    psCheck.setString(2, projectId);
		    psCheck.setString(3, strategy.getName());
		    psCheck.setBoolean(4, strategy.getIsSaved());
		    rsStrategy = psCheck.executeQuery();

		    if (rsStrategy.next()) {
			strategy.setStrategyId(rsStrategy.getInt("display_id"));
			strategy.setInternalId(rsStrategy.getInt("strategy_id"));
		    }
		}
	    }

            psStrategy = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "strategies SET name = ?, "
                    + "root_step_id = ?, is_saved = ?"
                    + "WHERE strategy_id = ?");
            psStrategy.setString(1, strategy.getName());
	    psStrategy.setInt(2, strategy.getLatestStep().getStepId());
            psStrategy.setBoolean(3, strategy.getIsSaved());
            psStrategy.setInt(4, strategy.getInternalId());
            int result = psStrategy.executeUpdate();
            if (result == 0)
                throw new WdkUserException("The strategy #"
                        + strategy.getStrategyId() + " of user "
                        + user.getEmail() + " cannot be found.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psStrategy);
		SqlUtils.closeResultSet(rsStrategy);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
	
    }

    // Note:  this function only adds the necessary row in strategies;  updating of answers
    // and steps, is handled in other functions.  Once the Step
    // object exists, all of this data is already in the db.
    Strategy createStrategy(User user, Step root, String name, boolean saved)
	throws WdkUserException, WdkModelException {
	int userId = user.getUserId();

	PreparedStatement psMax = null;
	PreparedStatement psStrategy = null;
        ResultSet rsMax = null;

        Connection connection = null;
	try {
            int strategyId = -1;
            int internalId = Integer.parseInt(platform.getNextId(loginSchema,
                    "strategies"));

            connection = dataSource.getConnection();
            synchronized (connection) {

                connection.setAutoCommit(false);
		
		// insert the row into strategies
		psStrategy = SqlUtils.getPreparedStatement(
			dataSource, "INSERT INTO " + loginSchema + "strategies "
			+ "(display_id, strategy_id, user_id, root_step_id, is_saved, name, project_id) "
			+ "VALUES ((SELECT max(max_id) + 1  FROM "
                        + "(SELECT max(display_id) AS max_id FROM "
                        + loginSchema + "strategies WHERE user_id = " + userId
                        + " UNION SELECT count(*) AS max_id FROM "
                        + loginSchema + "strategies WHERE user_id = ?) f)"
			+ ", ?, ?, ?, ?, ?, ?)");
		psStrategy.setInt(1, userId);
		psStrategy.setInt(2, internalId);
		psStrategy.setInt(3, userId);
		psStrategy.setInt(4, root.getStepId());
		psStrategy.setBoolean(5, saved);
		psStrategy.setString(6, name);
		psStrategy.setString(7, projectId);
		psStrategy.executeUpdate();

		// query to get the new strategy id
                psMax = connection.prepareStatement("SELECT"
                        + " max(display_id) AS max_id FROM " + loginSchema
                        + "strategies WHERE user_id = ? AND project_id = ?");
                psMax.setInt(1, userId);
		psMax.setString(2, projectId);
                rsMax = psMax.executeQuery();
                if (rsMax.next()) strategyId = rsMax.getInt("max_id");

                connection.commit();
	    }
	    if (strategyId < 0) {
		throw new WdkModelException("Unknown error while creating strategy.");
	    }
	   
	    // update the user's strategy count
            int strategyCount = getStrategyCount(user);
            user.setStrategyCount(strategyCount);
	    return loadStrategy(user, strategyId);
	}
	catch (SQLException ex) {
	    throw new WdkUserException(ex);
	}
	finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
		SqlUtils.closeStatement(psStrategy);
		SqlUtils.closeResultSet(rsMax);
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
                Timestamp createTime = rsHistory.getTimestamp("create_time");
                Timestamp lastRunTime = rsHistory.getTimestamp("last_run_time");

                History history = new History(this, user, historyId);
                history.setCreatedTime(new Date(createTime.getTime()));
                history.setLastRunTime(new Date(lastRunTime.getTime()));
                history.setCustomName(rsHistory.getString("custom_name"));
                history.setEstimateSize(rsHistory.getInt("estimate_size"));
                history.setBoolean(rsHistory.getBoolean("is_boolean"));
                history.setDeleted(rsHistory.getBoolean("is_deleted"));

                String paramsClob = platform.getClobData(rsHistory, "params");
                String questionName = rsHistory.getString("question_name");
                history.setQuestionName(questionName);

                // re-construct the answer
                AnswerValue answer;

                // get the params
                Map<String, Object> params;
                if (history.isBoolean()) {
                    params = new LinkedHashMap<String, Object>();
                    params.put("Boolean Expression", paramsClob);
                } else {
                    params = parseParams(paramsClob);
                }
                history.setParams(params);

                // construct answer of the history
                try {
                    if (history.isBoolean()) {
                        answer = constructBooleanAnswerValue(user, paramsClob);
                        history.setBooleanExpression(paramsClob);
                    } else {
                        answer = constructAnswerValue(user, questionName, params);
                    }
                    history.setAnswerValue(answer);
                    histories.put(historyId, history);
                } catch (WdkModelException ex) {
                    // invalid history
                    history.setValid(false);
                    invalidHistories.put(historyId, history);
                } catch (WdkUserException ex) {
                    // invalid history
                    history.setValid(false);
                    invalidHistories.put(historyId, history);
                }
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

            // load history info
            Timestamp createTime = rsHistory.getTimestamp("create_time");
            Timestamp lastRunTime = rsHistory.getTimestamp("last_run_time");

            History history = new History(this, user, historyId);
            history.setCreatedTime(new Date(createTime.getTime()));
            history.setLastRunTime(new Date(lastRunTime.getTime()));
            history.setCustomName(rsHistory.getString("custom_name"));
            history.setEstimateSize(rsHistory.getInt("estimate_size"));
            history.setBoolean(rsHistory.getBoolean("is_boolean"));
            history.setDeleted(rsHistory.getBoolean("is_deleted"));

            String paramsClob = platform.getClobData(rsHistory, "params");
            String questionName = rsHistory.getString("question_name");
            history.setQuestionName(questionName);

            // get the params
            Map<String, Object> params;
            if (history.isBoolean()) {
                params = new LinkedHashMap<String, Object>();
                params.put("Boolean Expression", paramsClob);
            } else {
                params = parseParams(paramsClob);
            }
            history.setParams(params);

            // re-construct the answer
            try {
                AnswerValue answer;
                if (history.isBoolean()) {
                    answer = constructBooleanAnswerValue(user, paramsClob);
                    history.setBooleanExpression(paramsClob);
                } else {
                    answer = constructAnswerValue(user, questionName, params);
                }
                history.setAnswerValue(answer);
            } catch (WdkModelException ex) {
                history.setValid(false);
            } catch (WdkUserException ex) {
                history.setValid(false);
            }

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

    private Map<String, Object> parseParams(String paramsClob) {
        String[] parts = paramsClob.split(Utilities.DATA_DIVIDER);
        // the first element is query name, ignored
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        for (int i = 1; i < parts.length; i++) {
            String pvPair = parts[i];
            int index = pvPair.indexOf('=');
            String paramName = pvPair.substring(0, index).trim();
            String value = pvPair.substring(index + 1).trim();
            params.put(paramName, value);
        }
        return params;
    }

    private AnswerValue constructAnswerValue(User user, String questionName,
            Map<String, Object> params) throws WdkModelException,
            WdkUserException {
        // obtain the question with full name
        Question question = (Question) wdkModel.resolveReference(questionName);

        // get the user's preferences

        AnswerValue answer = question.makeAnswerValue(params, 1, user.getItemsPerPage(),
                user.getSortingAttributes(questionName));
        String[] summaryAttributes = user.getSummaryAttributes(questionName);
        answer.setSumaryAttributes(summaryAttributes);
        return answer;
    }

    private AnswerValue constructBooleanAnswerValue(User user, String expression)
            throws WdkUserException, WdkModelException {
        BooleanExpression exp = new BooleanExpression(user);
        Map<String, String> operatorMap = getWdkModel().getBooleanOperators();
        BooleanQuestionNode root = exp.parseExpression(expression, operatorMap);

        return root.makeAnswerValue(1, user.getItemsPerPage());
    }



    History createHistory(User user, AnswerValue answer, String booleanExpression,
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
        String params = booleanExpression;
        if (!isBoolean)
            params = qinstance.getQuery().getFullName()
                    + qinstance.getParamsContent();

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
                platform.updateClobData(psHistory, 12, params, false);
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
            history.setAnswerValue(answer);
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
        logger.info("Save custom name: '" + history.getBaseCustomName() + "'");

        // update custom name
        Date lastRunTime = (updateTime) ? new Date() : history.getLastRunTime();
        PreparedStatement psHistory = null;
        try {
            psHistory = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "histories SET custom_name = ?, "
                    + " last_run_time = ?, is_deleted = ?, estimate_size = ? "
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

    void updateHistory(History history, String expression)
            throws WdkUserException, WdkModelException {
        User user = history.getUser();
        String email = user.getEmail();

        // check email existence
        if (!isExist(email))
            throw new WdkUserException("The user " + email
                    + " doesn't exist. Updating operation cancelled.");

        // TEST
        logger.info("Change boolean expression: '" + expression + "'");

        // prepare the fields to be updated
        AnswerValue answer = history.getAnswerValue();
        Date updateTime = new Date();
        String qiChecksum = answer.getIdsQueryInstance().getChecksum();
        int estimateSize = answer.getResultSize();
        String params = expression;
        int historyId = history.getHistoryId();

        PreparedStatement psHistory = null;
        try {
            psHistory = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + loginSchema + "histories SET create_time = ?, "
                    + "last_run_time = ?, query_instance_checksum = ?, "
                    + "estimate_size = ?, params = ? "
                    + "WHERE user_id = ? AND project_id = ? "
                    + "AND history_id = ?");
            psHistory.setTimestamp(1, new Timestamp(updateTime.getTime()));
            psHistory.setTimestamp(2, new Timestamp(updateTime.getTime()));
            psHistory.setString(3, qiChecksum);
            psHistory.setInt(4, estimateSize);
            // the platform set clob, and run the statement
            platform.updateClobData(psHistory, 5, params, false);

            psHistory.setInt(6, user.getUserId());
            psHistory.setString(7, projectId);
            psHistory.setInt(8, historyId);
            int result = psHistory.executeUpdate();
            if (result == 0)
                throw new WdkUserException("The history #" + historyId
                        + " of user " + email + " on project " + projectId
                        + " cannot be found.");

            // update the last run stamp
            history.setCreatedTime(updateTime);
            history.setLastRunTime(updateTime);
            history.setEstimateSize(estimateSize);
            history.setBooleanExpression(expression);
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

    void deleteStep(User user, int userAnswerId)
	throws WdkUserException {
        PreparedStatement psUserAnswer = null;
        try {
            // remove user answer
            psUserAnswer = SqlUtils.getPreparedStatement(dataSource, "DELETE "
                    + "FROM " + loginSchema + "steps WHERE user_id = ? "
                    + "AND project_id = ? AND display_id = ?");
            psUserAnswer.setInt(1, user.getUserId());
            psUserAnswer.setString(2, projectId);
            psUserAnswer.setInt(3, userAnswerId);
            int result = psUserAnswer.executeUpdate();
            if (result == 0)
                throw new WdkUserException("The user answer #" + userAnswerId
                        + " of user " + user.getEmail() + " cannot be found.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psUserAnswer);
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

    // TODO: Do we really offer this in the world where Step == UserAnswer?
    void deleteSteps(User user, boolean allProjects)
	throws WdkUserException {
	throw new WdkUserException("UserFactory.deleteUserAnswers() is not implemented.");
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

    // TODO: Do we really offer this in the world where Step == UserAnswer?
    public void deleteInvalidSteps(User user)
	throws WdkUserException, WdkModelException {
	Map<Integer, Step> invalidUserAnswers = new LinkedHashMap<Integer, Step>();
	loadSteps(user, invalidUserAnswers);
	for (int userAnswerId : invalidUserAnswers.keySet()) {
	    deleteStep(user, userAnswerId);
	}
    }

    public void deleteStrategy(User user, int strategyId) 
	throws WdkUserException {
        PreparedStatement psStrategy = null;
        try {
            // remove history
            psStrategy = SqlUtils.getPreparedStatement(dataSource, "DELETE "
                    + "FROM " + loginSchema + "strategies WHERE user_id = ? "
                    + "AND project_id = ? AND strategy_id = ?");
            psStrategy.setInt(1, user.getUserId());
            psStrategy.setString(2, projectId);
            psStrategy.setInt(3, strategyId);
            int result = psStrategy.executeUpdate();
            if (result == 0)
                throw new WdkUserException("The strategy #" + strategyId
                        + " of user " + user.getEmail() + " cannot be found.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psStrategy);
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

    private int getStrategyCount(User user)
	throws WdkUserException {
	ResultSet rsStrategy = null;
	try {
	    PreparedStatement psStrategy = SqlUtils.getPreparedStatement(
		    dataSource, "SELECT count(*) AS num FROM " + loginSchema
		    + "strategies WHERE user_id = ? AND project_id = ? ");
	    psStrategy.setInt(1, user.getUserId());
	    psStrategy.setString(2, projectId);
	    rsStrategy = psStrategy.executeQuery();
	    rsStrategy.next();
	    return rsStrategy.getInt("num");
	}
	catch (SQLException ex) {
	    throw new WdkUserException(ex);
	}
	finally {
	    try {
		SqlUtils.closeResultSet(rsStrategy);
	    }
	    catch (SQLException ex) {
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
