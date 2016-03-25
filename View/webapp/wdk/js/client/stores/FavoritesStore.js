import WdkStore from './WdkStore';
import {actionTypes} from '../actioncreators/FavoritesActionCreator';

export default class FavoritesStore extends WdkStore {

  getInitialState() {
    return {};
  }

  reduce(state, action) {
    let { type, payload } = action;
    switch (type) {
      case actionTypes.FAVORITES_STATUS_LOADING:
        return updateState(state, payload.record, { isLoading: true });
      case actionTypes.FAVORITES_STATUS_RECEIVED:
        return updateState(state, payload.record, { isLoading: false, isInFavorites: payload.status });
      case actionTypes.FAVORITES_STATUS_ERROR:
        return updateState(state, payload.record, { isLoading: false, error: payload.error });
      default:
        return state;
    }
  }

  getEntry(record) {
    let favorites = this.getState()[record.recordClassName]
    return favorites && favorites[makeKey(record.id)];
  }

}

function updateState(state, record, partialEntry) {
  return Object.assign({}, state, {
    [record.recordClassName]: updateFavorites(state[record.recordClassName], record, partialEntry)
  })
}

function updateFavorites(favorites = {}, record, partialEntry) {
  let key = makeKey(record.id);
  return Object.assign({}, favorites, {
    [key]: mergeEntry(favorites[key], partialEntry)
  });
}

function mergeEntry(entry = {}, partialEntry) {
  return Object.assign({}, entry, partialEntry);
}

function makeKey(id) {
  return id.map(p => p.value).join('//');
}
