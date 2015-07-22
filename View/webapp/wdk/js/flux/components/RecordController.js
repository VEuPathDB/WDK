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
    this.disposeSubscriptions();
    this.subscribeToStores();
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
        let { hiddenCategories } = recordState;
        let { questions } = questionState;
        let { recordClasses } = recordClassState;

        if (recordData != null) {
          let { meta, record } = recordData;
          this.setState({ meta, record, questions, recordClasses, hiddenCategories });
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
    let primaryKey = Object.keys(query).map(function(name) {
      return { name, value: query[name] };
    });
    let recordSpec = {
      primaryKey,
      attributes,
      tables
    };
    let recordActions = application.getActions(RecordActions);
    recordActions.fetchRecordDetails(params.class, recordSpec);
  },

  render() {
    if (this.state == null) return <Loading/>;

    let { meta, record, recordClasses, questions, hiddenCategories } = this.state;
    let recordActions = this.context.application.getActions(RecordActions);
    let recordClass = recordClasses.find(rc => rc.fullName === meta.class);
    let recordProps = { meta, record, recordClass, recordClasses, questions, recordActions, hiddenCategories };

    return (
      <Doc title={`${recordClass.displayName} ${record.attributes.primary_key.value}`}>
        <Record {...recordProps}/>
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
