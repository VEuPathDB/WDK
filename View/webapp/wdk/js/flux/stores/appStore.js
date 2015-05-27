import Store from '../core/store';
import {
  AppLoading,
  AppError
} from '../ActionType';

function createStore({ dispatcher }) {
  var state = {
    isLoading: 0,
    errors: []
  };
  return new Store(dispatcher, state, update);
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

export default {
  createStore
};
