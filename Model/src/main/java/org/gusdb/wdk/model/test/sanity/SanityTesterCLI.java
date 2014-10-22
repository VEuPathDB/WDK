package org.gusdb.wdk.model.test.sanity;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;

public class SanityTesterCLI {

  private static final String BEGIN_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

  public static void main(String[] args) {
    WdkModel wdkModel = null;
    long startTime = System.currentTimeMillis();
    int exitCode = 0;
    try {
      String cmdName = System.getProperty("cmdName");
      Options options = declareOptions();
      CommandLine cmdLine = parseOptions(cmdName, options, args);

      String modelName = cmdLine.getOptionValue("model");
      TestFilter testFilter = new TestFilter(cmdLine.getOptionValue("t"));
      boolean failuresOnly = cmdLine.hasOption("failuresOnly");
      boolean indexOnly = cmdLine.hasOption("indexOnly");
      boolean skipWebSvcQueries = cmdLine.hasOption("skipWebSvcQueries");

      wdkModel = WdkModel.construct(modelName, GusHome.getGusHome());

      SanityTester sanityTester = new SanityTester(wdkModel, testFilter,
          failuresOnly, indexOnly, skipWebSvcQueries);

      System.out.println(new StringBuilder()
        .append(NL)
        .append("Sanity Test: ").append(NL)
        .append(" [Model] ").append(modelName).append(NL)
        .append(" [Database] ").append(wdkModel.getAppDb().getConfig().getConnectionUrl()).append(NL)
        .append(" [Time] ").append(new SimpleDateFormat(BEGIN_DATE_FORMAT).format(new Date())).append(NL)
        .toString());

      List<TestResult> results = sanityTester.runTests();
      
      if (!indexOnly) {
        System.out.println(new StringBuilder().append(NL)
            .append("Sanity Test Complete.  Results Overview:").append(NL).toString());
        for (TestResult result : results) {
          if (!result.isPassed() || !failuresOnly) {
            System.out.println(result.getShortResultString());
          }
        }
        System.out.println(new StringBuilder().append(NL)
            .append("Results Summary:").append(NL).toString());
        System.out.print(sanityTester.getSummaryLine());
        if (sanityTester.isFailedOverall()) {
          System.out.println(SanityTester.getRerunLine(results));
          exitCode = 1;
        }
        System.out.println();
      }
    }
    catch (Exception e) {
      System.err.println(FormatUtil.getStackTrace(e));
      exitCode = 1;
    }
    finally {
      wdkModel.releaseResources();
    }

    System.out.println(new StringBuilder(NL)
      .append("Sanity Test run completed in ")
      .append((System.currentTimeMillis() - startTime) / 1000.0F)
      .append(" seconds (exit code ").append(exitCode).append(").")
      .append(NL).toString());
    
    System.exit(exitCode);
  }

  private static Options declareOptions() {
    Options options = new Options();

    // model name
    addOption(options, "model",
        "the name of the model.  This is used to find the Model XML file " +
        "($GUS_HOME/config/model_name.xml), the Model property file " +
        "($GUS_HOME/config/model_name.prop) and the Model config file " +
        "($GUS_HOME/config/model_name-config.xml)");

    // verbose
    Option verbose = new Option("verbose",
        "Print out more information while running test.");
    options.addOption(verbose);

    // verbose
    Option filter = new Option("t", true,
        "Optional list of tests to run (default=all)  e.g. '1,4-17,62'");
    options.addOption(filter);

    Option failuresOnly = new Option("failuresOnly",
        "Only print failures only.");
    options.addOption(failuresOnly);

    Option indexOnly = new Option("indexOnly",
        "Only print an index of the tests.");
    options.addOption(indexOnly);

    Option skipWebSvcQueries = new Option("skipWebSvcQueries",
        "Skip all questions and queries that use web service queries.");
    options.addOption(skipWebSvcQueries);

    return options;
  }

  private static void addOption(Options options, String argName, String desc) {
    Option option = new Option(argName, true, desc);
    option.setRequired(true);
    option.setArgName(argName);
    options.addOption(option);
  }
    
  private static CommandLine parseOptions(String cmdName, Options options, String[] args) {
    CommandLineParser parser = new BasicParser();
    CommandLine cmdLine = null;
    try {
      // parse the command line arguments
      cmdLine = parser.parse(options, args);
    }
    catch (ParseException exp) {
      // oops, something went wrong
      System.err.println();
      System.err.println("Parsing failed.  Reason: " + exp.getMessage());
      System.err.println();
      usage(cmdName, options);
    }

    return cmdLine;
  }

  private static void usage(String cmdName, Options options) {
    String newline = System.getProperty("line.separator");
    String cmdlineSyntax = cmdName
        + " -model model_name"
        + " [-t testIdList] [-failuresOnly | -indexOnly] [-skipWebSvcQueries]";
    String header = newline
        + "Run a test on all queries and records in a wdk model." + newline
        + newline + "Options:";
    String footer = "";
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(75, cmdlineSyntax, header, options, footer);
    System.exit(1);
  }
}
