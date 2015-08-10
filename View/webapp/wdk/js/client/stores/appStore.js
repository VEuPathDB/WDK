import Store from '../core/store';
import {
  APP_LOADING,
  APP_ERROR
} from '../constants/actionTypes';

function createStore({ dispatcher }) {
  var state = {
    isLoading: 0,
    errors: []
  };
  return new Store(dispatcher, state, update);
}

function update(state, action) {
  switch(action.type) {
    case APP_LOADING: return setLoading(state, action);
    case APP_ERROR: return setError(state, action);
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
