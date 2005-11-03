package org.gusdb.wdk.model;
import java.util.ArrayList;

/** QuestionSetName.java
* Created for Custom specifications 
* @author Nivedita Kaluskar
**/


 public class QuestionSetName
    {
        private String name;
        private String type;
        private String prompt;
        private String newGroup;
        private ArrayList questionList;
        private Page page;

        public QuestionSetName() {
              questionList = new ArrayList();
        }
       
 /*        public void setPage(Page newPage)
        {
            page = newPage;
    //        System.out.println("In setName");
        }*/

        public void setName(String newName)
        {
            name = newName;
         //   System.out.println("In setName");
        }
        public String getName()
        {
            return name;
        }

        public void setType(String newType)
        {
         //  System.out.println("in setType");
            type = newType;
        }
        public String getType()
        {

            return type;
        }
     
        public void setPrompt(String newPrompt)
        {
            prompt = newPrompt;
        }
        public String getPrompt()
        {
            return prompt;
        }
 
        public void setNewGroup(String newGroupVal)
        {
            newGroup = newGroupVal;
        }
        public String getNewGroup()
        {
            return newGroup;
        }



      public void addQuestionFullName(QuestionFullName questionFullName)
        {
            questionFullName.setPage(page);
            questionList.add(questionFullName);
        }
       
        public ArrayList getQuestionList()
        {
             return questionList;
        }
       

       public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("QuestionSetName: name='" + name + "'" + newline
                                           + "Type='"  + type + "'");
       
       return buf.toString();
    }


 }
  



