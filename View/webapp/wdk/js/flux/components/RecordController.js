import React from 'react';
import RecordStore from '../stores/recordStore';
import QuestionStore from '../stores/questionStore';
import RecordClassStore from '../stores/recordClassStore';
import RecordActions from '../actions/recordActions';
import combineStores from '../utils/combineStores';
import Doc from './Doc';
import Loading from './Loading';
import Record from './Record';
import wrappable from '../utils/wrappable';

let RecordController = React.createClass({

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
    this.setState({
      meta: null,
      record: null
    });
    this.disposeSubscriptions();
    this.subscribeToStores();
  },

  componentWillUpdate(nextProps, nextState) {
    if (nextState.record == null) {
      let { application } = this.context;
      let { params, query } = nextProps;
      let { recordClass } = nextState;
      let { fetchRecordDetails } = application.getActions(RecordActions);
      let attributes = recordClass.attributes.map(a => a.name);
      let tables = recordClass.tables.map(t => t.name);
      let recordSpec = {
        primaryKey: query,
        attributes,
        tables
      };
      fetchRecordDetails(params.class, recordSpec);
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
      ({ records, hiddenCategories }, { questions }, { recordClasses }) => {
        let { params, query } = this.props;
        let key = RecordStore.makeKey(params.class, query);
        let { meta, record } = (records[key] || {});
        let recordClass = recordClasses.find(rc => rc.fullName == params.class);
        this.setState({ meta, record, questions, recordClass, recordClasses, hiddenCategories });
      }
    );
  },

  disposeSubscriptions() {
    this.storeSubscription.dispose();
  },

  render() {
    if (this.state == null || this.state.record == null) return <Loading/>;

    let recordActions = this.context.application.getActions(RecordActions);
    let { record, recordClass } = this.state;

    return (
      <Doc title={`${recordClass.displayName} ${record.displayName}`}>
        <Record {...this.state} recordActions={recordActions}/>
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

export default wrappable(RecordController);
