import {PropTypes} from 'react';
import WdkViewController from './WdkViewController';
import { wrappable } from '../utils/componentUtils';
import {isEqual} from 'lodash';
import {
  loadAnswer,
  updateFilter,
  moveColumn,
  changeAttributes
} from '../actioncreators/AnswerViewActionCreator';
import Answer from '../components/Answer';

class AnswerController extends WdkViewController {

  constructor(props) {
    super(props);
    this.onSort = this.onSort.bind(this);
  }
  
  getStoreName() {
    return "AnswerViewStore";
  }

  getActionCreators() {
    return {
      updateFilter,
      moveColumn,
      changeAttributes
    };
  }

  isRenderDataLoaded(state) {
    return (state.records != null);
  }

  getTitle(state) {
    return (state.question == null ? "Loading..." : state.question.displayName);
  }

  renderView(state, eventHandlers) {
    let {
      meta,
      records,
      displayInfo,
      allAttributes,
      visibleAttributes,
      filterTerm,
      filterAttributes,
      filterTables,
      question,
      recordClass
    } = state;

    if (filterAttributes.length === 0 && filterTables.length === 0) {
      filterAttributes = recordClass.attributes.map(a => a.name);
      filterTables = recordClass.tables.map(t => t.name);
    }
    return (
      <Answer
        meta={meta}
        records={records}
        question={question}
        recordClass={recordClass}
        displayInfo={displayInfo}
        allAttributes={allAttributes}
        visibleAttributes={visibleAttributes}
        filterTerm={filterTerm}
        filterAttributes={filterAttributes}
        filterTables={filterTables}
        format="table"
        onSort={this.onSort}
        onMoveColumn={eventHandlers.moveColumn}
        onChangeColumns={eventHandlers.changeAttributes}
        onFilter={eventHandlers.updateFilter}
      />
    );
  }

  // Update the sorting of the Answer resource.  To do so, we must reload the
  // answer with new sorting parameters. TODO: update user preference
  onSort(attribute, direction) {

    // use the current question and recordclass URL segments
    let questionName = this.state.question.urlSegment;
    let recordClassName = this.state.recordClass.urlSegment;

    // create the new sort from params
    let attributeName = attribute.name;
    let newSort = { attributeName, direction };

    // create a new array by removing existing sort def for attribute
    // and adding the new sort def to the beginning of the array, only
    // retaining the last three defs
    let retainedSorting = this.state.displayInfo.sorting
      .filter(spec => spec.attributeName !== attributeName)
      .slice(0, 2);
    let sorting = [newSort].concat(retainedSorting);

    // construct new opts from old, overriding old sorting with new
    let displayInfo = Object.assign({}, this.state.displayInfo, { sorting });
    let parameters = this.state.parameters;
    let opts = { displayInfo, parameters };

    this.props.dispatchAction(loadAnswer(questionName, recordClassName, opts));
  }

  componentDidMount() {
    this.handleNewProps(this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.handleNewProps(nextProps);
  }

  handleNewProps(props) {
    // incoming values from the router
    let questionName = props.params.question;
    let recordClassName = props.params.recordClass;
    let parameters = props.location.query;

    // decide whether new answer needs to be loaded
    // TODO: isn't this all the time?
    if (this.state.question == null ||
        this.state.question.urlSegment !== questionName ||
        !isEqual(this.state.parameters, parameters)) {

      // (re)initialize the page
      let pagination = { numRecords: 1000, offset: 0 };
      let sorting = [{ attributeName: 'primary_key', direction: 'ASC' }];
      let displayInfo = { pagination, sorting };
      let opts = { displayInfo, parameters };
      props.dispatchAction(loadAnswer(questionName, recordClassName, opts));
    }
  }
}

export default wrappable(AnswerController);
