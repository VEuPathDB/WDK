package org.gusdb.wdk.service;

import static org.gusdb.fgputil.FormatUtil.TAB;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.user.User;

public class PageViewLogger {

  private static final Logger LOG = Logger.getLogger(PageViewLogger.class);

  public static void logPageView(String projectId, User user, String pagePath) {
    LOG.info(
        TAB + user.getUserId() +
        TAB + (user.isGuest() ? "guest" : "registered") +
        TAB + projectId +
        TAB + pagePath);
  }
}
