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

  public static final String LOG4J_REQUEST_TIMER = "requestTimer";
  public static final String LOG4J_IP_ADDRESS_KEY = "ipAddress";
  public static final String LOG4J_REQUESTED_DOMAIN_KEY = "requestedDomain";
  public static final String LOG4J_SESSION_ID_KEY = "sessionId";
  public static final String LOG4J_SHORT_SESSION_ID_KEY = "shortSessionId";
  public static final String LOG4J_REQUEST_ID_KEY = "requestId";

  public static void setRequestStartTime(final long startTime) {
    MDC.put(LOG4J_REQUEST_TIMER, new Object(){
      @Override public String toString() {
        return (System.currentTimeMillis() - startTime) + "ms";
      }
    });
  }

  public static String getRequestDuration() {
    Object obj = MDC.get(LOG4J_REQUEST_TIMER);
    return (obj == null ? null : obj.toString());
  }

  public static void setIpAddress(String ipAddress) {
    if (ipAddress != null) {
      MDC.put(LOG4J_IP_ADDRESS_KEY, ipAddress);
    }
  }

  public static String getIpAddress() {
    return (String)MDC.get(LOG4J_IP_ADDRESS_KEY);
  }

  public static void setRequestId(String requestId) {
    if (requestId != null) {
      MDC.put(LOG4J_REQUEST_ID_KEY, requestId);
    }
  }

  public static String getRequestId() {
    return (String)MDC.get(LOG4J_REQUEST_ID_KEY);
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
      MDC.put(LOG4J_SHORT_SESSION_ID_KEY,
          sessionId.substring(0, Math.min(5, sessionId.length())));
    }
  }

  public static void clearValues() {
    MDC.remove(LOG4J_REQUEST_TIMER);
    MDC.remove(LOG4J_IP_ADDRESS_KEY);
    MDC.remove(LOG4J_REQUESTED_DOMAIN_KEY);
    MDC.remove(LOG4J_SESSION_ID_KEY);
    MDC.remove(LOG4J_SHORT_SESSION_ID_KEY);
    MDC.remove(LOG4J_REQUEST_ID_KEY);
  }

  public static void setNonRequestThreadVars(String threadId) {
    MDCUtil.setRequestId(threadId);
    MDCUtil.setSessionId(threadId);
    MDCUtil.setIpAddress("<no_ip_address>");
    MDCUtil.setRequestStartTime(System.currentTimeMillis());
  }
}
