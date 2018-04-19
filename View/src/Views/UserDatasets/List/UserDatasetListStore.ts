import WdkStore, { BaseState } from 'Core/State/Stores/WdkStore';
import { UserDataset } from 'Utils/WdkModel';
import sharingReducer from 'Views/UserDatasets/Sharing/UserDatasetSharingReducer';
import {
  DetailRemoveSuccessAction,
  DetailUpdateErrorAction,
  DetailUpdateSuccessAction,
  ListErrorReceivedAction,
  ListLoadingAction,
  ListReceivedAction,
  ProjectFilterAction,
  SharingSuccessAction,
} from 'Views/UserDatasets/UserDatasetsActionCreators';
import { difference } from 'lodash';

type Action = ListLoadingAction
  | ListReceivedAction
  | ListErrorReceivedAction
  | DetailUpdateErrorAction
  | DetailUpdateSuccessAction
  | DetailRemoveSuccessAction
  | SharingSuccessAction
  | ProjectFilterAction;

export interface State extends BaseState {
  userDatasetsLoading: boolean;
  userDatasets: number[];
  userDatasetsById: Record<string, { isLoading: boolean, resource?: UserDataset }>;
  loadError: Error | null;
  filterByProject: boolean;
}

export default class UserDatasetListStore extends WdkStore<State> {

  getInitialState () {
    return Object.assign({
      userDatasetsLoading: false,
      userDatasets: [],
      userDatasetsById: {},
      loadError: null,
      filterByProject: true
    }, super.getInitialState());
  }

  handleAction (state: State, action: Action): State {
    switch (action.type) {
      case 'user-datasets/list-loading': return {
        ...state,
        userDatasetsLoading: true
      };

      case 'user-datasets/list-received': return {
        ...state,
        filterByProject: action.payload.filterByProject,
        userDatasetsLoading: false,
        userDatasets: action.payload.userDatasets.map(ud => ud.id),
        userDatasetsById: action.payload.userDatasets.reduce((uds, ud) =>
          Object.assign(uds, { [ud.id]: { loading: false, resource: ud }}), {} as State['userDatasetsById'])
      };

      case 'user-datasets/list-error': return {
        ...state,
        userDatasetsLoading: false,
        loadError: action.payload.error
      };

      case 'user-datasets/detail-update-success': return {
        ...state,
        userDatasetsById: {
          ...state.userDatasetsById,
          [action.payload.userDataset.id]: action.payload.userDataset
        }
      };

      case 'user-datasets/detail-remove-success': return {
        ...state,
        userDatasets: difference(state.userDatasets, [action.payload.userDataset.id]),
        userDatasetsById: {
          ...state.userDatasetsById,
          [action.payload.userDataset.id]: undefined
        }
      };

      case 'user-datasets/sharing-success': {
        const userDatasetsById = sharingReducer(state.userDatasetsById, action);
        return {
          ...state,
          userDatasetsById
        }
      }

      case 'user-datasets/project-filter-preference-received': {
        return {
          ...state,
          filterByProject: action.payload.filterByProject
        }
      }

      default:
        return state;
    }
  }
}
