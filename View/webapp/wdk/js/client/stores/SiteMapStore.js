import WdkStore from './WdkStore';
import { actionTypes } from '../actioncreators/SiteMapActionCreator';

export default class SiteMapStore extends WdkStore {

  // defines the structure of this store's data
  getInitialState() {
    return {
      tree: null,
      isLoading: false,
      expandedList : [],
      searchText: ""
    };
  }

  handleAction(state, { type, payload }) {
    switch(type) {
      case actionTypes.SITEMAP_LOADING:
        return setSiteMapLoading(state, true);

      case actionTypes.SITEMAP_INITIALIZE_STORE:
        return initializeSiteMap(state, payload.tree);

      case actionTypes.SITEMAP_UPDATE_EXPANDED:
        return updateExpanded(state, payload.expandedList);

      case actionTypes.SITEMAP_SET_SEARCH_TEXT:
        return setSearchText(state, payload.searchText);

      case actionTypes.APP_ERROR:
        return setSiteMapLoading(state, false);

      default:
        return state;
    }
  }
}

function setSiteMapLoading(state, isLoading) {
  return Object.assign({}, state, { isLoading });
}

function initializeSiteMap(state, tree) {
  return Object.assign({}, state, { tree, isLoading: false });
}

function setSearchText(state, searchText) {
  return Object.assign({}, state, { searchText });
}

function updateExpanded(state, expandedList) {
  return Object.assign({}, state, { expandedList });
}
