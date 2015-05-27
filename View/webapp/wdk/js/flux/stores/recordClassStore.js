import Store from '../core/store';
import {
  RecordClassesAdded
} from '../ActionType';

function createStore({ dispatcher }) {
  return new Store(dispatcher, undefined, update);
}

function update(state = {}, action) {
  if (action.type === RecordClassesAdded) {
    state.recordClasses = action.recordClasses;
    return state;
  }
}

export default {
  createStore
};
