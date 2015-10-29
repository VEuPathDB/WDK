package org.gusdb.wdk.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.json.JSONObject;

public class OAuthClient {

  private final String oauthServerBase;
  private final String clientId;
  private final String clientSecret;

  public OAuthClient(ModelConfig modelConfig) {
    oauthServerBase = modelConfig.getOauthUrl();
    clientId = modelConfig.getOauthClientId();
    clientSecret = modelConfig.getOauthClientSecret();
  }
  
  public int getUserIdFromAuthCode(String authCode, String authCodeRedirectUrl) throws WdkModelException {

    try {
      String oauthUrl = oauthServerBase + "/token";
  
      // build form parameters for token request
      MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
      formData.add("grant_type", "authorization_code");
      formData.add("code", authCode);
      formData.add("redirect_uri", authCodeRedirectUrl);
      formData.add("client_id", clientId);
      formData.add("client_secret", clientSecret);
  
      TrustManager trustManager = new X509ExtendedTrustManager() {
  
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { }
  
        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { }
  
        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0]; }
  
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
            throws CertificateException { }
  
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
            throws CertificateException { }
  
        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
            throws CertificateException { }
  
        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
            throws CertificateException { }
      };
      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, new TrustManager[]{ trustManager }, null);
      HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };
  
      // build request and get token response
      Response response = ClientBuilder.newBuilder()
          .withConfig(new ClientConfig())
          .sslContext(sslContext)
          .hostnameVerifier(hostnameVerifier)
          .build()
          .target(oauthUrl)
          .request(MediaType.APPLICATION_JSON)
          .post(Entity.form(formData));
  
      if (response.getStatus() == 200) {
        // Success!  Read result into buffer and convert to JSON
        InputStream resultStream = (InputStream)response.getEntity();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IoUtil.transferStream(buffer, resultStream);
        JSONObject json = new JSONObject(new String(buffer.toByteArray()));
        int userId = json.getInt("id");
        return userId;
      }
      else {
        // Failure; throw exception
        throw new WdkModelException("OAuth2 token request failed with status " +
            response.getStatus() + "!  " + response.getStatusInfo().getReasonPhrase());
      }
    }
    catch(WdkModelException e) {
      throw e;
    }
    catch(Exception e) {
      throw new WdkModelException("Unable to complete OAuth token request to fetch user id", e);
    }
  }
}
