// TODO Break this into two stores: Answers and UI
import curry from 'lodash/function/curry';
import Immutable from 'immutable';
import createStore from '../utils/createStore';
import * as ActionType from '../ActionType';

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
 *
 * This store is maintaining its internal state using an immutable data
 * structure (with the aid of the library Immutable.js
 * http://facebook.github.io/immutable-js/). The state will be initialized
 * with default values. It's type is Immutable.Map. See
 * http://facebook.github.io/immutable-js/docs/#/Map for method details. We
 * will mainly be using merge(), which accepts plain JavaScript objects, in
 * addition to any Immutable iterable data type. In our case, we will be
 * merging plain JavaScript objects. This operation will copy the keys of the
 * object to same-named keys of the Map. Any nested objects or arrays will be
 * recursively converteed to a Map or List, resp.
 *
 * Currently, getState() will return a plain-old JavaScript object using
 * the toJS() method provided by Immutate.js. This does a deep traversal
 * of state and converts all iterables to the JS alternative (e.g.,
 * Map -> Object, List -> Array, etc).
 */


// Split terms on whitespace, unless wrapped in quotes
var parseSearchTerms = function parseSearchTerms(terms) {
  var match = terms.match(/\w+|"[\w\s]*"/g) || [];
  return match.map(function(term) {
    // remove wrapping quotes from phrases
    return term.replace(/(^")|("$)/g, '');
  });
}


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
  var attributeValues = record
    .get('attributes').toList()
    .map(function(attribute) {
      return attribute.get('value');
    });

  var tableValues = record
    .get('tables').toList()
    .flatMap(function(table) {
      return table
        .get('rows')
        .flatMap(function(row) {
          return row.map(function(attribute) {
            return attribute.get('value');
          });
        });
    });

  var clob = attributeValues.concat(tableValues).join('\0');

  return clob.toLowerCase().indexOf(term.toLowerCase()) !== -1;
});

var state = Immutable.fromJS({
  isLoading: false,
  error: null,
  filterTerm: '',
  filteredRecords: [],
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
});

export default createStore({

  /**
   * The state of the store. We're using Immutable.js here, but not to its
   * fullest extent. This is sort of a trial run. So far, the interface is very
   * intuitive. -dmf
   */
  state: state,

  /** Used to roll back on loading errors */
  previousState: state,

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

      case ActionType.ANSWER_LOADING:
        this.handleAnswerLoading(action, emitChange);
        break;

      case ActionType.ANSWER_LOAD_SUCCESS:
        this.handleAnswerLoadSuccess(action, emitChange);
        break;

      case ActionType.ANSWER_LOAD_ERROR:
        this.handleAnswerLoadError(action, emitChange);
        break;

      case ActionType.ANSWER_MOVE_COLUMN:
        this.handleAnswerMoveColumn(action, emitChange);
        break;

      case ActionType.ANSWER_CHANGE_ATTRIBUTES:
        this.handleAnswerChangeAttributes(action, emitChange);
        break;

      case ActionType.ANSWER_FILTER:
        this.handleAnswerFilter(action, emitChange);
        break;
    }
  },

  handleAnswerLoading(action, emitChange) {
    var questionName = action.requestData.questionDefinition.questionName;

    /*
     * If the loading answer is for a different question, we want to remove
     * the current question. We will save it in case we have an error so that
     * we can roll back the state of the store.
     *
     * `state.getIn(...)` is another way to write
     * `state.get('questionDefinition').get('questionName')`, but without
     * creating intermediate copies.
     */
    if(this.state.getIn(['questionDefinition', 'questionName']) !== questionName) {
      /*
       * Cache previous state. It will be used to replace the state
       * if we handle ANSWER_LOAD_ERROR.
       */
      this.previousState = this.state;

      /*
       * Clear the current state. This helps keep the UI more consistent
       * by not showing unrelated results when loading.
       */
      this.state = this.state.merge({
        answer: {},
        questionDefinition: {},
        displayInfo: {}
      });
    }

    /*
     * Finally, set isLoading to true and error to null.
     */
    this.state = this.state.merge({
      isLoading: true,
      error: null
    });

    /*
     * This will cause subscribed functions to be called.
     */
    emitChange();
  },

  handleAnswerLoadSuccess(action, emitChange) {
    /* Answer resource */
    // answer = {
    //   meta,
    //   records: [{
    //     id, attributes, tables
    //   }]
    // }
    var answer = action.answer;

    /*
     * requestData is an object with the keys `displayInfo` and
     * `questionDefinition`. We will be merging these keys into `state`
     * below.
     */
    var requestData = action.requestData;
    var questionName = requestData.questionDefinition.questionName;
    var previousQuestionName = this.previousState.getIn([
      'questionDefinition',
      'questionName'
    ]);

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

    // For each record, attributes should be a map indexed by attribute name
    var records$ = Immutable.List().withMutations(list => {
      answer.records.forEach(record => {

        var id = record.id;

        var attributes = Immutable.Map().withMutations(map => {
          record.attributes.forEach(attribute => {
            map.set(attribute.name, Immutable.fromJS(attribute));
          });
        });

        var tables = Immutable.Map().withMutations(map => {
          record.tables.forEach(table => {
            map.set(table.name, Immutable.fromJS(table));
          });
        });

        list.push(Immutable.Map({ id, attributes, tables }));
      });
    });

    /*
     * This will update the keys 'isLoading', 'answer', 'displayInfo',
     * and 'questionDefinition' in `state`. `displayInfo` and
     * `questionDefinition` are defined on `requestData`.
     */
    this.state = this.state.merge({
      isLoading: false,
      answers: {
        [questionName]: { records: records$, meta: answer.meta }
      },
      filteredRecords: records$
    }, requestData);

    emitChange();
  },

  handleAnswerLoadError(action, emitChange) {
    /* rollback to the previous state, and add the error message */
    this.state = this.previousState.merge({
      error: action.error
    });

    emitChange();
  },

  handleAnswerMoveColumn(action, emitChange) {
    /* The name of the attribute being moved. */
    /* FIXME Should be attributeName */
    var columnName = action.columnName;

    /* The new position for the attribute */
    var newPosition = action.newPosition;

    /* used with setIn http://facebook.github.io/immutable-js/docs/#/Map/setIn */
    var keyPath = [ 'displayInfo', 'visibleAttributes' ];

    /* list of attributes we will be altering */
    var attributes = this.state.getIn(keyPath);

    /* The current position of the attribute being moved */
    var currentPosition = attributes.findIndex(function(attribute) {
      return attribute.get('name') === columnName;
    });

    /* The attribute being moved */
    var attribute = attributes.get(currentPosition);

    /* Make a temporary copy of attributes with the one being moved removed */
    var newAttributes = attributes.delete(currentPosition);

    /* Splice the attribute being moved into the new position */
    newAttributes = newAttributes.splice(newPosition, 0, attribute);

    /* Set the attributes to the new list */
    this.state = this.state.setIn(keyPath, newAttributes);

    emitChange();
  },

  handleAnswerChangeAttributes(action, emitChange) {
    /*
     * Create new Immutable list of attribtues. We have to use fromJS so
     * that we create Immutable data structures for nested attribute keys.
     */
    // var newList = Immutable.fromJS(action.attributes);

    /* set state.displayInfo.attributes to the new list */
    this.state = this.state.setIn(['displayInfo', 'visibleAttributes'], action.attributes);

    emitChange();
  },

  handleAnswerFilter(action, emitChange) {
    this.state = this.state.withMutations(function(state) {
      var terms = action.terms;
      var questionName = action.questionName;
      var parsedTerms = parseSearchTerms(terms);
      var records = state.getIn(['answers', questionName, 'records']);
      var filteredRecords = parsedTerms.reduce(function(records, term) {
        return records.filter(isTermInRecord(term));
      }, records);

      state.set('filterTerms', terms);
      state.set('filteredRecords', filteredRecords);
    });
    emitChange();
  },

  getState() {

    /*
     * Convert `state` to a plain JavaScript object. At some point, we should
     * explore returning `state` as-is. This will require updating any modules
     * that work with the store (AnswerPage, etc).
     */
    return this.state;
  }

});
