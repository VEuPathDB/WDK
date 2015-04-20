import Store from '../Store';
import { APP_LOADING, APP_ERROR } from '../ActionType';

export default class AppStore extends Store {

  init() {
    this.state = {
      isLoading: 0,
      errors: []
    };
    this.handleAction(APP_LOADING, this.setLoading);
    this.handleAction(APP_ERROR, this.setError);
  }

  setLoading(action) {
    if (action.isLoading) this.state.isLoading++;
    else this.state.isLoading--;
  }

  setError(action) {
    this.state.errors.unshift(action.error);
  }


}
