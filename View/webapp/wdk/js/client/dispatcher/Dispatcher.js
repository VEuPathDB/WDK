import { Dispatcher } from 'flux';

export default class WdkDispatcher extends Dispatcher {

  dispatch(action) {
    super.dispatch(action);
    return action;
  }

}
