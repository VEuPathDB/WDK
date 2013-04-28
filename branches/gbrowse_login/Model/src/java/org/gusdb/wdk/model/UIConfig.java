package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

public class UIConfig extends WdkModelBase {

  private static final String NL = System.getProperty("line.separator");
  
  public static class ExtraLogoutCookies extends ArrayList<WdkCookie> {
    private static final long serialVersionUID = 1L;
    @Override public String toString() {
      StringBuilder sb = new StringBuilder("  ExtraLogoutCookies {").append(NL);
      for (WdkCookie cookie : this) {
        sb.append("    Cookie { name='").append(cookie.getName())
          .append("' path='").append(cookie.getPath()).append("' }").append(NL);
      }
      return sb.append("  }").append(NL).toString();
    }
  }
  
  private ExtraLogoutCookies extraLogoutCookies = new ExtraLogoutCookies();
  
  public void setExtraLogoutCookies(ExtraLogoutCookies extraLogoutCookies) {
    this.extraLogoutCookies = extraLogoutCookies;
  }

  public List<WdkCookie> getExtraLogoutCookies() {
    return extraLogoutCookies;
  }
  
  public String toString() {
    return new StringBuilder()
      .append("UIConfig {").append(NL)
      .append(extraLogoutCookies.toString())
      .append("}").append(NL).toString();
  }
  
}
