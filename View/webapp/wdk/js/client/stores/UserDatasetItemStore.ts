import WdkStore, { BaseState } from './WdkStore';
import {
  ItemLoading,
  ItemUpdatingAction,
  ItemErrorAction,
  ItemReceivedAction,
  ItemUpdateErrorAction,
  ItemUpdateSuccessAction
} from '../actioncreators/UserDatasetsActionCreators';
import { UserDataset } from '../utils/WdkModel';

type Action = ItemLoading
            | ItemUpdatingAction
            | ItemErrorAction
            | ItemReceivedAction
            | ItemUpdateErrorAction
            | ItemUpdateSuccessAction

/**
 * If isLoading is false, and resource is undefined,
 * then assume the user dataset does not exist
 */
type UserDatasetEntry = {
  isLoading: boolean;
  resource?: UserDataset | void;
};

export interface State extends BaseState {
  userDatasetsById: { [key: number]: UserDatasetEntry };
  userDatasetUpdating: boolean;
  userDatasetUpdateError?: Error;
  loadError?: Error;
  updateError?: Error;
  userDatasetLoading: boolean;
}

/**
 * Stores a map of userDatasets by id. By not storing the current userDataset,
 * we avoid race conditions where the DATASET_ITEM_RECEIVED actions are
 * dispatched in a different order than the corresponding action creators are
 * invoked.
 */
export default class UserDatasetItemStore extends WdkStore<State> {

  getInitialState(): State {
    return Object.assign({
      userDatasetsById: {},
      userDatasetUpdating: false
    }, super.getInitialState());
  }

  handleAction(state: State, action: Action): State {
    switch (action.type) {
      case 'user-datasets/item-loading': return {
        ...state,
        userDatasetsById: {
          ...state.userDatasetsById,
          [action.payload.id]: {
            isLoading: true
          }
        }
      };

      case 'user-datasets/item-received': return {
        ...state,
        userDatasetLoading: false,
        userDatasetsById: {
          ...state.userDatasetsById,
          [action.payload.id]: {
            isLoading: false,
            resource: action.payload.userDataset
          }
        }
      };

      case 'user-datasets/item-error': return {
        ...state,
        userDatasetLoading: false,
        loadError: action.payload.error
      };

      case 'user-datasets/item-updating': return {
        ...state,
        userDatasetUpdating: true,
        updateError: undefined
      };

      case 'user-datasets/item-update-success': return {
        ...state,
        userDatasetUpdating: false,
        userDatasetsById: {
          ...state.userDatasetsById,
          [action.payload.userDataset.id]: {
            isLoading: false,
            resource: action.payload.userDataset
          }
        }
      };

      case 'user-datasets/item-update-error': return {
        ...state,
        userDatasetUpdating: false,
        updateError: action.payload.error
      };

      default:
        return state;
    }
  }
}
