/**
 * Created by dfalke on 8/17/16.
 */
import { ReduceStore } from 'flux/utils';
import WdkDispatcher, {Action} from '../dispatcher/Dispatcher';
import GlobalDataStore from './GlobalDataStore';
import {GlobalData} from "./GlobalDataStore";

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

  /*------------- Methods that should probably not be overridden -------------*/

  constructor(dispatcher: WdkDispatcher, channel: string, globalDataStore: GlobalDataStore) {
    super(dispatcher);
    this.channel = channel;
    this.globalDataStore = globalDataStore;
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

}
