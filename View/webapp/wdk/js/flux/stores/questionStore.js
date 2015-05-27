import Store from '../core/store';
import {
  QuestionsAdded
} from '../ActionType';

function createStore({ dispatcher }) {
  return new Store(dispatcher, undefined, update);
}

function update(state = {}, action) {
  if (action.type === QuestionsAdded) {
    state.questions = action.questions;
    return state;
  }
}

export default {
  createStore
};
