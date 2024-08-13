package org.gusdb.wdk.model.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig.AuthenticationMethod;

public class ModelConfigBuilder {

  private static final Logger LOG = Logger.getLogger(ModelConfigBuilder.class);

  // basic model information
  private String _modelName;
  private String _projectId;
  private String _gusHome;

  // basic model settings
  private boolean _caching = true;
  private boolean _useWeights = true;
  private String _paramRegex;

  // file locations
  private String _secretKeyFile;
  private String _secretKey;
  private String _wdkTempDir = "/tmp";

  // network locations
  private String _webServiceUrl;
  private String _assetsUrl;

  // email setup
  private String _smtpServer;
  private String _smtpUsername;
  private String _smtpPassword;
  private String _supportEmail;
  private List<String> _adminEmails = Collections.emptyList();
  private String _emailSubject = "";
  private String _emailContent = "";

  // databases
  private ModelConfigUserDB _userDB;
  private ModelConfigAppDB _appDB;

  // user dataset config
  private ModelConfigUserDatasetStore _userDatasetStoreConfig;

  // query performance monitoring
  private QueryMonitor _queryMonitor = new QueryMonitor();

  // JVM thread monitoring
  private boolean _monitorBlockedThreads = true;
  private int _blockedThreshold = 20;

  // user authentication setup
  private AuthenticationMethod _authenticationMethod = AuthenticationMethod.USER_DB;
  private String _oauthUrl = ""; // needed if method is OAUTH2
  private String _oauthClientId = ""; // needed if method is OAUTH2
  private String _oauthClientSecret = ""; // needed if method is OAUTH2
  private String _changePasswordUrl = ""; // probably needed if method is OAUTH2

  // SSL key store information
  private String _keyStoreFile = "";
  private String _keyStorePassPhrase = "";

  public ModelConfig build() throws WdkModelException {

    // data validation
    assertNonNull("modelName", _modelName);
    assertNonNull("projectId", _projectId);
    assertNonNull("gusHome", _gusHome);
    Path gusHome = Paths.get(_gusHome);
    // TODO: check gusHome presence and readability; we currently use a symbolic link which complicates matters
    Optional<Path> secretKeyFile = Optional.ofNullable(_secretKeyFile).map(f -> Paths.get(f));
    assertNonNull("wdkTempDir", _wdkTempDir);
    // create wdk temp dir and parents if they do not exist
    Path wdkTempDir = Functions.mapException(
        () -> IoUtil.createOpenPermsDirectories(Paths.get(_wdkTempDir)),
        e -> new WdkModelException(e));
    // confirm that temp dir is present and readable after possible creation
    wdkTempDir = IoUtil.getWriteableDirectoryOrThrow(wdkTempDir.toString(),
        () -> new WdkModelException("wdkTempDir " + _wdkTempDir + " is not a writeable directory.")).toPath();
    assertNonNull("webServiceUrl", _webServiceUrl);
    // TODO: assess how we can get rid of this - seems optional in some cases
    //assertNonNull("assetsUrl", _assetsUrl);
    assertNonNull("supportEmail", _supportEmail);
    assertNonNull("userDb", _userDB);
    assertNonNull("appDb", _appDB);
    // TODO: should probably have a default stub for this to avoid NPEs
    //assertNonNull("userDatasetStoreConfig", _userDatasetStoreConfig);
    Optional<String> smtpUsername = Optional.ofNullable(_smtpUsername).filter(s -> !s.isBlank());
    Optional<String> smtpPassword = Optional.ofNullable(_smtpPassword).filter(s -> !s.isBlank());
    String smtpServer = _smtpServer == null ? "localhost" : _smtpServer;

    return new ModelConfig(

      // basic model information
      _modelName,
      _projectId,
      gusHome,

      // basic model settings
      _caching,
      _useWeights,
      _paramRegex,

      // file locations
      secretKeyFile,
      _secretKey,
      wdkTempDir,

      // network locations
      _webServiceUrl,
      _assetsUrl,

      // email setup
      smtpServer,
      smtpUsername,
      smtpPassword,
      _supportEmail,
      _adminEmails,
      _emailSubject,
      _emailContent,

      // databases
      _userDB,
      _appDB,

      // user dataset config
      _userDatasetStoreConfig,

      // query performance monitoring
      _queryMonitor,

      // JVM thread monitoring
      _monitorBlockedThreads,
      _blockedThreshold,

      // user authentication setup
      _authenticationMethod,
      _oauthUrl,
      _oauthClientId,
      _oauthClientSecret,
      _changePasswordUrl,

      // SSL key store information
      _keyStoreFile,
      _keyStorePassPhrase
    );
  }

  private void assertNonNull(String name, Object value) throws WdkModelException {
    if (value == null) {
      throw new WdkModelException(name + " is a required property.");
    }
  }

  public void setMonitorBlockedThreads(boolean monitorBlockedThreads) {
    _monitorBlockedThreads = monitorBlockedThreads;
  }

  public void setBlockedThreshold(int blockedThreshold) {
    _blockedThreshold = blockedThreshold;
  }

  /**
   * @param projectId
   *          the projectId to set
   */
  public void setProjectId(String projectId) {
    _projectId = projectId;
  }

  /**
   * @param gusHome
   *          the gusHome to set
   */
  public void setGusHome(String gusHome) {
    _gusHome = gusHome;
  }

  /**
   * @param modelName
   *          the modelName to set
   */
  public void setModelName(String modelName) {
    _modelName = modelName;
  }

  public void setWebServiceUrl(String urlString) {
    _webServiceUrl = urlString;
  }

  /**
   * @param smtpServer
   *          The smtpServer to set.
   */
  public void setSmtpServer(String smtpServer) {
    _smtpServer = smtpServer;
  }

  public void setSmtpUsername(String smtpUsername) {
    _smtpUsername = smtpUsername;
  }

  public void setSmtpPassword(String smtpPassword) {
    _smtpPassword = smtpPassword;
  }

  /**
   * @param emailContent
   *          The emailContent to set.
   */
  public void setEmailContent(String emailContent) {
    _emailContent = emailContent;
  }

  /**
   * @param emailSubject
   *          The emailSubject to set.
   */
  public void setEmailSubject(String emailSubject) {
    _emailSubject = emailSubject;
  }

  public void setSupportEmail(String supportEmail) {
    _supportEmail = supportEmail;
  }

  public void setAssetsUrl(String assetsUrl) {
    _assetsUrl = assetsUrl;
  }

  /**
   * @param webAppUrl web app URL (no longer supported)
   */
  public void setWebAppUrl(String webAppUrl) {
    LOG.warn("WDK Model Config item `webAppUrl` is no longer used." +
        " If you rely on it in your custom code, consider using a WDK Model Property instead.");
  }

  /**
   * @param userDB
   *          the userDB to set
   */
  public void setUserDB(ModelConfigUserDB userDB) {
    _userDB = userDB;
  }

  /**
   * @param appDB
   *          the appDB to set
   */
  public void setAppDB(ModelConfigAppDB appDB) {
    _appDB = appDB;
  }

  /**
   * @param secretKeyFile
   *          the secretKeyFile to set
   */
  public void setSecretKeyFile(String secretKeyFile) {
    _secretKeyFile = secretKeyFile;
  }

  /**
   * @param secretKey
   *          the secretKeyFile to set
   */
  public void setSecretKey(String secretKey) {
    _secretKey = secretKey;
  }

  /**
   * @param authenticationMethod configured authentication method
   */
  public void setAuthenticationMethod(String authenticationMethod) {
    LOG.debug("Setting authentication method: " + authenticationMethod);
    _authenticationMethod = AuthenticationMethod.valueOf(authenticationMethod.toUpperCase());
  }

  /**
   * @param oauthUrl base URL of OAuth2 server to use for authentication
   * (used only if authentication method is OAUTH2)
   */
  public void setOauthUrl(String oauthUrl) {
    _oauthUrl = oauthUrl;
  }

  /**
   * @param oauthClientId OAuth2 client ID to use for authentication
   * (used only if authentication method is OAUTH2)
   */
  public void setOauthClientId(String oauthClientId) {
    _oauthClientId = oauthClientId;
  }

  /**
   * @param oauthClientSecret OAuth2 client secret to use for authentication
   * (used only if authentication method is OAUTH2)
   */
  public void setOauthClientSecret(String oauthClientSecret) {
    _oauthClientSecret = oauthClientSecret;
  }

  /**
   * @param changePasswordUrl custom change password URL to set
   */
  public void setChangePasswordUrl(String changePasswordUrl) {
    LOG.debug("Overriding Change Password Page URL: " + changePasswordUrl);
    _changePasswordUrl = changePasswordUrl;
  }

  /**
   * @param keyStoreFile key store file containing acceptable certs
   * (used only if authentication method is OAUTH2)
   */
  public void setKeyStoreFile(String keyStoreFile) {
    _keyStoreFile = keyStoreFile;
  }

  /**
   * @param keyStorePassPhrase pass phrase needed to access key store
   * (used only if authentication method is OAUTH2)
   */
  public void setKeyStorePassPhrase(String keyStorePassPhrase) {
    _keyStorePassPhrase = keyStorePassPhrase;
  }

  /**
   * @param useWeights whether to use weights
   */
  public void setUseWeights(boolean useWeights) {
    _useWeights = useWeights;
  }

  /**
   * @param adminEmail comma-delimited list of admin email addresses
   */
  public void setAdminEmail(String adminEmail) {
    _adminEmails = (adminEmail == null || adminEmail.trim().isEmpty() ?
      Collections.emptyList() : Arrays.asList(adminEmail.trim().split("[,\\s]+")));
  }

  /**
   * @param paramRegex
   *          the paramRegex to set
   */
  public void setParamRegex(String paramRegex) {
    _paramRegex = paramRegex;
  }

  /**
   * @param queryMonitor
   *          the queryMonitor to set
   */
  public void setQueryMonitor(QueryMonitor queryMonitor) {
    _queryMonitor = queryMonitor;
  }

  public void setCaching(boolean caching) {
    _caching = caching;
  }

  public void setUserDatasetStore(ModelConfigUserDatasetStore udsConfig) {
    _userDatasetStoreConfig = udsConfig;
  }

  public void setWdkTempDir(String wdkTempDir) {
    _wdkTempDir = wdkTempDir;
  }

}
