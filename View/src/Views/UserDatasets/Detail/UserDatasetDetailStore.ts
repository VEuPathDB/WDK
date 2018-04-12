import WdkStore, { BaseState } from 'Core/State/Stores/WdkStore';
import {
  DetailLoading,
  DetailUpdatingAction,
  DetailErrorAction,
  DetailReceivedAction,
  DetailUpdateErrorAction,
  DetailUpdateSuccessAction,
  DetailRemovingAction,
  DetailRemoveSuccessAction,
  DetailRemoveErrorAction,
  SharingSuccessAction
} from 'Views/UserDatasets/UserDatasetsActionCreators';
import { UserDataset } from 'Utils/WdkModel';

type Action = DetailLoading
            | DetailUpdatingAction
            | DetailErrorAction
            | DetailReceivedAction
            | DetailUpdateErrorAction
            | DetailUpdateSuccessAction
            | DetailRemovingAction
            | DetailRemoveSuccessAction
            | DetailRemoveErrorAction
            | SharingSuccessAction;

/**
 * If isLoading is false, and resource is undefined,
 * then assume the user dataset does not exist
 */
export type UserDatasetEntry = {
  isLoading: boolean;
  resource?: UserDataset;
};

export interface State extends BaseState {
  userDatasetsById: { [key: string]: UserDatasetEntry };
  userDatasetUpdating: boolean;
  userDatasetLoading: boolean;
  userDatasetRemoving: boolean;
  loadError?: Error;
  updateError?: Error;
  removalError? : Error;
}

/**
 * Stores a map of userDatasets by id. By not storing the current userDataset,
 * we avoid race conditions where the DATASET_DETAIL_RECEIVED actions are
 * dispatched in a different order than the corresponding action creators are
 * invoked.
 */
export default class UserDatasetDetailStore extends WdkStore<State> {

  getInitialState (): State {
    return Object.assign({
      userDatasetsById: {},
      userDatasetUpdating: false,
      userDatasetRemoving: false
    }, super.getInitialState());
  }

  handleAction (state: State, action: Action): State {
    switch (action.type) {
      case 'user-datasets/detail-loading': return {
        ...state,
        userDatasetsById: {
          ...state.userDatasetsById,
          [action.payload.id]: {
            isLoading: true
          }
        }
      };

      case 'user-datasets/detail-received': return {
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

      case 'user-datasets/detail-error': return {
        ...state,
        userDatasetLoading: false,
        loadError: action.payload.error
      };

      case 'user-datasets/detail-updating': return {
        ...state,
        userDatasetUpdating: true,
        updateError: undefined
      };

      case 'user-datasets/detail-update-success': return {
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

      case 'user-datasets/detail-update-error': return {
        ...state,
        userDatasetUpdating: false,
        updateError: action.payload.error
      };

      case 'user-datasets/detail-removing': return {
        ...state,
        userDatasetRemoving: true
      };

      case 'user-datasets/detail-remove-success': return {
        ...state,
        userDatasetRemoving: false,
        removalError: undefined
      };

      case 'user-datasets/detail-remove-error': return {
        ...state,
        userDatasetRemoving: false,
        removalError: action.payload.error
      };

      case 'user-datasets/sharing-success': {
        const { method, userDatasetIds, recipientUserIds } = action.payload;
        if (
          !Array.isArray(userDatasetIds)
          || !userDatasetIds.length
          || !Array.isArray(recipientUserIds)
          || !recipientUserIds.length
        ) return state;

        switch (method) {
          case 'add': {

            return state;
          }
          case 'delete': {
            const userDatasetsById = Object.entries(state.userDatasetsById).reduce((outputObject, [ id, entry ]) => {
              if (entry == null || entry.resource == null || entry.resource.sharedWith == null || !entry.resource.sharedWith.length)
                return Object.assign(outputObject, { [id]: entry });

              const updatedEntry = {
                ...entry,
                resource: {
                  ...entry.resource,
                  sharedWith: entry.resource.sharedWith.filter((userDatasetShare) => {
                    return !recipientUserIds.includes(userDatasetShare.user);
                  })
                }
              };

              return Object.assign(outputObject, { [id]: updatedEntry });
            }, {});

            return { ...state, userDatasetsById };
          }
          default: return state;
        }
      };

      default:
        return state;
    }
  }
}
