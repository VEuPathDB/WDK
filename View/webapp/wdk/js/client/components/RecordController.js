import React from 'react';
import mapValues from 'lodash/object/mapValues';
import Doc from './Doc';
import Loading from './Loading';
import Record from './Record';
import * as CommonActions from '../actions/commonActions';
import * as RecordActions from '../actions/recordActions';
import { wrappable } from '../utils/componentUtils';
import { makeKey } from '../utils/recordUtils';

let RecordController = React.createClass({

  componentWillMount() {
    let { store } = this.props;

    this.recordActions = mapValues(RecordActions, function(action) {
      return function dispatchWrapper(...args) {
        return store.dispatch(action(...args));
      };
    });
    this.commonActions = mapValues(CommonActions, function(action) {
      return function dispatchWrapper(...args) {
        return store.dispatch(action(...args));
      };
    });
    this.fetchRecordDetails(this.props);
    this.selectState(store.getState());
    this.storeSubscription = store.subscribe(this.selectState);
  },

  componentWillUnmount() {
    this.storeSubscription.dispose();
  },

  componentWillReceiveProps(nextProps) {
    this.fetchRecordDetails(nextProps);
  },

  fetchRecordDetails(props) {
    let { params, query, store } = props;
    let recordClassName = params.class;
    let primaryKey = query;

    Promise.all([
      this.commonActions.fetchRecordClasses(),
      this.commonActions.fetchQuestions()
    ]).then(() => {
      let recordClass = store.getState().resources.recordClasses.find(function(recordClass) {
        return recordClass.fullName === recordClassName;
      });
      let attributes = recordClass.attributes.map(a => a.name);
      let tables = recordClass.tables.map(t => t.name);
      let recordSpec = { primaryKey, attributes, tables };
      this.recordActions.fetchRecordDetails(recordClassName, recordSpec);
    });
  },

  selectState(state) {
    let { params, query } = this.props;
    let key = makeKey(params.class, query);
    let { records, recordClasses, questions } = state.resources;
    let { hiddenCategories, collapsedCategories } = state.views.record;
    let recordClass = recordClasses.find(r => r.fullName === params.class);
    let record = records[key];

    this.setState({ hiddenCategories, collapsedCategories, recordClass, recordClasses, questions });

    // only update record when it's available
    if (record) {
      this.setState({ record });
    }
  },

  render() {
    if (this.state == null || this.state.record == null) return <Loading/>;

    let { record, recordClass } = this.state;

    return (
      <Doc title={`${recordClass.displayName} ${record.displayName}`}>
        <Record {...this.state} recordActions={this.recordActions}/>
      </Doc>
    );
  }

});

export default wrappable(RecordController);
