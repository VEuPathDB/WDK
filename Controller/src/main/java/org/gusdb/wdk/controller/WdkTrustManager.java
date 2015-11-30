package org.gusdb.wdk.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;

/**
 * Implementation of SSL trust manager that uses a Java key store for the basis
 * of certification checks.  Failures are simply logged.  An alternate
 * constructor creates a "dummy" trust manager that allows anything (i.e. no
 * security at all).  This can be used when the keystore file is unknown,
 * unavailable, or unconfigured.
 * 
 * This code was adapted from a sample and explanation here:
 *   https://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html
 * 
 * To see the certs (probably) loaded by this class by EuPathDB, run:
 *   > keytool -list -v -keystore /etc/pki/java/cacerts
 * 
 * @author rdoherty
 */
public class WdkTrustManager extends X509ExtendedTrustManager {

  private static final Logger LOG = Logger.getLogger(WdkTrustManager.class);

  /**
   * The default PKIX X509ExtendedTrustManager. We'll delegate decisions to it,
   * and fall back to the logic in this class if the default
   * X509ExtendedTrustManager doesn't trust it.
   */
  private final X509ExtendedTrustManager _pkixTrustManager;

  public WdkTrustManager() {
    // do nothing; this constructor creates a trust manager that allows anything
    _pkixTrustManager = null;
  }

  public WdkTrustManager(Path keyStoreFile, String passPhrase) throws WdkModelException {
    try {
      // create a "default" JSSE X509ExtendedTrustManager
      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(new FileInputStream(keyStoreFile.toAbsolutePath().toString()), passPhrase.toCharArray());

      TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
      tmf.init(ks);

      TrustManager[] tms = tmf.getTrustManagers();

      /*
       * Iterate over the returned trust managers, look for an instance of X509TrustManager. If found, use that
       * as our "default" trust manager.
       */
      for (int i = 0; i < tms.length; i++) {
        if (tms[i] instanceof X509ExtendedTrustManager) {
          _pkixTrustManager = (X509ExtendedTrustManager) tms[i];
          return;
        }
      }

      // Can't find a valid trust manager in the factory
      throw new WdkModelException("Couldn't initialize trust manager using key store file " + keyStoreFile);
    }
    catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
      throw new WdkModelException("Couldn't initialize trust manager using key store file " + keyStoreFile, e);
    }
  }

  /*
   * Delegate to the default trust manager.
   */
  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    try {
      if (_pkixTrustManager != null) _pkixTrustManager.checkClientTrusted(chain, authType);
    }
    catch (CertificateException e) {
      LOG.error("SSL validation check failed.", e);
      throw e;
    }
  }

  /*
   * Delegate to the default trust manager.
   */
  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    try {
      if (_pkixTrustManager != null) _pkixTrustManager.checkServerTrusted(chain, authType);
    }
    catch (CertificateException e) {
      // Possibly pop up a dialog box asking whether to trust the cert chain?
      LOG.error("SSL validation check failed.", e);
      throw e;
    }
  }

  /*
   * Connection-sensitive verification.
   */
  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException {
    try {
      if (_pkixTrustManager != null) _pkixTrustManager.checkClientTrusted(chain, authType, socket);
    }
    catch (CertificateException e) {
      LOG.error("SSL validation check failed.", e);
      throw e;
    }
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
      throws CertificateException {
    try {
      if (_pkixTrustManager != null) _pkixTrustManager.checkClientTrusted(chain, authType, engine);
    }
    catch (CertificateException e) {
      LOG.error("SSL validation check failed.", e);
      throw e;
    }
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException {
    try {
      if (_pkixTrustManager != null) _pkixTrustManager.checkServerTrusted(chain, authType, socket);
    }
    catch (CertificateException e) {
      LOG.error("SSL validation check failed.", e);
      throw e;
    }
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
      throws CertificateException {
    try {
      if (_pkixTrustManager != null) _pkixTrustManager.checkServerTrusted(chain, authType, engine);
    }
    catch (CertificateException e) {
      LOG.error("SSL validation check failed.", e);
      throw e;
    }
  }

  /*
   * Merely pass this through.
   */
  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return _pkixTrustManager == null ?
        new X509Certificate[]{} :
        _pkixTrustManager.getAcceptedIssuers();
  }
}
