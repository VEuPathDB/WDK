/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.JSONException;

/**
 * @author xingao
 *
 */
public class QueryMonitor extends WdkModelBase {

    private long slowQueryThreshold = 10;
    private long brokenQueryThreshold = 60;
    private List<WdkModelText> ignoreSlowQueryRegexList = new ArrayList<WdkModelText>();
    private List<WdkModelText> ignoreBrokenQueryRegexList = new ArrayList<WdkModelText>();
    
    private Set<Pattern> ignoreSlowQueryRegexSet = new LinkedHashSet<Pattern>();
    private Set<Pattern> ignoreBrokenQueryRegexSet = new LinkedHashSet<Pattern>();
    
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
    
    public void addIgnoreSlowQueryRegex(WdkModelText regex) {
        ignoreSlowQueryRegexList.add(regex);
    }
    
    public void addIgnoreBrokenQueryRegex(WdkModelText regex) {
        ignoreBrokenQueryRegexList.add(regex);
    }
    
    public boolean isIgnoredSlowQuery(String sql) {
        for (Pattern pattern : ignoreSlowQueryRegexSet) {
            if (pattern.matcher(sql).find()) return true;
        }
        return false;
    }
    
    public boolean isIgnoredBrokenQuery(String sql) {
        for(Pattern pattern : ignoreBrokenQueryRegexSet) {
            if (pattern.matcher(sql).find()) return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude slow regex
        Set<String> slowSet = new HashSet<String>();
        for (WdkModelText text : ignoreSlowQueryRegexList) {
            if (text.include(projectId)) {
                text.excludeResources(projectId);
                String regex = text.getText();
                if (!slowSet.contains(regex)) {
                    slowSet.add(regex);
                    ignoreSlowQueryRegexSet.add(Pattern.compile(regex));
                }
            }
        }
        ignoreSlowQueryRegexList = null;
        
        // exclude broken regex
        Set<String> brokenSet = new HashSet<String>();
        for (WdkModelText text : ignoreBrokenQueryRegexList) {
            if (text.include(projectId)) {
                text.excludeResources(projectId);
                String regex = text.getText();
                if (!brokenSet.contains(regex)) {
                    brokenSet.add(regex);
                    ignoreBrokenQueryRegexSet.add(Pattern.compile(regex));
                }
            }
        }
        ignoreBrokenQueryRegexList = null;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // nothing to resolve
    }

}
