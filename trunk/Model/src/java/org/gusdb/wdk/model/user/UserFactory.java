/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author xingao
 * 
 */
public class UserFactory {

    public static final int REGISTERED_REFRESH_INTERVAL = 10;
    public static final int GUEST_REFRESH_INTERVAL = -1;

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

    // for the current release, just hard code support email, reset password
    // email subject and content. Consider to refine it be reading from
    // configurations
    private static String supportEmail = "jerric@pcbi.upenn.edu";
    private static String subject = "The account information on $$MODEL_NAME$$ website";
    private static String siteUrl = "http://localhost:8080/wdktoy";
    private static String content = "Welcome $$FIRST_NAME$$,\n\n"
            + "Thanks for registering on $$MODEL_NAME$$ web site. The following is your login information:\n\n"
            + "email: $$EMAIL$$\n"
            + "password: $$PASSWORD$$\n\n"
            + "Please login and change your password as soon as possible by visiting the following link:\n"
            + "$$SITE_URL$$.\n\n"
            + "Thank you,\n\n$$MODEL_NAME$$ support team.";

    public static UserFactory getInstance() throws WdkUserException {
        if (factory == null) {
            throw new WdkUserException(
                    "UserFactory is not initialized properly. Please Initialize WdkModel first.");
        }
        return factory;
    }

    public static void initialize(WdkModel wdkModel, String projectId, DataSource dataSource,
            String userTable, String roleTable, String historyTable,
            String preferenceTable, String defaultRole, String smtpServer) {
        factory = new UserFactory(dataSource);
        factory.wdkModel = wdkModel;
        factory.projectId = projectId;
        factory.userTable = userTable;
        factory.roleTable = roleTable;
        factory.historyTable = historyTable;
        factory.preferenceTable = preferenceTable;
        factory.defaultRole = defaultRole;
        factory.smtpServer = smtpServer;
    }
    
    public WdkModel getWdkModel() {
        return wdkModel;
    }
    
    public void sendEmail(String email, String reply, String subject,
            String content) throws WdkModelException {
        // create properties and get the session
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.debug", "true");
        Session session = Session.getInstance(props);

        // instantiate a message
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(supportEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(
                    email));
            message.setSubject(subject);
            message.setSentDate(new Date());
            message.setText(content); // set content

            // send email
            Transport.send(message);
        } catch (AddressException ex) {
            throw new WdkModelException(ex);
        } catch (MessagingException ex) {
            throw new WdkModelException(ex);
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
            String zipCode, String phoneNumber, String country)
            throws WdkUserException, WdkModelException {
        if (email == null)
            throw new WdkUserException("The user's email cannot be empty.");
        // format the info
        email = qualify(email.trim().toLowerCase());
        if (email.length() == 0)
            throw new WdkUserException("The user's email cannot be empty.");
        lastName = qualify(lastName.trim());
        firstName = qualify(firstName.trim());
        middleName = qualify(middleName.trim());
        title = qualify(title.trim());
        organization = qualify(organization.trim());
        department = qualify(department.trim());
        address = qualify(address.trim());
        city = qualify(city.trim());
        state = qualify(state.trim());
        zipCode = qualify(zipCode.trim());
        phoneNumber = qualify(phoneNumber.trim());
        country = qualify(country.trim());

        try {
            // check whether the user exist in the database already exist.
            // if loginId exists, the operation failed
            if (isExist(email))
                throw new WdkUserException("The email '" + email
                        + "' has been registered. Please choose another one.");

            // insert the user
            StringBuffer sql = new StringBuffer();
            sql.append("INSERT INTO " + userTable);
            sql.append(" (email, last_name, first_name, middle_name, title, "
                    + "organization, department, address, city, state, "
                    + "zip_code, phone_number, country) VALUES ('");
            sql.append(email + "', '" + lastName + "', '" + firstName + "', '");
            sql.append(middleName + "', '" + title + "', '" + organization);
            sql.append("', '" + department + "', '" + address + "', '" + city);
            sql.append("', '" + state + "', '" + zipCode + "', '");
            sql.append(phoneNumber + "', '" + country + "')");
            SqlUtils.execute(dataSource, sql.toString());

            // assign the role to the user
            sql = new StringBuffer();
            sql.append("INSERT INTO " + roleTable);
            sql.append(" (email, role) VALUES('");
            sql.append(email + "', '" + defaultRole + "')");
            SqlUtils.execute(dataSource, sql.toString());

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

            // generate a random password, and send to the user via email
            resetPassword(user);

            return user;
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }

    public User createGuestUser() {
        User user = new User("guest", this);
        user.setFirstName("Guest");
        user.setGuest(true);
        user.setRefreshInterval(GUEST_REFRESH_INTERVAL);
        return user;
    }

    public User authenticate(String email, String password)
            throws WdkModelException, WdkUserException {
        // convert email to lower case
        email = qualify(email.trim().toLowerCase());
        try {
            // encrypt password
            password = encrypt(password);

            // query on the database to see if the pair match
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT count(*) FROM " + userTable);
            sql.append(" WHERE email = '" + email);
            sql.append("' AND password = '" + password + "'");
            int count = SqlUtils.runIntegerQuery(dataSource, sql.toString());
            if (count != 1)
                throw new WdkUserException("The email/password pair cannot be "
                        + "found in the database. Please check your input and "
                        + "try again.");

            // passed validation, load user information
            return loadUser(email);
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }

    /**
     * Only load the basic information of the user
     * 
     * @param email
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public User loadUser(String email) throws WdkModelException,
            WdkUserException {
        // convert to lower case
        email = qualify(email.trim().toLowerCase());

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM " + userTable);
        sql.append(" WHERE email = '" + email + "'");
        try {
            ResultSet rs = SqlUtils.getResultSet(dataSource, sql.toString());
            if (!rs.next())
                throw new WdkUserException("The user with email '" + email
                        + "' doesn't exist.");

            // read user info
            User user = new User(email, this);
            user.setLastName(rs.getString("last_name"));
            user.setFirstName(rs.getString("first_name"));
            user.setMiddleName(rs.getString("middle_name"));
            user.setTitle(rs.getString("title"));
            user.setOrganization(rs.getString("organization"));
            user.setDepartment(rs.getString("department"));
            user.setAddress(rs.getString("address"));
            user.setCity(rs.getString("city"));
            user.setState(rs.getString("state"));
            user.setZipCode(rs.getString("zip_code"));
            user.setPhoneNumber(rs.getString("phone_number"));
            user.setCountry(rs.getString("country"));
            user.setGuest(false);
            user.setRefreshInterval(REGISTERED_REFRESH_INTERVAL);

            SqlUtils.closeResultSet(rs);

            // load the user's roles
            sql = new StringBuffer();
            sql.append("SELECT role from " + roleTable);
            sql.append(" WHERE email = '" + email + "'");
            rs = SqlUtils.getResultSet(dataSource, sql.toString());
            while (rs.next()) {
                user.addUserRole(rs.getString("role"));
            }
            SqlUtils.closeResultSet(rs);

            return user;
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }

    /**
     * Save the basic information of a user
     * 
     * @param user
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public void saveUser(User user) throws WdkModelException, WdkUserException {
        // check if the user is allowed to be persistant
        if (user.isGuest())
            throw new WdkUserException(
                    "The guest cannot be saved into database.");

        // check if user exists in the database. if not, fail and ask to create
        // the user first
        try {
            if (!isExist(user.getEmail()))
                throw new WdkUserException("The user with email "
                        + user.getEmail()
                        + " doesn't exist. Save operation cancelled.");

            // save the user's basic information
            StringBuffer sql = new StringBuffer();
            sql.append("UPDATE users SET ");
            sql.append("last_name = '" + qualify(user.getLastName().trim()) + "', ");
            sql.append("first_name = '" + qualify(user.getFirstName().trim()) + "', ");
            sql.append("middle_name = '" + qualify(user.getMiddleName().trim()) + "', ");
            sql.append("title = '" + qualify(user.getTitle().trim()) + "', ");
            sql.append("organization = '" + qualify(user.getOrganization().trim())
                    + "', ");
            sql.append("department = '" + qualify(user.getDepartment().trim()) + "', ");
            sql.append("address = '" + qualify(user.getAddress().trim()) + "', ");
            sql.append("city = '" + qualify(user.getCity().trim()) + "', ");
            sql.append("state = '" + qualify(user.getState().trim()) + "', ");
            sql.append("zip_code = '" + qualify(user.getZipCode().trim()) + "', ");
            sql.append("phone_number = '" + qualify(user.getPhoneNumber().trim())
                    + "', ");
            sql.append("country = '" + qualify(user.getCountry().trim()) + "' ");
            sql.append(" WHERE email = '" + qualify(user.getEmail()) + "'");
            SqlUtils.execute(dataSource, sql.toString());

            // save the user's roles
            // before that, remove the records first
            sql = new StringBuffer();
            sql.append("DELETE FROM " + roleTable);
            sql.append(" WHERE email = '" + qualify(user.getEmail()) + "'");
            SqlUtils.execute(dataSource, sql.toString());

            // Then get a prepared statement to do the insertion
            sql = new StringBuffer();
            sql.append("INSERT INTO " + roleTable + " (email, role)");
            sql.append(" VALUES(?, ?)");
            PreparedStatement stmt = SqlUtils.getPreparedStatement(dataSource,
                    sql.toString());
            String[] roles = user.getUserRoles();
            for (String role : roles) {
                stmt.setString(1, qualify(user.getEmail()));
                stmt.setString(2, role);
                stmt.execute();
            }
            SqlUtils.closeStatement(stmt);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }

    void loadHistory(User user) throws WdkUserException, WdkModelException {
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
            sql.append(" histories h, users u");
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
            throw new WdkModelException(ex);
        }
    }

    void saveHistories(User user) {

    }

    void deleteHistory(String loginId, int historyId) {

    }

    public void resetPassword(User user) throws WdkUserException, WdkModelException {
        try {
            if (!isExist(user.getEmail()))
                throw new WdkUserException(
                        "The user doesn't exist. Resetting operation cancelled.");
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }

        // generate a random password of 8 characters long, the range will be
        // [0-9A-Za-z]
        StringBuffer buffer = new StringBuffer();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) {
            int value = rand.nextInt(62);
            if (value < 10) { // number
                buffer.append(value);
            } else if (value < 36) { // upper case letters
                buffer.append((char) ('A' + value - 10));
            } else { // lower case letters
                buffer.append((char) ('a' + value - 36));
            }
        }
        String password = buffer.toString();

        savePassword(user.getEmail(), password);

        // send an email to the user
        String subj = subject.replaceAll("\\$\\$MODEL_NAME\\$\\$", projectId);
        String message = content.replaceAll("\\$\\$MODEL_NAME\\$\\$", projectId);
        message = message.replaceAll("\\$\\$FIRST_NAME\\$\\$",
                user.getFirstName());
        message = message.replaceAll("\\$\\$EMAIL\\$\\$", user.getEmail());
        message = message.replaceAll("\\$\\$PASSWORD\\$\\$", password);
        message = message.replaceAll("\\$\\$SITE_URL\\$\\$", siteUrl);
        sendEmail(user.getEmail(), supportEmail, subj, message);
    }

    void changePassword(String email, String oldPassword, String newPassword,
            String confirmPassword) throws WdkUserException, WdkModelException {
        email = qualify(email.trim().toLowerCase());
        
        // encrypt password
        try {
            oldPassword = encrypt(oldPassword);

            // check if the old password matches
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT count(*) FROM " + userTable);
            sql.append(" WHERE email ='" + email);
            sql.append("' AND password = '" + oldPassword + "'");
            int count = SqlUtils.runIntegerQuery(dataSource, sql.toString());
            if (count <= 0)
                throw new WdkUserException("The current password is incorrect.");

            // check if the new password matches
            if (!newPassword.equals(confirmPassword))
                throw new WdkUserException("The new password doesn't match, " +
                        "please type them again. It's case sensitive.");

            // passed check, then save the new password
            savePassword(email, newPassword);
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkModelException(ex);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }

    }

    void savePassword(String email, String password) throws WdkModelException {
        email = qualify(email.trim().toLowerCase());
        try {
            // encrypt the password, and save it
            String encrypted = encrypt(password);
            StringBuffer buffer = new StringBuffer();
            buffer.append("UPDATE users SET password = '" + encrypted + "'");
            buffer.append(" WHERE email = '" + email + "'");
            SqlUtils.executeUpdate(dataSource, buffer.toString());
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkModelException(ex);
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        }
    }

    private boolean isExist(String email) throws SQLException {
        email = qualify(email.trim().toLowerCase());
        // check if user exists in the database. if not, fail and ask to create
        // the user first
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT count(*) FROM " + userTable);
        sql.append(" WHERE email = '" + email + "'");
        int count = SqlUtils.runIntegerQuery(dataSource, sql.toString());
        return (count > 0);
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
    
    private String qualify(String content) {
        // replace all single quotes with two single quotes
        content = content.replaceAll("'", "''");
        return content;
    }

    public static void main(String[] args) {
        StringBuffer buffer = new StringBuffer();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) {
            int value = rand.nextInt(62);
            if (value < 10) { // number
                buffer.append(value);
            } else if (value < 36) { // upper case letters
                buffer.append((char) ('A' + value - 10));
            } else { // lower case letters
                buffer.append((char) ('a' + value - 36));
            }
        }
        String password = buffer.toString();

        System.out.println("random passwd: " + password);
    }
}
