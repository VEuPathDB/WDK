/**
 * 
 */
package org.gusdb.wdk.model.test;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Jerric
 * @created Sep 22, 2005
 */
public class CommandHelper {

    public static CommandLine parseOptions(String cmdName, Options options,
            String[] args) {

        CommandLineParser parser = new BasicParser();
        CommandLine cmdLine = null;
        try {
            // parse the command line arguments
            cmdLine = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("");
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.err.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }

    public static Options declareOptions(String[] names, String[] descs,
            boolean[] required, int[] args) {
        Options options = new Options();

        // get the minimal size
        int min = (names.length < descs.length) ? names.length : descs.length;
        if (min > required.length) min = required.length;
        if (min > args.length) min = args.length;

        // model name
        for (int i = 0; i < min; i++) {
            addOption(options, required[i], names[i], descs[i], args[i]);
        }

        // verbose
        Option verbose = new Option("verbose",
                "Print out more information while running test.");
        options.addOption(verbose);

        return options;
    }

    public static void addOption(Options options, boolean required,
            String argName, String desc, int arg) {

        Option option = new Option(argName, true, desc);
        option.setRequired(required);
        option.setArgName(argName);
        if (arg != 0) option.setArgs(arg);
        options.addOption(option);
    }

    public static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");

        StringBuffer sb = new StringBuffer();

        // add command name
        sb.append(cmdName);

        // add command syntax
        for (Object objOption : options.getOptions()) {
            Option option = (Option) objOption;
            if (option.isRequired()) sb.append(" -");
            else sb.append(" [-");
            sb.append(option.getOpt());
            sb.append(' ');
            sb.append(option.getOpt());
            if (!option.isRequired()) sb.append(']');
        }
        sb.append(newline);

        String cmdlineSyntax = sb.toString();

        String header = "Run Unit test cases. Options:";

        String footer = " ";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}