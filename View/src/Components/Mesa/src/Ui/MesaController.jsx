import React from 'react';
import PropTypes from 'prop-types';

import DataTable from '../Ui/DataTable';
import TableToolbar from '../Ui/TableToolbar';
import ActionToolbar from '../Ui/ActionToolbar';
import PaginationMenu from '../Ui/PaginationMenu';
import EmptyState from '../Ui/EmptyState';

class MesaController extends React.Component {
  constructor (props) {
    super(props);
    this.renderBody = this.renderBody.bind(this);
    this.renderToolbar = this.renderToolbar.bind(this);
    this.renderActionBar = this.renderActionBar.bind(this);
    this.renderEmptyState = this.renderEmptyState.bind(this);
    this.renderPaginationMenu = this.renderPaginationMenu.bind(this);
  }

  renderPaginationMenu () {
    const { uiState, eventHandlers } = this.props;
    const { pagination } = uiState ? uiState : {};
    const { currentPage, totalPages, rowsPerPage } = pagination ? pagination : {};
    const { onPageChange, onRowsPerPageChange } = eventHandlers ? eventHandlers : {};

    if (!onPageChange) return null;

    const props = { currentPage, totalPages, rowsPerPage, onPageChange, onRowsPerPageChange };
    return <PaginationMenu {...props} />
  }

  renderToolbar () {
    const { rows, options, columns, uiState, eventHandlers, children } = this.props;
    const props = { rows, options, columns, uiState, eventHandlers, children };
    if (!options || !options.toolbar) return null;

    return <TableToolbar {...props} />
  }

  renderActionBar () {
    const { rows, options, actions, eventHandlers, children } = this.props;
    let props = { rows, options, actions, eventHandlers };
    if (!actions || !actions.length) return null;
    if (!this.renderToolbar() && children) props = Object.assign({}, props, { children });

    return <ActionToolbar {...props} />
  }

  renderEmptyState () {
    const { uiState, options } = this.props;
    const { emptinessCulprit } = uiState ? uiState : {};
    const { renderEmptyState } = options ? options : {};

    return renderEmptyState ? renderEmptyState() : <EmptyState culprit={emptinessCulprit} />
  }

  renderBody () {
    const { rows, filteredRows, options, columns, actions, uiState, eventHandlers } = this.props;
    const props = { rows, filteredRows, options, columns, actions, uiState, eventHandlers };
    const Empty = this.renderEmptyState;

    return rows.length
      ? <DataTable {...props} />
      : <Empty />
  }

  render () {
    const { rows, options, columns, actions, uiState, eventHandlers } = this.props;
    const props = { rows, options, columns, actions, uiState, eventHandlers };

    const Body = this.renderBody;
    const Toolbar = this.renderToolbar;
    const ActionBar = this.renderActionBar;
    const PageNav = this.renderPaginationMenu;

    return (
      <div className="Mesa MesaComponent">
        <Toolbar />
        <ActionBar />
        <PageNav />
        <Body />
        <PageNav />
      </div>
    );
  }
};

MesaController.propTypes = {
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

export default MesaController;
