import WdkStore from './WdkStore';
import { actionTypes } from '../actioncreators/UserDatasetsActionCreators';

export default class UserDatasetsStore extends WdkStore {

  // defines the structure of this store's data
  getInitialState() {
    return {
      userDatasetsLoading: false,
      userDatasets: [],
      loadError: null
    };
  }

  handleAction(state, {type, payload}) {
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
