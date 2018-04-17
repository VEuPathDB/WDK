import React from 'react';

import Events from 'Mesa/Utils/Events';
import Icon from 'Mesa/Components/Icon';
import Modal from 'Mesa/Components/Modal';
import Checkbox from 'Mesa/Components/Checkbox';
import {
  hideColumn,
  showColumn,
  toggleColumnEditor,
  openColumnEditor,
  closeColumnEditor
} from 'Mesa/State/Actions';

class ColumnEditor extends React.PureComponent {
  constructor (props) {
    super(props);

    this.openEditor = this.openEditor.bind(this);
    this.closeEditor = this.closeEditor.bind(this);
    this.renderModal = this.renderModal.bind(this);
    this.toggleEditor = this.toggleEditor.bind(this);
    this.renderTrigger = this.renderTrigger.bind(this);
    this.showAllColumns = this.showAllColumns.bind(this);
    this.hideAllColumns = this.hideAllColumns.bind(this);
    this.renderColumnListItem = this.renderColumnListItem.bind(this);
  }

  openEditor () {
    const { dispatch } = this.props;
    dispatch(openColumnEditor());
    if (!this.closeListener) this.closeListener = Events.onKey('esc', this.closeEditor);
  }

  showAllColumns () {
    const { state, dispatch } = this.props;
    const { columns } = state;
    return columns.forEach(col => dispatch(showColumn(col)));
  }

  hideAllColumns () {
    const { state, dispatch } = this.props;
    const { columns } = state;
    const hideableColumns = columns.filter(col => col.hideable && !col.hidden);
    return hideableColumns.forEach(col => dispatch(hideColumn(col)));
  }

  closeEditor () {
    const { dispatch } = this.props;
    dispatch(closeColumnEditor());
    if (this.closeListener) Events.remove(this.closeListener);
  }

  toggleEditor () {
    const { dispatch } = this.props;
    dispatch(toggleColumnEditor());
  }

  renderTrigger () {
    const { children } = this.props;
    return (
      <div className="ColumnEditor-Trigger" onClick={this.toggleEditor}>
        {children}
      </div>
    )
  }

  renderColumnListItem (column) {
    const { dispatch } = this.props;
    const toggler = () => dispatch(column.hidden ? showColumn(column) : hideColumn(column));
    return (
      <li className="ColumnEditor-List-Item" key={column.key}>
        <Checkbox
          checked={!column.hidden}
          disabled={!column.hideable}
          onChange={toggler}
        />
        {' ' + (column.name || column.key)}
      </li>
    );
  }

  renderModal () {
    const { state } = this.props;
    const { columns, ui } = state;
    const { columnEditorOpen } = ui;

    return (
      <Modal open={columnEditorOpen} onClose={this.closeEditor}>
        <h3>Add / Remove Columns</h3>
        <small>
          <a onClick={this.showAllColumns}>Select All</a>
          <span> | </span>
          <a onClick={this.hideAllColumns}>Clear All</a>
        </small>
        <ul className="ColumnEditor-List">
          {columns.map(this.renderColumnListItem)}
        </ul>
        <button onClick={this.closeEditor} style={{ margin: '0 auto', display: 'block' }}>
          Close
        </button>
      </Modal>
    );
  }

  render () {
    const modal = this.renderModal();
    const trigger = this.renderTrigger();

    return (
      <div className="ColumnEditor">
        {trigger}
        {modal}
      </div>
    )
  }
}

export default ColumnEditor;
