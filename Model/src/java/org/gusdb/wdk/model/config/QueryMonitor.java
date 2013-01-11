/**
 * 
 */
package org.gusdb.wdk.model.config;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * An object representation of the <queryMonitor> tag in the model-config.xml.
 * it controls the logging of slow queries & broken queries.
 * 
 * When a SQL runs longer than broken threshold, it doesn't really mean the SQL
 * is failed, it's just extremely slow, and need to be fixed. If such a super
 * slow SQL occurs, an email with the SQL will be sent to the admin email.
 * 
 * @author xingao
 * 
 */
public class QueryMonitor {

  private long slowQueryThreshold = 10;
  private long brokenQueryThreshold = 60;

  private Set<Pattern> ignoreSlowQueryRegexes = new LinkedHashSet<Pattern>();
  private Set<Pattern> ignoreBrokenQueryRegexes = new LinkedHashSet<Pattern>();

  /**
   * @return the slowQueryThreshold
   */
  public long getSlowQueryThreshold() {
    return slowQueryThreshold;
  }

  /**
   * @param slowQueryThreshold
   *          the slowQueryThreshold to set
   */
  public void setSlowQueryThreshold(long slowQueryThreshold) {
    this.slowQueryThreshold = slowQueryThreshold;
  }

  /**
   * @return the brokenQueryThreshold
   */
  public long getBrokenQueryThreshold() {
    return brokenQueryThreshold;
  }

  /**
   * @param brokenQueryThreshold
   *          the brokenQueryThreshold to set
   */
  public void setBrokenQueryThreshold(long brokenQueryThreshold) {
    this.brokenQueryThreshold = brokenQueryThreshold;
  }

  public void addIgnoreSlowQueryRegex(String regex) {
    ignoreSlowQueryRegexes.add(Pattern.compile(regex));
  }

  public void addIgnoreBrokenQueryRegex(String regex) {
    ignoreBrokenQueryRegexes.add(Pattern.compile(regex));
  }

  public boolean isIgnoredSlowQuery(String sql) {
    for (Pattern pattern : ignoreSlowQueryRegexes) {
      if (pattern.matcher(sql).find())
        return true;
    }
    return false;
  }

  public boolean isIgnoredBrokenQuery(String sql) {
    for (Pattern pattern : ignoreBrokenQueryRegexes) {
      if (pattern.matcher(sql).find())
        return true;
    }
    return false;
  }
}
