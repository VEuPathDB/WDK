import React from 'react';
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
    let { subscribe } = this.context;

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
    this.context.dispatch(RecordActions.fetchRecord(params.class, query));
  },

  render() {
    if (this.state == null || this.state.record == null) return <Loading/>;

    let { record, recordClass } = this.state;

    return (
      <Doc title={`${recordClass.displayName} ${record.displayName}`}>
        <Record {...this.state} recordActions={RecordActions}/>
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
