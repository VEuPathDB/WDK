import WdkStore, { BaseState } from 'Core/State/Stores/WdkStore';
import {
  ListLoadingAction,
  ListReceivedAction,
  ListErrorReceivedAction,
  DetailUpdateErrorAction,
  DetailUpdateSuccessAction,
  DetailRemoveSuccessAction
} from 'Views/UserDatasets/UserDatasetsActionCreators';
import { UserDataset } from 'Utils/WdkModel';

type Action = ListLoadingAction
  | ListReceivedAction
  | ListErrorReceivedAction
  | DetailUpdateErrorAction
  | DetailUpdateSuccessAction
  | DetailRemoveSuccessAction;

export interface State extends BaseState {
  userDatasetsLoading: boolean;
  userDatasets: UserDataset[];
  loadError: Error | null;
}

export default class UserDatasetListStore extends WdkStore<State> {

  getInitialState () {
    return Object.assign({
      userDatasetsLoading: false,
      userDatasets: [],
      loadError: null
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
        userDatasetsLoading: false,
        userDatasets: action.payload.userDatasets
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

      default:
        return state;
    }
  }
}
