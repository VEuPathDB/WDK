import {
  APP_ERROR
} from '../constants/actionTypes';

function update(errors = [], action) {
  if (action.type === APP_ERROR) {
    errors = [action.error].concat(error);
  }
  return errors;
}

export default {
  update
};
