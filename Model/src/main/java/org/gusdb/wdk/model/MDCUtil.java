package org.gusdb.wdk.model;

import org.apache.log4j.MDC;

/**
 * Manages thread-based data using Log4j's MDC mechanism.
 * 
 * See {@code MDCServletFilter}
 * 
 * @author rdoherty
 */
public class MDCUtil {

  public static final String LOG4J_IP_ADDRESS_KEY = "ipAddress";
  public static final String LOG4J_REQUESTED_DOMAIN_KEY = "requestedDomain";
  public static final String LOG4J_SESSION_ID_KEY = "sessionId";

  public static void setIpAddress(String ipAddress) {
    if (ipAddress != null) {
      MDC.put(LOG4J_IP_ADDRESS_KEY, ipAddress);
    }
  }

  public static String getIpAddress() {
    return (String)MDC.get(LOG4J_IP_ADDRESS_KEY);
  }

  public static void setRequestedDomain(String domain) {
    if (domain != null) {
      MDC.put(LOG4J_REQUESTED_DOMAIN_KEY, domain);
    }
  }
  
  public static String getRequestedDomain() {
    return (String)MDC.get(LOG4J_REQUESTED_DOMAIN_KEY);
  }
  
  public static void setSessionId(String sessionId) {
    if (sessionId != null) {
      MDC.put(LOG4J_SESSION_ID_KEY, sessionId);
    }
  }

  public static void clearValues() {
    MDC.remove(LOG4J_IP_ADDRESS_KEY);
    MDC.remove(LOG4J_REQUESTED_DOMAIN_KEY);
    MDC.remove(LOG4J_SESSION_ID_KEY);
  }
}
