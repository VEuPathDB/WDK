package org.gusdb.wdk.controller.action;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author dfalke
 * 
 * Sends a support email to the support email address configured in
 * model-config.xml (see org.gusdb.wdk.model.ModelConfig.getSupportEmail())
 *
 */
public class ContactUsAction extends Action {
  /**
   * the action for email
   */
  private static final String PARAM_ACTION = "action";
  /**
   * the reply-to address
   */
  private static final String PARAM_REPLY = "reply";
  /**
   * the email subject
   */
  private static final String PARAM_SUBJECT = "subject";
  /**
   * the email content
   */
  private static final String PARAM_CONTENT = "content";
  /**
   * CC addresses
   */
  private static final String PARAM_ADDCC = "addCc";

  private static final Logger logger = Logger.getLogger(ContactUsAction.class.getName());

  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    logger.debug("Entering ContactUs...");
    response.setContentType("application/json");

    UserBean user = ActionUtility.getUser(servlet, request);
    WdkModelBean wdkModelBean = ActionUtility.getWdkModel(servlet);
    WdkModel wdkModel = wdkModelBean.getModel();

    String reply = request.getParameter(PARAM_REPLY);
    String subject = request.getParameter(PARAM_SUBJECT);
    String content = request.getParameter(PARAM_CONTENT);
    String supportEmail = wdkModel.getModelConfig().getSupportEmail();
    String addCc = request.getParameter(PARAM_ADDCC);
    String uid = Integer.toString(user.getUserId());
    String version = wdkModel.getVersion();
    String browser = request.getHeader("User-Agent");
    String referer = request.getHeader("Referer");
    String website = wdkModel.getDisplayName();
    String ipAddress = request.getRemoteAddr();
    String reporterEmail = "websitesupportform@apidb.org";
    String redmineEmail = "redmine@apidb.org";

    if (reply == null || reply.isEmpty()) {
      reply = supportEmail;
    }

    if (subject == null) {
      subject = "";
    }

    if (content == null) {
      content = "";
    }

    if (addCc == null) {
      addCc = "";
    } else if (addCc.split(",\\s*(?=\\w)").length > 10) {
      // only 10 addresses allowed
      return doError("No more than 10 Cc addresses are allowed. " +
          "Please reduce your list to 10 email addresses.",
          response);
    }

    String metaInfo = "ReplyTo: " + reply + "\n"
        + "CC: " + addCc + "\n"
        + "Privacy preferences: " + "\n"
        + "Uid: " + uid + "\n"
        + "Browser information: " + browser + "\n"
        + "Referer page: " + referer;

    String autoContent = "****THIS IS NOT A REPLY**** \nThis is an automatic" +
        " response, that includes your message for your records, to let you" +
        " know that we have received your email and will get back to you as" +
        " soon as possible. Thanks so much for contacting us!\n\nThis was" +
        " your message:\n\n---------------------\n" + content +
        "\n---------------------";

    String redmineMetaInfo = "Project: usersupportrequests\n" +
        "Category: " + website + "\n" +
        "\n" + metaInfo + "\n" +
        "Client IP Address: " + ipAddress + "\n";

    try {
      // send auto-reply
      Utilities.sendEmail(wdkModel, reply, supportEmail, subject,
          escapeHtml(metaInfo + "\n\n" + autoContent), addCc);

      // send support email
      Utilities.sendEmail(wdkModel, supportEmail, reply, subject,
          escapeHtml(metaInfo + "\n\n" + content));

      // send redmine email
      Utilities.sendEmail(wdkModel, redmineEmail, reporterEmail, subject,
          escapeHtml(redmineMetaInfo + "\n\n" + content));

      JSONObject jsMessage = new JSONObject();
      jsMessage.put("status", "success");
      jsMessage.put("message", "We appreciate your feedback. Your email was sent successfully.");
      PrintWriter writer = response.getWriter();
      writer.print(jsMessage.toString());
      return null;
    } catch (Exception ex) {
      //response.setStatus(500);
      logger.error(ex);
      //ex.printStackTrace();
      doError(ex.getMessage(), response);
      return null;
      //throw ex;
    }
  }

  private String escapeHtml(String str) {
    return str.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
        .replaceAll("&", "&amp;").replaceAll("\n", "<br>");
  }

  private ActionForward doError(String message, HttpServletResponse response)
    throws Exception {
      JSONObject jsMessage = new JSONObject();
      jsMessage.put("status", "error");
      jsMessage.put("message", message);
      PrintWriter writer = response.getWriter();
      writer.print(jsMessage.toString());
      return null;
  }
}
