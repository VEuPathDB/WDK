// TODO Break this into two stores: Answers and UI
import {
  assign,
  curry,
  flattenDeep,
  indexBy,
  property,
  values
} from 'lodash';
import createStore from '../utils/createStore';
import {
  ANSWER_LOAD_SUCCESS,
  ANSWER_MOVE_COLUMN,
  ANSWER_CHANGE_ATTRIBUTES,
  ANSWER_FILTER
} from '../ActionType';

/**
 * This module is exporting a store class (not an instance).
 *
 * `new Store(...)` is returning a class. The class constructor is called during
 * runtime to create a singleton instance. An instance of this store can be
 * retreived by a Controller View  using the `lookup` function that is passed to
 * it via `props`, e.g.:
 *
 *     // Some Controller View
 *     ...
 *
 *     componentDidMount() {
 *       var customStore = this.props.lookup(CustomStore);
 *       // do stuff with customStore...
 *     }
 *
 *     ...
 *
 *
 * The class will contain three methods and one property:
 *
 *   - subscribe(callback)   // register a funtion to be called when emitChange is called
 *   - unsubscribe(callback) // unregister a function
 *   - getState()            // get the current state of the store
 *
 *
 * The spec object passed to Store is used to define the behavior of this store
 * when an action is dispatched. All methods on the spec object are called with
 * the spec object as the receiver, so `this` in methods will refer to the spec
 * object. This makes it possible to factor out action handlers into specific
 * methods on your spec object.
 *
 *
 * This store retains the UI state for the AnswerPage, including the current
 * Answer resource being displayed.. UI state includes things like loading
 * state, error state, table sorting options, etc.
 */


// Split terms on whitespace, unless wrapped in quotes
var parseSearchTerms = function parseSearchTerms(terms) {
  var match = terms.match(/\w+|"[\w\s]*"/g) || [];
  return match.map(function(term) {
    // remove wrapping quotes from phrases
    return term.replace(/(^")|("$)/g, '');
  });
};


// Search record for a term.
//
// The approach here is pretty basic and probably ineffecient:
//   - Convert all attribute values to an array of values.
//   - Convert all table values to a flat array of values.
//   - Combine the above arrays into a single array.
//   - Join the array with a control character.
//   - Search the resulting string for the index of 'term'.
//   - return (index !== -1).
//
// There is much room for performance tuning here.
var isTermInRecord = curry(function isTermInRecord(term, record) {
  var attributeValues = values(record.attributes).map(property('value'));

  var tableValues = flattenDeep(values(record.tables)
    .map(function(table) {
      return table.rows.map(function(row) {
        return row.map(property('value'))
      });
    }));

  var clob = attributeValues.concat(tableValues).join('\0');

  return clob.toLowerCase().indexOf(term.toLowerCase()) !== -1;
});

export default createStore({

  init() {
    this.state = {
      filterTerm: '',
      filteredRecords: null,
      answers: {},
      displayInfo: {
        sorting: null,
        pagination: null,
        attributes: null,
        tables: null
      },
      questionDefinition: {
        questionName: null,
        params: null,
        filters: null
      }
    };
  },

  /**
   * Handle dispatched actions. Hopefully most of this is self explanatory.
   *
   * `action` is the action that is being dispatched. It is a plain JavaScript
   * object.
   *
   * `emitChange` is a function that, when called, will call any registered
   * callback functions via the `subscribe` method.
   */
  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case ANSWER_LOAD_SUCCESS:
        this.handleAnswerLoadSuccess(action, emitChange);
        break;

      case ANSWER_MOVE_COLUMN:
        this.handleAnswerMoveColumn(action, emitChange);
        break;

      case ANSWER_CHANGE_ATTRIBUTES:
        this.handleAnswerChangeAttributes(action, emitChange);
        break;

      case ANSWER_FILTER:
        this.handleAnswerFilter(action, emitChange);
        break;
    }
  },

  handleAnswerLoadSuccess(action, emitChange) {
    /* Answer resource */
    // answer = {
    //   meta,
    //   records: [{ id, attributes, tables }]
    // }
    var answer = action.answer;

    /*
     * requestData is an object with the keys `displayInfo` and
     * `questionDefinition`. We will be merging these keys into `state`
     * below.
     */
    var requestData = action.requestData;
    var questionName = requestData.questionDefinition.questionName;
    var previousQuestionName = this.state.questionDefinition.questionName;

    /*
     * If state.displayInfo.attributes isn't defined we want to use the
     * defaults. For now, we will just show whatever is in
     * answer.meta.attributes by default. This is probably wrong.
     * We probably also want to persist the user's choice somehow. Using
     * localStorage is one possble solution.
     */
    if (!requestData.displayInfo.visibleAttributes || previousQuestionName !== questionName) {
      requestData.displayInfo.visibleAttributes = answer.meta.summaryAttributes.map(attrName => {
        return answer.meta.attributes.find(attr => attr.name === attrName);
      });
    }

    answer.meta.attributes = answer.meta.attributes
      .filter(attr => attr.name != 'wdk_weight');

    // For each record, attributes should be an object-map indexed by attribute name
    answer.records.forEach(function(record) {
      record.attributes = indexBy(record.attributes, 'name');
      record.tables = indexBy(record.tables, 'name');
    });

    /*
     * This will update the keys `filteredRecords`, `displayInfo`, and
     * `questionDefinition` in `this.state`.
     */
    assign(this.state, {
      filteredRecords: answer.records,
      displayInfo: requestData.displayInfo,
      questionDefinition: requestData.questionDefinition
    });

    this.state.answers[questionName] = answer;

    emitChange();
  },

  handleAnswerMoveColumn(action, emitChange) {
    /* The name of the attribute being moved. */
    /* FIXME Should be attributeName */
    var columnName = action.columnName;

    /* The new position for the attribute */
    var newPosition = action.newPosition;

    /* list of attributes we will be altering */
    var attributes = this.state.displayInfo.visibleAttributes;

    /* The current position of the attribute being moved */
    var currentPosition = attributes.findIndex(function(attribute) {
      return attribute.get('name') === columnName;
    });

    /* The attribute being moved */
    var attribute = attributes[currentPosition];

    attributes
      // remove attribute from array
      .splice(currentPosition, 1)
      // then, insert into new position
      .splice(newPosition, 0, attribute);

    emitChange();
  },

  handleAnswerChangeAttributes(action, emitChange) {
    this.state.displayInfo.visibleAttributes = action.attributes;
    emitChange();
  },

  handleAnswerFilter(action, emitChange) {
    var terms = action.terms;
    var questionName = action.questionName;
    var parsedTerms = parseSearchTerms(terms);
    var records = this.state.answers[questionName].records;
    var filteredRecords = parsedTerms.reduce(function(records, term) {
      return records.filter(isTermInRecord(term));
    }, records);

    assign(this.state, {
      filterTerm: terms,
      filteredRecords: filteredRecords
    });

    emitChange();
  },

  getState() {
    return this.state;
  }

});
