import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import { parse } from 'querystring';
import AbstractViewController from './AbstractViewController';
import * as DownloadFormActionCreators from '../actioncreators/DownloadFormActionCreators';
import DownloadFormContainer from '../components/DownloadFormContainer';
import { State, default as DownloadFormStore } from "../stores/DownloadFormStore";

class DownloadFormController extends AbstractViewController<State, DownloadFormStore, typeof DownloadFormActionCreators> {

  getStoreClass() {
    return DownloadFormStore;
  }

  getStateFromStore() {
    return this.store.getState();
  }

  getActionCreators() {
    return DownloadFormActionCreators;
  }

  isRenderDataLoaded() {
    return (this.state.step != null && !this.state.isLoading);
  }

  isRenderDataLoadError() {
    return (
      this.state.error != null &&
      this.state.error.status !== 403 &&
      this.state.error.status !== 404
    );
  }

  isRenderDataNotFound() {
    return (
      this.state.error != null &&
      this.state.error.status === 404
    );
  }

  isRenderDataPermissionDenied() {
    return (
      this.state.error != null &&
      this.state.error.status === 403
    );
  }

  getTitle() {
    return (!this.isRenderDataLoaded() ? "Loading..." :
      "Download: " + this.state.step.displayName);
  }

  renderView() {
    // build props object to pass to form component
    let formProps = Object.assign({}, this.state, this.state.globalData, this.eventHandlers, {
      // passing summary view in case reporters handle view links differently
      summaryView: parse(this.props.location.search.slice(1))
    });
    return ( <DownloadFormContainer {...formProps}/> );
  }

  loadData() {
    // must reinitialize with every new props
    let { params } = this.props.match;
    if ('stepId' in params) {
      this.eventHandlers.loadPageDataFromStepId(params.stepId);
    }
    else if ('recordClass' in params) {
      this.eventHandlers.loadPageDataFromRecord(
          params.recordClass, params.primaryKey.split('/').join(','));
    }
    else {
      console.error("Neither stepId nor recordClass param was passed " +
          "to StepDownloadFormController component");
    }
  }
}

export default wrappable(DownloadFormController);
