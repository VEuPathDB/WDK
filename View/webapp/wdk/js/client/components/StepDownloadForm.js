import React from 'react';
import { wrappable } from '../utils/componentUtils';
import WdkServiceJsonReporterForm from './WdkServiceJsonReporterForm';

let StepDownloadForm = React.createClass({
  render() {
    return ( <WdkServiceJsonReporterForm {...this.props}/> );
  }
});

export default wrappable(StepDownloadForm);
