import Store from '../core/store';
import {
  QUESTIONS_ADDED
} from '../constants/actionTypes';

function createStore({ dispatcher }) {
  return new Store(dispatcher, undefined, update);
}

function update(state = {}, action) {
  if (action.type === QUESTIONS_ADDED) {
    state.questions = action.questions;
    return state;
  }
}

export default {
  createStore
};
