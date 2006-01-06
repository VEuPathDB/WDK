/**
 * Created on: Mar 18, 2005
 */
package org.gusdb.wdk.model;

/**
 * @author Jerric
 */
public class PrimaryKeyValue {

    private PrimaryKeyField field;
    private String          projectId;
    private String          projectName;
    private String          recordId;

    public PrimaryKeyValue(PrimaryKeyField field, String projectId,
			   String recordId) throws WdkModelException {
        this.field = field;
        this.projectId = projectId;
        this.recordId = recordId;

        // resolve project name
        this.projectName = null;
        if (projectId != null) {
            FlatVocabParam projectParam = field.getProjectParam();
            String[] keys = projectParam.getVocab();
            for (int i = 0; i < keys.length; i++) {
                String value = (String) projectParam.getInternalValue(keys[i]);
                if (value.equalsIgnoreCase(projectId)) projectName = keys[i];
            }
        }
    }

    public String getValue() throws WdkModelException {
        StringBuffer sb = new StringBuffer();
        if (projectName != null) {
            sb.append(projectName);
            sb.append(field.getDelimiter());
        }
        sb.append(field.getIdPrefix());
        sb.append(recordId);
        return sb.toString();
    }

    public String getBriefValue() throws WdkModelException {
        String value = getValue();
        Integer truncate = field.getTruncate();
        if (truncate == null) {
            truncate = WdkModel.TRUNCATE_DEFAULT;
        }
        int truncateInt = truncate.intValue();

        if (value.length() < truncateInt) {
            return value;
        } else {
            String returned = value.substring(0, truncateInt + 1) + ". . .";
            return returned;
        }
    }

    /**
     * @return Returns the recordId.
     */
    public String getRecordId() {
        return recordId;
    }

    /**
     * @return Returns the projectId.
     * @throws WdkModelException
     */
    public String getProjectId() {
        return this.projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof PrimaryKeyValue) {
            PrimaryKeyValue pk = (PrimaryKeyValue) obj;

            if (pk.recordId.equalsIgnoreCase(recordId)) {
                if (projectId == null) return true;
                return pk.getProjectName().equalsIgnoreCase(projectName);
            }
        }
        return false;
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int code = 0;
        try {
            String value = getValue();
            int pos = 0;
            for (int i = 0; i < value.length(); i++) {
                int c = value.charAt(i);
                code ^= (c << (pos * 8));
                if (pos == 3) pos = 0;
                else pos++;
            }
        } catch (WdkModelException ex) {
            // System.err.println(ex);
            ex.printStackTrace();
        }
        return code;
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
        try {
            return getBriefValue();
        } catch (WdkModelException ex) {
            // System.err.println(ex);
            ex.printStackTrace();
        }
        return super.toString();
    }
}
