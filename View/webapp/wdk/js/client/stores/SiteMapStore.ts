import WdkStore, { BaseState } from './WdkStore';
import { OntologyNode } from '../utils/OntologyUtils';
import {
  LoadingAction,
  InitializeAction,
  ErrorAction,
  ExpansionAction,
  SearchAction
} from '../actioncreators/SiteMapActionCreators';

// define action type to be any our supported actions
type Action = LoadingAction
            | InitializeAction
            | ErrorAction
            | ExpansionAction
            | SearchAction;

export type State = BaseState & {
  tree?: OntologyNode,
  isLoading: boolean,
  expandedList: string[],
  searchText: string
}

export default class SiteMapStore extends WdkStore<State> {

  // defines the structure of this store's data
  getInitialState(): State {
    return {
      ...super.getInitialState(),
      tree: undefined,
      isLoading: false,
      expandedList : [],
      searchText: ""
    };
  }

  handleAction(state: State, action: Action): State {
    switch(action.type) {
      case 'sitemap/loading':
        return setSiteMapLoading(state, true);

      case 'sitemap/initialize':
        return initializeSiteMap(state, action.payload.tree);

      case 'sitemap/updateExpanded':
        return updateExpanded(state, action.payload.expandedList);

      case 'sitemap/setSearchText':
        return setSearchText(state, action.payload.searchText);

      case 'sitemap/error':
        return setSiteMapLoading(state, false);

      default:
        return state;
    }
  }
}

function setSiteMapLoading(state: State, isLoading: boolean) {
  return { ...state, isLoading };
}

function initializeSiteMap(state: State, tree: InitializeAction['payload']['tree']) {
  return { ...state, tree, isLoading: false };
}

function setSearchText(state: State, searchText: SearchAction['payload']['searchText']) {
  return { ...state, searchText };
}

function updateExpanded(state: State, expandedList: ExpansionAction['payload']['expandedList']) {
  return { ...state, expandedList };
}
