package org.gusdb.wdk.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import org.gusdb.wdk.model.WdkModelException;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class GoogleClientUtil {

  private static GoogleIdTokenVerifier VERIFIER = null;
  
  private static synchronized GoogleIdTokenVerifier getVerifier(String clientId) {
    if (VERIFIER == null) {
      // this is ok because there is only one client ID per webapp
      VERIFIER = createVerifier(clientId);
    }
    return VERIFIER;
  }

  public static String getEmailFromTokenResponse(String idTokenStr, String clientId) throws WdkModelException {
    try {
      GoogleIdToken idToken = getVerifier(clientId).verify(idTokenStr);
      if (idToken == null) {
        throw new WdkModelException("Unable to verify Google ID token.  Verifier returned null.");
      }

      Payload payload = idToken.getPayload();
      // TODO: probably want to check the following...  later
      //   payload.getHostedDomain().equals(APPS_DOMAIN_NAME)
      //   { clientId }.contains(payload.getAuthorizedParty())
      return payload.getEmail();
    }
    catch (IOException | GeneralSecurityException e) {
      throw new WdkModelException("Unable to verify ID token", e);
    }
  }

  private static GoogleIdTokenVerifier createVerifier(String clientId) {
    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    return new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
        .setAudience(Arrays.asList(clientId)).build();
  }
}
