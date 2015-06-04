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
      this.disposeSubscriptions();
      this.subscribeToStores();
    }
  },

  subscribeToStores() {
    let { application } = this.context;
    let recordStore = application.getStore(RecordStore);
    let questionStore = application.getStore(QuestionStore);
    let recordClassStore = application.getStore(RecordClassStore);
    let hasFullRecord = false;

    this.storeSubscription = combineStores(
      recordStore,
      questionStore,
      recordClassStore,
      (recordState, questionState, recordClassState) => {
        let { params, query } = this.props;
        let key = RecordStore.makeKey(params.class, query);
        let recordData = recordState.records[key];
        let { questions } = questionState;
        let { recordClasses } = recordClassState;

        if (recordData != null) {
          let { meta, record } = recordData;
          this.setState({ meta, record, questions, recordClasses });
        }

        // get full record
        if (!hasFullRecord) {
          let recordClass = recordClasses.find(rc => rc.fullName == params.class);
          let attributes = recordClass.attributes.map(a => a.name);
          let tables = recordClass.tables.map(t => t.name);
          this.fetchRecordDetails(attributes, tables);
          hasFullRecord = true;
        }
      }
    );
  },

  disposeSubscriptions() {
    this.storeSubscription.dispose();
  },

  fetchRecordDetails(attributes, tables) {
    let { application } = this.context;
    let { params, query } = this.props;
    let recordActions = application.getActions(RecordActions);
    let recordSpec = {
      primaryKey: query,
      attributes,
      tables
    };
    recordActions.fetchRecordDetails(params.class, recordSpec);
  },

  render() {
    if (this.state == null) return <Loading/>;

    let { meta, record, recordClasses, questions } = this.state;
    let RecordComponent = this.context.application.getRecordComponent(meta.class, Record) || Record;
    let recordClass = recordClasses.find(recordClass => recordClass.fullName == meta.class);

    return (
      <Doc title={`${recordClass.displayName}: ${record.attributes.primary_key}`}>
        <RecordComponent meta={meta} record={record} recordClasses={recordClasses} questions={questions}/>
      </Doc>
    );
  }

});

function makeRecordSpecFromProps(props) {
  let { query, params } = props;
  return {
    class: params.class,
    primaryKey: query
  };
}

export default RecoredController;
