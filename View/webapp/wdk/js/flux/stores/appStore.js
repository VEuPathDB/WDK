import createStore from '../utils/createStore';
import { APP_LOADING, APP_ERROR } from '../ActionType';

export default createStore({

  state: {
    isLoading: 0,
    errors: []
  },

  dispatchHandler(action, emitChange) {
    switch(action.type) {
      case APP_LOADING:
        if (action.isLoading) this.state.isLoading++;
        else this.state.isLoading--;
        emitChange();
        break;

      case APP_ERROR:
        this.state.errors.unshift(action.error);
        emitChange();

        // remove error after 1 second
        // setTimeout(() => {
        //   this.state.errors.pop();
        //   emitChange();
        // }, 1000);
        break;
    }
  },

  getState() {
    return this.state;
  }

});
