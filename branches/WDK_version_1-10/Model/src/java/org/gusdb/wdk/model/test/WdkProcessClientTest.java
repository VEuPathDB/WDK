/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.gusdb.wdk.model.process.WdkProcessClient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WdkProcessClientTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    public WdkProcessClientTest() {
        super();
    }

    public WdkProcessClientTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
	testInvoke();
        //junit.textui.TestRunner.run(JUnitUserTest.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new WdkProcessClientTest("testInvoke"));
        return suite;
    }

    /*
     * Test method for
     * 'org.gusdb.wdk.model.process.WdkProcessClient.invokeString[], String[],
     * String[])'
     */
    public static void testInvoke() {
        // currently, the test case will be defined here; later the test cases
        // will be defined in sanityModel
	String processName = System.getProperty("process.name");
        if (processName == null) processName = "WuBlastProcessor";
	String db = System.getProperty("database.name");
        if (db == null) db = "Cparvum_nt.fsa"; 
        String[] params = { "BlastProgram", "BlastQuerySequence", "BlastDatabase" };
        String[] values = {
                "blastn",
                "AGCTTTTCATTCTGACTGCAACGGGCAATATGTCTCTGTGTGGATTAAAAAAAGAGTGTCTG"
                        + "ATAGCAGCTTCTGAACTGGTTACCTGCCGTGAGTAAATTAAAATTTTATTGA"
                        + "CTTAGGTCACTAAATACTTTAACCAATATAGGCATAGCGCACAGACAGATAA"
                        + "AAATTACAGAGTACACAACATCCATGAAACGCATTAGCACCACCATTACCAC"
                        + "CACCATCACCATTACCACAGGTAACGGTGCGGGCTGACGCGTACAGGAAACA"
                        + "CAGAAAAAAGCCCGCACCTGACAGTGCGGGCTTTTTTTTTCGACCAAAGGTA"
                        + "ACGAGGTAACAACCATGCGAGTGTTGAAGTTCGGCGGTACATCAGTGGCAAA"
                        + "TGCAGAACGTTTTCTGCGTGTTGCCGATATTCTGGAAAGCAATGCCAGGCAG"
                        + "GGGCAGGTGGCCACCGTCCTCTCTGCCCCCGCCAAAATCACCAACCACCTGG"
		        + "TGGCGATGATTGAAAAAACCATTAGCGGCCAGGATGCTTTACCCAATATCAG"
		        + "CGATGCCGAACGTATTTTTGCCGAACTTTT", db };
		//                + "CGATGCCGAACGTATTTTTGCCGAACTTTT", "Cparvum_nt.fsa" };

        // // no hit
        // String[] values = { "blastn", "QQQQQ", "c.parvum.nt" };

        // prepare the columns
        String[] columns = { "source_id", "TabularRow", "Alignment", "Header",
                "Footer" };
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < columns.length; i++) {
            map.put(columns[i], i);
        }

        try {
            String urlStr = System.getProperty("service.url");
	    if (urlStr == null) urlStr = "http://localhost:8090/axis/services/WdkProcessService";
            URL url = new URL(urlStr);
            WdkProcessClient client = new WdkProcessClient(url);

	    System.out.println("DEBUG: calling webservice at " + urlStr);
	    System.out.println("DEBUG: processName = " + processName);
	    System.out.println("DEBUG: params = " + arrayToString(params));
	    System.out.println("DEBUG: columns = " + arrayToString(columns));

            String[][] result = client.invoke(processName, params, values,
                    columns);

            // print out the result
            System.out.println("");
            for (int i = 0; i < result.length; i++) {
                System.out.println("================ " + result[i][0]
                        + " ================");
                for (String col : columns) {
                    System.out.println("------------ " + col + " ------------");
                    System.out.println(result[i][map.get(col)]);
                }
                System.out.println();
            }
        } catch (MalformedURLException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        } catch (ServiceException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        } catch (RemoteException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    static private String arrayToString(String[] vals) {
        StringBuffer b = new StringBuffer();
        for (String val : vals) {
            b.append(val + ";");
        }
        return b.toString();
    }
}
