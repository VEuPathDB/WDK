import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import * as DownloadFormActionCreators from '../actioncreators/DownloadFormActionCreators';
import DownloadFormContainer from '../components/DownloadFormContainer';

class DownloadFormController extends WdkViewController {

  getStoreName() {
    return "DownloadFormStore";
  }

  getActionCreators() {
    return DownloadFormActionCreators;
  }

  isRenderDataLoaded(state) {
    return (state.step != null && !state.isLoading);
  }

  getTitle(state) {
    return (!this.isRenderDataLoaded(state) ? "Loading..." :
      "Download: " + state.step.displayName);
  }

  renderView(state, eventHandlers) {
    // build props object to pass to form component
    let formProps = Object.assign({}, state, state.globalData, eventHandlers, {
      // passing summary view in case reporters handle view links differently
      summaryView: this.props.location.query && this.props.location.query.summaryView
    });
    return ( <DownloadFormContainer {...formProps}/> );
  }

  loadData(actionCreators, state, props) {
    // must reinitialize with every new props
    let { params } = props.match;
    if ('stepId' in params) {
      actionCreators.loadPageDataFromStepId(params.stepId);
    }
    else if ('recordClass' in params) {
      actionCreators.loadPageDataFromRecord(
          params.recordClass, params.primaryKey.split('/').join(','));
    }
    else {
      console.error("Neither stepId nor recordClass param was passed " +
          "to StepDownloadFormController component");
    }
  }
}

export default wrappable(DownloadFormController);
