/*
 * Created on Jun 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.gusdb.gus.wdk.view;

import org.gusdb.gus.wdk.model.RecordList;
import org.gusdb.gus.wdk.model.RecordListSet;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.WdkModelException;


/**
 * @author art
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class QueryRecordGroupMgr {

    /**
     * @param initRecordList
     * @return
     */
    public static RecordList getRecordList(WdkModel model, String initRecordList) {
        // TODO ErrorHandling
        int dotIndex = initRecordList.indexOf(".");
        String groupName = initRecordList.substring(0, dotIndex);
        String pairName = initRecordList.substring(dotIndex+1);
	try {
	    RecordListSet rls = model.getRecordListSet(groupName);
	    RecordList ret = rls.getRecordList(pairName);
	    return ret;
	} catch (WdkModelException e) {}
	return null;  // to get it to compile
    }

}
