import { Action } from '../dispatcher/Dispatcher';
import WdkStore, { BaseState } from './WdkStore';
import { actionTypes } from '../actioncreators/UserDatasetsActionCreators';

type UserDataset = any;

interface State extends BaseState {
  userDatasetsLoading: boolean;
  userDatasets: UserDataset[];
  loadError: Error | null;
}

export default class UserDatasetListStore extends WdkStore<State> {

  getInitialState() {
    return Object.assign({
      userDatasetsLoading: false,
      userDatasets: [],
      loadError: null
    }, super.getInitialState());
  }

  handleAction(state: State, {type, payload}: Action) {
    switch (type) {
      case actionTypes.LOADING: return Object.assign({}, state, {
        userDatasetsLoading: true
      });

      case actionTypes.LOAD_SUCCESS: return Object.assign({}, state, {
        userDatasetsLoading: false,
        userDatasets: payload.userDatasets
      });

      case actionTypes.LOAD_ERROR: return Object.assign({}, state, {
        userDatasetsLoading: false,
        loadError: payload.error
      });

      default:
        return state;
    }
  }
}
