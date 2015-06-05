import {
  AppLoading,
  AppError,
  AnswerAdded,
  AnswerMoveColumn,
  AnswerChangeAttributes,
  AnswerFilter,
  AnswerLoading
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
 *     var action = LoadingAnswerAction({ answer: responseData });
 *     action.type === APP_LOADING; // returns true
 *     action.type = 'some evil string'; // throws Error
 *
 *
 * Read more about Records here http://facebook.github.io/immutable-js/docs/#/Record
 */

// XXX Would it make sense to use Records for ActionTypes? This would have the
// nice effect of enforcing a data type for an action payload. It bears more
// thought as it might be in opposition to some underlying Flux ideology.


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

function createActions({ dispatcher, service }) {

  return {
    /**
     * Retrieve's an Answer resource and dispatches an action with the resource.
     *
     * Actions dispatched:
     *
     *   - LoadingAction
     *   - AnswerLoadSuccessAction
     *   - ErrorAction
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

      // Dispatch AnswerLoading action
      dispatcher.dispatch(AnswerLoading({ isLoading: true }));

      // Then, create a Promise for the answer resource.
      service.postResource('/answer', requestData)
        .then(answer => {
          var answerAction = AnswerAdded({
            requestData: requestData,
            answer: answer
          });
          dispatcher.dispatch(answerAction);
          dispatcher.dispatch(AnswerLoading({ isLoading: false }));
        }, error => {
          var action = AppError({ error: error });
          dispatcher.dispatch(action);
          dispatcher.dispatch(AnswerLoading({ isLoading: false }));
        })
        // Catch errors caused by Store callbacks.
        // This is a last-ditch effort to alert developers that there was an error
        // with how a Store handled the action.
        .catch(err => console.assert(false, err));
    },

    moveColumn(columnName, newPosition) {
      console.assert(typeof columnName === "string", `columnName ${columnName} should be a string.`);
      console.assert(typeof newPosition === "number", `newPosition ${newPosition} should be a number.`);

      var action = AnswerMoveColumn({
        columnName: columnName,
        newPosition: newPosition
      });

      dispatcher.dispatch(action);
    },

    changeAttributes(attributes) {
      // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Iteration_protocols
      console.assert(attributes[Symbol.iterator], `attributes ${attributes} should be iterable.`);

      var action = AnswerChangeAttributes({
        attributes: attributes
      });

      dispatcher.dispatch(action);
    },

    filterAnswer(questionName, terms) {
      var action = AnswerFilter({
        questionName: questionName,
        terms: terms
      });
      dispatcher.dispatch(action);
    }

  };
}

export default { createActions };
