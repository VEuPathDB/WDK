package org.gusdb.gus.wdk.model.query;


public abstract class Param {
    
    String name;
    String prompt;
    String help;
    String dfault;

    public Param () {}

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
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


    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    /**
     * @return Error string if an error.  null if no errors.
     */ 
    protected abstract String validateValue(String value);


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
}
