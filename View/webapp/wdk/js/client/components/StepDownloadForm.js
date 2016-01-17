import React from 'react';
import { wrappable } from '../utils/componentUtils';

let StepDownloadForm = React.createClass({

  render() {
    let inputs = JSON.stringify(this.props, null, '  ');
    let text = "Download Step " + this.props.step.id + " (passed from view: " + this.props.summaryView + ")";
    return (
      <div>
        <span>{text}</span>
        <pre>{inputs}</pre>
      </div>
    );
  }

});

export default wrappable(StepDownloadForm);