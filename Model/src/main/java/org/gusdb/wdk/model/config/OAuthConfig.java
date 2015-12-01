package org.gusdb.wdk.model.config;

public interface OAuthConfig {

  /**
   * @return base URL of client web application (OAuth client app)
   */
  public String getWebAppUrl();

  /**
   * @return base URL of OAuth2 server to use for authentication
   */
  public String getOauthUrl();

  /**
   * @return OAuth2 client ID to use for authentication
   */
  public String getOauthClientId();

  /**
   * @return OAuth2 client secret to use for authentication
   */
  public String getOauthClientSecret();

  /**
   * @return key store file containing acceptable SSL hosts/certs
   */
  public String getKeyStoreFile();

  /**
   * @return pass phrase needed to access key store
   */
  public String getKeyStorePassPhrase();
}
