import { Action } from '../dispatcher/Dispatcher';
import WdkStore, { BaseState } from './WdkStore';
import { actionTypes } from '../actioncreators/UserDatasetsActionCreators';
import { UserDataset } from '../utils/WdkModel';

interface State extends BaseState {
  userDatasetLoading: boolean;
  userDataset: UserDataset;
  loadError?: Error;
}

export default class UserDatasetItemStore extends WdkStore<State> {

  getInitialState() {
    return Object.assign({
      userDataset: undefined
    }, super.getInitialState());
  }

  handleAction(state: State, {type, payload}: Action) {
    switch (type) {
      case actionTypes.DATASET_ITEM_LOADING: return Object.assign({}, state, {
        userDatasetLoading: true
      });

      case actionTypes.DATASET_ITEM_RECEIVED: return Object.assign({}, state, {
        userDatasetLoading: false,
        userDataset: payload.userDataset
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
