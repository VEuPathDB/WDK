import React from 'react';
import { wrappable, addOrRemove } from '../utils/componentUtils';
import CheckboxList from './CheckboxList';

/**
 * The goal of the ReporterCheckboxList is to simplify the API of a CheckboxList
 * to be convenient to reporter forms, which will typically use it to display a
 * list of record attributes or tables for selection.
 */
let ReporterCheckboxList = React.createClass({

  onChange(event) {
    this.props.onChange(addOrRemove(this.props.selectedValueNames, event.target.value));
  },

  onSelectAll() {
    this.props.onChange(this.props.allValues.map(val => val.name));
  },

  onClearAll() {
    this.props.onChange([]);
  },

  render() {
    let { name, title, allValues, selectedValueNames } = this.props;
    return (
      <div>
        <h3>{title}</h3>
        <div style={{padding: '0 2em'}}>
          <CheckboxList name={name}
            onChange={this.onChange}
            onSelectAll={this.onSelectAll}
            onClearAll={this.onClearAll}
            selectedItems={selectedValueNames}
            items={allValues.map(val => ({ value: val.name, display: val.displayName }))}/>
        </div>
      </div>
    );
  }
});

export default wrappable(ReporterCheckboxList);
