import React, { PropTypes } from 'react';
import { wrappable } from '../utils/componentUtils';

let MultiSelect = React.createClass({

  onChange(event) {
    let options = event.target.options;
    let value = [];
    for (let i = 0; i < options.length; i++) {
      if (options[i].selected) {
        value.push(options[i].value);
      }
    }
    this.props.onChange(value);
  },

  render() {
    let { name, size, value, items, onChange } = this.props;
    return (
      <select {...this.props} name={name} size={size} multiple="true" value={value} onChange={this.onChange}>
        {items.map(item =>
          ( <option key={item.value} value={item.value}>{item.display}</option> )
        )}
      </select>
    );
  }
});

MultiSelect.propTypes = {

  /** Value to use for "name" attribute of the select form input **/
  name: PropTypes.string,

  /** Array of items to display in the list **/
  items: PropTypes.array.isRequired,

  /** Value of the option elements that should be selected **/
  value: PropTypes.array.isRequired,

  /**
   * Callback function that will be called when user changes selected value.
   * The new (string) value of the selected option will be passed to this
   * function.
   */
  onChange: PropTypes.func,
};

export default wrappable(MultiSelect)
