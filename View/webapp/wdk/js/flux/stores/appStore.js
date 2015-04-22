import Store from '../Store';
import {
  AppLoading,
  AppError
} from '../ActionType';

export default class AppStore extends Store {

  init() {
    this.state = {
      isLoading: 0,
      errors: []
    };
    this.handleAction(AppLoading, this.setLoading);
    this.handleAction(AppError, this.setError);
  }

  setLoading(action) {
    if (action.isLoading) this.state.isLoading++;
    else this.state.isLoading--;
  }

  setError(action) {
    this.state.errors.unshift(action.error);
  }

}
