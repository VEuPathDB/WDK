/*
 * Created on Jun 24, 2004
 *
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.json.JSONException;

/**
 * A common interface for the various sets in the WDK model files.
 * 
 * @author jerric
 *
 */
public interface ModelSetI {

    String getName();

    Object getElement(String elementName);

    void setResources(WdkModel model) throws WdkModelException;

    void resolveReferences(WdkModel model) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException;

}
