package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.TAB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class UserCreationScript {

  public static class UserLine extends ThreeTuple<Boolean, String, Map<String,String>> {

    public UserLine(Boolean shouldWriteUser, String email, Map<String, String> otherProps) {
      super(shouldWriteUser, email, otherProps);
    }

    public boolean shouldWriteUser() { return getFirst(); }
    public String getEmail() { return getSecond(); }
    public Map<String,String> getOtherProps() { return getThird(); }

    @Override
    public String toString() {
      return shouldWriteUser() + ", " + getEmail() + ", " + FormatUtil.prettyPrint(getOtherProps());
    }
  }

  public static void main(String[] args) throws WdkModelException, IOException {
    if (args.length != 1) {
      System.err.println(NL + 
          "USAGE: fgpJava " + UserCreationScript.class.getName() + " <project_id>" + NL + NL +
          "This script will read tab-delimited user properties from stdin" + NL);
    }
    try (WdkModel model = WdkModel.construct(args[0], GusHome.getGusHome());
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
      List<UserPropertyName> userProps = model.getModelConfig().getAccountDB().getUserPropertyNames();
      while (in.ready()) {
        UserLine parsedLine = parseLine(in.readLine(), userProps);
        if (parsedLine.shouldWriteUser()) {
          try {
            User user = model.getUserFactory().createUser(
                parsedLine.getEmail(), parsedLine.getOtherProps(),
                Collections.emptyMap(), Collections.emptyMap(), false);
            System.out.println(user.getUserId() + TAB + user.getEmail());
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
      return new UserLine(false, null, null);
    }
    String email = tokens[0];
    Map<String,String> propertyMap = new LinkedHashMap<>();
    boolean valid = true;
    for (int i = 0; i < userProps.size(); i++) {
      UserPropertyName propName = userProps.get(i);
      // split will trim off trailing empty tokens, so backfill
      String nextValue = tokens.length > i + 1 ? tokens[i + 1].trim() : "";
      if (propName.isRequired() && nextValue.isEmpty()) {
        System.err.println("Required value [" + propName.getName() + "] missing on line: " + line);
        valid = false;
      }
      propertyMap.put(userProps.get(i).getName(), nextValue);
    }
    return new UserLine(valid, email, propertyMap);
  }
}
