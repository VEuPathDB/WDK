/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import { RouteHandler } from 'react-router';
import ContextMixin from '../utils/contextMixin';
import wrappable from '../utils/wrappable';
import CommonActions from '../actions/commonActions';
import PreferenceActions from '../actions/preferenceActions';

let { contextTypes } = ContextMixin;

/*
 * RouterHandler is a special React component that the router uses to inject
 * matching child routes. For example, when the answer route is matched,
 * AnswerPage will be rendered as a child of RouteHandler.
 */

let AppController = React.createClass({

  propTypes: {
    store: contextTypes.store
  },

  mixins: [ React.addons.PureRenderMixin ],

  componentWillMount() {
    let { store } = this.props;

    store.dispatch(PreferenceActions.loadPreferences());
    store.dispatch(CommonActions.fetchCommonData());
    this.selectState(store.getState());
    this.storeSubscription = store.subscribe(this.selectState);
  },

  componentWillUnmount() {
    this.storeSubscription.dispose();
  },

  selectState(state) {
    this.setState({ errors: state.errors });
  },

  render() {
    let { errors } = this.state;

    if (errors.length > 0) {
      return (
        <div>
          <h3>An Unexpected Error Occurred</h3>
          <div className="wdkAnswerError">{errors}</div>
        </div>
      );
    }
    else {
      return (
        <RouteHandler store={this.props.store}/>
      );
    }
  }

});

export default wrappable(AppController);
