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
    this.state = this.store.getState();
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
    let { recordClass, splat } = props.params;
    this.actions.fetchRecordDetails(recordClass, splat.split('/'));
  }

  renderLoading() {
    if (this.state.isLoading) {
      return (
        <Loading/>
      );
    }
  }

  renderError() {
    if (this.state.error) {
      return (
        <div style={{padding: '1.5em', fontSize: '2em', color: 'darkred', textAlign: 'center'}}>
          The requested record could not be loaded.
        </div>
      );
    }
  }

  renderRecord() {
    if (this.state.record != null) {
      let title = this.state.recordClass.displayName + ' ' +
        this.state.record.displayName;

      return (
        <Doc title={title}>
          <RecordUI {...this.state} actions={this.actions}/>
        </Doc>
      );
    }
  }

  render() {
    return (
      <div>
        {this.renderLoading()}
        {this.renderError()}
        {this.renderRecord()}
      </div>
    );
  }

}

export default wrappable(RecordController);
