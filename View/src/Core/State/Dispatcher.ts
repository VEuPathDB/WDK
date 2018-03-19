import { Dispatcher } from 'flux';
import { Subject } from 'rxjs/Subject';

export interface Action {
  type: string | symbol;
  payload?: string | number | object;
  channel?: string;
  isBroadcast?: boolean;
};

export default class WdkDispatcher extends Dispatcher<Action> {

  /**
   * Actions will be pushed here after they have been handled by registered
   * callbacks. See http://reactivex.io/rxjs/manual/overview.html#subject
   * for more details on what a Subject is, and how they can be treated as
   * Observables.
   */
  private action$: Subject<Action> = new Subject();

  /**
   * Call super's dispatch method, then push values into action$ Subject.
   * Doing this here makes it possible for consumers of action$ emit futher
   * actions.
   */
  dispatch(action: Action) {
    super.dispatch(action);
    this.action$.next(action);
    return action;
  }

  asObservable() {
    // Only return the Observable functionality of action$.
    // E.g., consumers cannot push actions.
    return this.action$.asObservable();
  }

}
