import Store from '../Store';
import {
  QuestionsAdded
} from '../ActionType';


export default class QuestionStore extends Store {

  init() {
    this.state = {
      questions: []
    };
    this.handleAction(QuestionsAdded, this.setQuestions);
  }

  setQuestions({ questions }) {
    this.state.questions = questions;
  }

}
