import { parse } from 'querystring';
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

  isRenderDataLoadError(state) {
    return state.error
      && state.error.status !== 403
      && state.error.status !== 404;
  }

  isRenderDataNotFound(state) {
    return state.error
      && state.error.status === 404;
  }

  isRenderDataPermissionDenied(state) {
    return state.error
      && state.error.status === 403;
  }

  getTitle(state) {
    return (!this.isRenderDataLoaded(state) ? "Loading..." :
      "Download: " + state.step.displayName);
  }

  renderView(state, eventHandlers) {
    // build props object to pass to form component
    let formProps = Object.assign({}, state, state.globalData, eventHandlers, {
      // passing summary view in case reporters handle view links differently
      summaryView: parse(this.props.location.search.slice(1)).summaryView
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
