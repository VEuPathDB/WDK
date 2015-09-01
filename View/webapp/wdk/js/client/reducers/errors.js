import {
  APP_ERROR
} from '../constants/actionTypes';

export default function errors(errors = [], action) {
  if (action.type === APP_ERROR) {
    return [ action.error, ...errors ];
  }
  return errors;
}
