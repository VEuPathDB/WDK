import Store from '../Store';
import {
  QUESTION_LIST_LOAD_SUCCESS
} from '../ActionType';


export default class QuestionStore extends Store {

  init() {
    this.state = {
      questions: []
    };
    this.handleAction(QUESTION_LIST_LOAD_SUCCESS, this.setQuestions);
  }

  setQuestions({ questions }) {
    this.state.questions = questions;
  }

}
