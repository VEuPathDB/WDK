package org.gusdb.wdk.controller;

import java.util.List;
import java.util.Map;

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

/**
 * Provides OpenId-related authentication services
 * 
 * @author rdoherty
 * Though mostly copied from example code found here:
 * http://code.google.com/p/openid4java/wiki/SampleConsumer
 */
public class AuthenticationService {

    private ConsumerManager _manager;
    private DiscoveryInformation _discoveryInformation;
    private String _referringUrl;
    private boolean _rememberUser;
    
    public AuthenticationService() {
    	_manager = new ConsumerManager();
    }

    // --- placing the authentication request ---
    public String authRequest(String userSuppliedString, String webAppBaseUrl) {
        try {
            // configure the return_to URL where your application will receive
            // the authentication responses from the OpenID provider
            //String returnToUrl = "http://example.com/openid";
            String returnToUrl = webAppBaseUrl + "/openIdLogin.do";
        	
            // --- Forward proxy setup (only if needed) ---
            // ProxyProperties proxyProps = new ProxyProperties();
            // proxyProps.setProxyName("proxy.example.com");
            // proxyProps.setProxyPort(8080);
            // HttpClientFactory.setProxyProperties(proxyProps);

            // perform discovery on the user-supplied identifier
            @SuppressWarnings("unchecked")
            List<DiscoveryInformation> discoveries = _manager.discover(userSuppliedString);

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            _discoveryInformation = _manager.associate(discoveries);

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = _manager.authenticate(_discoveryInformation, returnToUrl);

            // Attribute Exchange example: fetching the 'email' attribute
            FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute("first", "http://schema.openid.net/namePerson/first", true);
            fetch.addAttribute("last", "http://schema.openid.net/namePerson/last", true);
            fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
            
            // attach the extension to the authentication request
            authReq.addExtension(fetch);

            /*
            boolean isVersion2 = _discoveryInformation.isVersion2();
            
            // always use old v1.x method of url redirection
            if (true)
            {
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited ~2048 bytes
                httpResp.sendRedirect(authReq.getDestinationUrl(true));
                return null;
            }
            else
            {
                // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
                RequestDispatcher dispatcher =
                        getServletContext().getRequestDispatcher("formredirection.jsp");
                httpReq.setAttribute("parameterMap", authReq.getParameterMap());
                httpReq.setAttribute("destinationUrl", authReq.getDestinationUrl(false));
                dispatcher.forward(httpReq, httpResp);
            }
            */
            return authReq.getDestinationUrl(true);
        }
        catch (OpenIDException e) {
        	throw new RuntimeException("Unable to initialize authentication", e);
        }
    }

    // --- processing the authentication response ---
    public OpenIdUser verifyResponse(String requestUrl, String queryString, Map<String, String[]> params) {
        try {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response = new ParameterList(params);

            // extract the receiving URL from the HTTP request
            StringBuilder receivingURL = new StringBuilder(requestUrl);
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(queryString);

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = _manager.verify(
                    receivingURL.toString(),
                    response, _discoveryInformation);

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
                String openIdReceived = verified.getIdentifier();
                OpenIdUser user = new OpenIdUser(normalizeOpenId(openIdReceived));
                AuthSuccess authSuccess = (AuthSuccess)verification.getAuthResponse();

                try {
                  if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                    FetchResponse fetchResp = (FetchResponse) authSuccess
                        .getExtension(AxMessage.OPENID_NS_AX);

                    // TODO: fix these attributes. Believe we are using the wrong request URLs above
                    String first = verified.getIdentifier(); // (String)fetchResp.getAttributeValues("first").get(0);
                    String last = null; // = (String)fetchResp.getAttributeValues("last").get(0);
                    String email = (String)fetchResp.getAttributeValues("email").get(0);
                    
                    user.setName(first + (last != null ? (" " + last) : ""));
                    user.setEmail(email);
                  }
                }
                catch (Exception e) {
                  // do nothing here; would be nice to retrieve requested attributes from user
                  //   but do not throw error; we should already have their email, etc.
                }
                
                return user;
            }

            return null;
        }
        catch (OpenIDException e) {
        	throw new RuntimeException("Could not complete user authentication", e);
        }
    }

	public String getReferringUrl() {
		return _referringUrl;
	}

	public void setReferringUrl(String referringUrl) {
		_referringUrl = referringUrl;
	}

	public boolean rememberUser() {
		return _rememberUser;
	}

	public void setRememberUser(boolean rememberUser) {
		_rememberUser = rememberUser;
	}

  public static String normalizeOpenId(String openId) {
    int protocolIndex = openId.indexOf("://");
    if (protocolIndex != -1) {
      openId = openId.substring(protocolIndex + 3);
    }
    int slashIndex = openId.indexOf("/");
    if (slashIndex != -1) {
      openId = openId.substring(0, slashIndex);
    }
    return openId;
  }
}
