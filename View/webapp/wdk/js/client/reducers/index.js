import { combineReducers } from '../utils/reducerUtils';

import errors from './errors';
import loading from './loading';
import preferences from './preferences';

import resources from './resources';
import views from './views';

export let reducer = combineReducers({
  errors,
  loading,
  preferences,

  resources,
  views
});
