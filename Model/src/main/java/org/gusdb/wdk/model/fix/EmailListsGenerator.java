package org.gusdb.wdk.model.fix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;


/**
 * @author xingao
 * 
 *        generate email lists to notify releases
 */
public class EmailListsGenerator extends BaseCLI {

  private static final Logger logger = Logger.getLogger(EmailListsGenerator.class);

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    EmailListsGenerator generator = new EmailListsGenerator(cmdName);
    try {
      generator.invoke(args);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
      logger.info("email lists generator done.");
      System.exit(0);
    }
  }

  /**
   * @param command
   * @param description
   */
  public EmailListsGenerator(String command) {
    super((command != null) ? command : "wdkGenerateEmailLists", "generate text files with emails lists for each project");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#declareOptions()
   */
  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "A comma-separated"
        + " list of ProjectIds, which should match the directory name"
        + " under $GUS_HOME, where model-config.xml is stored.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#execute()
   */
  @Override
  protected void execute() throws Exception {
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

    String strProject = (String) getOptionValue(ARG_PROJECT_ID);
    String[] projects = strProject.split(",");

      for (String projectId : projects) {
        logger.info("Generating list for project " + projectId);
        WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
        reportEmails(wdkModel);
        wdkModel.releaseResources();
        logger.info("=========================== done ============================");
      }

  }

 private void reportEmails(WdkModel wdkModel) throws SQLException,
      IOException {
    // determine the file name
    Calendar now = Calendar.getInstance();
    String name = "emails_" + wdkModel.getProjectId() + "_" + now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" +
        now.get(Calendar.DAY_OF_MONTH);
    File file = new File(name + ".log");
    int count = 0;
    while (file.exists()) {
      count++;
      file = new File(name + "_" + count + ".log");
    }
    PrintWriter writer = new PrintWriter(new FileWriter(file, true));

    String project_id = wdkModel.getProjectId();
    String preference = "preference_global_email_" + project_id.toLowerCase();
    if (project_id.equals("EuPathDB")) {
      preference = "preference_global_email_apidb";
    }

    String sql = "SELECT  email FROM userlogins5.users u,userlogins5.preferences p" + 
      " where p.user_id = u.user_id" + 
      " and p.user_id not in (" +  // removing some spam, about 15K users
      " select user_id from userlogins5.users where last_active is null and (" +
      " email like '%uukx.info' OR " +
      " email like '%sina.com' OR " +
      " email like '%mail.ru'  OR " +
      " email like '%qq.com' OR " +
      " email like '%badnewsol.com' OR " +
      " email like '%21cn.com' OR " +
      " email like '%sogou.com' OR " +
      " email like 'kokojunfetree%uvvc.info' OR " +
      " email like '%blackrayban%uvvc.info' OR " +
      " email like 'blackrefsagse%uvvc.info' OR " +
      " email like '%free%uvvc.info' OR " +
      " email like '@gmail.com' OR " +
      " email like '%.%.%.%.%@%' OR " +
      " email not like '%@%' " +
      " ))" +
      " and preference_name = '" + preference + "'" +
      " and preference_value = 'on'" +
      " order by email";   

    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql, "wdk-get-emails", 100);
      while (resultSet.next()) {
        writer.print(resultSet.getString("email") + ",\n");
      }
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
      writer.flush();
      writer.close();
      System.out.println("Emails saved at: " + file.getAbsolutePath());
    }
  }

}
