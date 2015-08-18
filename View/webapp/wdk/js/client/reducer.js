import combineReducers from './utils/combineReducers';

import answer from './reducers/answer';
import errors from './reducers/errors';
import loading from './reducers/loading';
import questions from './reducers/questions';
import recordClasses from './reducers/recordClasses';
import record from './reducers/record';
import preferences from './reducers/preferences';

export default combineReducers({
  answer,
  errors,
  loading,
  questions,
  recordClasses,
  record,
  preferences
});
