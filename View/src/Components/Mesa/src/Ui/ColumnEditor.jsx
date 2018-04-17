import React from 'react';

import Events from '../Utils/Events';
import Icon from '../Components/Icon';
import Modal from '../Components/Modal';
import Checkbox from '../Components/Checkbox';

class ColumnEditor extends React.PureComponent {
  constructor (props) {
    super(props);

    this.state = {
      editorOpen: false
    };

    this.openEditor = this.openEditor.bind(this);
    this.closeEditor = this.closeEditor.bind(this);
    this.toggleEditor = this.toggleEditor.bind(this);

    this.renderModal = this.renderModal.bind(this);
    this.renderTrigger = this.renderTrigger.bind(this);
    this.renderColumnListItem = this.renderColumnListItem.bind(this);

    this.showColumn = this.showColumn.bind(this);
    this.hideColumn = this.hideColumn.bind(this);
    this.showAllColumns = this.showAllColumns.bind(this);
    this.hideAllColumns = this.hideAllColumns.bind(this);
  }

  openEditor () {
    this.setState({ editorOpen: true })
    if (!this.closeListener) this.closeListener = Events.onKey('esc', this.closeEditor);
  }

  closeEditor () {
    this.setState({ editorOpen: false })
    if (this.closeListener) Events.remove(this.closeListener);
  }

  toggleEditor () {
    let { editorOpen } = this.state;
    return editorOpen ? this.closeEditor() : this.openEditor();
  }

  showColumn (column) {
    const { eventHandlers } = this.props;
    const { onShowColumn } = eventHandlers;
    if (onShowColumn) onShowColumn(column);
  }

  hideColumn (column) {
    const { eventHandlers } = this.props;
    const { onHideColumn } = eventHandlers;
    if (onHideColumn) onHideColumn(column);
  }

  showAllColumns () {
    const { columns, eventHandlers } = this.props;
    const hiddenColumns = columns.filter(col => col.hidden);
    hiddenColumns.forEach(column => this.showColumn(column));
  }

  hideAllColumns () {
    const { columns, eventHandlers } = this.props;
    const shownColumns = columns.filter(col => !col.hidden);
    shownColumns.forEach(column => this.hideColumn(column));
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
    return (
      <li className="ColumnEditor-List-Item" key={column.key}>
        <Checkbox
          checked={!column.hidden}
          disabled={!column.hideable}
          onChange={() => ((column.hidden ? this.showColumn : this.hideColumn)(column))}
        />
        {' ' + (column.name || column.key)}
      </li>
    );
  }

  renderModal () {
    const { editorOpen } = this.state;
    return (
      <Modal open={editorOpen} onClose={this.closeEditor}>
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
