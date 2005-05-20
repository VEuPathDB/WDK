package org.gusdb.wdk.controller.action;

import org.gusdb.wdk.model.WdkModelException;
import org.apache.struts.action.ActionForm;

import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;

/**
 *  form bean for showing a wdk question from a question set
 */

public class QuestionSetForm extends ActionForm {

    private String qFullName = null;

    public void setQuestionFullName(String qFN) {
   	//System.out.println("*** QuestionSetForm.setqfullname: ");

	String qsq = qFN;
	String qpName=null, qSetN=null, qN=null;
   	int sep = qsq.indexOf('.'); 



     if(sep>=0){//this was added by the cryptoui group
        qSetN = qsq.substring(0, sep);
	setQSetName(qSetN);
     	int sep2 = qsq.indexOf('.', sep+1);
	if(sep2 >=0){
                qN = qsq.substring(sep+1, sep2);
	        qpName = qsq.substring( sep2+1,  qsq.length());
		setKey(qpName);
	}else
		qN = qsq.substring(sep+1, qsq.length());

	setQName(qN);
      }
	this.qFullName = qFN;

	//System.out.println(" ");
   	//System.out.println("*** QuestionSetForm.setqfullname: " + ", qsetName= "+qSetN+ ", qName= "+qN  +", key=" +qpName  );


    }



    public String getQuestionFullName() {
	return this.qFullName;
    }

  /******************Added*************************/

    private String qName=null, qSetName=null;
    private String qName2=null, qSetName2=null;
    private String key=null,  allInput=null;
    private String key2=null;
    private String booleanOp, qFullName2;
    private String [] organism ={"Cryptosporidium hominis"},  organism2 ={"Cryptosporidium hominis"}; 
    private String [] value={"default "}, value2={"default "};
    private boolean needOrganism = false, isMultiPick=false;

   /* added for setting the Question Bean */	   
    private BooleanQuestionNodeBean rootNode = null;
    private BooleanQuestionLeafBean seedLeaf = null;
    private QuestionBean newQuestion;  
	
		
    public void reset() {
	setNeedOrganism(false);
	setIsMultiPick(false);
   }

    public void setQuestionFullName2(String qFN) {
	String qsq = qFN;
	String qpName=null, qSetN=null, qN=null;
   	int sep = qsq.indexOf('.'); 
     	int sep2 = qsq.indexOf('.', sep+1);

        qSetN = qsq.substring(0, sep);
        qN = qsq.substring(sep+1, sep2);

	if(sep2 >=0){
	        qpName = qsq.substring( sep2+1,  qsq.length());
		setKey2(qpName);

	}

	setQSetName2(qSetN);
	setQName2(qN);

        //System.out.println(" ");
   	//System.out.println("*** QuestionSetForm.setqfullname: " + ", qsetName= "+qSetN+ ", qName= "+qN  +", key=" +qpName  );

	this.qFullName2 = qFN;

    }


    public void setMyProp2(String key, String val)
    {
	String qsq = key;
   	int sep = qsq.indexOf('_'); 
     	int sep2 = qsq.indexOf('_', sep+1);
        String qSetN = qsq.substring(0, sep);
        String qN = qsq.substring(sep+1, sep2);
        String qpName = qsq.substring( sep2+1,  qsq.length());
	String [] val2={val.trim()};

	setQSetName(qSetN);
	setQName(qN);
	setKey(qpName);
	setValue(val2);
	setIsMultiPick(false);


	//System.out.println("*** QuestionSetForm.setMyProp2: " + qpName + " = " + val + 
	//		", qsetName= "+qSetN+ ", qName= "+qN    +"\n");

    }//end setMyProp
 
   /*Did not use this function */
    public void setMyProp3(String key, String val)
    {
	String qsq = key;
   	int sep = qsq.indexOf('_'); 
     	int sep2 = qsq.indexOf('_', sep+1);
        String qSetN = qsq.substring(0, sep);
        String qN = qsq.substring(sep+1, sep2);
        String qpName = qsq.substring( sep2+1,  qsq.length());
	String [] val2={val.trim()};

	setQSetName2(qSetN);
	setQName2(qN);
	setKey2(qpName);
	setValue2(val2);
	//System.out.println("*** QuestionSetForm.setMyProp3: " + qpName + " = " + val + 
	//		", qsetName= "+qSetN+ ", qName= "+qN    +"\n");

    }

    public String getMyProp2(String key)  throws WdkModelException
    {
        return " ";
    }

    public String getMyProp3(String key)  throws WdkModelException
    {
      
        return " ";
    }

   public void setMyMultiProp2(String key, String[] vals)
    {

	String qsq = key;
   	int sep = qsq.indexOf('_'); 
     	int sep2 = qsq.indexOf('_', sep+1);
        String qSetN = qsq.substring(0, sep);
        String qN = qsq.substring(sep+1, sep2);
        String qpName = qsq.substring( sep2+1,  qsq.length());

	setQSetName(qSetN);
	setQName(qN);
	setKey(qpName);
	setValue(vals);

	setIsMultiPick(true);
    }


    public String[] getMyMultiProp2(String key)  throws WdkModelException
    {
	String [] ret = {" "};
	return ret;
    }


    public String getQuestionFullName2() {
	return this.qFullName2;
    }


    public void setOrganism(String [] orgs){

	String name = orgs[0];
	int comma =name.indexOf(",");
	if(comma>-1){
	   organism = new String[2];	
	   organism[0]=(name.substring(0, comma)).trim();		
	   organism[1]=(name.substring(comma + 1, name.length())).trim();		
	}else{
	   organism=orgs;
	}

	for(int i=0; i<organism.length; i++)
	   System.out.println("QuestionSetForm, setOrg="+organism[i]);

    }

    public String [] getOrganism(){ return organism;}

    public void setOrganism2(String [] orgs){
	String name = orgs[0];
	int comma =name.indexOf(",");
	if(comma>-1){
	   organism2 = new String[2];
	   organism2[0]=(name.substring(0, comma)).trim();		
	   organism2[1]=(name.substring(comma + 1, name.length())).trim();		
	}else{
	   organism2=orgs;
	}

	for(int i=0; i<organism2.length; i++)
	   System.out.println("QuestionSetForm, setOrg2="+organism2[i]);

    }
    public String [] getOrganism2(){ return organism2;}

    public void setNeedOrganism(boolean org){ needOrganism = org;}
    public boolean getNeedOrganism(){ return needOrganism;}

    public void setIsMultiPick(boolean m){ isMultiPick=m;}
    public boolean getIsMultiPick(){ return isMultiPick;}

    public void setBooleanOp(String name){ booleanOp=name;}
    public String getBooleanOp(){ return booleanOp;}

    public void setQName(String name){ qName=name;}
    public String getQName(){ return qName;}

    public void setQSetName(String name){ qSetName=name;}
    public String getQSetName(){ return qSetName;}

    public void setKey(String name){ key=name;}
    public String getKey(){ return key;}

  //  public void setValue(String name){ value=name;}
  //  public String getValue(){ return value;}

    public void setValue(String[] name){ 
	int size = name.length;
	value = new String[size];
	for(int i =0; i< size; i++)
		value[i]=name[i].trim();
    }
    public String [] getValue(){ return value;}

    public void setQName2(String name){ qName2=name;}
    public String getQName2(){ return qName2;}

    public void setQSetName2(String name){ qSetName2=name;}
    public String getQSetName2(){ return qSetName2;}

    public void setKey2(String name){ key2=name;}
    public String getKey2(){ return key2;}

    public void setValue2(String [] name){ 
	int size = name.length;
	value2 = new String[size];
	for(int i =0; i< size; i++)
		value2[i]=name[i].trim();
    }
    public String [] getValue2(){ return value2;}

    /* added */ 
    public void setBooleanQuestionNode(BooleanQuestionNodeBean bqn) { rootNode = bqn; }
    public BooleanQuestionNodeBean getBooleanQuestionNode() { return rootNode; }
                                                       
    public void setBooleanQuestionLeaf(BooleanQuestionLeafBean bqf) { seedLeaf = bqf; }
    public BooleanQuestionLeafBean getBooleanQuestionLeaf() { return seedLeaf;}
	



}
