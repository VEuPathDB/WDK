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
      meta: null,                // Object: meta object from last service response
      records: null,             // Record[]: filtered records
      question: null,            // Object: question for this answer page
      recordClass: null,         // Object: record class for this answer page
      allAttributes: null,       // Attrib[]: all attributes available in the answer (from recordclass and question)
      visibleAttributes: null,   // String[]: ordered list of attributes currently being displayed
      unfilteredRecords: null,   // Record[]: list of records from last service response
      isLoading: false,          // boolean: whether to show loading icon
      filterTerm: '',            // String: value user typed into filter box
      filterAttributes: null,    // Attrib[]: list of attributes whose text is searched during filtering
      filterTables: null,        // Table[]: list of tables whose text is searched during filtering
      displayInfo: {             // Object: answer formatting object passed on answer request
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
    let { answer, displayInfo, question, recordClass } = payload;

    // in case we used a magic string to get attributes, reset fetched attributes in displayInfo
    displayInfo.attributes = answer.meta.attributes;

    // need to filter wdk_weight from multiple places;
    let isNotWeight = attr => attr != 'wdk_weight' && attr.name != 'wdk_weight';

    // collect attributes from recordClass and question
    let allAttributes = recordClass.attributes.concat(question.dynamicAttributes).filter(isNotWeight);

    // use previously selected visible attributes unless they don't exist
    let visibleAttributes = state.visibleAttributes;
    if (!visibleAttributes || state.meta.recordClass !== answer.meta.recordClass) {
      // need to populate attribute details for visible attributes
      visibleAttributes = answer.meta.attributes.map(attrName => {
        // first try to find attribute in record class
        let value = allAttributes.find(attr => attr.name === attrName);
        if (value === null) {
          // null value is bad, but we expect itRemove search weight from answer
          //   meta since it doens't apply to non-Step answers
          if (isNotWeight({ name: attrName })) {
            console.warn("Attribute name '" + attrName +
                "' does not correspond to a known attribute.  Skipping...");
          }
        }
        return value;
      }).filter(element => element != null); // filter unfound attributes
    }

    // Remove search weight from answer meta since it doens't apply to non-Step answers
    answer.meta.attributes = answer.meta.attributes.filter(isNotWeight);

    /*
     * This will update the keys `filteredRecords`, and `questionDefinition` in `state`.
     */
    return Object.assign({}, state, {
      meta: answer.meta,
      question,
      recordClass,
      allAttributes,
      visibleAttributes,
      unfilteredRecords: answer.records,
      records: filterRecords(answer.records, state),
      isLoading: false,
      displayInfo
    });
  }

  /**
   * Update the position of an attribute in the answer table.
   *
   * @param {string} columnName The name of the attribute being moved.
   * @param {number} newPosition The 0-based index to move the attribute to.
   */
  function moveTableColumn(state, { columnName, newPosition }) {
    /* make a copy of list of attributes we will be altering */
    let attributes = [ ...state.visibleAttributes ];

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
    // Create a new copy of visibleAttributes
    let visibleAttributes = attributes.slice(0);

    // Create a new copy of state
    return Object.assign({}, state, {
      visibleAttributes
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
