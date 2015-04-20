// Import modules
import _ from 'lodash';
import React from 'react';
import Router from 'react-router';
import combineStores from '../utils/combineStores';
import Loading from './Loading';
import Answer from './Answer';
import Doc from './Doc';
import Record from './Record';


// Answer is a React component which acts as a Controller-View, as well as
// a Route Handler.
//
// A Controller-View is a Flux concept in which a component can register a
// callback function with one or more Stores, and can call ActionCreators. The
// primary role of the Controller-View is to set up some state for a tree of
// React components. The Controller-View will pass objects to child components
// as `props`. Typically, these child components are reusable in the sense that
// they do not communcate directly with Stores; communication with these
// components happens solely through `props`, either as data or as callbacks.
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
//      screen for authentication. When this happens, the URL will also change.
//
// The router is initialized in the file ../main.js with something like:
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
// The primary responsibility of the Answer component is to call an
// ActionCreator to load the Answer resource from the REST service, and to
// update the page when the state of the AnswerStore has changed. The Answer
// component will determine what Answer to load based on the URL parameters.

// Define the React Component class.
// See http://facebook.github.io/react/docs/top-level-api.html#react.createclass
const AnswerController = React.createClass({

  // `propTypes` is a place to declare properties this component expects. The
  // mapping is property name => type. You can additionally declare a property
  // as "required". When the props passed to a component violate this
  // declaration, a warning is logged to the console, but React will render
  // anyway.
  //
  // NB, these warnings don't appear in non-development builds.
  propTypes: {

    // The application context used to look up services.
    application: React.PropTypes.object.isRequired
  },

  // When the component first mounts, fetch the answer.
  componentWillMount() {
    this.router = this.props.application.getRouter();
    this.fetchAnswer(this.props);
    this.subscribeToStores();
  },

  // This is called anytime the component gets new props, just before they are
  // actually passed to the component instance. In our case, this is when any
  // part of the URL changes. We will first check if a new answer resource needs
  // to be fetched. If not, then we will check if the filter needs to be updated.
  componentWillReceiveProps(nextProps) {
    // current query and params
    const { query, params } = this.props;

    // incoming query and params
    const { query: nextQuery, params: nextParams } = nextProps;

    // query keys to compare to determine if we need to fetch a new answer
    const queryKeys = [ 'sortBy', 'sortDir', 'numrecs', 'offset' ];

    // _.pick will create an object with keys from queryKeys, and values from
    // the source object (query and nextQuery).
    const answerQuery = _.pick(query, queryKeys);
    const nextAnswerQuery = _.pick(nextQuery, queryKeys);

    // fetch answer if the query has changed, or if the question name has changed
    if (!_.isEqual(answerQuery, nextAnswerQuery) || params.questionName != nextParams.questionName) {
      this.fetchAnswer(nextProps);
    }

    // filter answer if the filter terms have changed
    else if (query.filterTerm != nextQuery.filterTerm) {
      this.props.application.getActions('answerActions')
      .filterAnswer(nextParams.questionName, nextQuery.filterTerm);
    }

  },


  componentWillUnmount() {
    this.disposeSubscriptions();
  },

  // Create subscriptions to stores.
  subscribeToStores() {
    const { questionName } = this.props.params;
    const { getStore } = this.props.application;

    const answerStore = getStore('answerStore');
    const questionStore = getStore('questionStore');
    const recordClassStore = getStore('recordClassStore');

    this.subscription = combineStores(
      answerStore,
      questionStore,
      recordClassStore,
      (aState, qState, rState) => {
        const answer = aState.answers[questionName];
        const { displayInfo } = aState;
        const { filterTerm } = aState;
        const { filteredRecords } = aState;
        const { questions } = qState;
        const question = questions.find(q => q.name === questionName);
        const { recordClasses } = rState;
        const recordClass = recordClasses.find(r => r.fullName == question.class);

        this.setState({
          answer,
          displayInfo,
          filterTerm,
          filteredRecords,
          question,
          questions,
          recordClass,
          recordClasses
        });
      }
    );
  },


  disposeSubscriptions() {
    this.subscription.dispose();
  },


  // `fetchAnswer` will call the `loadAnswer` action creator. If either
  // `query.numrecs` or `query.offset` is not set, we will replace the current
  // URL by setting the query params to some default values. Otherwise, we will
  // call the `loadAnswer` action creator based on the `params` and `query`
  // objects.
  fetchAnswer(props) {

    // props.params and props.query are passed to this component by the Router.
    const path = 'answer';
    const params = props.params;
    const query = props.query;

    if (!query.numrecs || !query.offset) {
      // Replace the current undefined URL query params with default values
      Object.assign(query, {
        numrecs: query.numrecs || 1000,
        offset: query.offset || 0
      });

      // This method is provided by the `Router.Navigation` mixin. It replaces
      // the current URL, without adding an entry to the browser history. This
      // call will cause the Route Handler for 'answer' (this component) to be
      // rendered again. Since `query.numrecs` and `query.offset` are now set,
      // the else block below will get executed again.
      this.router.replaceWith(path, params, query);

    } else {

      // Get pagination info from `query`
      const pagination = {
        numRecords: Number(query.numrecs),
        offset: Number(query.offset)
      };

      // Get sorting info from `query`
      // FIXME make this one query param: sorting={attributeName}__{direction}
      const sorting = query.sortBy && query.sortDir
        ? [{
            attributeName: query.sortBy,
            direction: query.sortDir
          }]
        : this.state && this.state.displayInfo.sorting;

      // Combine `pagination` and `sorting` into a single object:
      //
      //     const displayInfo = {
      //       pagination: pagination,
      //       sorting: sorting
      //     };
      //
      const displayInfo = {
        pagination,
        sorting,
        visibleAttributes: this.state && this.state.displayInfo.visibleAttributes
      };

      // TODO Add params to loadAnswer call
      const answerParams = wrap(query.param).map(p => {
        const parts = p.split('__');
        return { name: parts[0], value: parts[1] };
      });

      const opts = {
        displayInfo,
        params: answerParams
      };

      // Call the AnswerCreator to fetch the Answer resource
      this.props.application.getActions('answerActions')
      .loadAnswer(params.questionName, opts);
    }
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
    onSort(attribute, direction) {

      // Update the query object with the new values.
      // See https://lodash.com/docs#assign
      const query = Object.assign({}, this.props.query, {
        sortBy: attribute.name,
        sortDir: direction
      });

      // This method is provided by the `Router.Navigation` mixin. It will
      // update the URL, which will trigger a new Route event, which will cause
      // this `this.componentWillReceiveProps` to be called, which will cause
      // this component to call `this.fetchAnswer()` with the sorting
      // configuration.
      this.router.replaceWith('answer', this.props.params, query);

      // This is an alternative way, which is to call loadAnswer.
      // The appeal of the above is that if the user clicks the browser refresh
      // button, they won't lose their sorting. We can also acheive that by
      // saving the display info to localStorage and reloading it when the page
      // is reloaded.
      //
      // const opts = {
      //   displayInfo: {
      //     sorting: [ { columnName: attribute.name, direction } ],
      //     attributes,
      //     pagination: {
      //       offset: 0,
      //       numRecords: 100
      //     }
      //   }
      // };
      // AnswerActions.loadAnswer(questionName, opts);
    },

    // Call the `moveColumn` action creator. This will cause the state of
    // the answer store to be updated. That will cause the state of this
    // component to be updated, which will cause the `render` method to be
    // called.
    onMoveColumn(columnName, newPosition) {
      this.props.application.getActions('answerActions')
      .moveColumn(columnName, newPosition);
    },

    // Call the `changeAttributes` action creator. This will cause the state of
    // the answer store to be updated. That will cause the state of this
    // component to be updated, which will cause the `render` method to be
    // called.
    onChangeColumns(attributes) {
      this.props.application.getActions('answerActions')
      .changeAttributes(attributes);
    },

    // This is a stub... yet to be completed
    onNewPage(offset, numRecords) {
      console.log(offset, numRecords);
    },

    xonRecordClick(record) {
      const path = 'answer';
      const params = this.props.params;
      const query = this.props.query;

      // update query with format and position
      query.format = 'list';
      query.position = _.indexOf(this.state.answer.records, record);

      // Method provided by Router.Navigation mixin
      this.transitionTo(path, params, query);
    },

    // FIXME This will be removed when the record service is serving up records
    onRecordClick(record) {
      const path = 'answer';
      const records = this.state.answer.records;

      // update query with format and position
      const query = Object.assign({}, this.props.query, {
        expandedRecord: records.findIndex(r => r === record)
      });

      // Method provided by Router.Navigation mixin
      this.transitionTo(path, this.props.params, query);
    },

    onToggleFormat() {
      const path = 'answer';
      const params = this.props.params;
      const query = this.props.query;

      // update query with format and position
      query.format = !query.format || query.format === 'table'
        ? 'list' : 'table';

      // Method provided by Router.Navigation mixin
      this.transitionTo(path, params, query);
    },

    recordHrefGetter(record) {
      const path = 'answer';
      const records = this.state.answer.records;

      // update query with format and position
      const query = Object.assign({}, this.props.query, {
        expandedRecord: records.indexOf(record)
      });

      // Method provided by Router.Navigation mixin
      return this.router.makeHref(path, this.props.params, query);
    },

    onFilter(terms) {
      const query = Object.assign({}, this.props.query, { filterTerm: terms });
      this.transitionTo('answer', this.props.params, query);
    }

  },

  // `render` is called when React.renderComponent is first invoked, and anytime
  // `props` or `state` changes. This latter will happen when any stores are
  // changed.
  //
  // TODO - Explain what's happening here in more detail.
  render() {

    if (this.state == null) return null;

    // use "destructuring" syntax to assign this.props.params.questionName to questionName
    const {
      answer,
      question,
      questions,
      recordClass,
      recordClasses,
      displayInfo,
      filterTerm,
      filteredRecords
    } = this.state;

    const { getCellRenderer, getRecordComponent } = this.props.application;

    // Bind methods of `this.answerEvents` to `this`. When they are called by
    // child elements, any reference to `this` in the methods will refer to
    // this component.
    const answerEvents = Object.keys(this.answerEvents)
      .reduce((events, key) => {
        events[key] = this.answerEvents[key].bind(this);
        return events;
      }, {});

    // Valid formats are 'table' and 'list'
    // TODO validation
    const format = this.props.query.format || 'table';

    const expandedRecord = this.props.query.expandedRecord;

    // List position of "selected" record (this is used to keep the same
    // record at the top when transitioning between formats.
    const position = Number(this.props.query.position) || 0;

    // `{...this.state}` is JSX short-hand syntax to pass each key-value pair of
    // this.state as a property to the component. It intentionally resembles
    // the JavaScript spread operator.
    //
    // See http://facebook.github.io/react/docs/transferring-props.html#transferring-with-...-in-jsx
    // and https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Spread_operator
    //
    // to understand the embedded XML, see: https://facebook.github.io/react/docs/jsx-in-depth.html

    // FIXME This will be removed when the record service is serving up records
    if (answer && expandedRecord != null) {
      const RecordComponent = getRecordComponent(answer.meta.class, Record)
        || Record;
      const record = answer.records[expandedRecord];

      return (
        <Doc title={`${recordClass.displayName}: ${record.id}`}>
          <RecordComponent
            record={record}
            questions={questions}
            recordClass={recordClass}
            recordClasses={recordClasses}
            attributes={answer.meta.attributes}
          />
        </Doc>
      );
    }

    else if (answer && question && recordClass) {
      return (
        <Doc title={`${question.displayName}`}>
          <Answer
            answer={answer}
            question={question}
            recordClass={recordClass}
            displayInfo={displayInfo}
            filterTerm={filterTerm}
            filteredRecords={filteredRecords}
            format={format}
            answerEvents={answerEvents}
            getCellRenderer={getCellRenderer}
          />
        </Doc>
      );
    }

    return <Loading/>;
  }

});

// Export the React Component class we just created.
export default AnswerController;


/**
 * wrap - Wrap `value` in array.
 *
 * If `value` is undefined, return an empty aray.
 * Else, if `value` is an array, return it.
 * Otherwise, return `[value]`.
 *
 * @param  {any} value The value to wrap
 * @return {array}
 */
function wrap(value) {
  if (typeof value === 'undefined') return [];
  if (!Array.isArray(value)) return [ value ];
  return value;
}
