package org.gusdb.wdk.model.config;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.oauth2.client.KeyStoreTrustManager.KeyStoreConfig;
import org.gusdb.oauth2.client.OAuthConfig;

/**
 * An object representation of the {@code model-config.xml} file. It holds all the configuration information
 * for the WDK system.
 * 
 * @author Jerric
 */
public class ModelConfig implements OAuthConfig, KeyStoreConfig {

  private static final Logger LOG = Logger.getLogger(ModelConfig.class);

  public static final String WSF_LOCAL = "local";

  public enum AuthenticationMethod implements NamedObject {
    USER_DB, OAUTH2;
    @Override
    public String getName() {
      return name();
    }
  }

  private final String _modelName;
  private final String _webServiceUrl;

  // the information for registration email

  /**
   * the SMTP server used to send registration & recover password emails.
   */
  private final String _smtpServer;

  /**
   * the reply of the registration & recover password emails.
   */
  private final String _supportEmail;

  /**
   * the relative or absolute url to find assets
   */
  private final String _assetsUrl;

  /**
   * the recipient of the super slow query log.
   */
  private final List<String> _adminEmails;

  /**
   * the subject of the registration email.
   */
  private final String _emailSubject;

  /**
   * the content of the registration email.
   */
  private final String _emailContent;

  private final ModelConfigUserDB _userDB;
  private final ModelConfigAppDB _appDB;

  private final ModelConfigUserDatasetStore _userDatasetStoreConfig;

  private final QueryMonitor _queryMonitor;

  /**
   * The projectId is not part of the config file content, it is input by the user
   */
  private final String _projectId;
  private final Path _gusHome;

  /**
   * enable/disable weight feature in the steps. default enabled.
   */
  private final boolean _useWeights;

  /**
   * location of secret key file
   */
  private final Optional<Path> _secretKeyFile;

  /**
   * cached secret key; value is assigned at construction or as soon as the
   * secretKeyFile is present and readable when the secret key is requested
   */
  private String _secretKey;

  /**
   * default regex used by all the stringParams
   */
  private final String _paramRegex;

  /**
   * turn on thread monitor process if set to true
   */
  private final boolean _monitorBlockedThreads;

  /**
   * if blocked
   */
  private final int _blockedThreshold;

  /**
   * enable or disable global caching. default enabled, and then each individual sqlQuery can control their
   * own cache behavior. if global caching is disabled, then caching on sqlQuery will always be disabled,
   * regardless of the individual setting on the query. Please note that this flag has no effect on
   * processQueries, which is always cached.
   */
  private final boolean _caching;

  /**
   * Authentication can be performed either the traditional way (i.e. directly
   * by WDK using the userDb), or using an OAuth server to authenticate users
   * remotely.  The OAuth server must provide access to a user id resource (a la
   * OpenID Connect).
   */
  private final AuthenticationMethod _authenticationMethod;
  private final String _oauthUrl;          // needed if method is OAUTH2
  private final String _oauthClientId;     // needed if method is OAUTH2
  private final String _oauthClientSecret; // needed if method is OAUTH2
  private final String _changePasswordUrl; // probably needed if method is OAUTH2

  /**
   * Specify keystore file and pass phrase if SSL security checking is desired
   */
  private final String _keyStoreFile;
  private final String _keyStorePassPhrase;

  /**
   * Specifies the directory within which the wdk will house temporary files.
   */
  private final Path _wdkTempDir;

  public ModelConfig(String modelName, String projectId, Path gusHome, boolean caching, boolean useWeights,
      String paramRegex, Optional<Path> secretKeyFile, Path wdkTempDir, String webServiceUrl, String assetsUrl,
      String smtpServer, String supportEmail, List<String> adminEmails, String emailSubject,
      String emailContent, ModelConfigUserDB userDB, ModelConfigAppDB appDB,
      ModelConfigUserDatasetStore userDatasetStoreConfig, QueryMonitor queryMonitor,
      boolean monitorBlockedThreads, int blockedThreshold, AuthenticationMethod authenticationMethod,
      String oauthUrl, String oauthClientId, String oauthClientSecret, String changePasswordUrl,
      String keyStoreFile, String keyStorePassPhrase) {

    // basic model information
    _modelName = modelName;
    _projectId = projectId;
    _gusHome = gusHome;

    // basic model settings
    _caching = caching;
    _useWeights = useWeights;
    _paramRegex = paramRegex;

    // file locations
    _wdkTempDir = wdkTempDir;
    _secretKeyFile = secretKeyFile;

    // network locations
    _webServiceUrl = webServiceUrl;
    _assetsUrl = assetsUrl;

    // email setup
    _smtpServer = smtpServer;
    _supportEmail = supportEmail;
    _adminEmails = adminEmails;
    _emailSubject = emailSubject;
    _emailContent = emailContent;

    // databases
    _userDB = userDB;
    _appDB = appDB;

    // user dataset config
    _userDatasetStoreConfig = userDatasetStoreConfig;

    // query performance monitoring
    _queryMonitor = queryMonitor;

    // JVM thread monitoring
    _monitorBlockedThreads = monitorBlockedThreads;
    _blockedThreshold = blockedThreshold;

    // user authentication setup
    _authenticationMethod = authenticationMethod;
    _oauthUrl = oauthUrl;
    _oauthClientId = oauthClientId;
    _oauthClientSecret = oauthClientSecret;
    _changePasswordUrl = changePasswordUrl;

    // SSL key store information
    _keyStoreFile = keyStoreFile;
    _keyStorePassPhrase = keyStorePassPhrase;

    // get secret key at object creation time if available
    getSecretKey();
  }

  /**
   * If it returns true, a monitoring thread will be turned on when webapp is initialized.
   * 
   * @return
   */
  public boolean isMonitorBlockedThreads() {
    return _monitorBlockedThreads;
  }

  /**
   * An report will be fired when the number of blocked threads reaches the threshold.
   * 
   * @return
   */
  public int getBlockedThreshold() {
    return _blockedThreshold;
  }

  /**
   * @return the projectId
   */
  public String getProjectId() {
    return _projectId;
  }

  /**
   * @return the gusHome
   */
  public Path getGusHome() {
    return _gusHome;
  }

  /**
   * @return the modelName
   */
  public String getModelName() {
    return _modelName;
  }

  public String getWebServiceUrl() {
    return _webServiceUrl;
  }

  /**
   * @return Returns the smtpServer.
   */
  public String getSmtpServer() {
    return _smtpServer;
  }

  /**
   * @return Returns the emailContent.
   */
  public String getEmailContent() {
    return _emailContent;
  }

  /**
   * @return Returns the emailSubject.
   */
  public String getEmailSubject() {
    return _emailSubject;
  }

  public String getSupportEmail() {
    return _supportEmail;
  }

  public String getAssetsUrl() {
    return _assetsUrl;
  }

  /**
   * @return the userDB
   */
  public ModelConfigUserDB getUserDB() {
    return _userDB;
  }

  /**
   * @return the appDB
   */
  public ModelConfigAppDB getAppDB() {
    return _appDB;
  }

  /**
   * Returns a cached secret key, generated by encrypting the value in the
   * configured secret key file.  If the configured filename is null or the contents
   * of the file cannot be read for any reason, null is returned.
   * 
   * @return secret key
   */
  public String getSecretKey() {
    if (_secretKey == null && _secretKeyFile.isPresent()) {
      try (FileReader in = new FileReader(_secretKeyFile.get().toFile())) {
        _secretKey = EncryptionUtil.md5(IoUtil.readAllChars(in).strip());
      }
      catch (IOException e) {
        // log error but otherwise ignore so null is returned; problem may be remedied in the future
        LOG.warn("Unable to read secret key value from file: " + _secretKeyFile, e);
      }
    }
    return _secretKey;
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
    return getAuthenticationMethodEnum().getName();
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
   * @return OAuth2 client ID to use for authentication
   * (called only if authentication method is OAUTH2)
   */
  @Override
  public String getOauthClientId() {
    return _oauthClientId;
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
   * @return custom change password URL if specified
   */
  public String getChangePasswordUrl() {
    return _changePasswordUrl;
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
   * @return pass phrase needed to access key store
   * (called only if authentication method is OAUTH2)
   */
  @Override
  public String getKeyStorePassPhrase() {
    return _keyStorePassPhrase;
  }

  /**
   * @return the useWeights
   */
  public boolean getUseWeights() {
    return _useWeights;
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
   * @return the paramRegex
   */
  public String getParamRegex() {
    return _paramRegex;
  }

  /**
   * @return the queryMonitor
   */
  public QueryMonitor getQueryMonitor() {
    return _queryMonitor;
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

  /**
   * The config for a user dataset store. Optional.  Might be null.
   * @return
   */
  public ModelConfigUserDatasetStore getUserDatasetStoreConfig() {
    return _userDatasetStoreConfig;
  }

  public Path getWdkTempDir() {
    return _wdkTempDir;
  }

}
