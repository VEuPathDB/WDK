package org.gusdb.wdk.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utilities {

    private static final Logger logger = Logger.getLogger(Utilities.class);

    public static final int TRUNCATE_DEFAULT = 100;

    /**
     * param values with is prefix means the result of that value is a checksum
     * for the real value. if the param is a datasetParam, then the checksum is
     * an index to a dataset obejct; if the param is of other types, then the
     * value of the param can be found in the clob values table by the checksum
     */
    public static final String PARAM_COMPRESSE_PREFIX = "[C]";
    public static final String DATASET_PARAM_KEY_PREFIX = "[K]";

    /**
     * The maximum size for parameter values that will be displayed in thr URL
     * as plain values
     */
    public static final int MAX_PARAM_VALUE_SIZE = 100;

    /**
     * The maximum number of attributes used in sorting an answer
     */
    public static final int SORTING_LEVEL = 3;

    /**
     * command-line argument: -model
     */
    public static final String ARGUMENT_PROJECT_ID = "model";

    /**
     * system property: gusHome
     */
    public static final String SYSTEM_PROPERTY_GUS_HOME = "GUS_HOME";

    public static final int MAXIMUM_RECORD_INSTANCES = 10000;

    public static final String ALIAS_OLD_KEY_COLUMN_PREFIX = "old_";

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int DEFAULT_SUMMARY_ATTRIBUTE_SIZE = 6;

    public static final int DEFAULT_WEIGHT = 10;

    public static final int MAX_PK_COLUMN_COUNT = 3;

    public static final int MAX_PK_COLUMN_VALUE_SIZE = 1999;

    public static final String INTERNAL_PARAM_SET = "InternalParams";
    public static final String INTERNAL_QUERY_SET = "InternalQueries";
    public static final String INTERNAL_QUESTION_SET = "InternalQuestions";

    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_PK_PREFIX = "pk_column_";
    public static final String COLUMN_WEIGHT = "wdk_weight";

    public static final String PARAM_PROJECT_ID = COLUMN_PROJECT_ID;
    public static final String PARAM_USER_ID = COLUMN_USER_ID;

    public static final String MACRO_ID_SQL = "##WDK_ID_SQL##";

    public static final String QUERY_CTX_QUESTION = "wdk-question";
    public static final String QUERY_CTX_PARAM = "wdk-param";
    public static final String QUERY_CTX_QUERY = "wdk-query";
    public static final String QUERY_CTX_USER = "wdk-user";

    private static final String ALGORITHM = "MD5";
    
    /*
     * Inner class to act as a JAF datasource to send HTML e-mail content
     */
    private static class HTMLDataSource implements javax.activation.DataSource {

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

    public static String encrypt(String data) throws WdkModelException {
        return encrypt(data, false);
    }

    public static String encrypt(String data, boolean shortDigest)
            throws WdkModelException {
        // cannot encrypt null value
        if (data == null || data.length() == 0)
            throw new WdkModelException("Cannot encrypt an empty/null string");
        
        byte[] byteBuffer = getEncryptedBytes(data.toString());
        if (shortDigest) {
            // just take the first 8 bytes from MD5 hash
            int size = Math.min(byteBuffer.length, 8);
            byte[] newBuffer = new byte[size];
            System.arraycopy(byteBuffer, 0, newBuffer, 0, newBuffer.length);
            byteBuffer = newBuffer;
        }
        // convert each byte into hex format
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < byteBuffer.length; i++) {
            int code = (byteBuffer[i] & 0xFF);
            if (code < 0x10) buffer.append('0');
            buffer.append(Integer.toHexString(code));
        }
        return buffer.toString();
    }

    public static byte[] getEncryptedBytes(String str) {
    	try {
    		MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
    		return digest.digest(str.getBytes());
    	}
    	catch (NoSuchAlgorithmException e) {
			throw new WdkRuntimeException("Unable to initialize MessageDigest with algorithm " + ALGORITHM, e);
    	}
	}
    
    public static String replaceMacros(String text, Map<String, Object> tokens) {
        for (String token : tokens.keySet()) {
            Object object = tokens.get(token);
            String value = (object == null) ? "" : object.toString();
            String macro = Pattern.quote("$$" + token + "$$");
            text = text.replaceAll(macro, value);
        }
        return text;
    }

    public static String[] toArray(String data) {
        if (data == null || data.length() == 0) {
            String[] values = new String[0];
            return values;
        }
        data = data.replace(',', ' ');
        data = data.replace(';', ' ');
        data = data.replace('\t', ' ');
        data = data.replace('\n', ' ');
        data = data.replace('\r', ' ');
        return data.trim().split("\\s+");
    }

    public static String fromArray(String[] data) {
        return fromArray(data, ",");
    }

    public static String fromArray(String[] data, String delimiter) {
        if (data == null) return null;
        StringBuffer sb = new StringBuffer();
        for (String value : data) {
            if (sb.length() > 0) sb.append(delimiter);
            sb.append(value);
        }
        return sb.toString();
    }

    public static String parseValue(Object objValue) throws SQLException {
        String value;
        if (objValue == null) value = null;
        else if (objValue instanceof Clob) {
            Clob clob = (Clob) objValue;
            value = clob.getSubString(1, (int) clob.length());
        } else value = objValue.toString();
        return value;
    }

    public static String[][] convertContent(String content)
            throws JSONException {
        JSONArray jsResult = new JSONArray(content);
        JSONArray jsRow = (JSONArray) jsResult.get(0);
        String[][] result = new String[jsResult.length()][jsRow.length()];
        for (int row = 0; row < result.length; row++) {
            jsRow = (JSONArray) jsResult.get(row);
            for (int col = 0; col < result[row].length; col++) {
                Object cell = jsRow.get(col);
                result[row][col] = (cell == null || cell == JSONObject.NULL) ? null
                        : cell.toString();
            }
        }
        return result;
    }

    public static void sendEmail(WdkModel wdkModel, String email, String reply,
            String subject, String content) throws WdkUserException,
            WdkModelException {
        String smtpServer = wdkModel.getModelConfig().getSmtpServer();

        logger.debug("Sending message to: " + email + ", reply: " + reply
                + ", using SMPT: " + smtpServer);

        // create properties and get the session
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.debug", "true");
        Session session = Session.getInstance(props);

        // instantiate a message
        Message message = new MimeMessage(session);
        try {
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
            throw new WdkModelException(ex);
        }
    }

    public static byte[] readFile(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        InputStream stream = new FileInputStream(file);
        stream.read(buffer, 0, buffer.length);
        stream.close();
        return buffer;
    }
}
