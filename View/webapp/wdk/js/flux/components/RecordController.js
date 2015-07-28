import React from 'react';
import RecordClassStore from '../stores/recordClassStore';
import RecordStore from '../stores/recordStore';
import RecordActions from '../actions/recordActions';
import Doc from './Doc';
import Loading from './Loading';
import Record from './Record';
import combineStores from '../utils/combineStores';
import wrappable from '../utils/wrappable';

let RecordController = React.createClass({

  contextTypes: {
    application: React.PropTypes.object.isRequired
  },

  getInitialState() {
    return {
      recordClass: null
    };
  },

  componentWillMount() {
    let { application } = this.context;
    let recordStore = application.getStore(RecordStore);

    this.storeSubscription = recordStore.subscribe(({ records, hiddenCategories }) => {
      let { params, query } = this.props;
      let key = RecordStore.makeKey(params.class, query);
      let { meta, record } = (records[key] || {});
      this.setState({ meta, record, hiddenCategories });
    });

    this.fetchRecordDetails(this.props);
  },

  componentWillUnmount() {
    this.storeSubscription.dispose();
  },

  componentWillReceiveProps(nextProps) {
    this.fetchRecordDetails(nextProps);
  },

  fetchRecordDetails(props) {
    let { application } = this.context;
    let { params, query } = props;
    let recordClassStore = application.getStore(RecordClassStore);
    let { fetchRecordDetails } = application.getActions(RecordActions);

    // Subscribe to recordClassStore to get attributes and tables for record's
    // recordClass, which will be used to fetch details. Then, dispose the
    // subscription immediately. If we were using RxJS observables, we could
    // rewrite this using the .last() operator, without expilicity calling
    // dispose:
    //
    //     recordClassStore.last().subscribe( ... );
    //
    let subscription = recordClassStore.subscribe(({ recordClasses }) => {
      let recordClass = recordClasses.find(r => r.fullName === params.class);
      let attributes = recordClass.attributes.map(a => a.name);
      let tables = recordClass.tables.map(t => t.name);
      let recordSpec = {
        primaryKey: query,
        attributes,
        tables
      };

      // update the recordClass if it changes
      if (this.state.recordClass !== recordClass) {
        this.setState({ recordClass });
      }

      fetchRecordDetails(params.class, recordSpec);

      subscription.dispose();
    });

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
