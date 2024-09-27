package org.gusdb.wdk.service;

import static org.gusdb.fgputil.FormatUtil.TAB;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.user.User;

public class PageViewLogger {

  private static final Logger LOG = Logger.getLogger(PageViewLogger.class);

  private static final String CLIENT_PREFIX = "/web-client";

  public static void logPageView(String projectId, User user, String submittedPath) {
    LOG.info(
        TAB + user.getUserId() +
        TAB + (user.isGuest() ? "guest" : "registered") +
        TAB + projectId +
        TAB + trimWebappAndClientApp(submittedPath));
  }

  // a -> a
  // a/ -> a
  // webapp/app -> /web-client
  // webapp/app/ -> /web-client
  // webapp/app/blah -> /web-client/blah
  private static String trimWebappAndClientApp(String submittedPath) {
    String[] segments = submittedPath.split("/");
    switch (segments.length) {
      case 0:
        // unrecognized path; don't legitimize with client prefix
        return "/";
      case 1:
        // unrecognized path; don't legitimize with client prefix
        return "/" + segments[0];
      default:
        // start with base URL of the webapp, then add remaining segments
        String newPath = CLIENT_PREFIX;
        for (int i = 2; i < segments.length; i++) {
          newPath += "/" + segments[i];
        }
        return newPath;
    }
  }

  public static void main(String[] args) {
    String[] tests = new String[] { "a", "a/", "webapp/app", "webapp/app/", "webapp/app/blah" };
    for (String test : tests) {
      System.out.println(test + " = " + trimWebappAndClientApp(test));
    }
  }
}
