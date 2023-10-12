package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModel;

public class RegisteredUser extends User {

  public RegisteredUser(WdkModel wdkModel, long userId, String email, String signature, String stableId) {
    super(wdkModel, userId, email, signature, stableId);
  }

  @Override
  public boolean isGuest() {
    return false;
  }

  @Override
  public String getDisplayName() {
    return (
        formatNamePart(_properties.get("firstName")) +
        formatNamePart(_properties.get("middleName")) +
        formatNamePart(_properties.get("lastName"))).trim();
  }

  private static String formatNamePart(String namePart) {
    return (namePart == null || namePart.isEmpty() ? "" : " " + namePart.trim());
  }

}
