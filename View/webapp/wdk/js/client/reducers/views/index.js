import { combineReducers } from '../../utils/reducerUtils';
import answer from './answer';
import record from './record';

export default combineReducers({
  answer,
  record
});
