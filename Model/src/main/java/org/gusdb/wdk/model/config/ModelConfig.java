package org.gusdb.wdk.model.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;

/**
 * An object representation of the {@code model-config.xml} file. It holds all the configuration information
 * for the WDK system.
 * 
 * @author Jerric
 * @modified Jan 6, 2006 - Jerric add a property for the name of query history table
 */
public class ModelConfig implements OAuthConfig {

  private static final Logger LOG = Logger.getLogger(ModelConfig.class);

  public static final String WSF_LOCAL = "local";

  public static enum AuthenticationMethod {
    USER_DB, OAUTH2;
    public String getName() {
      return name();
    }
  }

  private String _modelName;
  private String _webServiceUrl;

  // the information for registration email

  /**
   * the SMTP server used to send registration & recover password emails.
   */
  private String _smtpServer;

  /**
   * the reply of the registration & recover password emails.
   */
  private String _supportEmail;

  /**
   * the relative or absolute url to find assets
   */
  private String _assetsUrl;

  /**
   * the absolute url to this site's webapp
   */
  private String _webAppUrl;

  /**
   * the recipient of the super slow query log.
   */
  private List<String> _adminEmails = Collections.EMPTY_LIST;

  /**
   * the subject of the registration email.
   */
  private String _emailSubject;

  /**
   * the content of the registration email.
   */
  private String _emailContent;

  private ModelConfigUserDB _userDB;
  private ModelConfigAppDB _appDB;
  private ModelConfigAccountDB _accountDB;

  private ModelConfigUserDatasetStore _userDatasetStoreConfig;

  private QueryMonitor _queryMonitor = new QueryMonitor();

  /**
   * The projectId is not part of the config file content, it is input by the user
   */
  private String _projectId;
  private String _gusHome;

  /**
   * location of secret key file
   */
  private String _secretKeyFile;

  /**
   * enable/disable weight feature in the steps. default enabled.
   */
  private boolean _useWeights = true;

  /**
   * default regex used by all the stringParams
   */
  private String _paramRegex;

  /**
   * turn on thread monitor process if set to true
   */
  private boolean _monitorBlockedThreads = true;

  /**
   * if blocked
   */
  private int _blockedThreshold = 20;

  /**
   * enable or disable global caching. default enabled, and then each individual sqlQuery can control their
   * own cache behavior. if global caching is disabled, then caching on sqlQuery will always be disabled,
   * regardless of the individual setting on the query. Please note that this flag has no effect on
   * processQueries, which is always cached.
   */
  private boolean _caching = true;

  /**
   * Authentication can be performed either the traditional way (i.e. directly
   * by WDK using the userDb), or using an OAuth server to authenticate users
   * remotely.  The OAuth server must provide access to a user id resource (a la
   * OpenID Connect).
   */
  private AuthenticationMethod _authenticationMethod = AuthenticationMethod.USER_DB;
  private String _oauthUrl = "";          // needed if method is OAUTH2
  private String _oauthClientId = "";     // needed if method is OAUTH2
  private String _oauthClientSecret = ""; // needed if method is OAUTH2
  private String _changePasswordUrl = ""; // probably needed if method is OAUTH2

  /**
   * Specify keystore file and pass phrase if SSL security checking is desired
   */
  private String _keyStoreFile = "";
  private String _keyStorePassPhrase = "";

  /**
   * Specifies the directory within which the wdk will house temporary files.
   */
  private String _wdkTempDir;

  /**
   * If it returns true, a monitoring thread will be turned on when webapp is initialized.
   * 
   * @return
   */
  public boolean isMonitorBlockedThreads() {
    return _monitorBlockedThreads;
  }

  public void setMonitorBlockedThreads(boolean monitorBlockedThreads) {
    _monitorBlockedThreads = monitorBlockedThreads;
  }

  /**
   * An report will be fired when the number of blocked threads reaches the threshold.
   * 
   * @return
   */
  public int getBlockedThreshold() {
    return _blockedThreshold;
  }

  public void setBlockedThreshold(int blockedThreshold) {
    _blockedThreshold = blockedThreshold;
  }

  /**
   * @return the projectId
   */
  public String getProjectId() {
    return _projectId;
  }

  /**
   * @param projectId
   *          the projectId to set
   */
  public void setProjectId(String projectId) {
    _projectId = projectId;
  }

  /**
   * @return the gusHome
   */
  public String getGusHome() {
    return _gusHome;
  }

  /**
   * @param gusHome
   *          the gusHome to set
   */
  public void setGusHome(String gusHome) {
    _gusHome = gusHome;
  }

  /**
   * @return the modelName
   */
  public String getModelName() {
    return _modelName;
  }

  /**
   * @param modelName
   *          the modelName to set
   */
  public void setModelName(String modelName) {
    _modelName = modelName;
  }

  public String getWebServiceUrl() {
    return _webServiceUrl;
  }

  public void setWebServiceUrl(String urlString) {
    _webServiceUrl = urlString;
  }

  /**
   * @return Returns the smtpServer.
   */
  public String getSmtpServer() {
    return _smtpServer;
  }

  /**
   * @param smtpServer
   *          The smtpServer to set.
   */
  public void setSmtpServer(String smtpServer) {
    _smtpServer = smtpServer;
  }

  /**
   * @return Returns the emailContent.
   */
  public String getEmailContent() {
    return _emailContent;
  }

  /**
   * @param emailContent
   *          The emailContent to set.
   */
  public void setEmailContent(String emailContent) {
    _emailContent = emailContent;
  }

  /**
   * @return Returns the emailSubject.
   */
  public String getEmailSubject() {
    return _emailSubject;
  }

  /**
   * @param emailSubject
   *          The emailSubject to set.
   */
  public void setEmailSubject(String emailSubject) {
    _emailSubject = emailSubject;
  }

  public String getSupportEmail() {
    return _supportEmail;
  }

  public void setSupportEmail(String supportEmail) {
    _supportEmail = supportEmail;
  }

  public String getAssetsUrl() {
    return (_assetsUrl != null ? _assetsUrl : _webAppUrl);
  }

  public void setAssetsUrl(String assetsUrl) {
    _assetsUrl = assetsUrl;
  }

  @Override
  public String getWebAppUrl() {
    return _webAppUrl;
  }

  public String getApplicationBaseUrl() {
    return getWebAppUrlParts().getFirst();
  }

  public String getWebAppName() {
    return getWebAppUrlParts().getSecond();
  }

  private TwoTuple<String,String> getWebAppUrlParts() {
    // trim trailing slash
    String trimmedUrl = _webAppUrl;
    if (trimmedUrl.endsWith("/")) {
      trimmedUrl = trimmedUrl.substring(0, trimmedUrl.length() - 1);
    }
    int splitIndex = trimmedUrl.lastIndexOf("/");
    return new TwoTuple<>(trimmedUrl.substring(0, splitIndex), trimmedUrl.substring(splitIndex + 1));
  }

  public void setWebAppUrl(String webAppUrl) {
    _webAppUrl = webAppUrl;
  }

  /**
   * @return the userDB
   */
  public ModelConfigUserDB getUserDB() {
    return _userDB;
  }

  /**
   * @param userDB
   *          the userDB to set
   */
  public void setUserDB(ModelConfigUserDB userDB) {
    _userDB = userDB;
  }

  /**
   * @return the appDB
   */
  public ModelConfigAppDB getAppDB() {
    return _appDB;
  }

  /**
   * @param appDB
   *          the appDB to set
   */
  public void setAppDB(ModelConfigAppDB appDB) {
    _appDB = appDB;
  }

  /**
   * @return the accountDB
   */
  public ModelConfigAccountDB getAccountDB() {
    return _accountDB;
  }

  /**
   * @param accountDB
   *          the accountDB to set
   */
  public void setAccountDB(ModelConfigAccountDB accountDB) {
    _accountDB = accountDB;
  }

  /**
   * @return the secretKeyFile
   */
  public String getSecretKeyFile() {
    return _secretKeyFile;
  }

  /**
   * @param secretKeyFile
   *          the secretKeyFile to set
   */
  public void setSecretKeyFile(String secretKeyFile) {
    _secretKeyFile = secretKeyFile;
  }

  /**
   * @return configured authentication method
   */
  public AuthenticationMethod getAuthenticationMethodEnum() {
    return _authenticationMethod;
  }

  /**
   * @return configured authentication method name as a String.
   * Used by Java management beans to register configuration values
   * by introspection of getter methods.
   */
  public String getAuthenticationMethod() {
    return this.getAuthenticationMethodEnum().getName();
  }

  /**
   * @param authenticationMethod configured authentication method
   */
  public void setAuthenticationMethod(String authenticationMethod) {
    LOG.debug("Setting authentication method: " + authenticationMethod);
    _authenticationMethod = AuthenticationMethod.valueOf(authenticationMethod.toUpperCase());
  }

  /**
   * @return base URL of OAuth2 server to use for authentication
   * (called only if authentication method is OAUTH2)
   */
  @Override
  public String getOauthUrl() {
    return _oauthUrl;
  }

  /**
   * @param oauthUrl base URL of OAuth2 server to use for authentication
   * (used only if authentication method is OAUTH2)
   */
  public void setOauthUrl(String oauthUrl) {
    _oauthUrl = oauthUrl;
  }

  /**
   * @return OAuth2 client ID to use for authentication
   * (called only if authentication method is OAUTH2)
   */
  @Override
  public String getOauthClientId() {
    return _oauthClientId;
  }

  /**
   * @param _oauthUrl OAuth2 client ID to use for authentication
   * (used only if authentication method is OAUTH2)
   */
  public void setOauthClientId(String oauthClientId) {
    _oauthClientId = oauthClientId;
  }

  /**
   * @return OAuth2 client secret to use for authentication
   * (called only if authentication method is OAUTH2)
   */
  @Override
  public String getOauthClientSecret() {
    return _oauthClientSecret;
  }

  /**
   * @param _oauthUrl OAuth2 client secret to use for authentication
   * (used only if authentication method is OAUTH2)
   */
  public void setOauthClientSecret(String oauthClientSecret) {
    _oauthClientSecret = oauthClientSecret;
  }

  /**
   * @return custom change password URL if specified
   */
  public String getChangePasswordUrl() {
    return _changePasswordUrl;
  }

  /**
   * @param changePasswordUrl custom change password URL to set
   */
  public void setChangePasswordUrl(String changePasswordUrl) {
    LOG.debug("Overriding Change Password Page URL: " + changePasswordUrl);
    _changePasswordUrl = changePasswordUrl;
  }

  /**
   * @return key store file containing acceptable SSL hosts/certs
   * (called only if authentication method is OAUTH2)
   */
  @Override
  public String getKeyStoreFile() {
    return _keyStoreFile;
  }

  /**
   * @param keyStoreFile key store file containing acceptable certs
   * (used only if authentication method is OAUTH2)
   */
  public void setKeyStoreFile(String keyStoreFile) {
    _keyStoreFile = keyStoreFile;
  }

  /**
   * @return pass phrase needed to access key store
   * (called only if authentication method is OAUTH2)
   */
  @Override
  public String getKeyStorePassPhrase() {
    return _keyStorePassPhrase;
  }

  /**
   * @param keyStorePassPhrase pass phrase needed to access key store
   * (used only if authentication method is OAUTH2)
   */
  public void setKeyStorePassPhrase(String keyStorePassPhrase) {
    _keyStorePassPhrase = keyStorePassPhrase;
  }

  /**
   * @return the useWeights
   */
  public boolean getUseWeights() {
    return _useWeights;
  }

  /**
   * @param _secretKeyFile
   *          the secretKeyFile to set
   */
  public void setUseWeights(boolean useWeights) {
    _useWeights = useWeights;
  }

  /**
   * @return the adminEmails as a list of individual addresses
   */
  public List<String> getAdminEmails() {
    return _adminEmails;
  }

  /**
   * @return comma-delimited list of admin emails; this is probably the
   * original value contained in the XML file but possibly slightly different
   */
  public String getAdminEmail() {
    return FormatUtil.join(_adminEmails.toArray(), ",");
  }
  

  /**
   * @param adminEmail comma-delimited list of admin email addresses
   */
  public void setAdminEmail(String adminEmail) {
    _adminEmails = (adminEmail == null || adminEmail.trim().isEmpty() ?
      Collections.EMPTY_LIST : Arrays.asList(adminEmail.trim().split("[,\\s]+")));
  }

  /**
   * @return the paramRegex
   */
  public String getParamRegex() {
    return _paramRegex;
  }

  /**
   * @param paramRegex
   *          the paramRegex to set
   */
  public void setParamRegex(String paramRegex) {
    _paramRegex = paramRegex;
  }

  /**
   * @return the queryMonitor
   */
  public QueryMonitor getQueryMonitor() {
    return _queryMonitor;
  }

  /**
   * @param queryMonitor
   *          the queryMonitor to set
   */
  public void setQueryMonitor(QueryMonitor queryMonitor) {
    _queryMonitor = queryMonitor;
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
    return _caching;
  }

  public void setCaching(boolean caching) {
    _caching = caching;
  }

  public void setUserDatasetStore(ModelConfigUserDatasetStore udsConfig) {
    _userDatasetStoreConfig = udsConfig;
  }

  /**
   * The config for a user dataset store. Optional.  Might be null.
   * @return
   */
  public ModelConfigUserDatasetStore getUserDatasetStoreConfig() {
    return _userDatasetStoreConfig;
  }

  public String getWdkTempDir() {
    return _wdkTempDir;
  }

  public void setWdkTempDir(String wdkTempDir) {
    _wdkTempDir = wdkTempDir;
  }

}
