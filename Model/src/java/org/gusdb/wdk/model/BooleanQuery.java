package org.gusdb.wdk.model;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.HashMap;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;


/**
 * Created: Fri May 21 1821:30 EDT 2004
 *
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class BooleanQuery extends Query {

    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------
    public static final String FIRST_INSTANCE_PARAM_NAME = "firstQueryInstanceId";
    public static final String SECOND_INSTANCE_PARAM_NAME = "secondQueryInstanceId";
    public static final String BOOLEAN_PARAM_SET_NAME = "booleanParamSet";
    

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    
    public BooleanQuery(){
	setName("BooleanQuery");
	setSetName("BooleanQuerySet");
	StringParam firstParam = new StringParam();
	firstParam.setName(FIRST_INSTANCE_PARAM_NAME);
	firstParam.setFullName(BOOLEAN_PARAM_SET_NAME);
	addParam(firstParam);

	StringParam secondParam = new StringParam();
	secondParam.setName(SECOND_INSTANCE_PARAM_NAME);
	secondParam.setFullName(BOOLEAN_PARAM_SET_NAME);
	addParam(secondParam);
    }

    /**
     * Required to override method in Query, but this is not the way to create a BooleanQueryInstance.
     * BooleanQueryInstances should be obtained from the WdkModel by calling its <code>makeBooleanQueryInstance</code>
     * method.
     */
    public QueryInstance makeInstance(){
	return null;

    }

}
