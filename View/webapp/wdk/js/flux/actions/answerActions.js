import Immutable from 'immutable';
import createActionCreators from '../utils/createActionCreators';
import {
  ANSWER_LOADING,
  ANSWER_LOAD_SUCCESS,
  ANSWER_LOAD_ERROR,
  ANSWER_MOVE_COLUMN,
  ANSWER_CHANGE_ATTRIBUTES,
  ANSWER_FILTER,
  QUESTION_LIST_LOAD_SUCCESS,
  RECORD_CLASS_LOAD_SUCCESS
} from '../ActionType';


/**
 * Action definitions.
 *
 * Actions are defined using Immutable.Record, which defines
 * a class that enforces a specific set of keys. Immutable.Record itself
 * returns a constructor. The argument passed to Immutable.Record is the
 * definiton of the keys, and default values. When an instance of one of these
 * Records is created, only keys present in the definition will be set, and any
 * keys which are missing will be set to the default values.
 *
 *
 * In practice, a Record instance can be used just like a plain JavaScript
 * object. A key difference is that properties cannot be set directly. E.g.,
 *
 *     var action = new LoadingAnswerAction({ answer: responseData });
 *     action.type === ANSWER_LOADING; // returns true
 *     action.type = 'some evil string'; // throws Error
 *
 *
 * Read more about Records here http://facebook.github.io/immutable-js/docs/#/Record
 */

// XXX Would it make sense to use Records for ActionTypes? This would have the
// nice effect of enforcing a data type for an action payload. It bears more
// thought as it might be in opposition to some underlying Flux ideology.
var Record = Immutable.Record;

var AnswerLoadingAction = new Record({
  type: ANSWER_LOADING,
  requestData: {}
});

var AnswerLoadSuccessAction = new Record({
  type: ANSWER_LOAD_SUCCESS,
  requestData: {},
  answer: {}
});

var AnswerLoadErrorAction = new Record({
  type: ANSWER_LOAD_ERROR,
  requestData: {},
  error: {}
});

var AnswerMoveColumnAction = new Record({
  type: ANSWER_MOVE_COLUMN,
  columnName: '',
  newPosition: -1
});

var AnswerChangeAttributesAction = new Record({
  type: ANSWER_CHANGE_ATTRIBUTES,
  attributes: []
});

var AnswerFilterAction = new Record({
  type: ANSWER_FILTER,
  questionName: null,
  terms: ''
});

var QuestionsLoadSuccessAction = new Record({
  type: QUESTION_LIST_LOAD_SUCCESS,
  questions: null
});

var RecordClassLoadSuccessAction = new Record({
  type: RECORD_CLASS_LOAD_SUCCESS,
  recordClass: null
});


/**
 * ActionCreators
 *
 * ActionCreators define the public interface of a set of actions. An
 * ActionCreator's role is to make calls to external APIs, to create
 * instances of Actions, and to dispatch the Actions on the Dispatcher.
 * One or more Stores will handle the dispatched Action.
 *
 * ActionCreators do not return a value. This is by design. This allows an
 * ActionCreator to do asynchronous work transparently. Once an Action is
 * dispatched, the handling of the Action will be synchronous.
 */

export default createActionCreators({

  /**
   * Retrieve's an Answer resource and dispatches an action with the resource.
   *
   * Actions dispatched:
   *
   *   - AnswerLoadingAction
   *   - AnswerLoadSuccessAction
   *   - AnswerLoadErrorAction
   *
   *
   * Usage:
   *
   *    loadAnswer('GeneRecords.GenesByTaxon', { params: { ... }, filters: { ... }, displayInfo: { ... } });
   *
   *
   * Request data format, POSTed to service:
   *
   *     {
   *       "questionDefinition": {
   *         "questionName": String,
   *         "params": [ {
   *           "name": String, “value”: Any
   *         } ],
   *         "filters": [ {
   *           “name": String, value: Any
   *         } ]
   *       },
   *       displayInfo: {
   *         pagination: { offset: Number, numRecords: Number },
   *         attributes: [ attributeName: String ],
   *         sorting: [ { attributeName: String, direction: Enum[ASC,DESC] } ]
   *       }
   *     }
   *
   * @param {string} questionName Fully qualified WDK Question name.
   * @param {object} opts Addition data to include in request.
   * Options properties of `opts`:
   *   - params: Object of key-value pairs for Question params.
   *   - filters: Object of key-value pairs for Question filters.
   *   - displayInfo: Object with display details (see Request data format below).
   */
  // We will make requests for the following resources:
  // - question
  // - answer
  // - recordClass
  //
  // Once all are loaded, we will dispatch the load action
  loadAnswer(questionName, opts = {}) {
    var dispatch = this.dispatch;
    var { params = [], filters = [], displayInfo } = opts;

    // default values for pagination and sorting
    var defaultPagination= { offset: 0, numRecords: 100 };
    var defaultSorting= [{ attributeName: 'primary_key', direction: 'ASC' }];

    // Set defaults if not defined in `opts`
    displayInfo.pagination = displayInfo.pagination || defaultPagination;
    displayInfo.sorting = displayInfo.sorting || defaultSorting;

    // FIXME Set attributes to whatever we're soring on. This is required by
    // the service, but it doesn't appear to have any effect at this time. I
    // think what we want is for the service to use default attributes defined
    // in the model XML. We also need a way to ask for all attributes (and
    // tables). An alternative is to get the list of available attributes from a
    // preferences service.
    displayInfo.attributes = displayInfo.sorting.map(s => s.attributeName);
    displayInfo.tables = [];

    // Build XHR request data for '/answer'
    var questionDefinition = { questionName, params, filters };
    var requestData = { questionDefinition, displayInfo };

    // Dispatch loading action.
    var action = new AnswerLoadingAction({ requestData: requestData });
    dispatch(action);

    // The next section of code deals with composing Promises. Simply put, a
    // Promise is a container for an asynchronous operation, such as an Ajax
    // request. Promises can be combined in various ways to allow what would
    // otherwise require complex bookkeeping to be expressed in a more
    // declarative way.
    //
    // Ultimately, the code below will be making a request for the question and
    // answer resource in parallel. When the question request is complete, we
    // will then make the request for the recordClass resource. Once these
    // three requests are complete, we will dispatch three related actions.

    // First, create a Promise for the question resource (the ajax request will
    // be made as soon as possible (which will more-or-less be when the current
    // method's execution is complete).
    var questionPromise = this.serviceAPI.getResource('/question?expandQuestions=true');

    // Then, create a Promise for the answer resource.
    var answerPromise = this.serviceAPI.postResource('/answer', requestData);

    // This is the "tricky" part. This code block says, "When the question
    // Promise is fulfilled, create a Promise for the record resource.
    // Then, using `Promise.all`, create yet another Promise that is fulfilled
    // when all three Promises are fulfilled." It takes advantage of two
    // important properties of Promises:
    //
    //   1. `Promise.prototype.then` itself returns a Promise. The value that
    //      Promise is fulfilled with is determined by the return value. If the
    //      value is another Promise, it will fulfill with what ever value that
    //      Promise fulfills with; otherwise it will fulfill with the
    //      non-Promise value.
    //
    //   2. `Promise.all` accepts an array of Promises or values, and returns a
    //      Promise that is fulfilled with the an array whose elements are the
    //      values that each Promise in the array is fulfilled with, or the
    //      non-Promise element of the array.
    //
    //   Thus, `combinedPromise` is a Promise which is fulfilled with the
    //   question resource, the recordClass resource, and the answer resource as
    //   an array.
    var combinedPromise = questionPromise.then(questions => {
      var question = questions.find(question => question.name === questionName);
      var recordClassPromise = this.serviceAPI.getResource('/record/' + question.class);

      // Note that `question` is not a Promise, but is the value with which
      // `questionPromise` was fulfilled. This value will simply be passed to
      // any fulfilled handlers (see this in action below).
      return Promise.all([questions, recordClassPromise, answerPromise]);
    });

    // Finally, we register a callback for when the combinedPromise is
    // fulfilled. We are simply dispatching actions based on the values.
    combinedPromise.then(responses => {
      var [ questions, recordClass, answer ] = responses;

      var questionAction = new QuestionsLoadSuccessAction({ questions });
      dispatch(questionAction);

      var recordClassAction = new RecordClassLoadSuccessAction({ recordClass });
      dispatch(recordClassAction);

      var answerAction = new AnswerLoadSuccessAction({
        requestData: requestData,
        answer: answer
      });
      dispatch(answerAction);
    }, error => {
      var action = new AnswerLoadErrorAction({
        requestData: requestData,
        error: error
      });
      dispatch(action);
    })
    // Catch errors caused by Store callbacks.
    // This is a last-ditch effort to alert developers that there was an error
    // with how a Store handled the action.
    .catch(err => console.assert(false, err));
  },

  moveColumn(columnName, newPosition) {
    console.assert(typeof columnName === "string", `columnName ${columnName} should be a string.`);
    console.assert(typeof newPosition === "number", `newPosition ${newPosition} should be a number.`);

    var action = new AnswerMoveColumnAction({
      columnName: columnName,
      newPosition: newPosition
    });

    this.dispatch(action);
  },

  changeAttributes(attributes) {
    console.assert(Array.isArray(attributes), `attributes ${attributes} should be an array.`);

    var action = new AnswerChangeAttributesAction({
      attributes: attributes
    });

    this.dispatch(action);
  },

  filterAnswer(questionName, terms) {
    var action = new AnswerFilterAction({
      questionName: questionName,
      terms: terms
    });
    this.dispatch(action);
  }

});
