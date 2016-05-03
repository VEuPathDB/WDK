import React from 'react';
import { wrappable, filterOutProps } from '../utils/componentUtils';
import StepDownloadForm from './StepDownloadForm';
import PrimaryKeySpan from './PrimaryKeySpan';
import RadioList from './RadioList';

let NO_REPORTER_SELECTED = "_none_";

let ReporterSelect = props => {
  let { reporters, selected, onChange } = props;
  if (reporters.length < 2) return ( <noscript/> );
  let nestedDivStyle = { display: 'inline-block', verticalAlign: 'top' };
  let items = reporters.map(reporter => ({ value: reporter.name, display: reporter.displayName }));
  return (
    <div style={{ margin: '20px 0'}}>
      <div style={nestedDivStyle}>
        <span style={{marginRight:'0.5em', fontWeight:'bold'}}>Choose a Report:</span>
      </div>
      <div style={nestedDivStyle}>
        <RadioList items={items} value={selected} onChange={onChange}/>
      </div>
    </div>
  );
}

function getTitle(scope, step, recordClass) {
  switch (scope) {
    case 'results':
      return (
        <div>
          <h1>Download {step.estimatedSize} {recordClass.displayNamePlural}</h1>
          <span style={{fontSize: "1.5em"}}>Results are from search: {step.displayName}</span>
        </div>
      );
    case 'record':
      return ( <div><h1>Download {recordClass.displayName}: <PrimaryKeySpan primaryKeyString={step.displayName}/></h1></div> );
    default:
      return ( <div><h1>Download Results</h1></div> );
  }
}

let StepDownloadFormPage = React.createClass({

  changeReporter(newValue) {
    this.props.onReporterChange(newValue);
  },

  render() {

    // get the props needed in this component's render
    let { scope, step, availableReporters, selectedReporter, recordClass, onSubmit } = this.props;

    // create page title element
    let title = getTitle(scope, step, recordClass);

    // filter props we don't want to send to the child form
    let formProps = filterOutProps(this.props, [ 'onReporterChange' ]);

    // incoming store value of null indicates no format currently selected
    if (selectedReporter == null) {
      selectedReporter = NO_REPORTER_SELECTED;
    }

    return (
      <div style={{margin: '1em 3em'}}>
        {title}
        <ReporterSelect reporters={availableReporters} selected={selectedReporter} onChange={this.changeReporter}/>
        <StepDownloadForm {...formProps}/>
      </div>
    );
  }

});

export default wrappable(StepDownloadFormPage);
