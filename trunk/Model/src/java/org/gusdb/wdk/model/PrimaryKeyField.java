package org.gusdb.wdk.model;

public class PrimaryKeyField implements FieldI {

    private String name;
    private String displayName;
    private String help;
    private Integer truncate;
    
    // Added by Jerric
    private String         idPrefix;
    private String         delimiter;
    private FlatVocabParam projectParam;

    /**
     * Modified by Jerric
     * @param name
     * @param displayName
     * @param help
     * @param projectParam	this is a part of combined PK for federation
     */
    public PrimaryKeyField(String name, String displayName, String help, 
            FlatVocabParam projectParam) {
	this.name = name;
	this.displayName = displayName;
	this.help = help;
    this.projectParam = projectParam;
    this.idPrefix = "G.";	// default value;
    this.delimiter = ":";	// default value;
    }

    public String getName() {
	return name;
    }
	
    public Boolean getIsInternal() {
	return new Boolean(false);
    }
   
    public String getDisplayName() {
	return displayName;
    }
	
    public String getHelp() {
	return help;
    }

    public String getType() {
	return null;
    }

    public String toString() {
	return getDisplayName();
    }

    public void setTruncate(String truncate){
	this.truncate = new Integer(truncate);
    }

    public Integer getTruncate(){
	return truncate;
    }


    // =======================================================================
    // Following methods are added by Jerric
    /**
     * @return Returns the delimiter.
     */
    public String getDelimiter() {
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
    public String getIdPrefix() {
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
    public FlatVocabParam getProjectParam() {
        return projectParam;
    }

}

