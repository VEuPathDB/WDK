import React from 'react';
import Doc from './Doc';
import Loading from './Loading';
import Record from './Record';
import combineStores from '../utils/combineStores';
import wrappable from '../utils/wrappable';
import ContextMixin from '../utils/contextMixin';
import { makeKey } from '../utils/recordUtils';

let RecordController = React.createClass({

  mixins: [ ContextMixin ],

  getInitialState() {
    return {
      recordClass: null
    };
  },

  componentWillMount() {
    let { recordStore, recordClassStore, questionStore } = this.context.stores;

    this.storeSubscription = combineStores(
      recordStore,
      recordClassStore,
      questionStore,
      ({ records, hiddenCategories, collapsedCategories }, { recordClasses }, { questions }) => {
      let { params, query } = this.props;
      let key = makeKey(params.class, query);
      let { meta, record } = (records[key] || {});
      this.setState({ meta, record, hiddenCategories, collapsedCategories, recordClasses, questions });
    });

    this.fetchRecordDetails(this.props);
  },

  componentWillUnmount() {
    this.storeSubscription.dispose();
    this.fetchSubcription.dispose();
  },

  componentWillReceiveProps(nextProps) {
    this.fetchSubcription.dispose();
    this.fetchRecordDetails(nextProps);
  },

  fetchRecordDetails(props) {
    let { recordClassStore } = this.context.stores;
    let { recordActions } = this.context.actions;
    let { params, query } = props;

    // Subscribe to recordClassStore to get attributes and tables for record's
    // recordClass, which will be used to fetch details.
    this.fetchSubcription = recordClassStore.subscribe(value => {
      let recordClass = value.recordClasses.find(r => r.fullName === params.class);
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

      recordActions.fetchRecordDetails(params.class, recordSpec);
    });
  },

  render() {
    if (this.state == null || this.state.record == null) return <Loading/>;

    let { record, recordClass } = this.state;

    return (
      <Doc title={`${recordClass.displayName} ${record.displayName}`}>
        <Record {...this.state} recordActions={this.context.actions.recordActions}/>
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
