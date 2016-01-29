import React, { Component, PropTypes } from 'react';
import uid from 'lodash/utility/uniqueId';
import { wrappable } from '../utils/componentUtils';

/**
 * Render a list of checkboxes. Checkbox state is managed locally, unless
 * `seletedItems` is provided as a prop. (See this component's propTypes
 * documentation.)
 */
class CheckboxList extends Component {

  constructor(props) {
    super(...arguments);
    this.id = uid('CheckboxList.');
    this.controlled = this.props.selectedItems != null;

    if (!this.controlled) {
      this.state = {
        selectedItems: this.props.defaultSelectedItems
      };
    }
  }

  toggle(event, item) {
    this.props.onChange(event, item);

    if (!this.controlled && !event.defaultPrevented) {
      this.setState({
        selectedItems: event.target.checked
          ? this.state.selectedItems.concat(item)
          : this.state.selectedItems.filter(i => i !== item)
      });
    }
  }

  selectAll(event) {
    this.props.onSelectAll(event);

    if (!this.controlled && !event.defaultPrevented) {
      this.setState({
        selectedItems: this.props.items.slice()
      });
    }

    // prevent update to URL
    event.preventDefault();
  }

  clearAll(event) {
    this.props.onClearAll(event);

    if (!this.controlled && !event.defaultPrevented) {
      this.setState({
        selectedItems: []
      });
    }

    // prevent update to URL
    event.preventDefault();
  }

  render() {
    let { selectedItems } = this.controlled ? this.props : this.state;
    return (
      <div className="wdk-CheckboxList">
        <div>
          {this.props.items.map(item => {
            let id = `${this.id}.${item.value}`;
            return (
              <div key={item.value} className="wdk-CheckboxListItem">
                <input
                  id={id}
                  type="checkbox"
                  name={this.props.name}
                  value={item.value}
                  checked={selectedItems.includes(item.value)}
                  onChange={e => this.toggle(e, item)}
                />
                <label htmlFor={id}> {item.display} </label>
              </div>
            );
          })}
        </div>
        <div>
          <a href="#" onClick={e => this.selectAll(e)}>select all</a>
          {' | '}
          <a href="#" onClick={e => this.clearAll(e)}>clear all</a>
        </div>
      </div>
    );
  }
}

CheckboxList.propTypes = {

  /** Value to use for "name" attribute of checkbox form input element **/
  name: PropTypes.string,

  /** Array of items to display in the list **/
  items: PropTypes.array.isRequired,

  /**
   * Default list of selected items. If provided, these items will be checked
   * for the initial render.
   */
  defaultSelectedItems: PropTypes.array,

  /**
   * List of selected items.
   *
   * - If omitted, this component will track state locally.
   *
   * - If provided, items in this list will be checked, regardless of the value
   *   of `defaultSelectedItems`. Also, state will not be tracked locally.
   *   `onChange` should be used to detect changes and update extrernally
   *   tracked state.
   */
  selectedItems: PropTypes.array,

  /**
   * Callback function that will be called when the set of selected items
   * changes. The function will be called with two arguments:
   *
   *   - The event that triggered the change. This gives consumers a change to
   *     revert the state of the checked items.
   *
   *   - An array of checked items.
   */
  onChange: PropTypes.func,

  /**
   * Called when the "select all" link is clicked.
   * If state is managed locally, all items will be checked.
   */
  onSelectAll: PropTypes.func,

  /**
   * Called when the "clear all" link is clicked.
   * If state is managed locall, all items will be unchecked.
   */
  onClearAll: PropTypes.func
};

CheckboxList.defaultProps = {
  defaultSelectedItems: [],
  onChange() { /* noop */ },
  onSelectAll() { /* noop */ },
  onClearAll() { /* noop */ }
};

export default wrappable(CheckboxList)
