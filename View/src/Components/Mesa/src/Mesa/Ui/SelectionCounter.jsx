import React from 'react';

import PaginationUtils from 'Mesa/Utils/PaginationUtils';
import { selectRowsByIds, deselectRowsByIds, setPaginatedActiveItem } from 'Mesa/State/Actions';

class SelectionCounter extends React.Component {
  constructor (props) {
    super(props);
    this.selectAllRows = this.selectAllRows.bind(this);
    this.deselectAllRows = this.deselectAllRows.bind(this);
    this.goToSelection = this.goToSelection.bind(this);
  }

  noun (size) {
    size = (typeof size === 'number' ? size : size.length);
    return 'row' + (size === 1 ? '' : 's');
  }

  /* Actions -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

  selectAllRows () {
    const { dispatch, filteredRows } = this.props;
    const ids = filteredRows.map(row => row.__id);
    dispatch(selectRowsByIds(ids));
  }

  deselectAllRows () {
    const { dispatch, filteredRows } = this.props;
    const ids = filteredRows.map(row => row.__id);
    dispatch(deselectRowsByIds(ids));
  }

  goToSelection () {
    const { state, dispatch, filteredRows } = this.props;
    const { selection, pagination } = state.ui;
    const { paginate } = state.options;
    const spread = PaginationUtils.getSpread(filteredRows, pagination, paginate);

    const target = selection.find(id => !spread.includes(id));
    if (!target) return;

    const targetIndex = filteredRows.findIndex(row => row.__id === target);
    if (targetIndex < 0) return;

    dispatch(setPaginatedActiveItem(targetIndex + 1));
  }

  /* Renderers -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

  renderSelectionCount () {
    const { state, dispatch, filteredRows } = this.props;
    const { selection } = state.ui;

    const allIds = filteredRows.map(row => row.__id);
    const allSelected = PaginationUtils.isSpreadSelected(allIds, selection);

    return (
      <div className="SelectionCounter">
        {allSelected ? 'All ' : ''}<b>{selection.length}</b> {this.noun(selection)} {allSelected ? 'are' : ''} selected.
        <br />
        <a onClick={this.deselectAllRows}>Clear selection.</a>
      </div>
    )
  }

  renderPaginatedSelectionCount () {
    const { state, dispatch, filteredRows } = this.props;
    const { selection, pagination } = state.ui;
    const allIds = filteredRows.map(row => row.__id);
    const allSelected = PaginationUtils.isSpreadSelected(allIds, selection);
    const spread = PaginationUtils.getSpread(filteredRows, pagination, true);
    const pageCoverage = PaginationUtils.countSelectedInSpread(spread, selection);
    const totalCoverage = PaginationUtils.countSelectedInSpread(allIds, selection);

    const outsideCoverage = selection.length - pageCoverage;
    const pageSelected = pageCoverage === spread.length;

    return (
      <div className="SelectionCounter">
        {pageSelected && !allSelected
          ? <span>All <b>{pageCoverage}</b> {this.noun(pageCoverage)} on this page are selected. </span>
          : null
        }
        {pageCoverage && !pageSelected && !allSelected
          ? <span><b>{pageCoverage}</b> {this.noun(pageCoverage)} selected on this page. </span>
          : null
        }
        {outsideCoverage && !allSelected
          ? <span><b>{outsideCoverage}</b> {this.noun(outsideCoverage)} selected on <a onClick={this.goToSelection}>other pages</a>. </span>
          : null
        }
        {allSelected
          ? <span>All <b>{totalCoverage}</b> {this.noun(totalCoverage)} are selected. </span>
          : null
        }

        <br />

        {pageSelected && !allSelected
          ? <a onClick={this.selectAllRows}>Select all <b>{filteredRows.length}</b> {this.noun(filteredRows.length)}. </a>
          : null
        }
        <a onClick={this.deselectAllRows}>Clear selection.</a>
      </div>
    );
  }

  render () {
    const { paginate } = this.props.state.options;
    const { selection } = this.props.state.ui;

    if (!selection.length) return null;
    return !paginate ? this.renderSelectionCount() : this.renderPaginatedSelectionCount();
  }
};

export default SelectionCounter;
