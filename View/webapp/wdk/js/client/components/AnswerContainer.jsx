import {Component, PropTypes} from 'react';
import isEqual from 'lodash/lang/isEqual';
import {
  updateFilter,
  loadAnswer,
  moveColumn,
  changeAttributes
} from '../actioncreators/AnswerViewActionCreator';
import Answer from './Answer';
import Doc from './Doc';
import Loading from './Loading';

/**
 * Container for Answer page.
 *
 * A Container is essentially what we have been calling a ViewController. The
 * major difference is that a Container doesn't know about the router. This has
 * an impact on the Contaienr's props. When handling props that come from the
 * router, implementation details of the router are exposed and must be handled
 * by the Component. It also ties the Component to a particular URL structure,
 * making it far less reusable.
 *
 * For now, this is mostly exploratory. It is serving an immediate need to
 * resuse this Component on our legacy search pages. Note that we may also
 * want to reuse this Component on the future, Flux-based search page.
 */
class AnswerContainer extends Component {

  constructor(props) {
    super(props);
    let { questionName } = this.props;
    this.store = this.props.stores.AnswerViewStore;
    this.sortingPreferenceKey = 'sorting::' + questionName;

    // bind handler methods
    this.onSort = this.onSort.bind(this);
    this.onMoveColumn = this.onMoveColumn.bind(this);
    this.onChangeColumns = this.onChangeColumns.bind(this);
    this.onFilter = this.onFilter.bind(this);
  }

  componentWillMount() {
    let state = this.store.getState();
    if (state.question == null || state.question.urlSegment !== this.props.questionName || !isEqual(state.parameters, this.props.parameters)) {
      this.fetchAnswer(this.props);
    }
    else {
      this.setState(this.store.getState());
    }
    this.storeSubscription = this.store.addListener(() => {
      this.setState(this.store.getState());
    });
  }

  componentWillReceiveProps(nextProps) {
    this.fetchAnswer(nextProps);
  }

  componentWillUnmount() {
    this.storeSubscription.remove();
  }

  fetchAnswer(props) {
    let { questionName, recordClassName, parameters } = props;
    let pagination = { numRecords: 1000, offset: 0 };
    let sorting = [{ attributeName: 'primary_key', direction: 'ASC' }];
    let displayInfo = { pagination, sorting };
    let opts = { displayInfo, parameters };
    props.dispatchAction(loadAnswer(questionName, recordClassName, opts));
  }

  // Update the sorting of the Answer resource. In this handler, we trigger a
  // call to udpate the sorting by updating the URL via `this.replaceWith`.
  // This will cause `this.componentWillReceiveProps` to be called. See the
  // comment below for an alternative way calling `loadAnswer` directly. Yet
  // another way would be to have a `sortAnswer` action creator.
  onSort(attribute, direction) {
    let attributeName = attribute.name;
    let { questionName, recordClassName, parameters } = this.props;
    let newSort = { attributeName, direction };
    // Create a new array by removing existing sort def for attribute
    // and adding the new sort def to the beginning of the array, only
    // retaining the last three defs.
    let sorting = this.state.displayInfo.sorting.
      filter(spec => spec.attributeName !== attributeName).
      slice(0, 2);

    sorting = [newSort].concat(sorting);

    let displayInfo = Object.assign({}, this.state.displayInfo, { sorting });
    let opts = { displayInfo, parameters };

    this.props.dispatchAction(loadAnswer(questionName, recordClassName, opts));
  }

  // Call the `moveColumn` action creator. This will cause the state of
  // the answer store to be updated. That will cause the state of this
  // component to be updated, which will cause the `render` method to be
  // called.
  onMoveColumn(columnName, newPosition) {
    this.props.dispatchAction(moveColumn(columnName, newPosition));
  }

  // Call the `changeAttributes` action creator. This will cause the state of
  // the answer store to be updated. That will cause the state of this
  // component to be updated, which will cause the `render` method to be
  // called.
  onChangeColumns(attributes) {
    this.props.dispatchAction(changeAttributes(attributes));
  }

  onFilter(terms, attributes, tables) {
    this.props.dispatchAction(updateFilter({ terms, attributes, tables }));
  }

  render() {
    if (this.state == null || this.state.records == null) return <Loading/>;

    let {
      isLoading,
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
    } = this.state;

    if (filterAttributes.length === 0 && filterTables.length === 0) {
      filterAttributes = recordClass.attributes.map(a => a.name);
      filterTables = recordClass.tables.map(t => t.name);
    }
    return (
      <Doc title={question.displayName}>
        {isLoading ? <Loading/> : null}
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
          onMoveColumn={this.onMoveColumn}
          onChangeColumns={this.onChangeColumns}
          onFilter={this.onFilter}
        />
      </Doc>
    );
  }

}

AnswerContainer.propTypes = {
  dispatchAction: PropTypes.func.isRequired,
  stores: PropTypes.object.isRequired,
  questionName: PropTypes.string.isRequired,
  recordClassName: PropTypes.string.isRequired,
  parameters: PropTypes.object
};

export default AnswerContainer;
