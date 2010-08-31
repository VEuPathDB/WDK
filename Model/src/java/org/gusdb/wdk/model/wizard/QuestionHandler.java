package org.gusdb.wdk.model.wizard;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.user.User;

public class QuestionHandler implements StageHandler {

    private static final String PARAM_QUESTION_NAME = "questionFullName";
    
    private static final String ATTR_QUESTION = "question";
    
    public Map<String, Object> execute(WdkModel wdkModel, User user,
            Map<String, String> params) throws Exception {
        String questionName = params.get(PARAM_QUESTION_NAME);
        Question question = (Question)wdkModel.resolveReference(questionName);
        QuestionBean bean = new QuestionBean(question);
        
        Map<String, Object> values = new HashMap<String, Object>();
        for (String param : params.keySet()) {
            values.put(param, params.get(param));
        }
        values.put(ATTR_QUESTION, bean);
        return values;
    }

}
