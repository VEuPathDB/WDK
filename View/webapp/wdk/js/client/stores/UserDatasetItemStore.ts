import { Action } from '../dispatcher/Dispatcher';
import WdkStore, { BaseState } from './WdkStore';
import { actionTypes } from '../actioncreators/UserDatasetsActionCreators';
import { UserDataset } from '../utils/WdkModel';

interface State extends BaseState {
  userDatasetsById: { [key: number]: UserDataset };
  loadError?: Error;
}

/**
 * Stores a map of userDatasets by id. By not storing the current userDataset,
 * we avoid race conditions where the DATASET_ITEM_RECEIVED actions are
 * dispatched in a different order than the corresponding action creators are
 * invoked.
 */
export default class UserDatasetItemStore extends WdkStore<State> {

  getInitialState() {
    return Object.assign({
      userDatasetsById: {}
    }, super.getInitialState());
  }

  handleAction(state: State, {type, payload}: Action) {
    switch (type) {
      case actionTypes.DATASET_ITEM_LOADING: return Object.assign({}, state, {
        userDatasetLoading: true
      });

      case actionTypes.DATASET_ITEM_RECEIVED: return Object.assign({}, state, {
        userDatasetLoading: false,
        userDatasetsById: Object.assign({}, state.userDatasetsById, {
          [payload.userDataset.id]: payload.userDataset
        })
      });

      case actionTypes.DATASET_ITEM_ERROR: return Object.assign({}, state, {
        userDatasetLoading: false,
        loadError: payload.error
      });

      default:
        return state;
    }
  }
}
