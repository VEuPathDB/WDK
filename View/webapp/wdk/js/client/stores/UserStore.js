import {ReduceStore} from 'flux/utils';
import {filterRecords} from '../utils/recordUtils';
import UserActionCreator from '../actioncreators/UserActionCreator';

let action = UserActionCreator.actionTypes;

export default class UserStore extends ReduceStore {

  // defines the structure of this store's data
  getInitialState() {
    return {
      user: null,
      preferences: null,
      baskets: {},
      isLoading: false
    };
  }

  reduce(state, { type, payload }) {
    switch(type) {
      case action.USER_LOADING:
        return userLoading(state, { isLoading: true });

      case action.USER_INITIALIZE_STORE:
        return initializeUser(state, payload);

      case action.USER_PROFILE_UPDATE:
        return updateProfile(state, payload);

      case action.USER_PROPERTY_UPDATE:
        return updateProperties(state, payload);

      case action.USER_PREFERENCE_UPDATE:
        return updatePreferences(state, payload);

      case action.BASKET_STATUS_LOADING:
        return basketStatusLoading(state, payload);

      case action.BASKET_STATUS_RECEIVED:
        return basketStatus(state, payload);

      case action.BASKET_STATUS_ERROR:
        return basketStatusError(state, payload);

      case action.APP_ERROR:
        return userLoading(state, { isLoading: false });

      default:
        return state;
    }
  }
}

function userLoading(state, payload) {
  return Object.assign({}, state, { isLoading: payload.isLoading });
}

function initializeUser(state, payload) {
  return Object.assign({}, state, payload, { isLoading: false });
}

function basketStatusLoading(state, { recordClassName, primaryKey }) {
  return updateBasket(state, recordClassName, primaryKey, { isLoading: true });
}

function basketStatus(state, { recordClassName, primaryKey, inBasket }) {
  return updateBasket(state, recordClassName, primaryKey, { isLoading: false, inBasket });
}

function basketStatusError(state, { recordClassName, primaryKey, error }) {
  return updateBasket(state, recordClassName, primaryKey, { isLoading: false, error });
}

// merge `status` into the exsting basket object, or create a new one
// this could be a lot cleaner using React's immutability helper addon:
// http://facebook.github.io/react/docs/update.html
//
// We could replace the code below with this if we used it:
//
//    return update(state, {
//      [recordClassName]: {
//        [primaryKeyString]: { $merge: status }
//      }
//    });
//
function updateBasket(state, recordClassName, primaryKey, status) {
  // stringify the primaryKey object
  let primaryKeyString = JSON.stringify(primaryKey);
  let currentBasket = state.baskets[recordClassName] || {};

  // make a new copy of the basket item with the status object values merged
  let basketItem = Object.assign(
    {},
    currentBasket[primaryKeyString],
    status
  );

  // make a new copy of the recordclass basket, with the basketItem item merged
  let basket = Object.assign(
    {},
    currentBasket,
    { [primaryKeyString]: basketItem }
  );

  // make a new copy of all baskets, with the updated record class basket merged
  let baskets = Object.assign(
    {},
    state.baskets,
    { [recordClassName]: basket }
  );

  // finally, return a new state object with updated basket merged
  return Object.assign({}, state, { baskets })
}
