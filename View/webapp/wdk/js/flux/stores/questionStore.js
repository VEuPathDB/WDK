import Store from '../Store';
import {
  QuestionsAdded
} from '../ActionType';


export default function createQuestionStore() {
  return Store.createStore(function(state = {}, action) {
    if (action.type === QuestionsAdded) {
      state.questions = action.questions;
      return state;
    }
  });
}
