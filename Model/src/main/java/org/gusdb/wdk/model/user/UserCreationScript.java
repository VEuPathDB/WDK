package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.TAB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class UserCreationScript {

  public static class UserLine {

    private final boolean _shouldWriteUser;
    private final String _email;
    private final Map<String,String> _globalUserPrefs;
    private final Map<String,String> _userProperties;
    
    public UserLine(boolean shouldWriteUser, String email,
        Map<String,String> globalUserPrefs, Map<String, String> userProperties) {
      _shouldWriteUser = shouldWriteUser;
      _email = email;
      _globalUserPrefs = globalUserPrefs;
      _userProperties = userProperties;
    }

    public boolean shouldWriteUser() { return _shouldWriteUser; }
    public String getEmail() { return _email; }
    public Map<String,String> getGlobalUserPrefs() { return _globalUserPrefs; }
    public Map<String,String> getUserProperties() { return _userProperties; }

    public String getAttributesString() { return getEmail() + ", " +
        FormatUtil.prettyPrint(_userProperties) + ", " +
        FormatUtil.prettyPrint(_globalUserPrefs); }

    @Override
    public String toString() {
      return shouldWriteUser() + ", " + getAttributesString();
    }
  }

  public static void main(String[] args) throws WdkModelException, IOException {
    if (!(args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("test")))) {
      System.err.println(NL + 
          "USAGE: fgpJava " + UserCreationScript.class.getName() + " <project_id> [test]" + NL + NL +
          "This script will read tab-delimited user properties from stdin" + NL +
          "Passed project_id value is used only to look up account-db access information in gus_home" + NL +
          "If 'test' is specified as a second argument, no records will be written to the DB; " +
          "instead diagnostics will be printed to stdout" + NL);
      System.exit(1);
    }
    boolean testOnly = args.length == 2;
    try (WdkModel model = WdkModel.construct(args[0], GusHome.getGusHome());
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
      List<UserPropertyName> userProps = model.getModelConfig().getAccountDB().getUserPropertyNames();
      int i=0;
      int j=0;
      while (in.ready()) {
        UserLine parsedLine = parseLine(in.readLine(), userProps);
        if (parsedLine.shouldWriteUser()) {
          try {
            // create or edit user
            User user = model.getUserFactory().getUserByEmail(parsedLine.getEmail());
            if (user == null) {
              i++;
              if (testOnly) {
                System.out.println("Would create user: " + parsedLine.getAttributesString());
              }
              else {
                // create new user and assign preferences
                user = model.getUserFactory().createUser(
                    parsedLine.getEmail(), parsedLine.getUserProperties(),
                    parsedLine.getGlobalUserPrefs(), Collections.emptyMap(), false, false);
                System.out.println("Created user with ID " + user.getUserId() + " and email " + user.getEmail());
              }
            }
            else {
              j++;
              String message = "User with email " + user.getEmail() +
                  " exists; %s preferences " + FormatUtil.prettyPrint(parsedLine.getGlobalUserPrefs());
              if (testOnly) {
                System.out.println(String.format(message, "would add"));
              }
              else {
                // user exists already; simply add preferences
                UserPreferences userPrefs = user.getPreferences();
                for (Entry<String,String> newPref : parsedLine.getGlobalUserPrefs().entrySet()) {
                  userPrefs.setGlobalPreference(newPref.getKey(), newPref.getValue());
                }
                user.setPreferences(userPrefs);
                model.getUserFactory().savePreferences(user);
                System.out.println(String.format(message, "adding"));
              }
					  }
            System.out.println("Number of new users: " + i);
            System.out.println("Number of existing users we added preferences to (could be more than one project): " + j);
          }
          catch (InvalidEmailException e) {
            System.err.println("Invalid email '" + parsedLine.getEmail() + "': " + e.getMessage());
          }
        }
      }
    }
  }

  static UserLine parseLine(
      String line, List<UserPropertyName> userProps) {
    String[] tokens = line.split(TAB);
    if (tokens.length == 0 || tokens[0].trim().isEmpty()) {
      System.err.println("Required value [email] missing on line: " + line);
      return new UserLine(false, null, null, null);
    }
    String email = tokens[0];

    // next token is project_ids user wants emails from
    Map<String,String> globalUserPrefs = (tokens.length > 1 ?
        getEmailPrefsFromProjectIds(tokens[1]) : Collections.emptyMap());

    boolean valid = true;
    Map<String,String> propertyMap = new LinkedHashMap<>();
    for (int i = 0; i < userProps.size(); i++) {
      UserPropertyName propName = userProps.get(i);
      // split will trim off trailing empty tokens, so backfill
      String nextValue = tokens.length > i + 2 ? tokens[i + 2].trim() : "";
      if (propName.isRequired() && nextValue.isEmpty()) {
        System.err.println("Required value [" + propName.getName() + "] missing on line: " + line);
        valid = false;
      }
      propertyMap.put(userProps.get(i).getName(), nextValue);
    }
    return new UserLine(valid, email, globalUserPrefs, propertyMap);
  }

  private static Map<String, String> getEmailPrefsFromProjectIds(String commaDelimitedListOfProjectIds) {
    String[] projectIds = commaDelimitedListOfProjectIds.trim().isEmpty() ?
        new String[0] : commaDelimitedListOfProjectIds.split(",");
    return Arrays.stream(projectIds)
        .filter(projectId -> !projectId.trim().isEmpty())
        .map(projectId -> "preference_global_email_" + projectId.trim().toLowerCase())
        .collect(Collectors.toMap(Function.identity(), val -> "on"));
    
  }
}
