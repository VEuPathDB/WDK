import React from 'react';

import Checkbox from '../Components/Checkbox';
import PaginationUtils from '../Utils/PaginationUtils';
import { toggleRowSelectionById, selectRowsByIds, deselectRowsByIds } from '../State/Actions';

class SelectionCell extends React.PureComponent {
  constructor (props) {
    super(props);
    this.selectAllRows = this.selectAllRows.bind(this);
    this.deselectAllRows = this.deselectAllRows.bind(this);
    this.renderPageCheckbox = this.renderPageCheckbox.bind(this);
    this.renderRowCheckbox = this.renderRowCheckbox.bind(this);
  }

  selectAllRows () {
    const { rows, options, eventHandlers } = this.props;
    const { isRowSelected } = options;
    const { onRowSelect, onMultipleRowSelect } = eventHandlers;
    const unselectedRows = rows.filter(row => !isRowSelected(row));
    if (onMultipleRowSelect) return onMultipleRowSelect(unselectedRows);
    else return unselectedRows.forEach(row => onRowSelect(row));
  }

  deselectAllRows () {
    const { rows, options, eventHandlers } = this.props;
    const { isRowSelected } = options;
    const { onRowDeselect, onMultipleRowDeselect } = eventHandlers;
    const selection = rows.filter(isRowSelected);
    if (onMultipleRowDeselect) return onMultipleRowDeselect(selection);
    else return selection.forEach(row => onRowDeselect(row));
  }

  renderPageCheckbox () {
    const { rows, isRowSelected, eventHandlers } = this.props;
    const selection = rows.filter(isRowSelected);
    const checked = rows.every(isRowSelected);

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
