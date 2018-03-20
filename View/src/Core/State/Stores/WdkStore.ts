import { ReduceStore } from 'flux/utils';

import WdkDispatcher, { Action } from 'Core/State/Dispatcher';
import GlobalDataStore from 'Core/State/Stores/GlobalDataStore';
import { GlobalData } from 'Core/State/Stores/GlobalDataStore';
import { ActionCreatorServices, Epic, EpicServices } from 'Utils/ActionCreatorUtils';
import { Observable, Subject } from 'rxjs/Rx';
import { setTimeout } from 'timers';

export interface BaseState {
  globalData: Partial<GlobalData>;
}

export default class WdkStore<State extends BaseState = BaseState> extends ReduceStore<State, Action> {

  /** The name of the channel on which this store listens to actions. */
  channel: string;

  /** The store that provides global state */
  globalDataStore: GlobalDataStore;

  // Makes it possible to access the type of the Store's state via typescript.
  // E.g., Store['state'].
  get state() {
    return this.getState();
  }

  /*--------------- Methods that should probably be overridden ---------------*/

  getInitialState(): State {
    return {
      globalData: {}
    } as State;
  }

  /**
   * Does nothing by default for other actions; subclasses will probably
   * override. This is the store's opportunity to handle channel specific
   * actions.
   */
  handleAction(state: State, action: Action): State {
    return state;
  }

  /**
   * Return an array of Epics that will observe actions handled by this store.
   * Epics only respond to actions for which `storeShouldReceiveAction(action.chanel)`
   * returns true.
   */
  getEpics(): Epic[] {
    return [] as Epic[]
  }

  /*---------- Methods that may be overridden in special cases ----------*/

  /**
   * By default this store will receive the action if the action's channel is
   * undefined (indicating a broadcast action) or the channel matches this
   * store's channel name.  To receive actions on channels intended for other
   * stores, override this method.
   */
  storeShouldReceiveAction(channel?: string): boolean {
    return (channel === undefined /* broadcast */ || channel === this.channel);
  }

  /**
   * A root epic that merges the observables returned by `getEpics()`.
   */
  rootEpic(actions$: Observable<Action>, services: ActionCreatorServices): Observable<Action> {
    const epicServices = { ...services, store: this };
    const epicActions = this.getEpics().map(epic => epic(actions$, epicServices));
    return Observable.merge(...epicActions);
  }

  /*------------- Methods that should probably not be overridden -------------*/

  constructor(dispatcher: WdkDispatcher, channel: string, globalDataStore: GlobalDataStore, services: ActionCreatorServices) {
    super(dispatcher);
    this.channel = channel;
    this.globalDataStore = globalDataStore;
    this.configureEpic(dispatcher, services);
  }

  reduce(state: State, action: Action): State {
    this.getDispatcher().waitFor([ this.globalDataStore.getDispatchToken() ]);
    if (this.globalDataStore.hasChanged()) {
      state = Object.assign({}, state, {
        globalData: this.globalDataStore.getState()
      });
      return this.handleAction(state, action);
    }
    else if (this.storeShouldReceiveAction(action.channel)) {
      return this.handleAction(state, action);
    }
    return state;
  }

  configureEpic(dispatcher: WdkDispatcher, services: ActionCreatorServices) {
    // Wire up epics.
    const action$ = dispatcher.asObservable().filter(action =>
      this.storeShouldReceiveAction(action.channel));

    const logError = (error: Error) =>
      services.wdkService.submitError(error);

    const startEpic = (): Observable<Action> =>
      this.rootEpic(action$, services)
        // Assign channel unless action isBroadcast
        .map(action => ({ ...action, channel: action.isBroadcast ? undefined : this.channel }))
        .catch((error: Error, caught) => {
          console.error(error);
          logError(error);
          // restart epic
          return startEpic();
        })

    startEpic().subscribe(
      action => {
        dispatcher.dispatch(action)
      },
      error => {
        console.error(error);
        logError(error);
      },
      () => {
        console.debug('epic has completed in store "%s"', this.channel);
      }
    );
  }

}
