import React from 'react';
import { wrappable, addOrRemove } from '../utils/componentUtils';
import NativeCheckboxList from './NativeCheckboxList';

/**
 * The goal of CheckboxList is to simplify the API of a NativeCheckboxList
 * to be convenient to simple form components, which will typically use it to
 * display a list of record attributes or tables for selection.
 */
let CheckboxList = React.createClass({

  onChange(event) {
    this.props.onChange(addOrRemove(this.props.value, event.target.value));
  },

  onSelectAll() {
    this.props.onChange(this.props.items.map(item => item.value));
  },

  onClearAll() {
    this.props.onChange([]);
  },

  render() {
    let { name, title, items, value } = this.props;
    return (
      <NativeCheckboxList name={name}
         onChange={this.onChange}
         onSelectAll={this.onSelectAll}
         onClearAll={this.onClearAll}
         selectedItems={value}
         items={items}/>
    );
  }
});

export default wrappable(CheckboxList);
