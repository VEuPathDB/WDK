import WdkStore from './WdkStore';
import { actionTypes } from '../actioncreators/SiteMapActionCreator';

export default class SiteMapStore extends WdkStore {

  // defines the structure of this store's data
  getInitialState() {
    return {
      tree: null,
      isLoading: true,
      expandedList : [],
      searchText: ""
    };
  }

  reduce(state, action) {
    let { type, payload } = action;
    switch(type) {
      case actionTypes.SITEMAP_LOADING:
        return siteMapLoading(state, { isLoading: true });

      case actionTypes.SITEMAP_INITIALIZE_STORE:
        return initializeSiteMap(state, payload);

      case actionTypes.SITEMAP_UPDATE_EXPANDED:
        return updateExpanded(state, payload);

      case actionTypes.SITEMAP_SET_SEARCH_TEXT:
        return setSearchText(state, payload);

      case actionTypes.APP_ERROR:
        return siteMapLoading(state, { isLoading: false });

      default:
        return state;
    }
  }
}

function siteMapLoading(state, payload) {
  return Object.assign({}, state, { isLoading: payload.isLoading });
}

function initializeSiteMap(state, payload) {
  return Object.assign({}, state, {
    tree: payload.tree,
    isLoading: false });
}

function setSearchText(state, payload) {
  return Object.assign({}, state, { searchText: payload.searchText, isLoading: false });
}

function updateExpanded(state, payload) {
  return Object.assign({}, state, { expandedList: payload.expandedList, isLoading: false });
}
