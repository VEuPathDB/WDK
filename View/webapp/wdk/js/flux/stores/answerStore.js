// TODO Break this into two stores: Answers and UI
import {
  assign,
  curry,
  flattenDeep,
  indexBy,
  property,
  values
} from 'lodash';
import Store from '../Store';
import {
  AnswerAdded,
  AnswerMoveColumn,
  AnswerChangeAttributes,
  AnswerFilter
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


export default class AnswerStore extends Store {

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

    this.handleAction(AnswerAdded, this.addAnswer);
    this.handleAction(AnswerMoveColumn, this.moveTableColumn);
    this.handleAction(AnswerChangeAttributes, this.updateVisibleAttributes);
    this.handleAction(AnswerFilter, this.filterAnswer);
  }

  /**
   * answer = {
   *   meta,
   *   records: [{ id, attributes, tables }]
   * }
   *
   * requestData = {
   *   displayInfo,
   *   questionDefinition
   * }
   *
   * requestData is an object with the keys `displayInfo` and
   * `questionDefinition`. We will be merging these keys into `state`
   * below.
   */
  addAnswer({ answer, requestData }) {
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
  }

  /**
   * Update the position of an attribute in the answer table.
   *
   * @param {string} columnName The name of the atribute being moved.
   * @param {number} newPosition The 0-based index to move the attribute to.
   */
  moveTableColumn({ columnName, newPosition }) {
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
  }

  updateVisibleAttributes({ attributes }) {
    this.state.displayInfo.visibleAttributes = attributes;
  }

  /**
   * Filter the results of an answer. The filtered results are stored in a
   * separate property.
   *
   * @param {string} terms The search phrase.
   * @param {string} questionName The questionName of the answer to filter.
   */
  filterAnswer({ terms, questionName }) {
    var parsedTerms = parseSearchTerms(terms);
    var records = this.state.answers[questionName].records;
    var filteredRecords = parsedTerms.reduce(function(records, term) {
      return records.filter(isTermInRecord(term));
    }, records);

    assign(this.state, {
      filterTerm: terms,
      filteredRecords: filteredRecords
    });
  }

}
