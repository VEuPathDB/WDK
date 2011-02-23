/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
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
     * @param slowQueryThreshold the slowQueryThreshold to set
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
     * @param brokenQueryThreshold the brokenQueryThreshold to set
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
            if (pattern.matcher(sql).find()) return true;
        }
        return false;
    }
    
    public boolean isIgnoredBrokenQuery(String sql) {
        for(Pattern pattern : ignoreBrokenQueryRegexes) {
            if (pattern.matcher(sql).find()) return true;
        }
        return false;
    }
}
