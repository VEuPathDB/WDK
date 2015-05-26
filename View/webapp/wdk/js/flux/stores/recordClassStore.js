import Store from '../Store';
import {
  RecordClassesAdded
} from '../ActionType';


export default function createRecordClassStore() {
  return Store.createStore(function(state = {}, action) {
    if (action.type === RecordClassesAdded) {
      state.recordClasses = action.recordClasses;
      return state;
    }
  });
}
