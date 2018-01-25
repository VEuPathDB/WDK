package org.gusdb.wdk.model;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.List;

public class UIConfig extends WdkModelBase {

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

  private boolean _showStratPanelVisibilityControls = false;
  private boolean _showStratPanelByDefault = true;
  private ExtraLogoutCookies _extraLogoutCookies = new ExtraLogoutCookies();

  public void setShowStratPanelVisibilityControls(boolean showStratPanelVisibilityControls) {
    _showStratPanelVisibilityControls = showStratPanelVisibilityControls;
  }

  public boolean getShowStratPanelVisibilityControls() {
    return _showStratPanelVisibilityControls;
  }

  public void setShowStratPanelByDefault(boolean showStratPanelByDefault) {
    _showStratPanelByDefault = showStratPanelByDefault;
  }

  public boolean getShowStratPanelByDefault() {
    return _showStratPanelByDefault;
  }

  public void setExtraLogoutCookies(ExtraLogoutCookies extraLogoutCookies) {
    _extraLogoutCookies = extraLogoutCookies;
  }

  public List<WdkCookie> getExtraLogoutCookies() {
    return _extraLogoutCookies;
  }

  @Override
  public String toString() {
    return new StringBuilder()
      .append("UIConfig {").append(NL)
      .append(_extraLogoutCookies.toString())
      .append("}").append(NL).toString();
  }
  
}
