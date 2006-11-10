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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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

import org.gusdb.wdk.model.WdkModel;
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

    public static final int REGISTERED_REFRESH_INTERVAL = 10;
    public static final int GUEST_REFRESH_INTERVAL = -1;

    public static final String GLOBAL_PREFERENCE_KEY = "[Global]";

    private static UserFactory factory;

    private DataSource dataSource;
    private String userTable;
    private String roleTable;
    private String historyTable;
    private String preferenceTable;
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
            DataSource dataSource, String userTable, String roleTable,
            String historyTable, String preferenceTable, String defaultRole,
            String smtpServer, String registerEmail, String emailSubject,
            String emailContent) {
        factory = new UserFactory(dataSource);
        factory.wdkModel = wdkModel;
        factory.projectId = projectId;
        factory.userTable = userTable;
        factory.roleTable = roleTable;
        factory.historyTable = historyTable;
        factory.preferenceTable = preferenceTable;
        factory.defaultRole = defaultRole;
        factory.smtpServer = smtpServer;
        factory.registerEmail = registerEmail;
        factory.emailContent = emailContent;
        factory.emailSubject = emailSubject;
    }

    public WdkModel getWdkModel() {
        return wdkModel;
    }

    public void sendEmail(String email, String reply, String subject,
            String content) throws WdkUserException {
        // TEST
        System.out.println("Reply email is: " + reply);

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

    private UserFactory(DataSource dataSource) {
        this.dataSource = dataSource;
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
            Map<String, String> projectPreferences) throws WdkUserException {
        return createUser(email, lastName, firstName, middleName, title,
                organization, department, address, city, state, zipCode,
                phoneNumber, country, globalPreferences, projectPreferences,
                true);
    }

    public User createUser(String email, String lastName, String firstName,
            String middleName, String title, String organization,
            String department, String address, String city, String state,
            String zipCode, String phoneNumber, String country,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences, boolean resetPwd)
            throws WdkUserException {
        if (email == null)
            throw new WdkUserException("The user's email cannot be empty.");
        // format the info
        email = email.trim();
        if (email.length() == 0)
            throw new WdkUserException("The user's email cannot be empty.");

        PreparedStatement psUser = null;
        PreparedStatement psRole = null;
        try {
            // check whether the user exist in the database already exist.
            // if loginId exists, the operation failed
            if (isExist(email))
                throw new WdkUserException("The email '" + email
                        + "' has been registered. Please choose another one.");

            psUser = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + userTable
                    + " (email, last_name, first_name, middle_name, title, "
                    + "organization, department, address, city, state, "
                    + "zip_code, phone_number, country) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            psUser.setString(1, email);
            psUser.setString(2, lastName);
            psUser.setString(3, firstName);
            psUser.setString(4, middleName);
            psUser.setString(5, title);
            psUser.setString(6, organization);
            psUser.setString(7, department);
            psUser.setString(8, address);
            psUser.setString(9, city);
            psUser.setString(10, state);
            psUser.setString(11, zipCode);
            psUser.setString(12, phoneNumber);
            psUser.setString(13, country);
            psUser.execute();

            // assign the role to the user
            psRole = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + roleTable + " (email, \"role\") VALUES(?, ?)");
            psRole.setString(1, email);
            psRole.setString(2, defaultRole);
            psRole.execute();

            // create user object
            User user = new User(email, this);
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
            user.setRefreshInterval(REGISTERED_REFRESH_INTERVAL);

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
                SqlUtils.closeStatement(psRole);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    public User getGuestUser() {
        User user = new User("guest", this);
        user.setFirstName("Guest");
        user.setGuest(true);
        user.setRefreshInterval(GUEST_REFRESH_INTERVAL);
        return user;
    }

    public User authenticate(String email, String password)
            throws WdkUserException {
        // convert email to lower case
        email = email.trim();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // encrypt password
            password = encrypt(password);

            // query on the database to see if the pair match
            ps = SqlUtils.getPreparedStatement(dataSource, "SELECT count(*) "
                    + "FROM " + userTable
                    + " WHERE email = ? AND \"password\" = ?");
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
                // SqlUtils.closeStatement(ps);
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
     */
    public User loadUser(String email) throws WdkUserException {
        // convert to lower case
        email = email.trim();

        PreparedStatement psUser = null;
        PreparedStatement psRole = null;
        ResultSet rsUser = null;
        ResultSet rsRole = null;
        try {
            // get user information
            psUser = SqlUtils.getPreparedStatement(dataSource, "SELECT * FROM "
                    + userTable + " WHERE email = ?");
            psUser.setString(1, email);
            rsUser = psUser.executeQuery();
            if (!rsUser.next())
                throw new WdkUserException("The user with email '" + email
                        + "' doesn't exist.");

            // read user info
            User user = new User(email, this);
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
            user.setGuest(false);
            user.setRefreshInterval(REGISTERED_REFRESH_INTERVAL);

            // load the user's roles
            psRole = SqlUtils.getPreparedStatement(dataSource,
                    "SELECT \"role\" from " + roleTable + " WHERE email = ?");
            psRole.setString(1, email);
            rsRole = psRole.executeQuery();
            while (rsRole.next()) {
                user.addUserRole(rsRole.getString("role"));
            }
            SqlUtils.closeResultSet(rsRole);

            // load user's preferences
            loadPreferences(user);
            return user;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsUser);
                SqlUtils.closeResultSet(rsRole);
                // SqlUtils.closeStatement(psUser);
                // SqlUtils.closeStatement(psRole);
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
    public void saveUser(User user) throws WdkUserException {
        // check if the user is allowed to be persistant
        if (user.isGuest())
            throw new WdkUserException(
                    "The guest cannot be saved into database.");

        // check if user exists in the database. if not, fail and ask to create
        // the user first
        PreparedStatement psUser = null;
        PreparedStatement psRoleDelete = null;
        PreparedStatement psRoleInsert = null;
        try {
            if (!isExist(user.getEmail()))
                throw new WdkUserException("The user with email "
                        + user.getEmail()
                        + " doesn't exist. Save operation cancelled.");

            // save the user's basic information
            psUser = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + userTable + " SET last_name = ?, first_name = ?, "
                    + "middle_name = ?, title = ?,  organization = ?, "
                    + "department = ?, address = ?, city = ?, state = ?, "
                    + "zip_code = ?, phone_number = ?, country = ? "
                    + "WHERE email = ?");
            psUser.setString(1, user.getLastName());
            psUser.setString(2, user.getFirstName());
            psUser.setString(3, user.getMiddleName());
            psUser.setString(4, user.getTitle());
            psUser.setString(5, user.getOrganization());
            psUser.setString(6, user.getDepartment());
            psUser.setString(7, user.getAddress());
            psUser.setString(8, user.getCity());
            psUser.setString(9, user.getState());
            psUser.setString(10, user.getZipCode());
            psUser.setString(11, user.getPhoneNumber());
            psUser.setString(12, user.getCountry());
            psUser.setString(13, user.getEmail());
            psUser.execute();

            // save the user's roles
            // before that, remove the records first
            psRoleDelete = SqlUtils.getPreparedStatement(dataSource,
                    "DELETE FROM " + roleTable + " WHERE email = ?");
            psRoleDelete.setString(1, user.getEmail());
            psRoleDelete.execute();

            // Then get a prepared statement to do the insertion
            psRoleInsert = SqlUtils.getPreparedStatement(dataSource, "INSERT "
                    + "INTO " + roleTable + " (email, \"role\") VALUES(?, ?)");
            String[] roles = user.getUserRoles();
            for (String role : roles) {
                psRoleInsert.setString(1, user.getEmail());
                psRoleInsert.setString(2, role);
                psRoleInsert.execute();
            }
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

    void loadHistories(User user) throws WdkUserException {
        // check if the user is guest user
        if (user.isGuest())
            throw new WdkUserException(
                    "The guest user doesn't have persistant history");

        // check if the user exist in the database
        try {
            if (!isExist(user.getEmail()))
                throw new WdkUserException(
                        "The user doesn't exist. Save operation cancelled.");

            // clear the history cache first
            user.clearHistories();

            StringBuffer sql = new StringBuffer();
            sql.append("SELECT h.history_id, h.full_name, h.custom_name, h.created_time, h.params");
            sql.append(" " + historyTable + " h, " + userTable + " u");
            sql.append(" WHERE u.login_id = h.login_id");
            sql.append(" AND h.projectId = '" + projectId + "'");
            ResultSet rs = SqlUtils.getResultSet(dataSource, sql.toString());
            while (rs.next()) {
                History history = new History(user, rs.getInt("h.history_id"));
                history.setCreatedTime(rs.getDate("h.created_time"));
                history.setFullName(rs.getString("h.full_name"));
                // set custom name without updating back into database
                history.setCustomTimeNoUp(rs.getString("custom_name"));
                // get params and values, pairs separated by ;, and key value
                // separated by =
                String strParams = rs.getString("h.params");
                String[] params = strParams.split(";");
                for (String strPair : params) {
                    String[] pair = strPair.split("=");
                    String param = pair[0].trim();
                    String value = pair[1].trim();
                    history.addParam(param, value);
                }
                // add history to the user
                user.addHistory(history);
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        }
    }

    void saveHistories(User user) {

    }

    void deleteHistory(String loginId, int historyId) {

    }

    private void savePreferences(User user) throws WdkUserException {
        String email = user.getEmail();
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;
        try {
            // delete preferences
            psDelete = SqlUtils.getPreparedStatement(dataSource, "DELETE FROM "
                    + preferenceTable + " WHERE email = ? AND project_id = ?");
            psDelete.setString(1, email);
            psDelete.setString(2, GLOBAL_PREFERENCE_KEY);
            psDelete.execute();
            psDelete.setString(1, email);
            psDelete.setString(2, projectId);
            psDelete.execute();

            // insert preferences
            psInsert = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + preferenceTable + " (email, project_id, "
                    + "preference_name, preference_value) "
                    + "VALUES (?, ?, ?, ?)");
            Map<String, String> global = user.getGlobalPreferences();
            for (String prefName : global.keySet()) {
                String prefValue = global.get(prefName);
                psInsert.setString(1, email);
                psInsert.setString(2, GLOBAL_PREFERENCE_KEY);
                psInsert.setString(3, prefName);
                psInsert.setString(4, prefValue);
                psInsert.execute();
            }
            Map<String, String> project = user.getProjectPreferences();
            for (String prefName : project.keySet()) {
                String prefValue = project.get(prefName);
                psInsert.setString(1, email);
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
        String email = user.getEmail();
        PreparedStatement psGlobal = null, psProject = null;
        ResultSet rsGlobal = null, rsProject = null;
        try {
            // load global preferences
            psGlobal = SqlUtils.getPreparedStatement(dataSource, "SELECT "
                    + "preference_name, preference_value FROM "
                    + preferenceTable + " WHERE email = ? AND project_id = '"
                    + GLOBAL_PREFERENCE_KEY + "'");
            psGlobal.setString(1, email);
            rsGlobal = psGlobal.executeQuery();
            while (rsGlobal.next()) {
                String prefName = rsGlobal.getString("preference_name");
                String prefValue = rsGlobal.getString("preference_value");
                user.setGlobalPreference(prefName, prefValue);
            }

            // load project specific preferences
            psProject = SqlUtils.getPreparedStatement(dataSource, "SELECT "
                    + "preference_name, preference_value FROM "
                    + preferenceTable + " WHERE email = ? AND project_id = ?");
            psProject.setString(1, email);
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

    public void resetPassword(User user) throws WdkUserException {
        if (!isExist(user.getEmail()))
            throw new WdkUserException(
                    "The user doesn't exist. Resetting operation cancelled.");

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

        savePassword(user.getEmail(), password);

        // send an email to the user
        String message = emailContent.replaceAll("\\$\\$FIRST_NAME\\$\\$",
                user.getFirstName());
        message = message.replaceAll("\\$\\$EMAIL\\$\\$", user.getEmail());
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
                    + "FROM " + userTable + " WHERE email =? "
                    + "AND \"password\" = ?");
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

    public void savePassword(String email, String password) throws WdkUserException {
        email = email.trim();
        PreparedStatement ps = null;
        try {
            // encrypt the password, and save it
            String encrypted = encrypt(password);
            ps = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + userTable + " SET \"password\" = ? WHERE email = ?");
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
            ps = SqlUtils.getPreparedStatement(dataSource,
                    "SELECT count(*) FROM " + userTable + " WHERE email = ?");
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
    
    public User[] queryUsers(String emailPattern) throws WdkUserException {
        String sql = "SELECT email FROM " + userTable;;
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
                String email = rs.getString("email");
                User user = loadUser(email);
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
    
    public void deleteUser(String email) throws WdkUserException {
        email = email.replaceAll("'", "");
        String where = " WHERE email = '" + email + "'";
        try {
            // delete preference
            String sql = "DELETE FROM " + preferenceTable + where;
            SqlUtils.executeUpdate(dataSource, sql);
            
            // delete history
            sql = "DELETE FROM " + historyTable + where;
            SqlUtils.executeUpdate(dataSource, sql);
            
            // delete user roles
            sql = "DELETE FROM " + roleTable + where;
            SqlUtils.executeUpdate(dataSource, sql);
            
            // delete user
            sql = "DELETE FROM " + userTable + where;
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
