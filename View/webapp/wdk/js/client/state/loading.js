import {
  APP_LOADING
} from '../constants/actionTypes';

function update(loading = 0, action) {
  if (action.type === APP_LOADING) {
    action.isLoading ? loading++ : loading--;
  }
  return loading
}

export default {
  update
};
