import { Component, PropTypes } from 'react';
import Doc from '../components/Doc';
import LoadError from '../components/LoadError';
import Loading from '../components/Loading';
import { wrapActions } from '../utils/componentUtils';

class WdkViewController extends Component {

  /*--------------- Methods that should probably be overridden ---------------*/

  /**
   * Returns this controller's store name; should be overridden if the
   * controller uses data in a store.
   */
  getStoreName() {
    return null;
  }

  /**
   * Returns an object containing named event action creators.  These
   * functions can refer to 'this' and will be bound to the child view
   * controller instance before being passed to renderView().
   */
  getActionCreators() {
    return {};
  }

  /**
   * This is a good place to perform side-effects, such as calling an action
   * creator to load data for a store.
   *
   * Called when the component is first mounted with the state of the store
   * and the initial props. Also called when new props are received, with the
   * state of the store, the new props, and the old props. On the first call
   * when the component is first mounted, the old props will be undefined.
   *
   * @param {Object} state The current state.
   * @param {Object} nextProps The incoming props
   * @param {Object} previousProps The previous props
   * @returns {void}
   */
  loadData(state, nextProps, previousProps) {
    return;
  }

  /**
   * Returns whether an initial data load error has occurred which would prevent
   * the page from rendering.
   * @param {Object} state The current state.
   */
  isRenderDataLoadError(state) {
    return false;
  }

  /**
   * Returns whether enough data has been loaded into the store to render the
   * page.
   */
  isRenderDataLoaded(state) {
    return true;
  }

  /**
   * Returns the title of this page
   */
  getTitle(state) {
    return "WDK";
  }

  /**
   * Renders the highest page component below the Doc tag.
   */
  renderView(state, eventHandlers) {
    return ( <span>Page for View Controller: {this.name}</span> );
  }

  /*---------- Methods that may be overridden in special cases ----------*/

  /**
   * Returns the channel name.  If not overridden, this function returns the
   * store name.
   */
  getChannelName() {
    return this.getStoreName();
  }

  /*------------- Methods that should probably not be overridden -------------*/

  /**
   * Registers with this controller's store if it has one and sets initial state
   */
  constructor(...args) {
    super(...args);
    this.dispatchAction = this.props.makeDispatchAction(this.getChannelName());
    this.eventHandlers = wrapActions(this.dispatchAction, this.getActionCreators());
    let storeName = this.getStoreName();
    if (storeName != null) {
      this.store = this.props.stores[storeName];
      if (this.store == null) {
        console.warn("View controller " + this.name +
            " has specified a store '" + storeName +
            "' that does not exist.");
      }
      else {
        this.state = this.store.getState();
      }
    }
  }

  componentDidMount() {
    if (this.store != null) {
      this.storeSubscription = this.store.addListener(() => {
        this.setState(this.store.getState());
      });
    }
    this.loadData(this.state, this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.loadData(this.state, nextProps, this.props);
  }

  /**
   * Removes subscription to this controller's store
   */
  componentWillUnmount() {
    if (this.storeSubscription != null) {
      this.storeSubscription.remove();
    }
  }

  /**
   * Provides the context to be passed to this controller's children.  WDK
   * components should never use context to access data, but if components are
   * overridden and need to access data not passed to the replaced component
   * (via props), the context can be used to read store data and dispatch
   * actions to contact the server or load extra data into the store.
   */
  getChildContext() {
    return {
      store: this.store,
      makeDispatchAction: this.props.makeDispatchAction,
      dispatchAction: this.dispatchAction,
      eventHandlers: this.eventHandlers
    };
  }

  /**
   * Renders the page of this controller.  Subclasses may override, but may
   * save effort by overriding renderView() instead.  This method will call that
   * one but only after checking if data required for render has been not yet
   * fully loaded or has erred during loading.
   */
  render() {
    let title = this.getTitle(this.state);
    if (this.isRenderDataLoadError(this.state)) {
      return ( <Doc title={title}><LoadError/></Doc> );
    }
    else if (!this.isRenderDataLoaded(this.state)) {
      return ( <Doc title={title}><Loading/></Doc> );
    }
    else {
      return ( <Doc title={title}>{this.renderView(this.state, this.eventHandlers)}</Doc> );
    }
  }
}

WdkViewController.childContextTypes = {
  store: PropTypes.object,
  makeDispatchAction: PropTypes.func,
  dispatchAction: PropTypes.func,
  eventHandlers: PropTypes.object
};

export default WdkViewController;
