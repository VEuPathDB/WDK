/**
 * AnswerPage is a React component which acts as a Controller-view.
 *
 * This component is rendered when the URL matches a route rule. The route
 * rules are defined in ../routes.js. The route rules are processed at three
 * distinct times:
 *
 *   1. When the webpage is initially loaded.
 *   2. When the URL is changed due to some user interaction (e.g., clicking a
 *      link).
 *   3. When the route is manually transitioned to.
 *
 * The third case is similar forwarding one action to another based on some
 * set of criteria, such as forwarding to a login screen for authentication.
 *
 * The router is initialized in the file ../../wdk-3.0.js with the lines:
 *
 *     Router.run(appRoutes, (Handler, state) => React.render(
 *       <Handler {...state}/>, document.body
 *     ));
 *
 * The first argument is the set of route rules. The second argument is a
 * function which is called everytime route rules are processed. This function
 * will render `Handler`, which is the component registered for the matched
 * route, and it will pass `state` as a set of props (`this.props` in this
 * component).
 *
 * See https://github.com/rackt/react-router/blob/master/docs/api/run.md#callbackhandler-state
 *
 * The primary responsibility of the AnswerPage is to call an ActionCreator to
 * load the Answer resource from the REST service, and to update the page when
 * the state of the AnswerStore has changed. The AnswerPage will determine what
 * Answer to load based on the URL parameters.
 */

/** Import object/functions from other modules */
import _ from 'lodash';
import React from 'react';
import Router from 'react-router';
import Answer from '../components/Answer';
import AnswerPageStore from '../stores/AnswerPageStore';
import AnswerPageActions from '../actions/AnswerPageActions';
import createStoreMixin from '../mixins/StoreMixin';

/**
 * Export the React component class for AnswerPage.
 *
 * See http://facebook.github.io/react/docs/top-level-api.html#react.createclass
 */
var AnswerPage = React.createClass({

  /**
   * This is used by React to do runtime type-checking of properties passed to
   * a component. If a type doesn't match, React will log a warning in the
   * browser console. These checks are skipped in production builds.
   *
   * In this case, we're using shape for both `query` and `params` to describe
   * what properties each of those objects are expected to have. You might also
   * notice that query.numrecs and query.offset are strings, instead of numbers.
   * This is becuase these values are parsed out from the URL, which is a
   * string. They will be converted to numbers in loadAnswerStore() below.
   *
   * See http://facebook.github.io/react/docs/reusable-components.html#prop-validation
   */
  propTypes: {
    query: React.PropTypes.shape({
      numrecs: React.PropTypes.string,
      offset: React.PropTypes.string
    }),
    params: React.PropTypes.shape({
      questionName: React.PropTypes.string.isRequired
    }).isRequired
  },

  /**
   * mixins are used to share behaviors between otherwise unrelated components.
   * React will use each object in the provided array to attach additional
   * behavior to instances of the component class.
   *
   * The store mixin is used to reduce some boiler-plate which is necessary to
   * update the state of the component based on changes to a store. In this
   * case, we create a store mixin based on the AnswerStore.
   *
   * See http://facebook.github.io/react/docs/component-specs.html#mixins
   */
  mixins: [ createStoreMixin(AnswerPageStore), Router.Navigation ],



  /**
   * loadAnswerPage will call the loadAnswer action creator. Props are
   * receieved from the router. (Typically they are acquired by the router
   * from attributes in the JSX tag that called it.) The props will include the
   * params and query related to the URL.
   *
   * See more at https://github.com/rackt/react-router/blob/master/docs/api/run.md#state
   *
   *
   * This method will be called during two React lifecycle event hooks:
   *
   *   1. componentDidMount
   *   2. componentWillReceiveProps
   *
   * See http://facebook.github.io/react/docs/component-specs.html#mounting-componentdidmount
   */
  loadAnswerPage(props) {
    var { params, query } = props;

    if (!query.numrecs || !query.offset) {
      this.replaceWith('answer', params, _.defaults({
        numrecs: query.numrecs || 100,
        offset: query.offset || 0
      }, query));
    } else {
      var pagination = {
        numRecords: Number(query.numrecs),
        offset: Number(query.offset)
      };
      var sorting = [{
        attributeName: query.sortBy,
        direction: query.sortDir
      }];
      var displayInfo = { pagination, sorting };
      this.actions.loadAnswer(params.questionName, { displayInfo });
    }
  },


  /** See loadAnswerPage() */
  componentDidMount() {
    this.actions = this.props.lookup(AnswerPageActions);
    this.loadAnswerPage(this.props);
  },


  /** See loadAnswerPage() */
  componentWillReceiveProps(newProps) {
    this.loadAnswerPage(newProps);
  },


  /**
   * This is a collection of event handlers that will be passed to the Answer
   * component (which will, in turn, pass these to the RecordTable component.
   * In our render() method, we will bind these methods to the component
   * instance so that we can use `this` as expected. Normally, React will do
   * this for us, but since we are nesting methods within a property, we have
   * to do this ourselves. This is all really simply for brevity in code when
   * we pass these handers to the Answer component. I will also make
   * refactoring a little easier in the future.
   */
  answerEvents: {

    onSort(attribute) {
      var { params, query } = this.props;
      var { displayInfo: { sorting } } = this.state;
      var sortColumn = sorting[0];
      var direction;

      if (sortColumn.attributeName === attribute.name) {
        direction = sortColumn.direction === 'ASC' ? 'DESC' : 'ASC';
      } else {
        direction = 'ASC';
      }

      // This method is provided by the Router.Navigation mixin. It will update
      // the URL, which will trigger a new Route event, which will cause this
      // component to get new props, which will cause this component to call
      // loadAnswer() with the sorting configuration.
      this.replaceWith('answer', params, _.defaults({
        sortBy: attribute.name,
        sortDir: direction
      }, query));

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

    onMoveColumn(columnName, newPosition) {
      this.actions.moveColumn(columnName, newPosition);
    },

    onChangeColumns(attributes) {
      this.actions.changeAttributes(attributes);
    },

    onNewPage(offset, numRecords) {
      console.log(offset, numRecords);
    }

  },

  /**
   * This is called initially by the router, and subsequently any time the
   * AnswerStore emits a change (see StoreMixin(AnswerStore) above).
   *
   * This render method is fairly simple. It will get the question name
   * from the props received from the router (stored in this.props), and assign it to a local variable
   * named `questionName`. It will then display a heading2 element with the
   * question name as its content, and then it will render the Answer component
   * as a child. This component will pass the question name, along with it's own
   * state, as properties to the Answer component.
   */
  render() {

    /* use "destructuring" syntax to assign this.props.params.questionName to questionName */
    var { isLoading, error, answer, displayInfo, questionDefinition: { questionName } } = this.state;

    // Bind methods to `this`
    var answerEvents = _.mapValues(this.answerEvents, event => _.bind(event, this));

    /**
     * {...this.state} is JSX short-hand syntax to pass each key-value pair of
     * this.state as a property to the component. It intentionally resembles
     * the JavaScript spread operator.
     *
     * See http://facebook.github.io/react/docs/transferring-props.html#transferring-with-...-in-jsx
     * and https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Spread_operator
     *
     * to understand the embedded XML, see: https://facebook.github.io/react/docs/jsx-in-depth.html
     */
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

export default AnswerPage;
