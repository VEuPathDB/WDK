/*
 * Created on Jun 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.gusdb.gus.wdk.controller;

import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.Reference;
import org.gusdb.gus.wdk.model.Summary;
import org.gusdb.gus.wdk.model.SummarySet;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.WdkModelException;
import org.gusdb.gus.wdk.model.WdkUserException;


/**
 * @author art
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WdkModelExtra {

    /**
     * @param initRecordList
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public static Summary getSummary(WdkModel wm, String initRecordList) {
        try {
            Reference r = new Reference(initRecordList);
            SummarySet ss = wm.getSummarySet(r.getSetName());
            return ss.getSummary(r.getElementName());
        }
        catch (WdkModelException exp) {
            throw new RuntimeException(exp);
        }
        catch (WdkUserException exp) {
            throw new RuntimeException(exp);
        }
    }

    
    public static Record getRecord(WdkModel wm, String recordReference) {
        try {
            Reference r = new Reference(recordReference);
            RecordSet rs = wm.getRecordSet(r.getSetName());
            return rs.getRecord(r.getElementName());
        }
        catch (WdkModelException exp) {
            throw new RuntimeException(exp);
        }
        catch (WdkUserException exp) {
            throw new RuntimeException(exp);
        }
    }
    
}
