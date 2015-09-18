import { combineReducers } from '../../lib/utils';
import questions from './questions';
import recordClasses from './recordClasses';
import records from './records';

export default combineReducers({
  questions,
  records,
  recordClasses
});
