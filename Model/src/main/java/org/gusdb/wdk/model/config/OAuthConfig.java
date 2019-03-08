package org.gusdb.wdk.model.config;

public interface OAuthConfig {

  /**
   * @return base URL of OAuth2 server to use for authentication
   */
  String getOauthUrl();

  /**
   * @return OAuth2 client ID to use for authentication
   */
  String getOauthClientId();

  /**
   * @return OAuth2 client secret to use for authentication
   */
  String getOauthClientSecret();

  /**
   * @return key store file containing acceptable SSL hosts/certs
   */
  String getKeyStoreFile();

  /**
   * @return pass phrase needed to access key store
   */
  String getKeyStorePassPhrase();
}
