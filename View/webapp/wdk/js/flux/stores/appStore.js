import Store from '../Store';
import {
  AppLoading,
  AppError
} from '../ActionType';

export default function createAppStore() {
  var initialState = {
    isLoading: 0,
    errors: []
  };
  return Store.createStore(initialState, update);
}

function update(state, action) {
  switch(action.type) {
    case AppLoading: return setLoading(state, action);
    case AppError: return setError(state, action);
  }
}

function setLoading(state, action) {
  if (action.isLoading) state.isLoading++;
  else state.isLoading--;
  return state;
}

function setError(state, action) {
  state.errors.unshift(action.error);
  return state;
}
