import * as React from 'react';
import PropTypes from 'prop-types';
import { parse } from 'querystring';
import { ActionCreator } from '../ActionCreator';
import { wrapActions } from '../utils/componentUtils';
import WdkStore, { BaseState } from '../stores/WdkStore';
import { Action } from "../dispatcher/Dispatcher";
import { ViewControllerProps, DispatchAction, MakeDispatchAction, StoreConstructor } from "../CommonTypes";

import Page from '../components/Page';
import NotFound from '../components/NotFound';
import PermissionDenied from '../components/PermissionDenied';
import LoadError from '../components/LoadError';
import Loading from '../components/Loading';

/**
 * Abstract base class for all ViewContoller classes in WDK. This base class is
 * responsible for:
 *  - managing store subscription
 *  - binding action creators to dispatcher
 *  - exposing store, dispatcher, and bound action creators on context
 *
 * It is also a type-safe abstraction over some constraints of WDK ViewControllers:
 *  - All ViewControllers must provide a `Store`.
 *  - The `Store` must be a subclass of `WdkStore`.
 *  - The state of the ViewController must be a transformation of the `Store`'s state.
 *
 */
export default abstract class AbstractViewController<State extends {} = BaseState, Store extends WdkStore = WdkStore, ActionCreators extends Record<any, ActionCreator<Action>> = {}> extends React.PureComponent<ViewControllerProps<Store>, State> {

  store: Store;

  storeSubscription: {
    remove(): void;
  }

  dispatchAction: DispatchAction;

  eventHandlers: ActionCreators;

  // TODO Remove makeDispatchAction and eventHandlers from context.
  static childContextTypes = {
    store: PropTypes.object,
    makeDispatchAction: PropTypes.func,
    dispatchAction: PropTypes.func,
    eventHandlers: PropTypes.object
  };

  childContext: {
    store: WdkStore;
    makeDispatchAction: MakeDispatchAction;
    dispatchAction: DispatchAction;
    eventHandlers: ActionCreators
  };

  /*-------------- Abstract methods to implement to receive store data --------------*/

  // These are methods are abstract, rather than concrete with a default
  // implementation, becuase the type system cannot ensure that the generic
  // type argument `State` is compatible with `BaseState`. Since this aims to
  // be type safe, we leave these to the implementor to define.

  /**
   * Returns this controller's `Store` class.
   */
  abstract getStoreClass(): StoreConstructor<Store>;

  /**
   * Transforms the `Store`'s state into the `ViewController`'s state.
   */
  abstract getStateFromStore(): State;

  /*--------------- Methods to override to display content ---------------*/

  /**
   * Returns the title of this page
   */
  getTitle(): string {
    return "WDK";
  }

  /**
   * Renders the highest page component below the Page tag.
   */
  renderView(): JSX.Element | null {
    return ( <span>Page for View Controller: {this.constructor.name}</span> );
  }

  /*-------- Methods to override to call ACs and load initial data --------*/

  /**
   * Returns an object containing named event action creators.  These
   * functions can refer to 'this' and will be bound to the child view
   * controller instance before being passed to renderView().
   */
  getActionCreators(): ActionCreators {
    return {} as ActionCreators;
  }

  /**
   * This is a good place to perform side-effects, such as calling an action
   * creator to load data for a store.
   *
   * Called when the component is first mounted with the state of the store
   * and the initial props. Also called when new props are received, with the
   * state of the store, the new props, and the old props. On the first call
   * when the component is first mounted, the old props will be undefined.
   */
  loadData(nextProps?: ViewControllerProps<Store>): void {
    return undefined;
  }

  /*------------ Methods to override to use placeholder pages ------------*/

  /**
   * Returns whether an initial data load error has occurred which would prevent
   * the page from rendering.
   * @param {Object} state The current state.
   */
  isRenderDataLoadError(): boolean {
    return false;
  }

  /**
   * Returns whether enough data has been loaded into the store to render the
   * page.
   */
  isRenderDataLoaded(): boolean {
    return true;
  }

  /**
   * Returns whether required data resources are not found.
   */
  isRenderDataNotFound(): boolean {
    return false;
  }

  /**
   * Returns whether access to required data resources are forbidden for current user.
   */
  isRenderDataPermissionDenied(): boolean {
    return false;
  }

  /*------------- Methods that should probably not be overridden -------------*/

  getChildContext() {
    return this.childContext;
  }

  getQueryParams() {
    return parse(this.props.location.search.slice(1));
  }

  /**
   * Registers with this controller's store if it has one and sets initial state
   */
  constructor(props: ViewControllerProps<Store>) {
    super(props);
    this.dispatchAction = this.props.makeDispatchAction(this.getChannelName(), this.props.history);
    this.eventHandlers = wrapActions(this.dispatchAction, this.getActionCreators()) as ActionCreators;
    const StoreClass = this.getStoreClass();
    this.store = this.props.stores.get(StoreClass);
    this.state = this.getStateFromStore();
    this.childContext = {
      store: this.store,
      makeDispatchAction: this.props.makeDispatchAction,
      dispatchAction: this.dispatchAction,
      eventHandlers: this.eventHandlers
    };
  }

  setDocumentTitle(state: State): void {
    if (this.isRenderDataLoadError()) {
      document.title = "Error";
    }
    else if (this.isRenderDataNotFound()) {
      document.title = "Page not found";
    }
    else if (this.isRenderDataPermissionDenied()) {
      document.title = "Permission denied";
    }
    else if (!this.isRenderDataLoaded()) {
      document.title = "Loading...";
    }
    else {
      document.title = this.getTitle();
    }
  }

  componentDidMount(): void {
    if (this.store != null) {
      this.storeSubscription = this.store.addListener(() => {
        this.setState(this.getStateFromStore());
      });
    }
    this.loadData();
    this.setDocumentTitle(this.state);
  }

  componentWillReceiveProps(nextProps: ViewControllerProps<Store>): void {
    this.loadData(nextProps);
  }

  componentDidUpdate(): void {
    this.setDocumentTitle(this.state);
  }

  /**
   * Removes subscription to this controller's store
   */
  componentWillUnmount(): void {
    if (this.storeSubscription != null) {
      this.storeSubscription.remove();
    }
  }

  /**
   * Returns the channel name.  If not overridden, this function returns the
   * store name.  Channels control which store's receive actions from ACs called
   * from this VC.  Typically, ACs send actions either on the channel passed
   * to them, or they send broadcast actions (which are received by all stores).
   * You probably need a pretty good reason to do something different.
   */
  getChannelName(): string {
    return this.getStoreClass().name;
  }

  /**
   * Renders the page of this controller.  Subclasses may override, but may
   * save effort by overriding renderView() instead.  This method will call that
   * one but only after checking if data required for render has been not yet
   * fully loaded or has erred during loading.
   */
  render() {
    if (this.isRenderDataLoadError()) {
      return ( <Page><LoadError/></Page> );
    }
    else if (this.isRenderDataNotFound()) {
      return ( <Page><NotFound/></Page> );
    }
    else if (this.isRenderDataPermissionDenied()) {
      return ( <Page><PermissionDenied/></Page> );
    }
    else if (!this.isRenderDataLoaded()) {
      return ( <Page><Loading/></Page> );
    }
    else {
      return ( <Page>{this.renderView()}</Page> );
    }
  }
}
