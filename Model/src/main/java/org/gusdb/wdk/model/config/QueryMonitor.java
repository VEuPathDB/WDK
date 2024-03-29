package org.gusdb.wdk.model.config;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.gusdb.fgputil.db.slowquery.QueryLogConfig;

/**
 * An object representation of the &lt;queryMonitor> tag in the model-config.xml.
 * it controls the logging of slow queries and broken queries.
 * 
 * When an SQL statement runs longer than broken threshold, it doesn't really mean the SQL
 * is failed, it's just extremely slow, and need to be fixed. If such a super
 * slow SQL occurs, an email with the SQL will be sent to the admin email.
 * 
 * @author xingao
 * 
 */
public class QueryMonitor implements QueryLogConfig {

  private double baseline = DEFAULT_BASELINE;
  private double slow = DEFAULT_SLOW;

  private Set<Pattern> ignoreSlowRegexes = new LinkedHashSet<Pattern>();
  private Set<Pattern> ignoreBaselineRegexes = new LinkedHashSet<Pattern>();

  @Override
  public double getBaseline() {
    return baseline;
  }

  public void setBaseline(double baseline) {
    this.baseline = baseline;
  }

  @Override
  public double getSlow() {
    return slow;
  }

  public void setSlow(double slow) {
    this.slow = slow;
  }

  public void addIgnoreSlowRegex(String regex) {
    ignoreSlowRegexes.add(Pattern.compile(regex));
  }

  public void addIgnoreBaselineRegex(String regex) {
    ignoreBaselineRegexes.add(Pattern.compile(regex));
  }

  @Override
  public boolean isIgnoredSlow(String sql) {
    for (Pattern pattern : ignoreSlowRegexes) {
      if (pattern.matcher(sql).find())
        return true;
    }
    return false;
  }

  @Override
  public boolean isIgnoredBaseline(String sql) {
    for (Pattern pattern : ignoreBaselineRegexes) {
      if (pattern.matcher(sql).find())
        return true;
    }
    return false;
  }
}
