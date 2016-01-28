import ActionCreator from '../utils/ActionCreator';

// Action types
let actionTypes = {
  ANSWER_ADDED: 'answer/added',
  ANSWER_CHANGE_ATTRIBUTES: 'answer/attributes-changed',
  ANSWER_LOADING: 'answer/loading',
  ANSWER_MOVE_COLUMN: 'answer/column-moved',
  ANSWER_UPDATE_FILTER: 'answer/filtered',
  APP_ERROR: 'answer/error'
};

let hasUrlSegment = (urlSegment) => (e) => e.urlSegment === urlSegment;

export default class AnswerViewActionCreator extends ActionCreator {

  /**
   * Retrieve's an Answer resource from the WDK REST Service and dispatches an
   * action with the resource. This uses the restAction helper function
   * (see ../filters/restFilter).
   *
   * Request data format, POSTed to service:
   *
   *     {
   *       "questionDefinition": {
   *         "questionName": String,
   *         "parameters": Object (map of paramName -> paramValue),
   *         "filters": [ {
   *           “name": String, value: Any
   *         } ],
   *         "viewFilters": [ {
   *           “name": String, value: Any
   *         } ]
   *       },
   *       formatting: {
   *         formatConfig: {
   *           pagination: { offset: Number, numRecords: Number },
   *           attributes: [ attributeName: String ],
   *           sorting: [ { attributeName: String, direction: Enum[ASC,DESC] } ]
   *         }
   *       }
   *     }
   *
   * @param {string} questionUrlSegment
   * @param {Object} opts Addition data to include in request.
   * @param {Array<Object>} opts.parameters Array of param spec objects: { name: string; value: any }
   * @param {Array<Object>} opts.filters Array of filter spec objects: { name: string; value: any }
   * @param {Array<Object>} opts.viewFilters Array of view filter  spec objects: { name: string; value: any }
   * @param {Object} opts.displayInfo.pagination Pagination specification.
   * @param {number} opts.displayInfo.pagination.offset 0-based index for first record.
   * @param {number} opts.displayInfo.pagination.numRecord The number of records to include.
   * @param {Array<string>} opts.displayInfo.attributes Array of attribute names to include.
   * @param {Array<Object>} opts.displayInfo.sorting Array of sorting spec objects: { attributeName: string; direction: "ASC" | "DESC" }
   */
  loadAnswer(questionUrlSegment, recordClassUrlSegment, opts = {}) {
    let { parameters = {}, filters = [], displayInfo } = opts;

    // FIXME Set attributes to whatever we're sorting on. This is required by
    // the service, but it doesn't appear to have any effect at this time. We
    // should be passing the attribute in based on info from the RecordClass.
    displayInfo.attributes = "__DISPLAYABLE_ATTRIBUTES__"; // special string for all displayable attributes
    displayInfo.tables = [];

    this._dispatch({ type: actionTypes.ANSWER_LOADING });

    let questionPromise = this._service.findQuestion(hasUrlSegment(questionUrlSegment));
    let recordClassPromise = this._service.findRecordClass(hasUrlSegment(recordClassUrlSegment));
    let answerPromise = questionPromise.then(question => {
      // Build XHR request data for '/answer'
      let questionDefinition = { questionName: question.name, parameters, filters };
      let formatting = { formatConfig: displayInfo };
      return this._service.getAnswer(questionDefinition, formatting);
    });

    Promise.all([ answerPromise, questionPromise, recordClassPromise ])
    .then(([ answer, question, recordClass]) => {
      this._dispatch({
        type: actionTypes.ANSWER_ADDED,
        payload: {
          answer,
          question,
          recordClass,
          displayInfo
        }
      })
    }, error => {
      this._dispatch({
        type: actionTypes.APP_ERROR,
        payload: { error }
      });
      throw error;
    });

  }

  /**
   * Change the position of a column in the answer table.
   *
   * @param {string} columnName The name of the attribute to move.
   * @param {number} newPosition The new 0-based index position of the attribute.
   */
  moveColumn(columnName, newPosition) {
    this._dispatch({
      type: actionTypes.ANSWER_MOVE_COLUMN,
      payload: {
        columnName,
        newPosition
      }
    });
  }

  /**
   * Update the set of visible attributes in the answer table.
   *
   * @param {Array<Object>} attributes The new set of attributes to show in the table.
   */
  changeAttributes(attributes) {
    this._dispatch({
      type: actionTypes.ANSWER_CHANGE_ATTRIBUTES,
      payload: {
        attributes
      }
    });
  }

  /**
   * Set the filter for the answer table.
   *
   * FIXME use a service object to filter the answer.
   *
   * @param {Object} spec The filter specification.
   * @param {string} spec.terms The string to parse and filter.
   * @param {Array<string>} spec.attributes The set of attribute names whose values should be queried.
   * @param {Array<string>} spec.tables The set of table names whose values should be queried.
   */
  updateFilter({ terms, attributes, tables }) {
    this._dispatch({
      type: actionTypes.ANSWER_UPDATE_FILTER,
      payload: {
        terms,
        attributes,
        tables
      }
    });
  }

}

AnswerViewActionCreator.actionTypes = actionTypes;
