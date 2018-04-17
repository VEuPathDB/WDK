import { fail, badType, missingFromState } from '../Utils/Errors';

export const createSelection = (_selection = []) => {
  if (!Array.isArray(_selection))
    return badType('addIdToSelection', '_selection', 'array', typeof _selection);
  const selection = new Set(_selection);
  return [...selection];
};

export const selectionFromRows = (rows, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('selectionFromRows', 'idAccessor', 'function', typeof idAccessor);
  const idList = mapListToIds(rows, idAccessor);
  return createSelection(idList);
};

export const addRowToSelection = (_selection, row, idAccessor) => {
  if (!Array.isArray(_selection))
    return badType('addIdToSelection', '_selection', 'array', typeof _selection);
  if (typeof idAccessor !== 'function')
    return badType('addRowToSelection', 'idAccessor', 'function', typeof idAccessor);
  const id = idAccessor(row);
  return addIdToSelection(_selection, id);
};

export const addIdToSelection = (_selection = [], id) => {
  console.log('adding id', id, 'to selection', _selection);
  if (!Array.isArray(_selection))
    return badType('addIdToSelection', '_selection', 'array', typeof _selection);
  const selection = new Set(_selection);
  selection.add(id);
  return [...selection];
}

export const removeRowFromSelection = (_selection, row, idAccessor) => {
  console.log('removing row', row, 'from selection', _selection);
  if (!Array.isArray(_selection))
    return badType('addIdToSelection', '_selection', 'array', typeof _selection);
  if (typeof idAccessor !== 'function')
    return badType('removeRowFromSelection', 'idAccessor', 'function', typeof idAccessor);
  const id = idAccessor(row);
  return removeIdFromSelection(_selection, id);
};

export const removeIdFromSelection = (_selection, id) => {
  if (!Array.isArray(_selection))
    return badType('addIdToSelection', '_selection', 'array', typeof _selection);
  const selection = new Set(Array.isArray(_selection) ? _selection : []);
  selection.delete(id);
  return [...selection];
}

export const isRowSelected = (selection, row, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('isRowSelected', 'idAccessor', 'function', typeof idAccessor);
  const id = idAccessor(row);
  return selection.includes(id);
};

export const mapListToIds = (list, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('mapListToIds', 'idAccessor', 'function', typeof idAccessor);
  return list.map(idAccessor);
};

export const intersectSelection = (_selection, _list, idAccessor) => {
  if (typeof idAccessor !== 'function')
    return badType('intersectSelection', 'idAccessor', 'function', typeof idAccessor);
  const idList = mapListToIds(_list);
  const selection = new Set(Array.isArray(_selection) ? _selection : []);
  const intersection = new Set(idList);
  return [...selection].filter(item => intersection.has(item));
};
