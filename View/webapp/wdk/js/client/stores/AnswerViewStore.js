import {ReduceStore} from 'flux/utils';
import {filterRecords} from '../utils/recordUtils';
import AnswerViewActionCreator from '../actioncreators/AnswerViewActionCreator';

let {
  ANSWER_ADDED,
  ANSWER_CHANGE_ATTRIBUTES,
  ANSWER_LOADING,
  ANSWER_MOVE_COLUMN,
  ANSWER_UPDATE_FILTER,
  APP_ERROR
} = AnswerViewActionCreator.actionTypes;

export default class AnswerViewStore extends ReduceStore {

  getInitialState() {
    return {
      meta: null,
      records: null,
      question: null,
      recordClass: null,
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
  }

  reduce(state, { type, payload }) {
    switch(type) {
      case ANSWER_ADDED:
        return addAnswer(state, payload);

      case ANSWER_CHANGE_ATTRIBUTES:
        return updateVisibleAttributes(state, payload);

      case ANSWER_LOADING:
        return answerLoading(state, { isLoading: true });

      case ANSWER_MOVE_COLUMN:
        return moveTableColumn(state, payload);

      case ANSWER_UPDATE_FILTER:
        return updateFilter(state, payload);

      case APP_ERROR:
        return answerLoading(state, { isLoading: false });

      default:
        return state;
    }
  }

}

  function addAnswer(state, payload) {
    let { answer, displayInfo } = payload;

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
      question: payload.question,
      recordClass: payload.recordClass,
      unfilteredRecords: answer.records,
      records: filterRecords(answer.records, state),
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
    attributes.splice(currentPosition, 1);

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

  function updateFilter(state, payload) {
    let filterSpec = {
      filterTerm: payload.terms,
      filterAttributes: payload.attributes || [],
      filterTables: payload.tables || []
    };
    return Object.assign({}, state, filterSpec, {
      records: filterRecords(state.unfilteredRecords, filterSpec)
    });
  }

  function answerLoading(state, payload) {
    return Object.assign({}, state, { isLoading: payload.isLoading });
  }
