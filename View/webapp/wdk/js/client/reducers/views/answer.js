// FIXME Store records as list of IDs
// TODO Break this into two stores: Answers and UI
import {
  ANSWER_ADDED,
  ANSWER_CHANGE_ATTRIBUTES,
  ANSWER_LOADING,
  ANSWER_MOVE_COLUMN,
  ANSWER_UPDATE_FILTER,
  APP_ERROR
} from '../../constants/actionTypes';
import * as RecordUtils from '../../utils/recordUtils';


let initialState = {
  meta: null,
  records: null,
  unfilteredRecords: null,
  isLoading: false,
  filterTerm: '',
  filterAttributes: null,
  filterTables: null,
  displayInfo: {
    sorting: null,
    pagination: null,
    attributes: null,
    tables: null
  }
};

export default function answer(state = initialState, action) {
  switch(action.type) {
    case ANSWER_ADDED: return addAnswer(state, action);
    case ANSWER_CHANGE_ATTRIBUTES: return updateVisibleAttributes(state, action);
    case ANSWER_LOADING: return answerLoading(state, action);
    case ANSWER_MOVE_COLUMN: return moveTableColumn(state, action);
    case ANSWER_UPDATE_FILTER: return updateFilter(state, action);
    case APP_ERROR: return answerLoading(state, action);
    default: return state;
  }
}

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
 *       let customStore = this.props.lookup(CustomStore);
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
function addAnswer(state, action) {
  let answer = action.response;
  let { displayInfo } = action.requestData;

  /*
   * If state.displayInfo.attributes isn't defined we want to use the
   * defaults. For now, we will just show whatever is in
   * answer.meta.attributes by default. This is probably wrong.
   * We probably also want to persist the user's choice somehow. Using
   * localStorage is one possble solution.
   */
  let visibleAttributes = state.displayInfo.visibleAttributes;

  if (!visibleAttributes || state.meta.class !== answer.meta.class) {
    visibleAttributes = answer.meta.summaryAttributes.map(attrName => {
      return answer.meta.attributes.find(attr => attr.name === attrName);
    });
  }

  Object.assign(displayInfo, { visibleAttributes });

  // Remove search weight since it doens't apply to non-Step answers
  answer.meta.attributes = answer.meta.attributes.filter(attr => attr.name != 'wdk_weight');


  /*
   * This will update the keys `filteredRecords`, and `questionDefinition` in `state`.
   */
  return Object.assign({}, state, {
    meta: answer.meta,
    unfilteredRecords: answer.records,
    records: RecordUtils.filterRecords(answer.records, state),
    isLoading: false,
    displayInfo
  });
}

/**
 * Update the position of an attribute in the answer table.
 *
 * @param {string} columnName The name of the atribute being moved.
 * @param {number} newPosition The 0-based index to move the attribute to.
 */
function moveTableColumn(state, { columnName, newPosition }) {
  /* make a copy of list of attributes we will be altering */
  let attributes = [ ...state.displayInfo.visibleAttributes ];

  /* The current position of the attribute being moved */
  let currentPosition = attributes.findIndex(function(attribute) {
    return attribute.name === columnName;
  });

  /* The attribute being moved */
  let attribute = attributes[currentPosition];

  // remove attribute from array
  attributes.splice(currentPosition, 1)

  // then, insert into new position
  attributes.splice(newPosition, 0, attribute);

  return updateVisibleAttributes(state, { attributes });
}

function updateVisibleAttributes(state, { attributes }) {
  // Create a new copy of displayInfo
  let displayInfo = Object.assign({}, state.displayInfo, {
    visibleAttributes: attributes
  });

  // Create a new copy of state
  return Object.assign({}, state, {
    displayInfo
  });
}

function updateFilter(state, action) {
  let filterSpec = {
    filterTerm: action.terms,
    filterAttributes: action.attributes || [],
    filterTables: action.tables || []
  };
  return Object.assign({}, state, filterSpec, {
    records: RecordUtils.filterRecords(state.unfilteredRecords, filterSpec)
  });
}

function answerLoading(state, action) {
  if (action.type === ANSWER_LOADING && state.isLoading === false) {
    return Object.assign({}, state, { isLoading: true });
  } else if (action.type === APP_ERROR && state.isLoading === true) {
    return Object.assign({}, state, { isLoading: false });
  }
  return state;
}
