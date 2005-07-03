package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.WdkModelException;

/**
 * SanityElementI.java
 *
 * Elements to be tested as part of a sanity test.  More of a convenience interface than anything else.
 *
 * Created: Mon August 23 12:00:00 2004 EDT
 *
 * @author David Barkan
 * @version $Revision$ $Date$Author: art $
 */

public interface SanityElementI {

    /**
     * Dump a command-line that can be used to individually test this element, outside of the
     * context of a sanity test (the command line will include all necessary parameters, the values
     * of which will be the same as those that the test attempted to use previously.  This command
     * is usually printed when a sanity test has failed.
     *
     * @param globalArgs Arguments not specific to the Element itself; basically anything in the
     *                   Sanity test that was required to run all elements.
     */
    public String getCommand(String globalArgs) throws WdkModelException;

    /**
     * Return the name of this element.
     */
    public String getName();

    /**
     * Return the general type of this element (for instance, "query" or "record".
     */ 
    public String getType();
}
