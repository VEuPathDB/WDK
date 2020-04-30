package org.gusdb.wdk.model.user;

import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.wdk.model.user.UserCreationScript.UserLine;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class UserCreationScriptTest {

  private static final String[] TEST_CASES = { "\ta\tb\tc\td",
    "email\ta\t\tc\td", "email\t\tb\tc\td", "email\ta\tb\t\t\t\t" };

  private static final UserPropertyName[] USER_PROPS = {
    new UserPropertyName("firstName", null, true),
    new UserPropertyName("middleName", null, false),
    new UserPropertyName("lastName", null, true),
    new UserPropertyName("organization", null, true) };

  @Test
  public void testParsing() {
    List<UserPropertyName> userProps = Arrays.asList(USER_PROPS);
    for (String testCase : TEST_CASES) {
      UserLine userLine = UserCreationScript.parseLine(testCase, userProps);
      System.out.println(userLine);
    }
  }
}
