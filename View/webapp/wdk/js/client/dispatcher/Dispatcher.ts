import { Dispatcher } from 'flux';

export interface Action {
  type: string;
  payload: any;
};

export default class WdkDispatcher extends Dispatcher<Action> {

  dispatch(action: Action) {
    super.dispatch(action);
    return action;
  }

}
