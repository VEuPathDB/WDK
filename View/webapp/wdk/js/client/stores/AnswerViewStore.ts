import WdkStore, { BaseState } from './WdkStore';
import { filterRecords } from '../utils/recordUtils';
import {Answer, Question, RecordInstance, RecordClass, AttributeField} from '../utils/WdkModel';
import {
  AnswerOptions,
  LoadingAction,
  AddedAction,
  ErrorAction,
  TableSortedAction,
  AttributesChangedAction,
  ColumnMovedAction,
  TableFilteredAction
} from '../actioncreators/AnswerViewActionCreators';

type Action = LoadingAction
            | AddedAction
            | ErrorAction
            | TableSortedAction
            | AttributesChangedAction
            | ColumnMovedAction
            | TableFilteredAction;

export type FilterState = {
  filterTerm: string;
  filterAttributes: string[];
  filterTables: string[];
}

export type State = BaseState & Answer & AnswerOptions & FilterState & {
  question: Question;
  recordClass: RecordClass;
  allAttributes: AttributeField[];
  visibleAttributes: AttributeField[];
  unfilteredRecords: RecordInstance[];
  isLoading: boolean;
}

export default class AnswerViewStore extends WdkStore<State> {

  getInitialState() {
    return Object.assign({
      meta: undefined,                // Object: meta object from last service response
      records: undefined,             // Record[]: filtered records
      question: undefined,            // Object: question for this answer page
      recordClass: undefined,         // Object: record class for this answer page
      parameters: undefined,          // Object: parameters used for answer
      allAttributes: undefined,       // Attrib[]: all attributes available in the answer (from recordclass and question)
      visibleAttributes: undefined,   // String[]: ordered list of attributes currently being displayed
      unfilteredRecords: undefined,   // Record[]: list of records from last service response
      isLoading: false,               // boolean: whether to show loading icon
      filterTerm: '',                 // String: value user typed into filter box
      filterAttributes: [],           // Attrib[]: list of attributes whose text is searched during filtering -- FIXME handle undefined in Components
      filterTables: [],               // Table[]: list of tables whose text is searched during filtering      -- FIXME handle undefined in Components
      displayInfo: {                  // Object: answer formatting object passed on answer request
        sorting: undefined,
        pagination: undefined,
        attributes: undefined,
        tables: undefined,
        customName: undefined
      }
    }, super.getInitialState());
  }

  handleAction(state: State, action: Action) {
    switch(action.type) {
      case 'answer/loading': return { ...state, isLoading: true, error: undefined };
      case 'answer/error': return { ...this.getInitialState(), ...action.payload.error };
      case 'answer/added': return addAnswer(state, action.payload);
      case 'answer/attributes-changed': return updateVisibleAttributes(state, action.payload);
      case 'answer/sorting-updated': return updateSorting(state, action.payload);
      case 'answer/column-moved': return moveTableColumn(state, action.payload);
      case 'answer/filtered': return updateFilter(state, action.payload);
      default: return state;
    }
  }
}

function addAnswer( state: State, payload: AddedAction["payload"] ) {
  let { answer, displayInfo, question, recordClass, parameters } = payload;

  // in case we used a magic string to get attributes, reset fetched attributes in displayInfo
  displayInfo.attributes = answer.meta.attributes;

  // need to filter wdk_weight from multiple places;
  let isNotWeight = (attr: string | AttributeField) =>
    typeof attr === 'string' ? attr != 'wdk_weight' : attr.name != 'wdk_weight';

  // collect attributes from recordClass and question
  let allAttributes = recordClass.attributes.concat(question.dynamicAttributes).filter(isNotWeight);

  // use previously selected visible attributes unless they don't exist
  let visibleAttributes = state.visibleAttributes;
  if (!visibleAttributes || state.meta.recordClassName !== answer.meta.recordClassName) {
    // need to populate attribute details for visible attributes
    visibleAttributes = allAttributes
      .filter(attr => isNotWeight(attr) && question.defaultAttributes.includes(attr.name));
  }

  // Remove search weight from answer meta since it doens't apply to non-Step answers
  answer.meta.attributes = answer.meta.attributes.filter(isNotWeight);

  /*
   * This will update the keys `filteredRecords`, and `answerSpec` in `state`.
   */
  return Object.assign({}, state, {
    meta: answer.meta,
    question,
    recordClass,
    parameters,
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
function moveTableColumn(state: State, { columnName, newPosition }: ColumnMovedAction["payload"]) {
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

  return Object.assign({}, state, { visibleAttributes: attributes });
}

function updateVisibleAttributes(state: State, { attributes }: AttributesChangedAction["payload"]) {
  // Create a new copy of visibleAttributes
  let visibleAttributes = attributes.slice(0);

  // Create a new copy of state
  return Object.assign({}, state, { visibleAttributes });
}

function updateSorting(state: State, { sorting }: TableSortedAction["payload"]) {
  return Object.assign({}, state, {
    displayInfo: Object.assign({}, state.displayInfo, { sorting })
  });
}

function updateFilter(state: State, payload: TableFilteredAction["payload"]) {
  let filterSpec = {
    filterTerm: payload.terms,
    filterAttributes: payload.attributes || [],
    filterTables: payload.tables || []
  };
  return Object.assign({}, state, filterSpec, {
    records: filterRecords(state.unfilteredRecords, filterSpec)
  });
}
