import { combineReducers } from '../../lib/utils';
import answer from './answer';
import record from './record';

export default combineReducers({
  answer,
  record
});
