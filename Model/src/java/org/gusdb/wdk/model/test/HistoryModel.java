package org.gusdb.wdk.model.test;

import java.util.Vector;

import org.gusdb.wdk.model.WdkModelException;
import org.w3c.dom.Document;

/**
 * HistoryModel.java
 * 
 * Model used in a sanity test. Contains all sanity elements representing
 * records and queries that will be excercised over the course of the sanity
 * test.
 * 
 * Created: Mon August 23 12:00:00 2004 EDT
 * 
 * @author Jerric Gao
 * @version $Revision: 3256 $ $Date: 2005-08-19 20:38:53 -0400 (Tue, 09 Aug
 *          2005) $Author: sfischer $
 */

public class HistoryModel {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /**
     * SanityRecords contained in this model.
     */
    protected Vector<SanityUser> sanityUsers = new Vector<SanityUser>();

    /**
     * Document set by the xml parser that creates this model. (DTB -- not sure
     * where document is ever used!).
     */
    private Document document;

    /**
     * Instance object used by HistoryTestXmlParser to create a HistoryModel if
     * no schema is provided.
     */
    public static final HistoryModel INSTANCE = new HistoryModel();

    // ------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------

    // SanityRecord Sets
    public void addSanityUser(SanityUser sanityUser) throws WdkModelException {
        sanityUsers.add(sanityUser);
    }

    public SanityUser[] getSanityUsers() {
        SanityUser[] users = new SanityUser[sanityUsers.size()];
        sanityUsers.toArray(users);
        return users;
    }

    public String toString() {
        StringBuffer result = new StringBuffer("HistoryModel\nSanityUsers:\n");
        SanityUser[] sanityUsers = getSanityUsers();
        for (SanityUser sanityUser : sanityUsers) {
            result.append(sanityUser.toString() + "\n");
        }
        return result.toString();
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
