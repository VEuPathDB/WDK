import React from 'react';

import DataTable from '../Ui/DataTable';
import TableToolbar from '../Ui/TableToolbar';
import ActionToolbar from '../Ui/ActionToolbar';
import PaginationMenu from '../Ui/PaginationMenu';

class Mesa extends React.Component {
  constructor (props) {
    super(props);
    this.renderToolbar = this.renderToolbar.bind(this);
    this.renderActionBar = this.renderActionBar.bind(this);
    this.renderPaginationMenu = this.renderPaginationMenu.bind(this);
  }

  renderPaginationMenu () {
    const { uiState, eventHandlers } = this.props;
    const { currentPage, totalPages, rowsPerPage } = uiState.pagination;
    const { onPageChange, onRowsPerPageChange } = eventHandlers;
    if (!onPageChange) return null;
    const props = { currentPage, totalPages, rowsPerPage, onPageChange, onRowsPerPageChange };

    return <PaginationMenu {...props} />
  }

  renderToolbar () {
    const { rows, options, columns, uiState, eventHandlers, children } = this.props;
    const props = { rows, options, columns, uiState, eventHandlers };
    if (!options.toolbar) return <div>{children}</div>;

    return <TableToolbar {...props}>{children}</TableToolbar>
  }

  renderActionBar () {
    const { rows, options, actions, eventHandlers } = this.props;
    const props = { rows, options, actions, eventHandlers };
    if (!actions.length) return null;

    return <ActionToolbar {...props} />
  }

  render () {
    const { rows, options, columns, actions, uiState, eventHandlers } = this.props;
    const props = { rows, options, columns, actions, uiState, eventHandlers };

    const PageNav = this.renderPaginationMenu;
    const Toolbar = this.renderToolbar;
    const ActionBar = this.renderActionBar;

    return (
      <div className="TableController">
        <Toolbar />
        <ActionBar />
        <PageNav />
        <DataTable {...props} />
        <PageNav />
      </div>
    );
  }
};


DataTable.propTypes = {
  rows: PropTypes.array,
  columns: PropTypes.array,
  options: PropTypes.object,
  actions: PropTypes.arrayOf(PropTypes.shape({
    element: PropTypes.oneOfType([ PropTypes.func, PropTypes.node, PropTypes.element ]),
    handler: PropTypes.func,
    callback: PropTypes.func
  })),
  uiState: PropTypes.object,
  eventHandlers: PropTypes.objectOf(PropTypes.func)
};

export default Mesa;
