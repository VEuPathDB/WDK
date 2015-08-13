import React from 'react';
import mapValues from 'lodash/object/mapValues';
import Doc from './Doc';
import Loading from './Loading';
import Record from './Record';
import RecordActions from '../actions/recordActions';
import wrappable from '../utils/wrappable';
import ContextMixin from '../utils/contextMixin';
import { makeKey } from '../utils/recordUtils';

let RecordController = React.createClass({

  mixins: [ ContextMixin ],

  componentWillMount() {
    let { dispatch, subscribe } = this.context;

    this.recordActions = mapValues(RecordActions, function(action) {
      return function dispatchWrapper(...args) {
        return dispatch(action(...args));
      };
    });

    this.storeSubscription = subscribe(state => {
      let { params, query } = this.props;
      let key = makeKey(params.class, query);
      let {
        record: { records, hiddenCategories, collapsedCategories },
        recordClasses,
        questions
      } = state;
      let { meta, record } = (records[key] || {});
      let recordClass = recordClasses.find(r => r.fullName === params.class);
      this.setState({ meta, record, hiddenCategories, collapsedCategories, recordClass, recordClasses, questions });
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
    let { params, query } = props;
    this.recordActions.fetchRecord(params.class, query);
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

function makeRecordSpecFromProps(props) {
  let { query, params } = props;
  return {
    class: params.class,
    primaryKey: query
  };
}

export default wrappable(RecordController);
