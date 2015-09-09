import React from 'react';
import TabbableContainer from './TabbableContainer';
import { wrappable } from '../utils/componentUtils';

function renderFilterField(field, isChecked, handleChange) {
  return (
    <div key={field.name}>
      <label>
        <input type="checkbox" value={field.name} checked={isChecked} onChange={handleChange}/>
        {' ' + field.displayName}
      </label>
    </div>
  );
}

let $ = window.jQuery;

let AnswerFilterSelector = React.createClass({

  componentDidMount() {
    if (this.props.open) {
      document.addEventListener('click', this.handleDocumentClick);
    }
  },

  componentWillUnmount() {
    document.removeEventListener('click', this.handleDocumentClick);
  },

  componentDidUpdate() {
    document.removeEventListener('click', this.handleDocumentClick);
    if (this.props.open) {
      document.addEventListener('click', this.handleDocumentClick);
    }
  },

  handleKeyPress(e) {
    if (e.key === 'Escape') {
      this.props.onClose();
    }
  },

  handleDocumentClick(e) {
    // close if the click target is not contained by this node
    let node = React.findDOMNode(this);
    if ($(e.target).closest(node).length === 0) {
      this.props.onClose();
    }
  },

  render() {
    if (!this.props.open) {
      return null;
    }

    let {
      recordClass,
      filterAttributes,
      filterTables,
      selectAll,
      clearAll,
      onClose,
      toggleAttribute,
      toggleTable
    } = this.props;

    return (
      <TabbableContainer
        onKeyDown={this.handleKeyPress}
        className="wdk-Answer-filterFieldSelector">

        <p>
          <a href="#" onClick={selectAll}>select all</a>
          {' | '}
          <a href="#" onClick={clearAll}>clear all</a>
        </p>

        {recordClass.attributes.map(attr => {
          let isChecked = filterAttributes.includes(attr.name);
          return renderFilterField(attr, isChecked, toggleAttribute);
        })}

        {recordClass.tables.map(table => {
          let isChecked = filterTables.includes(table.name);
          return renderFilterField(table, isChecked, toggleTable);
        })}

        <div className="wdk-Answer-filterFieldSelectorCloseIconWrapper">
          <button
            className="fa fa-close wdk-Answer-filterFieldSelectorCloseIcon"
            onClick={onClose}
          />
        </div>

      </TabbableContainer>
    );
  }

});

export default wrappable(AnswerFilterSelector);
