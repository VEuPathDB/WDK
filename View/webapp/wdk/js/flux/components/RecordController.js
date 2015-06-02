import React from 'react';
import isEqual from 'lodash/lang/isEqual';
import RecordStore from '../stores/recordStore';
import QuestionStore from '../stores/questionStore';
import RecordClassStore from '../stores/recordClassStore';
import RecordActions from '../actions/recordActions';
import combineStores from '../utils/combineStores';
import Doc from './Doc';
import Loading from './Loading';
import Record from './Record';

let RecoredController = React.createClass({

  contextTypes: {
    application: React.PropTypes.object.isRequired
  },

  componentWillMount() {
    fetchRecordDetails(this.props, this.context);
    this.subscribeToStores();
  },

  componentWillUnmount() {
    this.disposeSubscriptions();
  },

  componentWillReceiveProps(nextProps) {
    // Check if query or params are different
    let { query, params } = this.props;
    let { query: nextQuery, params: nextParams } = nextProps;
    if (params.class !== nextParams.class || !isEqual(query, nextQuery)) {
      fetchRecordDetails(nextProps, this.context);
    }
  },

  subscribeToStores() {
    let { application } = this.context;
    let recordStore = application.getStore(RecordStore);
    let questionStore = application.getStore(QuestionStore);
    let recordClassStore = application.getStore(RecordClassStore);

    this.storeSubscription = combineStores(
      recordStore,
      questionStore,
      recordClassStore,
      (recordState, questionState, recordClassState) => {
        let { params, query } = this.props;
        let key = RecordStore.makeKey(params.class, query);
        let recordData = recordState.records[key];
        if (recordData != null) {
          let { meta, record } = recordData;
          let { questions } = questionState;
          let { recordClasses } = recordClassState;
          this.setState({ meta, record, questions, recordClasses });
        }
      }
    );
    this.storeSubscription = recordStore.subscribe(state => {
    });
  },

  disposeSubscriptions() {
    this.storeSubscription.dispose();
  },

  render() {
    if (this.state == null) return <Loading/>;

    let { meta, record, recordClasses, questions } = this.state;
    let RecordComponent = this.context.application.getRecordComponent(meta.class, Record) || Record;

    return (
      <Doc title={`${meta.class.displayName}: ${record.id}`}>
        <RecordComponent meta={meta} record={record} recordClasses={recordClasses} questions={questions}/>
      </Doc>
    );
  }

});

function fetchRecordDetails(props, context) {
  let { application } = context;
  let { params, query } = props;
  let recordActions = application.getActions(RecordActions);
  let recordSpec = {
    primaryKey: query,
    attributes: [ 'primary_key' ],
    tables: []
  };
  recordActions.fetchRecordDetails(params.class, recordSpec);
}

function makeRecordSpecFromProps(props) {
  let { query, params } = props;
  return {
    class: params.class,
    primaryKey: query
  };
}

export default RecoredController;
