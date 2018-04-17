import React from 'react';

import Icon from 'Mesa/Components/Icon';
import Events from 'Mesa/Utils/Events';
import Modal from 'Mesa/Components/Modal';
import Store from 'Mesa/State/Store';
import Checkbox from 'Mesa/Components/Checkbox';
import { hideColumn, showColumn } from 'Mesa/State/Actions';

class ColumnEditor extends React.Component {
  constructor (props) {
    super(props);

    let { hiddenColumns } = Store.getState();
    this.state = { editorOpen: false, hiddenColumns };

    this.openEditor = this.openEditor.bind(this);
    this.closeEditor = this.closeEditor.bind(this);
    this.toggleEditor = this.toggleEditor.bind(this);
    this.renderTrigger = this.renderTrigger.bind(this);
    this.renderModal = this.renderModal.bind(this);
    this.showAllColumns = this.showAllColumns.bind(this);
    this.hideAllColumns = this.hideAllColumns.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);
    this.componentWillUnmount = this.componentWillUnmount.bind(this);
  }

  componentDidMount () {
    this.subscription = Store.subscribe(() => {
      let { hiddenColumns } = Store.getState();
      if (hiddenColumns === this.state.hiddenColumns) return;
      let { editorOpen } = this.state;
      console.log('Store updated:', hiddenColumns);
      let newState = Object.assign({ hiddenColumns, editorOpen });
    });
  }

  componentWillUnmount () {
    this.subscription();
  }

  openEditor () {
    let editorOpen = true;
    this.setState({ editorOpen }, () => {
      this.closeListener = Events.onKey('esc', this.closeEditor);
    });
  }

  showAllColumns () {
    const { columns } = this.props;
    return columns.forEach(col => Store.dispatch(showColumn(col)));
  }

  hideAllColumns () {
    const { columns } = this.props;
    const { hiddenColumns } = Store.getState();
    const hideableColumns = columns.filter(col => hiddenColumns.indexOf(col) < 0 && col.hideable);
    return hideableColumns.forEach(col => Store.dispatch(hideColumn(col)));
  }

  closeEditor () {
    let editorOpen = false;
    this.setState({ editorOpen },() => {
      Events.remove(this.closeListener);
    });
  }

  toggleEditor () {
    let { editorOpen } = this.state;
    return editorOpen ? this.closeEditor() : this.openEditor();
  }

  renderTrigger () {
    const { children } = this.props;
    return (
      <div className="ColumnEditor-Trigger" onClick={this.toggleEditor}>
        {children}
      </div>
    )
  }

  renderColumnListItem (column, hiddenColumns) {
    const isShown = !hiddenColumns.includes(column);
    const toggler = () => Store.dispatch(isShown ? hideColumn(column) : showColumn(column));

    return (
      <li className="ColumnEditor-List-Item" key={column.key}>
        <Checkbox checked={isShown} disabled={!column.hideable} onChange={toggler} />
        {' ' + (column.name || column.key)}
      </li>
    );
  }

  renderColumnList () {
    const { columns } = this.props;
    const { hiddenColumns } = this.state;

    const columnList = columns.map(col => this.renderColumnListItem(col, hiddenColumns));

    return (
      <ul className="ColumnEditor-List">
        {columnList}
      </ul>
    )
  }

  renderModal () {
    const { editorOpen } = this.state;

    let columnList = this.renderColumnList();

    return (
      <Modal open={editorOpen} onClose={this.closeEditor}>
        <h3>Add / Remove Column</h3>
        <small>
          <a onClick={this.showAllColumns}>Select All</a>
          <span> | </span>
          <a onClick={this.hideAllColumns}>Clear All</a>
        </small>
        {columnList}
        <button onClick={this.closeEditor} style={{ margin: '0 auto', display: 'block' }}>
          Close
        </button>
      </Modal>
    );
  }

  render () {
    const { children } = this.props;
    const { editorOpen } = this.state;

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
