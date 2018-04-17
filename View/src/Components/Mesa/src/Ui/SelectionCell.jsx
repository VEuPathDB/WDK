import React from 'react';

import Checkbox from '../Components/Checkbox';

class SelectionCell extends React.PureComponent {
  constructor (props) {
    super(props);
    this.selectAllRows = this.selectAllRows.bind(this);
    this.deselectAllRows = this.deselectAllRows.bind(this);
    this.renderPageCheckbox = this.renderPageCheckbox.bind(this);
    this.renderRowCheckbox = this.renderRowCheckbox.bind(this);
  }

  selectAllRows () {
    const { filteredRows, options, eventHandlers } = this.props;
    const { isRowSelected } = options;
    const { onRowSelect, onMultipleRowSelect } = eventHandlers;
    const unselectedRows = filteredRows.filter(row => !isRowSelected(row));
    if (onMultipleRowSelect) return onMultipleRowSelect(unselectedRows);
    else return unselectedRows.forEach(row => onRowSelect(row));
  }

  deselectAllRows () {
    const { filteredRows, options, eventHandlers } = this.props;
    const { isRowSelected } = options;
    const { onRowDeselect, onMultipleRowDeselect } = eventHandlers;
    const selection = filteredRows.filter(isRowSelected);
    if (onMultipleRowDeselect) return onMultipleRowDeselect(selection);
    else return selection.forEach(row => onRowDeselect(row));
  }

  renderPageCheckbox () {
    const { filteredRows, isRowSelected, eventHandlers } = this.props;
    const selection = filteredRows.filter(isRowSelected);
    const checked = filteredRows.every(isRowSelected);

    let handler = (e) => {
      e.stopPropagation();
      return checked
        ? this.deselectAllRows()
        : this.selectAllRows();
    };

    return (
      <th className="SelectionCell" onClick={handler}>
        <Checkbox checked={checked} />
      </th>
    )
  }

  renderRowCheckbox () {
    const { row, isRowSelected, eventHandlers } = this.props;
    const { onRowSelect, onRowDeselect } = eventHandlers;
    const checked = isRowSelected(row);

    let handler = (e) => {
      e.stopPropagation();
      return checked
        ? onRowDeselect(row)
        : onRowSelect(row);
    };

    return (
      <td className="SelectionCell" onClick={handler}>
        <Checkbox checked={checked} />
      </td>
    );
  }

  render () {
    let { heading } = this.props;
    return heading ? this.renderPageCheckbox() : this.renderRowCheckbox();
  }
};

export default SelectionCell;
