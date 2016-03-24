import update from 'react-addons-update';
import WdkStore from './WdkStore';
import {actionTypes} from '../actioncreators/BasketActionCreator';

export default class BasketStore extends WdkStore {

  getInitialState() {
    return {};
  }

  reduce(state, action) {
    let { type, payload } = action;
    switch (type) {
      case actionTypes.BASKET_STATUS_LOADING:
        return updateState(state, payload.record, { isLoading: true });
      case actionTypes.BASKET_STATUS_RECEIVED:
        return updateState(state, payload.record, { isLoading: false, isInBasket: payload.status });
      case actionTypes.BASKET_STATUS_ERROR:
        return updateState(state, payload.record, { isLoading: false, error: payload.error });
      default:
        return state;
    }
  }

  getEntry(record) {
    let basket = this.getState()[record.recordClassName]
    return basket && basket[makeKey(record.id)];
  }

}

function updateState(state, record, partialEntry) {
  return Object.assign({}, state, {
    [record.recordClassName]: updateBasket(state[record.recordClassName], record, partialEntry)
  })
}

function updateBasket(basket = {}, record, partialEntry) {
  let key = makeKey(record.id);
  return Object.assign({}, basket, {
    [key]: mergeEntry(basket[key], partialEntry)
  });
}

function mergeEntry(entry = {}, partialEntry) {
  return Object.assign({}, entry, partialEntry);
}

function makeKey(id) {
  return id.map(p => p.value).join('//');
}
