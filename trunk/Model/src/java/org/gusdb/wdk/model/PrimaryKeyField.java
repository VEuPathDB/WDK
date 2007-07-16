package org.gusdb.wdk.model;

public class PrimaryKeyField extends AttributeField{

    private String idPrefix;
    private String delimiter;
    private FlatVocabParam projectParam;

    /**
     * Modified by Jerric
     * 
     * @param name
     * @param displayName
     * @param help
     * @param projectParam this is a part of combined PK for federation
     */
    public PrimaryKeyField(String name, String displayName, String help,
            FlatVocabParam projectParam) {
        super();
        this.name = name;
        this.displayName = displayName;
        this.help = help;
        this.projectParam = projectParam;
        this.idPrefix = "G."; // default value;
        this.delimiter = ":"; // default value;
    }
    
    public boolean hasProjectParam() {
        return (projectParam != null);
    }

    /**
     * @return Returns the delimiter.
     */
    String getDelimiter() {
        return delimiter;
    }

    /**
     * @param delimiter The delimiter to set.
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * @return Returns the idPrefix.
     */
    String getIdPrefix() {
        return idPrefix;
    }

    /**
     * @param idPrefix The idPrefix to set.
     */
    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    /**
     * @return Returns the project.
     */
    FlatVocabParam getProjectParam() {
        return projectParam;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
        // do nothing
    }
}
