import { cloneElement } from 'react';
import PropTypes from 'prop-types';
import Page from '../components/Page';
import NotFound from '../components/NotFound';
import LoadError from '../components/LoadError';
import Loading from '../components/Loading';
import { wrapActions, PureComponent } from '../utils/componentUtils';

/**
 * Base class for all ViewContoller classes in WDK.
 */
class WdkViewController extends PureComponent {

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
   * Get state from store required for the view being rendered
   */
  getStateFromStore(store) {
    return store.getState();
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
    return undefined;
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
   * Returns whether required data resources are not found.
   */
  isRenderDataNotFound(state) {
    return false;
  }

  /**
   * Returns the title of this page
   */
  getTitle(state) {
    return "WDK";
  }

  /**
   * Renders the highest page component below the Page tag.
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
        this.state = this.getStateFromStore(this.store);
      }
    }
    this.contextElement = makeContextElement({
      store: this.store,
      makeDispatchAction: this.props.makeDispatchAction,
      dispatchAction: this.dispatchAction,
      eventHandlers: this.eventHandlers
    });
  }

  componentDidMount() {
    if (this.store != null) {
      this.storeSubscription = this.store.addListener(() => {
        this.setState(this.getStateFromStore(this.store));
      });
    }
    this.loadData(this.state, this.props, undefined);
  }

  componentWillReceiveProps(nextProps) {
    this.loadData(this.state, nextProps, this.props);
  }

  componentDidUpdate() {
    if (this.isRenderDataLoadError(this.state)) {
      document.title = "Error";
    }
    else if (this.isRenderDataNotFound(this.state)) {
      document.title = "Page not found";
    }
    else if (!this.isRenderDataLoaded(this.state)) {
      document.title = "Loading...";
    }
    else {
      document.title = this.getTitle(this.state);
    }
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
   * Renders the page of this controller.  Subclasses may override, but may
   * save effort by overriding renderView() instead.  This method will call that
   * one but only after checking if data required for render has been not yet
   * fully loaded or has erred during loading.
   */
  render() {
    let page;
    if (this.isRenderDataLoadError(this.state)) {
      page = ( <Page><LoadError/></Page> );
    }
    else if (this.isRenderDataNotFound(this.state)) {
      page = ( <Page><NotFound/></Page> );
    }
    else if (!this.isRenderDataLoaded(this.state)) {
      page = ( <Page><Loading/></Page> );
    }
    else {
      page = ( <Page>{this.renderView(this.state, this.eventHandlers)}</Page> );
    }
    return cloneElement(this.contextElement, null, page);
  }
}

export default WdkViewController;

/**
 * Helper to create the context provider React Element.
 */
function makeContextElement(context) {
  /**
   * Provides the context to be passed to this controller's children.  WDK
   * components should never use context to access data, but if components are
   * overridden and need to access data not passed to the replaced component
   * (via props), the context can be used to read store data and dispatch
   * actions to contact the server or load extra data into the store.
   */
  class WdkViewControllerContext extends PureComponent {
    getChildContext() {
      return context;
    }
    render() {
      return this.props.children;
    }
  }
  // TODO Remove makeDispatchAction and eventHandlers from context.
  WdkViewControllerContext.childContextTypes = {
    store: PropTypes.object,
    makeDispatchAction: PropTypes.func,
    dispatchAction: PropTypes.func,
    eventHandlers: PropTypes.object
  };
  return <WdkViewControllerContext/>;
}
