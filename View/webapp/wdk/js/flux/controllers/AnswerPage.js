// Import modules
import React from 'react';
import Router from 'react-router';
import Answer from '../components/Answer';
import createStoreMixin from '../mixins/createStoreMixin';
import createActionCreatorsMixin from '../mixins/createActionCreatorsMixin';

// AnswerPage is a React component which acts as a Controller-View, as well as
// a Route Handler.
//
// A Controller-View is a Flux concept in which a component can register a
// callback function with one or more Stores, and can call ActionCreators. The
// primary role of the Controller-View is to set up some state for a tree of
// React components. The Controller-View will pass objects to child components
// as `props`. Typically, these child components are reusable in the sense that
// they do not communcate directly with Stores.
//
//
// A Route Handler is rendered when the URL matches a route rule. The route
// rules are defined in ../routes.js. Each Route is assigned a Handler, which
// is simply a React component. The route rules are processed at three distinct
// times:
//
//   1. When the webpage is initially loaded.
//   2. When the URL is changed due to some user interaction (e.g., clicking a
//      link).
//   3. When the route is manually transitioned to, via react-router methods.
//      In this component, we make use of this.replaceWith, which is provided
//      by the Router.Navigation mixin. This is similar forwarding one action
//      to another based on some set of criteria, such as forwarding to a login
//      screen for authentication.
//
// The router is initialized in the file ../bootsrap.js with something like:
//
//     Router.run(appRoutes, function handleRoute(Handler, state) {
//       React.render( <Handler {...state}/>, document.body);
//     });
//
// The first argument to Router.run is the set of route rules. The second
// argument is a function which is called everytime route rules are processed.
// This function will render `Handler`, which is the component registered for
// the matched route, and it will pass `state` as a set of props (`this.props`
// in this component). This component is one such Handler.
//
// See https://github.com/rackt/react-router/blob/master/docs/api/run.md#callbackhandler-state
//
//
// The primary responsibility of the AnswerPage is to call an ActionCreator to
// load the Answer resource from the REST service, and to update the page when
// the state of the AnswerStore has changed. The AnswerPage will determine what
// Answer to load based on the URL parameters.

// Define the React Component class.
// See http://facebook.github.io/react/docs/top-level-api.html#react.createclass
var AnswerPage = React.createClass({

  // mixins are used to share behaviors between otherwise unrelated components.
  // React will use each object in the provided array to attach additional
  // behavior to instances of the component class.
  //
  // See http://facebook.github.io/react/docs/component-specs.html#mixins
  mixins: [

    // Registers a callback with the `answerPageStore`. The callback will use
    // `getStateFromStores` (defined below) in the callback. This mixin will also
    // use `getStateFromStores` in `getInitialState`.
    createStoreMixin('answerPageStore'),

    // Adds a property to this component with the same name as the action
    // creators. In this case, `this.answerPageActions`.
    createActionCreatorsMixin('answerPageActions'),

    // Adds methods to handle navigating to other routes. We use
    // `replaceWith()` in this component.
    // See https://github.com/rackt/react-router/blob/master/docs/api/mixins/Navigation.md
    Router.Navigation,

    // Adds methods to get properties of the current state of the Router.
    // We use it to get the current `params` and `query` objects.
    // See https://github.com/rackt/react-router/blob/master/docs/api/mixins/State.md
    Router.State
  ],


  // This is used by the `createStoreMixin` mixin as the return value for
  // `getInitialState` and as the value passed to `setState` when the store's
  // state changes.
  // See http://facebook.github.io/react/docs/component-specs.html#getinitialstate
  // and http://facebook.github.io/react/docs/component-api.html#setstate
  getStateFromStores(stores) {
    return stores.answerPageStore.getState();
  },


  // `fetchAnswer` will call the `loadAnswer` action creator. If either
  // `query.numrecs` or `query.offset` is not set, we will replace the current
  // URL by setting the query params to some default values. Otherwise, we will
  // call the `loadAnswer` action creator based on the `params` and `query`
  // objects.
  fetchAnswer() {

    // These methods are provided by the `Router.State` mixin
    var params = this.getParams();
    var query = this.getQuery();

    if (!query.numrecs || !query.offset) {
      // Replace the current undefined URL query params with default values
      Object.assign(query, {
        numrecs: query.numrecs || 100,
        offset: query.offset || 0
      });

      // This method is provided by the `Router.Navigation` mixin. It replaces
      // the current URL, without adding an entry to the browser history. This
      // call will cause the Route Handler for 'answer' (this component) to be
      // rendered again. Since `query.numrecs` and `query.offset` are now set,
      // the else block below will get executed again.
      this.replaceWith('answer', params, query);

    } else {

      // Get pagination info from `query`
      var pagination = {
        numRecords: Number(query.numrecs),
        offset: Number(query.offset)
      };

      // Get sorting info from `query`
      // FIXME make this one query param: sorting={attributeName}__{direction}
      var sorting = query.sortBy && query.sortDir
        ? [{
            attributeName: query.sortBy,
            direction: query.sortDir
          }]
        : this.state.displayInfo.sorting;

      // Combine `pagination` and `sorting` into a single object:
      //
      //     var displayInfo = {
      //       pagination: pagination,
      //       sorting: sorting
      //     };
      //
      var displayInfo = {
        pagination,
        sorting,
        visibleAttributes: this.state.displayInfo.visibleAttributes
      };

      // Call the AnswerCreator to fetch the Answer resource
      this.answerPageActions.loadAnswer(params.questionName, { displayInfo });
    }
  },


  // When the component first mounts, fetch the answer.
  componentDidMount() {
    this.fetchAnswer();
  },


  // This is called anytime this component is rendered. That includes when we
  // call `this.replaceWith(...)` in `this.fetchAnswer()`.
  componentWillReceiveProps() {
    this.fetchAnswer();
  },


  // This is a collection of event handlers that will be passed to the Answer
  // component (which will, in turn, pass these to the RecordTable component.
  // In our render() method, we will bind these methods to the component
  // instance so that we can use `this` as expected. Normally, React will do
  // this for us, but since we are nesting methods within a property, we have
  // to do this ourselves. This is all really simply for brevity in code when
  // we pass these handers to the Answer component. It will also make
  // refactoring a little easier in the future.
  answerEvents: {

    // Update the sorting of the Answer resource. In this handler, we trigger a
    // call to udpate the sorting by updating the URL via `this.replaceWith`.
    // This will cause `this.componentWillReceiveProps` to be called. See the
    // comment below for an alternative way calling `loadAnswer` directly. Yet
    // another way would be to have a `sortAnswer` action creator.
    onSort(attribute) {

      // These methods are provided by the mixin `Router.State`.
      var params = this.getParams();
      var query = this.getQuery();

      // Create a local var `sorting` using descturing.
      var { displayInfo: { sorting } } = this.state;

      var sortAttribute = sorting[0];
      var direction;

      // Determine the sort direction. If the attribute is the same, then
      // we will reverse the direction... otherwise, we will default to `ASC`.
      if (sortAttribute.attributeName === attribute.name) {
        direction = sortAttribute.direction === 'ASC' ? 'DESC' : 'ASC';
      } else {
        direction = 'ASC';
      }

      // Update the query object with the new values.
      // See https://lodash.com/docs#assign
      Object.assign(query, { sortBy: attribute.name, sortDir: direction });

      // This method is provided by the `Router.Navigation` mixin. It will
      // update the URL, which will trigger a new Route event, which will cause
      // this `this.componentWillReceiveProps` to be called, which will cause
      // this component to call `this.fetchAnswer()` with the sorting
      // configuration.
      this.replaceWith('answer', params, query);

      // This is an alternative way, which is to call loadAnswer.
      // The appeal of the above is that if the user clicks the browser refresh
      // button, they won't lose their sorting. We can also acheive that by
      // saving the display info to localStorage and reloading it when the page
      // is reloaded.
      //
      // var opts = {
      //   displayInfo: {
      //     sorting: [ { columnName: attribute.name, direction } ],
      //     attributes,
      //     pagination: {
      //       offset: 0,
      //       numRecords: 100
      //     }
      //   }
      // };
      // AnswerPageActions.loadAnswer(questionName, opts);
    },

    // Call the `moveColumn` action creator. This will cause the state of
    // the answer store to be updated. That will cause the state of this
    // component to be updated, which will cause the `render` method to be
    // called.
    onMoveColumn(columnName, newPosition) {
      this.answerPageActions.moveColumn(columnName, newPosition);
    },

    // Call the `changeAttributes` action creator. This will cause the state of
    // the answer store to be updated. That will cause the state of this
    // component to be updated, which will cause the `render` method to be
    // called.
    onChangeColumns(attributes) {
      this.answerPageActions.changeAttributes(attributes);
    },

    // This is a stub... yet to be completed
    onNewPage(offset, numRecords) {
      console.log(offset, numRecords);
    }

  },

  // This is called initially by the router, and subsequently any time the
  // AnswerStore emits a change (via the mixin `createStoreMixin`).
  //
  // This render method is fairly simple. It will create several local
  // references to properties of `this.state`. It will then create an H2
  // element with the question name in it, and then it will render the Answer
  // component as a child, passing the local variables as props.
  render() {

    // use "destructuring" syntax to assign this.props.params.questionName to questionName
    var { isLoading, error, answer, displayInfo, questionDefinition: { questionName } } = this.state;

    // Bind methods of `this.answerEvents` to `this`. When they are called by
    // child elements, any reference to `this` in the methods will refer to
    // this component.
    var answerEvents = Object.keys(this.answerEvents)
    .reduce((events, key) => {
      events[key] = this.answerEvents[key].bind(this);
      return events;
    }, {});

    // `{...this.state}` is JSX short-hand syntax to pass each key-value pair of
    // this.state as a property to the component. It intentionally resembles
    // the JavaScript spread operator.
    //
    // See http://facebook.github.io/react/docs/transferring-props.html#transferring-with-...-in-jsx
    // and https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Spread_operator
    //
    // to understand the embedded XML, see: https://facebook.github.io/react/docs/jsx-in-depth.html
    return (
      <div>
        <h2>{questionName}</h2>
        <Answer
          questionName={questionName}
          answer={answer}
          answerEvents={answerEvents}
          isLoading={isLoading}
          error={error}
          displayInfo={displayInfo}/>
      </div>
    );
  }

});

// Export the React Component class we just created.
export default AnswerPage;
