package org.gusdb.wdk.model.config;

import org.apache.log4j.Logger;

/**
 * An object representaion of the {@code model-config.xml} file. It holds all the configuration information
 * for the WDK system.
 * 
 * @author Jerric
 * @modified Jan 6, 2006 - Jerric add a property for the name of query history table
 */
public class ModelConfig {

  private static final Logger LOG = Logger.getLogger(ModelConfig.class);

  public static final String WSF_LOCAL = "local";

  public static enum AuthenticationMethod {
    USER_DB, OAUTH2;
    public String getName() {
      return name();
    }
  }
  
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
   * the relative or absolute url to find assets
   */
  private String assetsUrl;

  /**
   * the absolute url to this site's webapp
   */
  private String webAppUrl;

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
   * The projectId is not part of the config file content, it is input by the user
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
   * enable or disable global caching. default enabled, and then each individual sqlQuery can control their
   * own cache behavior. if global caching is disabled, then caching on sqlQuery will always be disabled,
   * regardless of the individual setting on the query. Please note that this flag has no effect on
   * processQueries, which is always cached.
   */
  private boolean caching = true;

  /**
   * Authentication can be performed either the traditional way (i.e. directly
   * by WDK using the userDb), or using an OAuth server to authenticate users
   * remotely.  The OAuth server must provide access to a user id resource (a la
   * OpenID Connect).
   */
  private AuthenticationMethod authenticationMethod = AuthenticationMethod.USER_DB;
  private String oauthUrl = ""; // needed if method is OAUTH2

  /**
   * If it returns true, a monitoring thread will be turned on when webapp is initialized.
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
   * An report will be fired when the number of blocked threads reaches the threshold.
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

  public String getAssetsUrl() {
    return assetsUrl;
  }

  public void setAssetsUrl(String assetsUrl) {
    this.assetsUrl = assetsUrl;
  }

  public String getWebAppUrl() {
    return webAppUrl;
  }

  public void setWebAppUrl(String webAppUrl) {
    this.webAppUrl = webAppUrl;
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
   * @return configured authentication method
   */
  public AuthenticationMethod getAuthenticationMethodEnum() {
    LOG.info("Returning authentication method: " + authenticationMethod);
    return authenticationMethod;
  }

  /**
   * @param authenticationMethod configured authentication method
   */
  public void setAuthenticationMethod(String authenticationMethod) {
    LOG.info("Setting authentication method: " + authenticationMethod);
    this.authenticationMethod = AuthenticationMethod.valueOf(authenticationMethod.toUpperCase());
  }

  /**
   * @return base URL of OAuth2 server to use for authentication
   * (called only if authentication method is OAUTH2)
   */
  public String getOauthUrl() {
    LOG.info("Returning OAuth2 URL: " + oauthUrl);
    return oauthUrl;
  }

  /**
   * @param oauthUrl base URL of OAuth2 server to use for authentication
   * (used only if authentication method is OAUTH2)
   */
  public void setOauthUrl(String oauthUrl) {
    LOG.info("Settign OAuth2 URL: " + oauthUrl);
    this.oauthUrl = oauthUrl;
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

  /**
   * enable or disable global caching. default enabled, and then each individual sqlQuery can control their
   * own cache behavior. if global caching is disabled, then caching on sqlQuery will always be disabled,
   * regardless of the individual setting on the query. Please note that this flag has no effect on
   * processQueries, which is always cached.
   * 
   * @return whether global caching is turned on or not.
   */
  public boolean isCaching() {
    return caching;
  }

  public void setCaching(boolean caching) {
    this.caching = caching;
  }

}
