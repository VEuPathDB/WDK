package org.gusdb.wdk.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

/**
 * This class provided constants that are shared among different WDK model
 * classes. Furthermore, it also provides utility functions to send
 * email, encrypt text, parse text, etc.
 *
 * @author jerric
 */
public class Utilities {

  private static final Logger LOG = Logger.getLogger(Utilities.class);

  public static final int TRUNCATE_DEFAULT = 100;

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

  public static final int DEFAULT_SUMMARY_ATTRIBUTE_SIZE = 6;

  public static final int DEFAULT_WEIGHT = 10;

  /**
   * The Maximum number of columns allowed in the primary key field. We cannot
   * support unlimited number of columns since each column will be stored in an
   * individual field in the dataset, basket, and favorite tables.
   */
  public static final int MAX_PK_COLUMN_COUNT = 4;

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
  public static final String MACRO_ID_SQL_NO_FILTERS = "##WDK_ID_SQL_NO_FILTERS##";
  public static final String MACRO_CACHE_TABLE = "##WDK_CACHE_TABLE##";
  public static final String MACRO_CACHE_INSTANCE_ID = "##WDK_CACHE_INSTANCE_ID##";

  // keys used to access context data via strings to WSF plugins (process queries)
  public static final String CONTEXT_KEY_QUESTION_FULL_NAME = "wdk-question";
  public static final String CONTEXT_KEY_QUERY_FULL_NAME = "wdk-query";
  public static final String CONTEXT_KEY_PARAM_NAME = "wdk-param";
  public static final String CONTEXT_KEY_USER_ID = "wdk-user-id";
  public static final String CONTEXT_KEY_BEARER_TOKEN_STRING = "wdk-user-token";

  public static final String RECORD_DIVIDER = "\n";
  public static final String COLUMN_DIVIDER = ",";

  // keys used to access objects set on ServletContext and Request context objects
  public static final String CONTEXT_KEY_WDK_MODEL_OBJECT = "wdk-model";
  public static final String CONTEXT_KEY_USER_OBJECT = "wdk-user";
  public static final String CONTEXT_KEY_VALIDATED_TOKEN_OBJECT = "validated-user-token";

  public static final String WDK_SERVICE_ENDPOINT_KEY = "wdkServiceEndpoint";

  /*
   * Inner class to act as a JAF DataSource to send HTML e-mail content
   */
  private static class HTMLDataSource implements javax.activation.DataSource {

    private String html;

    public HTMLDataSource(String htmlString) {
      html = htmlString;
    }

    // Return html string in an InputStream.
    // A new stream must be returned each time.
    @Override
    public InputStream getInputStream() throws IOException {
      if (html == null)
        throw new IOException("Null HTML");
      return new ByteArrayInputStream(html.getBytes());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      throw new IOException("This DataHandler cannot write HTML");
    }

    @Override
    public String getContentType() {
      return "text/html";
    }

    @Override
    public String getName() {
      return "JAF text/html dataSource to send e-mail only";
    }
  }

  // for use with randomAlphaNumericString
  private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz";
  static final String RANDOM_CHARS = "0123456789" + ALPHA + ALPHA.toUpperCase();

  public static String randomAlphaNumericString(int numChars) {
    StringBuilder str = new StringBuilder();
    new Random()
      .ints(numChars, 0, RANDOM_CHARS.length())
      .forEach(i -> str.append(RANDOM_CHARS.charAt(i)));
    return str.toString();
  }

  public static String replaceMacros(String text, Map<String, Object> tokens) {
    for (String token : tokens.keySet()) {
      Object object = tokens.get(token);
      String value = (object == null) ? "" : Matcher.quoteReplacement(object.toString());
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

  public static String parseValue(Object objValue) {
    if (objValue == null)
      return null;
    if (objValue instanceof Clob) {
      return parseClob((Clob) objValue);
    }
    return objValue.toString();
  }

  private static String parseClob(Clob clobValue) {
    try {
      return clobValue.getSubString(1, (int) clobValue.length());
    } catch (SQLException e) {
      throw new WdkRuntimeException("Error while reading Clob", e);
    }
  }

  // sendEmail() method overloading: different number of parameters (max 8), different type for attachments

  // 7 parameters (missing bcc, datahandlers instead of attachments)
  // used by?
  public static void sendEmail(String smtpServer, String sendTos, String reply,
    String subject, String content, String ccAddresses,
    DataHandler[] attachmentDataHandlers) throws WdkModelException {
      Attachment[] attachments = Stream
        .of(attachmentDataHandlers)
        .map(dataHandler -> new Attachment(dataHandler, dataHandler.getName()))
        .toArray(Attachment[]::new);
      // should call the 8 parameter one straight?
      sendEmail(smtpServer, sendTos, reply, subject, content, ccAddresses, attachments);
  }

  // sendEmail()  6 parameters (missing bcc, attachments)
  // used by?
  public static void sendEmail(String smtpServer, String sendTos, String reply,
    String subject, String content, String ccAddresses)
    throws WdkModelException {
      // should call the 8 parameter one straight
      sendEmail(smtpServer, sendTos, reply, subject, content, ccAddresses, new Attachment[] {});
  }

  // sendEmail()  5 parameters (missing cc, bcc, attachments)
  // used by?
  public static void sendEmail(String smtpServer, String sendTos, String reply,
    String subject, String content) throws WdkModelException {
      // should call the 8 parameter one straight
      sendEmail(smtpServer, sendTos, reply, subject, content, null, new Attachment[] {});
  }

  // sendEmail()  7 parameters (missing bcc)
  // used by al of the above
  public static void sendEmail(String smtpServer, String sendTos, String reply, 
    String subject, String content, String ccAddresses, Attachment[] attachments) 
    throws WdkModelException {
      //  call the 8 parameter one
      sendEmail(smtpServer, null, null, sendTos, reply, subject, content, ccAddresses, null, attachments, 25, false);
  }

  public static void sendEmail(String smtpServer, String sendTos, String reply,
                               String subject, String content, String ccAddresses, String bccAddresses,
                               Attachment[] attachments) throws WdkModelException {
    sendEmail(smtpServer, null, null, sendTos, reply, subject, content, ccAddresses, bccAddresses, attachments, 25, false);
  }

  // sendEmail()  all 12 parameters
  public static void sendEmail(String smtpServer, String username, String password, String sendTos, String reply,
    String subject, String content, String ccAddresses, String bccAddresses, Attachment[] attachments, int smtpPort, boolean tlsEnabled) throws WdkModelException {

    LOG.debug("Sending message to: " + sendTos + ", bcc to: " + bccAddresses +
      ",reply: " + reply + ", using SMPT: " + smtpServer);

    // create properties and get the session
    Properties props = new Properties();
    props.put("mail.smtp.host", smtpServer);
    props.put("mail.debug", "true");
    props.put("mail.smtp.port", smtpPort);

    if (tlsEnabled) {
      props.put("mail.smtp.starttls.enable","true");
      props.put("mail.smtp.ssl.protocols", "TLSv1.2");
      props.put("mail.smtp.ssl.trust", smtpServer);
    }

    Authenticator auth = null;

    if (username != null && password != null) {
      props.put("mail.smtp.auth", "true");
      auth = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(username, password);
        }
      };
    }

    Session session = Session.getInstance(props, auth);

    // instantiate a message
    Message message = new MimeMessage(session);
    try {
      Address[] replyAddresses = InternetAddress.parse(reply, true);
      message.setFrom(replyAddresses[0]);
      message.setReplyTo(replyAddresses);
      message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(sendTos));

      // add Cc addresses
      if (ccAddresses != null && !ccAddresses.isEmpty()) {
        message.setRecipients(Message.RecipientType.CC,
            InternetAddress.parse(ccAddresses));
      }
      // add bcc addresses
      if (bccAddresses != null && !bccAddresses.isEmpty()) {
        message.setRecipients(Message.RecipientType.BCC,
            InternetAddress.parse(bccAddresses));
      }
      message.setSubject(subject);
      message.setSentDate(new Date());

      // set html content
      MimeBodyPart messagePart = new MimeBodyPart();
      messagePart.setDataHandler(new DataHandler(new HTMLDataSource(content)));

      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(messagePart);

      // add attachment
      if (attachments != null) {
        for (Attachment attachment : attachments) {
          MimeBodyPart attachmentPart = new MimeBodyPart();
          attachmentPart.setDataHandler(attachment.getDataHandler());
          attachmentPart.setFileName(attachment.getFileName());
          multipart.addBodyPart(attachmentPart);
        }
      }

      message.setContent(multipart);
      // message.setDataHandler(new DataHandler(new
      // ByteArrayDataSource(content.getBytes(), "text/plain")));

      // send email
      Transport.send(message);
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

  public static Map<String, Boolean> parseSortList(String sortList) throws WdkModelException {
    Map<String, Boolean> sortingMap = new LinkedHashMap<String, Boolean>();
    String[] attrCombines = sortList.split(",");

    for (String attrCombine : attrCombines) {
      String[] sorts = attrCombine.trim().split("\\s+");
      if (sorts.length != 2)
        throw new WdkModelException("The sorting format is wrong: " + sortList);
      String attrName = sorts[0].trim();
      String strAscend = sorts[1].trim().toLowerCase();
      boolean ascending = strAscend.equals("asc");
      if (!sortingMap.containsKey(attrName))
        sortingMap.put(attrName, ascending);
    }

    return sortingMap;
  }

}
