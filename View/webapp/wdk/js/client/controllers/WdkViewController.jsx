import { Component, PropTypes } from 'react';
import Doc from '../components/Doc';
import LoadError from '../components/LoadError';
import Loading from '../components/Loading';
import { wrappable, wrapActions } from '../utils/componentUtils';

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
   * Returns whether an initial data load error has occurred which would prevent
   * the page from rendering.
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

  /*------------- Methods that should probably not be overridden -------------*/

  /**
   * Registers with this controller's store if it has one and sets initial state
   */
  componentWillMount() {
    this.wrappedEventHandlers = wrapActions(this.props.dispatchAction, this.getActionCreators());
    let storeName = this.getStoreName();
    if (storeName != null) {
      let store = this.props.stores[storeName];
      if (store == null) {
        console.warn("View controller " + this.name +
            " has specified a store '" + storeName +
            "' that does not exist.");
      }
      else {
        this.store = store;
        this.setState(store.getState());
        this.storeSubscription = store.addListener(() => {
          this.setState(store.getState());
        });
      }
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
   * Provides the context to be passed to this controller's children.  WDK
   * components should never use context to access data, but if components are
   * overridden and need to access data not passed to the replaced component
   * (via props), the context can be used to read store data and dispatch
   * actions to contact the server or load extra data into the store.
   */
  getChildContext() {
    return {
      store: this.store,
      dispatchAction: this.props.dispatchAction
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
      return ( <Doc title={title}>{this.renderView(this.state, this.wrappedEventHandlers)}</Doc> );
    }
  }
}

WdkViewController.childContextTypes = {
  store: PropTypes.object,
  dispatchAction: PropTypes.func
};

export default WdkViewController;
