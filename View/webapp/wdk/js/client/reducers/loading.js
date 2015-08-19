import {
  APP_LOADING
} from '../constants/actionTypes';

export default function loading(loading = 0, action) {
  if (action.type === APP_LOADING) {
    action.isLoading ? loading++ : loading--;
  }
  return loading
}
