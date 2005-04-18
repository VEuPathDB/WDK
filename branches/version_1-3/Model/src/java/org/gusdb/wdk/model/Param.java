package org.gusdb.wdk.model;


public abstract class Param {
    
    String name;
    String prompt;
    String help;
    String dfault;
    String fullName;

    public Param () {}

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public String getFullName() {
	return fullName;
    }

    /**
     * Assumes that the name of this param has already been set.  Note this is slightly
     * different than a simple accessor in that the full name of the param is <code>paramSetName</code>
     * concatenated with ".paramName".
     *
     * @param paramSetName name of the paramSet to which this param belongs.
     */
    public void setFullName(String paramSetName){
	this.fullName = paramSetName + "." + name;
    }


    public void setPrompt(String prompt) {
	this.prompt = prompt;
    }

    public String getPrompt() {
	return prompt;
    }

    public void setHelp(String help) {
	this.help = help;
    }

    public String getHelp() {
	return help;
    }

    public void setDefault(String dfault) {
	this.dfault = dfault;
    }

    public String getDefault() {
	return dfault;
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       String classnm = this.getClass().getName();
       StringBuffer buf = 
	   new StringBuffer(classnm + ": name='" + name + "'" + newline +
			    "  prompt='" + prompt + "'" + newline +
			    "  help='" + help + "'" + newline +
			    "  default='" + dfault + "'" + newline
			    );

       return buf.toString();
    }

    /**
     * @return Error string if an error.  null if no errors.
     */ 
    public abstract String validateValue(Object value) throws WdkModelException ;

    //////////////////////////////////////////////////////////////////////
    // protected methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Transforms external value into internal value if needed
     * By default returns provided value
     */
    protected Object getInternalValue(Object value) throws WdkModelException {
	return value;
    }

    protected abstract void resolveReferences(WdkModel model) throws WdkModelException;

    protected void setResources(WdkModel model) throws WdkModelException {}


}
