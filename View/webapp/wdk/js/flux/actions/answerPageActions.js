import Immutable from 'immutable';
import createActionCreators from '../utils/createActionCreators';
import {
  ANSWER_LOADING,
  ANSWER_LOAD_SUCCESS,
  ANSWER_LOAD_ERROR,
  ANSWER_MOVE_COLUMN,
  ANSWER_CHANGE_ATTRIBUTES
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

    // Build XHR request data
    var questionDefinition = { questionName, params, filters };
    var requestData = { questionDefinition, displayInfo };

    var action = new AnswerLoadingAction({ requestData: requestData });
    dispatch(action);

    // Call `serviceAPI.postResource` to get the Answer resource. `serviceAPI`
    // is an instance of a configuered serviceAPI object that is injected at
    // runtime.
    //
    // `serviceAPI.postResource` returns a JavaScript `Promise`. The first
    // argument to `.then` is a success handler, in which we dispatch a
    // loadSuccess action. The second argument is an error handler, in which we
    // dispatch a loadError action. The `.catch` method is used to handle any
    // uncaught errors thrown in the success or error handlers.
    this.serviceAPI.postResource('/answer', requestData)
      .then(answer => {
        var action = new AnswerLoadSuccessAction({
          requestData: requestData,
          answer: answer
        });
        dispatch(action);
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
  }

});
