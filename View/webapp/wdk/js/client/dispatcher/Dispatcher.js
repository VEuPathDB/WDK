import { Dispatcher } from 'flux';

export default class WdkDispatcher extends Dispatcher {

  // log
  dispatch(action) {
    console.log('Dispatching action', action);
    super.dispatch(action);
  }

}
