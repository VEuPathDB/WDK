import WdkViewController from './WdkViewController';
import { wrappable } from '../utils/componentUtils';
import {isEqual} from 'lodash';
import {
  loadAnswer,
  updateFilter,
  moveColumn,
  changeAttributes,
  sort
} from '../actioncreators/AnswerViewActionCreators';
import Answer from '../components/Answer';
import Loading from '../components/Loading';

class AnswerController extends WdkViewController {

  getStoreName() {
    return "AnswerViewStore";
  }

  getActionCreators() {
    return {
      loadAnswer,
      updateFilter,
      moveColumn,
      changeAttributes,
      sort
    };
  }

  loadData(actionCreators, state, props) {
    // incoming values from the router
    let { question, recordClass: recordClassName } = props.params;
    let [ , questionName, customName ] = question.match(/([^:]+):?(.*)/);
    let parameters = props.location.query;

    // decide whether new answer needs to be loaded (may not need to be loaded
    //   if user goes someplace else and hits 'back' to here- store already correct)
    if (state.question == null ||
        state.question.urlSegment !== questionName ||
        !isEqual(state.parameters, parameters)) {

      // (re)initialize the page
      let pagination = { numRecords: 1000, offset: 0 };
      let sorting = [{ attributeName: 'primary_key', direction: 'ASC' }];
      let displayInfo = { pagination, sorting, customName };
      let opts = { displayInfo, parameters };
      actionCreators.loadAnswer(questionName, recordClassName, opts);
    }
  }

  isRenderDataLoaded(state) {
    return state.records != null;
  }

  isRenderDataLoadError(state) {
    return state.error != null;
  }

  getTitle(state) {
    return state.error ? 'Error loading results'
         : state.records ? state.displayInfo.customName || state.question.displayName
         : 'Loading...';
  }

  renderLoading(state) {
    return state.isLoading && <Loading/>;
  }

  renderAnswer(state, eventHandlers) {
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
        onSort={eventHandlers.sort}
        onMoveColumn={eventHandlers.moveColumn}
        onChangeColumns={eventHandlers.changeAttributes}
        onFilter={eventHandlers.updateFilter}
      />
    );
  }

  renderView(state, eventHandlers) {
    return (
      <div>
        {this.renderLoading(state)}
        {this.renderAnswer(state, eventHandlers)}
      </div>
    );
  }

}

export default wrappable(AnswerController);
