import Store from '../core/store';
import {
  RECORD_CLASSES_ADDED
} from '../constants/actionTypes';

function createStore({ dispatcher }) {
  return new Store(dispatcher, undefined, update);
}

function update(state = {}, action) {
  if (action.type === RECORD_CLASSES_ADDED) {
    state.recordClasses = action.recordClasses;
    return state;
  }
}

export default {
  createStore
};
