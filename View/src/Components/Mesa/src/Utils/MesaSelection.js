import { fail, badType, missingFromState } from '../Utils/Errors';

class MesaSelection {
  constructor (idAccessor) {
    if (typeof idAccessor !== 'function')
      return badType('MesaSelection:constructor', 'idAccessor', 'function', typeof idAccessor);
    this.idAccessor = idAccessor;
    this.selection = new Set();

    this.getSelection = this.getSelection.bind(this);
    this.onRowSelect = this.onRowSelect.bind(this);
    this.onMultiRowSelect = this.onMultiRowSelect.bind(this);
    this.onRowDeselect = this.onRowDeselect.bind(this);
    this.onMultiRowDeselect = this.onMultiRowDeselect.bind(this);
    this.isRowSelected = this.isRowSelected.bind(this);
  }

  getSelection () {
    return this.selection;
  }

  onRowSelect (row) {
    const id = this.idAccessor(row);
    this.selection.add(id);
    return this.selection;
  }

  onMultiRowSelect (rows) {
    rows.forEach(row => this.selection.add(this.idAccessor(row)));
    return this.selection;
  }

  onRowDeselect (row) {
    const id = this.idAccessor(row);
    this.selection.delete(id);
    return this.selection;
  }

  onMultiRowDeselect (rows) {
    rows.forEach(row => this.selection.delete(this.idAccessor(row)));
    return this.selection;
  }

  intersectWith (rows) {
    const rowIds = rows.map(this.idAccessor);
    this.selection.forEach(row => rowIds.includes(row) ? null : this.selection.delete(row));
    return this.selection;
  }

  isRowSelected (row) {
    const id = this.idAccessor(row);
    return this.selection.has(id);
  }
};

export default MesaSelection;
