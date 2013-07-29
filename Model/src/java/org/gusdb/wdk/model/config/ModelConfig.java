package org.gusdb.wdk.model.config;


/**
 * An object representaion of the {@code model-config.xml} file. It holds all
 * the configuration information for the WDK system.
 * 
 * @author Jerric
 * @modified Jan 6, 2006 - Jerric add a property for the name of query history
 *           table
 */
public class ModelConfig {

  private String modelName;
  private String webServiceUrl;

  private String defaultRole = "wdk_user";

  // the information for registration email

  /**
   * the SMTP server used to send registration & recover password emails.
   */
  private String smtpServer;

  /**
   * the reply of the registration & recover password emails.
   */
  private String supportEmail;

  /**
   * the recipient of the super slow query log.
   */
  private String adminEmail;

  /**
   * the subject of the registration email.
   */
  private String emailSubject;

  /**
   * the content of the registration email.
   */
  private String emailContent;

  private ModelConfigUserDB userDB;
  private ModelConfigAppDB appDB;

  private QueryMonitor queryMonitor = new QueryMonitor();

  /**
   * The projectId is not part of the config file content, it is input by the
   * user
   */
  private String projectId;
  private String gusHome;

  /**
   * location of secret key file
   */
  private String secretKeyFile;

  /**
   * enable/disable weight feature in the steps. default enabled.
   */
  private boolean useWeights = true;

  /**
   * default regex used by all the stringParams
   */
  private String paramRegex;

  /**
   * turn on thread monitor process if set to true
   */
  private boolean monitorBlockedThreads = true;

  /**
   * if blocked
   */
  private int blockedThreshold = 20;

  /**
   * If it returns true, a monitoring thread will be turned on when webapp is
   * initialized.
   * 
   * @return
   */
  public boolean isMonitorBlockedThreads() {
    return monitorBlockedThreads;
  }

  public void setMonitorBlockedThreads(boolean monitorBlockedThreads) {
    this.monitorBlockedThreads = monitorBlockedThreads;
  }

  /**
   * An report will be fired when the number of blocked threads reaches the
   * threshold.
   * 
   * @return
   */
  public int getBlockedThreshold() {
    return blockedThreshold;
  }

  public void setBlockedThreshold(int blockedThreshold) {
    this.blockedThreshold = blockedThreshold;
  }

  /**
   * @return the projectId
   */
  public String getProjectId() {
    return this.projectId;
  }

  /**
   * @param projectId
   *          the projectId to set
   */
  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  /**
   * @return the gusHome
   */
  public String getGusHome() {
    return this.gusHome;
  }

  /**
   * @param gusHome
   *          the gusHome to set
   */
  public void setGusHome(String gusHome) {
    this.gusHome = gusHome;
  }

  /**
   * @return the modelName
   */
  public String getModelName() {
    return this.modelName;
  }

  /**
   * @param modelName
   *          the modelName to set
   */
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getWebServiceUrl() {
    return webServiceUrl;
  }

  public void setWebServiceUrl(String urlString) {
    webServiceUrl = urlString;
  }

  /**
   * @return Returns the smtpServer.
   */
  public String getSmtpServer() {
    return smtpServer;
  }

  /**
   * @param smtpServer
   *          The smtpServer to set.
   */
  public void setSmtpServer(String smtpServer) {
    this.smtpServer = smtpServer;
  }

  /**
   * @return Returns the emailContent.
   */
  public String getEmailContent() {
    return emailContent;
  }

  /**
   * @param emailContent
   *          The emailContent to set.
   */
  public void setEmailContent(String emailContent) {
    this.emailContent = emailContent;
  }

  /**
   * @return Returns the emailSubject.
   */
  public String getEmailSubject() {
    return emailSubject;
  }

  /**
   * @param emailSubject
   *          The emailSubject to set.
   */
  public void setEmailSubject(String emailSubject) {
    this.emailSubject = emailSubject;
  }

  public String getSupportEmail() {
    return supportEmail;
  }

  public void setSupportEmail(String supportEmail) {
    this.supportEmail = supportEmail;
  }

  /**
   * @return the userDB
   */
  public ModelConfigUserDB getUserDB() {
    return userDB;
  }

  /**
   * @param userDB
   *          the userDB to set
   */
  public void setUserDB(ModelConfigUserDB userDB) {
    this.userDB = userDB;
  }

  /**
   * @return the appDB
   */
  public ModelConfigAppDB getAppDB() {
    return appDB;
  }

  /**
   * @param appDB
   *          the appDB to set
   */
  public void setAppDB(ModelConfigAppDB appDB) {
    this.appDB = appDB;
  }

  /**
   * @return the defaultRole
   */
  public String getDefaultRole() {
    return defaultRole;
  }

  /**
   * @param defaultRole
   *          the defaultRole to set
   */
  public void setDefaultRole(String defaultRole) {
    this.defaultRole = defaultRole;
  }

  /**
   * @return the secretKeyFile
   */
  public String getSecretKeyFile() {
    return secretKeyFile;
  }

  /**
   * @param secretKeyFile
   *          the secretKeyFile to set
   */
  public void setSecretKeyFile(String secretKeyFile) {
    this.secretKeyFile = secretKeyFile;
  }

  /**
   * @return the useWeights
   */
  public boolean getUseWeights() {
    return useWeights;
  }

  /**
   * @param secretKeyFile
   *          the secretKeyFile to set
   */
  public void setUseWeights(boolean useWeights) {
    this.useWeights = useWeights;
  }

  /**
   * @return the adminEmail
   */
  public String getAdminEmail() {
    return adminEmail;
  }

  /**
   * @param adminEmail
   *          the adminEmail to set
   */
  public void setAdminEmail(String adminEmail) {
    if (adminEmail != null && adminEmail.length() == 0)
      adminEmail = null;
    this.adminEmail = adminEmail;
  }

  /**
   * @return the paramRegex
   */
  public String getParamRegex() {
    return paramRegex;
  }

  /**
   * @param paramRegex
   *          the paramRegex to set
   */
  public void setParamRegex(String paramRegex) {
    this.paramRegex = paramRegex;
  }

  /**
   * @return the queryMonitor
   */
  public QueryMonitor getQueryMonitor() {
    return queryMonitor;
  }

  /**
   * @param queryMonitor
   *          the queryMonitor to set
   */
  public void setQueryMonitor(QueryMonitor queryMonitor) {
    this.queryMonitor = queryMonitor;
  }
}
