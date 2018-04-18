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
  userDatasets: UserDataset[];
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
        userDatasets: action.payload.userDatasets,
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
        userDatasets: [...state.userDatasets].map((userDataset: UserDataset): UserDataset => {
          return userDataset.id === action.payload.userDataset.id
            ? action.payload.userDataset
            : userDataset
        })
      };

      case 'user-datasets/detail-remove-success': return {
        ...state,
        userDatasets: [...state.userDatasets].filter((userDataset: UserDataset): boolean => {
          return userDataset.id !== action.payload.userDataset.id;
        })
      };

      case 'user-datasets/detail-update-success': return {
        ...state,
        userDatasets: [...state.userDatasets].map((userDataset: UserDataset): UserDataset => {
          return userDataset.id === action.payload.userDataset.id
            ? action.payload.userDataset
            : userDataset
        })
      };

      case 'user-datasets/sharing-success': {
        const userDatasetsById = sharingReducer(state.userDatasetsById, action);
        const userDatasets = state.userDatasets.map(ud =>
          userDatasetsById[ud.id].resource || ud);
        return {
          ...state,
          userDatasetsById,
          userDatasets
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
