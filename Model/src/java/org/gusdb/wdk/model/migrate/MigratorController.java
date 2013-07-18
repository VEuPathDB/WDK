/**
 * @description
 */
package org.gusdb.wdk.model.migrate;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

/**
 * @author Jerric
 * @created May 22, 2007
 * @modified May 22, 2007
 */
public class MigratorController {

  public static final String ARG_MODEL = "model";

  private static final Logger logger = Logger.getLogger(MigratorController.class);

  /**
   * Before running this command, the preparation steps are expected to be
   * performed, such as setting up the new table schema, running the SQL
   * migration script.
   * 
   * The arguments for the command are:
   * 
   * -model <model_name>: the name of the model to be used
   * 
   * -version <old_ver>: the version of the model to be migrated from; the
   * version is used to determine which migration code to be executed;
   * 
   * -schema <old_user_schema>: the old user login schema, where the user data
   * is migrated from
   * 
   * @param args
   * @throws WdkModelException
   * @throws WdkUserException
   * @throws JSONException
   * @throws SQLException
   * @throws NoSuchAlgorithmException
   */
  public static void main(String[] args) throws WdkModelException,
      WdkUserException, NoSuchAlgorithmException, SQLException, JSONException {
    MigratorController controller = new MigratorController();

    if (args.length < 2) {
      controller.printUsage();
      System.exit(-1);
    }

    try {
      Migrator migrator = controller.getMigrator(args);

      // prepare and parse command line arguments
      CommandLine commandLine = controller.parseOptions(args);

      // get model;
      String modelName = commandLine.getOptionValue(ARG_MODEL);
      String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
      WdkModel wdkModel = WdkModel.construct(modelName, gusHome);

      // invoke the migrator;
      logger.debug("Invoking migrator...");
      migrator.migrate(wdkModel, commandLine);
      logger.debug("Migration done.");
      System.exit(0);
    } catch (Exception ex) {
      ex.printStackTrace();
      controller.printUsage();
      System.exit(-1);
    }
  }

  private Options options;
  private String helpHeader;
  private String helpFooter;
  private String commandName;

  public MigratorController() {
    commandName = System.getProperty("cmdName", "migrate");
    helpHeader = "Migration script fro WDK releases. The first argument must "
        + "be the class name of the actual migrator";
    helpFooter = "";
  }

  public Migrator getMigrator(String[] args) throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    // determine which migrator to be used
    String className = args[0];
    logger.debug("Initializing migrator: " + className);

    Class<? extends Migrator> migratorClass = Class.forName(className).asSubclass(
        Migrator.class);
    Migrator migrator = migratorClass.newInstance();

    // declare options
    options = declareOptions(migrator);

    return migrator;
  }

  public CommandLine parseOptions(String[] args) throws ParseException {
    // strip away the first argument, which is the migrator class.
    String[] newArgs = new String[args.length - 1];
    if (newArgs.length > 0)
      System.arraycopy(args, 1, newArgs, 0, newArgs.length);

    CommandLineParser parser = new BasicParser();
    // parse the command line arguments
    CommandLine commandLine = parser.parse(options, newArgs);
    return commandLine;
  }

  private Options declareOptions(Migrator migrator) {
    Options options = new Options();
    Option option = new Option("model", true,
        "the name of the model.  This is used to find the Model XML "
            + "file ($GUS_HOME/config/model_name.xml) the Model "
            + "property file ($GUS_HOME/config/model_name.prop) "
            + "and the Model config file "
            + "($GUS_HOME/config/model_name-config.xml)");
    option.setRequired(true);
    option.setArgName("model");
    options.addOption(option);

    // add additional options from migrator
    migrator.declareOptions(options);

    return options;
  }

  protected void printUsage() {
    String newline = System.getProperty("line.separator");

    // print command syntax
    StringBuffer syntax = new StringBuffer(commandName);
    for (Object object : options.getOptions()) {
      Option option = (Option) object;
      syntax.append(option.isRequired() ? " -" : " [-");
      syntax.append(option.getArgName());
      if (option.hasArg()) {
        syntax.append(" <" + option.getArgName());
        syntax.append(option.hasArgs() ? " ...>" : ">");
      }
      if (!option.isRequired())
        syntax.append(']');
    }
    syntax.append(newline);

    // PrintWriter stderr = new PrintWriter(System.err);
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(75, syntax.toString(), helpHeader, options, helpFooter);
  }
}
