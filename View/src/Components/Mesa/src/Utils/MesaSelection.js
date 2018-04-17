import { fail, badType, missingFromState } from '../Utils/Errors';

export const createSelection = (_selection = []) => {
  const selection = new Set(_selection);
  return [...selection];
};

export const selectionFromRows = (rows, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('selectionFromRows', 'idAccessor', 'function', typeof idAccessor);
  const idList = mapListToIds(rows, idAccessor);
  return createSelection(idList);
}

export const addRowToSelection = (_selection, row, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('addRowToSelection', 'idAccessor', 'function', typeof idAccessor);
  const selection = new Set(_selection);
  const id = idAccessor(row);
  selection.add(id);
  return [...selection]
};

export const removeRowFromSelection = (_selection, row, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('removeRowFromSelection', 'idAccessor', 'function', typeof idAccessor);
  const selection = new Set(_selection);
  const id = idAccessor(row);
  selection.delete(id);
  return [...selection];
}

export const isRowSelected = (selection, row, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('isRowSelected', 'idAccessor', 'function', typeof idAccessor);
  const id = idAccessor(row);
  return selection.includes(id);
}

export const mapListToIds = (list, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('mapListToIds', 'idAccessor', 'function', typeof idAccessor);
  return list.map(idAccessor);
}

export const intersectSelection = (_selection, _list, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('intersectSelection', 'idAccessor', 'function', typeof idAccessor);
  const idList = mapListToIds(_list);
  const selection = new Set(_selection);
  const intersection = new Set(idList);
  return [...selection].filter(item => intersection.has(item));
}

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */
/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

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
