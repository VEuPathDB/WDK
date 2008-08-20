package org.gusdb.wdk.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONException;

public class Utilities {

    /**
     * param values with is prefix means the result of that value is a checksum
     * for the real value. if the param is a datasetParam, then the checksum is
     * an index to a dataset obejct; if the param is of other types, then the
     * value of the param can be found in the clob values table by the checksum
     */
    public static final String COMPRESSED_VALUE_PREFIX = "[C]";

    /**
     * The maximum size for parameter values that will be displayed in thr URL
     * as plain values
     */
    public static final int MAX_PARAM_VALUE_SIZE = 100;

    /**
     * The maximum number of attributes used in sorting an answer
     */
    public static final int SORTING_LEVEL = 3;

    /**
     * command-line argument: -model
     */
    public static final String ARGUMENT_PROJECT_ID = "model";

    /**
     * system property: gusHome
     */
    public static final String SYSTEM_PROPERTY_GUS_HOME = "GUS_HOME";

    public static final int MAXIMUM_NESTED_RECORD_INSTANCES = 1000;

    public static final String COLUMN_PROJECT_ID = "project_id";

    public static final String PARAM_PROJECT_ID = COLUMN_PROJECT_ID;

    public static final String ALIAS_NEW_KEY_COLUMN_PREFIX = "new_";

    public static final String INTERNAL_PARAM_SET = "internalParams";

    public static final String INTERNAL_QUERY_SET = "internalQueries";

    public static final String INTERNAL_QUESTION_SET = "internalQuestions";

    public static String encrypt(String data) throws WdkModelException,
            NoSuchAlgorithmException {
        // cannot encrypt null value
        if (data == null || data.length() == 0)
            throw new WdkModelException("Cannot encrypt an empty/null string");

        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] byteBuffer = digest.digest(data.toString().getBytes());
        // convert each byte into hex format
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < byteBuffer.length; i++) {
            int code = (byteBuffer[i] & 0xFF);
            if (code < 0x10) buffer.append('0');
            buffer.append(Integer.toHexString(code));
        }
        return buffer.toString();
    }

    public static String replaceMacros(String text, Map<String, Object> tokens)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException {
        for (String token : tokens.keySet()) {
            Object object = tokens.get(token);
            String value = (object == null) ? "" : object.toString();
            String macro = Pattern.quote("$$" + token + "$$");
            text = text.replaceAll(macro, value);
        }
        return text;
    }

    public static String[] toArray(String data) {
        if (data == null || data.length() == 0) {
            String[] values = new String[0];
            return values;
        }
        data = data.replace(',', ' ');
        data = data.replace(';', ' ');
        data = data.replace('\t', ' ');
        data = data.replace('\n', ' ');
        data = data.replace('\r', ' ');
        return data.trim().split("\\s+?");
    }

    public static String fromArray(String[] data) {
        StringBuffer sb = new StringBuffer();
        for (String value : data) {
            if (sb.length() > 0) sb.append(",");
            sb.append(value);
        }
        return sb.toString();
    }
}
