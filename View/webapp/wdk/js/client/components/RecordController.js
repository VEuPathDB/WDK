import { Component } from 'react';
import mapValues from 'lodash/object/mapValues';
import Doc from './Doc';
import Loading from './Loading';
import RecordUI from './RecordUI';
import { wrappable } from '../utils/componentUtils';

class RecordController extends Component {

  constructor(props) {
    super(props);
    this.store = props.stores.RecordViewStore;
    this.actions = props.actionCreators.RecordViewActionCreator;
  }

  componentWillMount() {
    this.storeSubscription = this.store.addListener(() => {
      this.setState(this.store.getState());
    });
    this.fetchRecord(this.props);
  }

  componentWillUnmount() {
    this.storeSubscription.remove();
  }

  componentWillReceiveProps(nextProps) {
    this.fetchRecord(nextProps);
  }

  fetchRecord(props) {
    this.actions.fetchRecordDetails(props.params.class, props.query);
  }

  render() {
    if (this.state == null || this.state.record == null) return <Loading/>;

    let title = this.state.recordClass.displayName + ' ' +
      this.state.record.displayName;

    return (
      <Doc title={title}>
        <RecordUI {...this.state} actions={this.actions}/>
      </Doc>
    );
  }

}

export default wrappable(RecordController);
