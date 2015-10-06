import { Component } from 'react';
import mapValues from 'lodash/object/mapValues';
import Doc from './Doc';
import Loading from './Loading';
import RecordUI from './RecordUI';
import * as CommonActions from '../actions/commonActions';
import * as RecordActions from '../actions/recordActions';
import { wrappable } from '../utils/componentUtils';
import { makeKey } from '../utils/recordUtils';

class RecordController extends Component {

  constructor(props) {
    super(props);
    let { store } = props;

    // Given `action`, returns a function that will call `action` with its
    // arguments, and will call `store.dispatch` with the result.
    let bindAction = action => (...args) => store.dispatch(action(...args));

    this.actions = Object.assign(
      mapValues(RecordActions, bindAction),
      mapValues(CommonActions, bindAction)
    );

    this.fetchRecordDetails(props);
    this.selectState(store.getState());
    this.storeSubscription = store.subscribe(state => this.selectState(state));
  }

  componentWillUnmount() {
    this.storeSubscription.dispose();
  }

  componentWillReceiveProps(nextProps) {
    this.fetchRecordDetails(nextProps);
  }

  fetchRecordDetails(props) {
    let { params, query, store } = props;
    let recordClassName = params.class;
    let primaryKey = query;

    Promise.all([
      this.actions.fetchRecordClasses(),
      this.actions.fetchQuestions()
    ]).then(() => {
      let recordClass = store.getState().resources.recordClasses.find(function(recordClass) {
        return recordClass.fullName === recordClassName;
      });
      let attributes = recordClass.attributes.map(a => a.name);
      let tables = recordClass.tables.map(t => t.name);
      let recordSpec = { primaryKey, attributes, tables };
      this.actions.fetchRecordDetails(recordClassName, recordSpec);
    });
  }

  selectState(state) {
    let { params, query } = this.props;
    let key = makeKey(params.class, query);
    let { records, recordClasses, questions } = state.resources;
    let recordClass = recordClasses.find(r => r.fullName === params.class);
    let record = records[key];
    let {
      collapsedCategories = recordClass && recordClass.collapsesCategories || [],
      collapsedTables = recordClass && recordClass.collapsesTables || []
    } = state.views.record;

    this.setState({ collapsedCategories, collapsedTables, recordClass });

    // only update record when it's available
    if (record) {
      this.setState({ record });
    }
  }

  render() {
    if (this.state == null || this.state.record == null) return <Loading/>;

    let { record, recordClass } = this.state;

    return (
      <Doc title={`${recordClass.displayName} ${record.displayName}`}>
        <RecordUI {...this.state} actions={this.actions}/>
      </Doc>
    );
  }

}

export default wrappable(RecordController);
