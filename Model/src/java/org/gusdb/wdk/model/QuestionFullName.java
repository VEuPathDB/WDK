package org.gusdb.wdk.model;
import java.util.ArrayList;
import org.gusdb.wdk.model.Defaults;
import org.gusdb.wdk.model.PageDefaults;
import org.gusdb.wdk.model.Parameter;

/** QuestionFullName.java
* Created for Custom specifications 
* @author Nivedita Kaluskar
**/

 public class QuestionFullName
    {
        private String name;
        private String hiddenName;
        private Defaults defaults;
        private Parameter parameter; 
        private String type="";
        private Page page;
        private ArrayList parameters;
        
 
        public QuestionFullName() {
             parameters = new ArrayList();
             parameter = new Parameter();
             
          //type = Defaults.getQuestionType();


           //  System.out.println("defaults:getQuestionType : "+Defaults.getQuestionType());
        }

 

 
        public void setName(String newName)
        {
            name = newName;
          //  System.out.println("In setName");
        }
        public String getName()
        {
            return name;
        }

        public void setHiddenName(String newHiddenName)
        { 
           hiddenName = newHiddenName;
        }

        public String getHiddenName() 
        {
            return hiddenName;
        }

        public void setType(String newType)
        {
          // System.out.println("in setType");
            type = newType;
        }
        public String getType()
        {
           // System.out.println("Question type :" + type);
            return type;
        }

        public void setPage(Page newPage)
        {
            page = newPage;
        }
      
        public Page getPage()
        {
            return page;
        }
     
      public void addParameter(Parameter parameter)
        {
         //  if (page.getPageDefaults() != null) 
          //   parameter.setPageDefaults(page.getPageDefaults());
            parameters.add(parameter);
        }
       
        public ArrayList getParameters()
        {
             return parameters;
        }
       

       public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("QuestionFullName: name='" + name + "'" + newline
                                           + "Type='"  + type + "'");
       
       return buf.toString();
    }


 }
  



