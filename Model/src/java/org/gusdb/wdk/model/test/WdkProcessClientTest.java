/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
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
        junit.textui.TestRunner.run(JUnitUserTest.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new WdkProcessClientTest("testInvoke"));
        return suite;
    }

    /*
     * Test method for
     * 'org.gusdb.wdk.model.process.WdkProcessClient.invoke(String[], String[],
     * String[])'
     */
    public void testInvoke() {
        // currently, the test case will be defined here; later the test cases
        // will be defined in sanityModel
        // String processName = "NcbiBlastProcessor";
        String processName = "WuBlastProcessor";
        // prepare parameters
        // String[] params = { "Application", "Sequence", "-d" };
        String[] params = { "Application", "Sequence", "Database" };
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
                        + "CGATGCCGAACGTATTTTTGCCGAACTTTT", "Cparvum_nt.fsa" };
        // + "CGATGCCGAACGTATTTTTGCCGAACTTTT", "c.parvum.nt" };

        // // no hit
        // String[] values = { "blastn", "QQQQQ", "c.parvum.nt" };

        // prepare the columns
        String[] columns = { "Id", "TabularRow", "Alignment", "Header",
                "Footer" };
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < columns.length; i++) {
            map.put(columns[i], i);
        }

        try {
            // URL url = new URL(
            // "http://localhost:8080/axis/services/WdkProcessService");
            URL url = new URL(
                    "http://delphi.pcbi.upenn.edu:8080/axis/services/WdkProcessService");
            WdkProcessClient client = new WdkProcessClient(url);
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
}
