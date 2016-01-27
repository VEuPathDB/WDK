import React from 'react';
import { wrappable } from '../utils/componentUtils';
import CheckboxList from './CheckboxList';

let StepDownloadForm = React.createClass({

  componentWillMount() {
    // if formState is null, then initialize form state based on user preferences
    // TODO: get default attributes from user prefs
    setTimeout(() => {
      this.props.onFormChange({
        attributes: [],
        tables: []
      });
      // currently no special UI state on this form
      this.props.onFormUiChange({});
    }, 0);
  },

  onAttributeChange(event) {
    alert(event);
  },

  onTableChange(event) {
    alert(event);
  },

  render() {
    let {
      step,
      summaryView,
      question,
      recordClass,
      user,
      preferences,
      selectedReporter,
      formState,
      formUiState,
      onFormChange,
      onFormUiChange
    } = this.props;

    let reporterDisplay = (selectedReporter == null ? "WDK Standard JSON" :
      recordClass.formats.find(r => (r.name === selectedReporter)).displayName);
    return ( <span>Form for {reporterDisplay}</span> );
/*
    return (
      <div>
        <h3>Choose Attributes</h3>
        <CheckboxList name="attributes" onChange={this.onAttributeChange}
            selectedItems={formState.attributes}
            items={recordClass.attributes.map(attr =>
              ({ value: attr.name, display: attr.displayValue }))}/>
        <h3>Choose Tables</h3>
        <CheckboxList name="tables" onChange={this.onTableChange}
            selectedItems={formState.tables}
            items={recordClass.tables.map(table =>
              ({ value: table.name, display: table.displayValue }))}/>
      </div>
    );
*/
  }
});

export default wrappable(StepDownloadForm);
