import React from 'react';

import Checkbox from 'Mesa/Components/Checkbox';
import PaginationUtils from 'Mesa/Utils/PaginationUtils';
import { toggleRowSelectionById, selectRowsByIds, deselectRowsByIds } from 'Mesa/State/Actions';

class SelectionCell extends React.PureComponent {
  constructor (props) {
    super(props);
    this.renderPageCheckbox = this.renderPageCheckbox.bind(this);
    this.renderRowCheckbox = this.renderRowCheckbox.bind(this);
  }

  diffuseClick (e) {
    return e.stopPropagation();
  }

  renderPageCheckbox () {
    let { filteredRows, state, dispatch } = this.props;
    let { selection, pagination } = state.ui;
    let { paginate } = state.options;
    let spread = PaginationUtils.getSpread(filteredRows, pagination, paginate);
    let checked = filteredRows.length && PaginationUtils.isSpreadSelected(spread, selection);

    let handler = (e) => {
      e.stopPropagation();
      dispatch(checked ? deselectRowsByIds(spread) : selectRowsByIds(spread));
    };

    return (
      <th className="SelectionCell" onClick={handler}>
        <Checkbox checked={checked} />
      </th>
    )
  }

  renderRowCheckbox () {
    let { row, state, dispatch } = this.props;
    let { selection } = state.ui;
    let checked = selection.includes(row.__id);

    let handler = (e) => {
      e.stopPropagation();
      dispatch(toggleRowSelectionById(row.__id))
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
