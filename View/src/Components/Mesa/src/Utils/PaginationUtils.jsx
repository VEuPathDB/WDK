const PaginationUtils = {
  getCurrentPage (list, { itemsPerPage, anchorIndex }) {
    let pages = PaginationUtils.splitIntoPages(list, { itemsPerPage });
    let pageIndex = PaginationUtils.getCurrentPageIndex({ itemsPerPage, anchorIndex });
    return pages[pageIndex];
  },
  getCurrentPageNumber ({ itemsPerPage, anchorIndex }) {
    return Math.ceil(anchorIndex / itemsPerPage);
  },
  getCurrentPageIndex ({ itemsPerPage, anchorIndex }) {
    let pageNumber = PaginationUtils.getCurrentPageNumber({ itemsPerPage, anchorIndex });
    return pageNumber - 1;
  },
  getSpread (list, { itemsPerPage, anchorIndex }, usePagination) {
    let spread = usePagination
      ? PaginationUtils.getCurrentPage(list, { itemsPerPage, anchorIndex })
      : list;
    return spread ? spread.map(item => item.__id) : [];
  },
  isSpreadSelected (spread, selection) {
    if (!Array.isArray(spread) || !Array.isArray(selection)) return;
    return selection.length >= spread.length && spread.every(id => selection.includes(id))
  },
  countSelectedInSpread (spread, selection) {
    if (!Array.isArray(spread) || !Array.isArray(selection)) return;
    let selected = spread.filter(id => selection.includes(id));
    return selected.length;
  },
  splitIntoPages (list, { itemsPerPage }) {
    return PaginationUtils.chunkArray(list, itemsPerPage);
  },
  totalPages (list, { itemsPerPage }) {
    if (!Array.isArray(list)) return null;
    return Math.ceil(list.length / itemsPerPage);
  },
  generatePageList (size) {
    return new Array(size).fill({}).map((empty, index) => index + 1);
  },
  chunkArray (array, size = 1) {
    if (!Array.isArray(array)) return array;
    let chunks = Math.ceil(array.length / size);
    return Array(chunks).fill({}).map((_, i) => array.slice(i * size, i * size + size));
  },
  firstItemOnPage (pageNumber, { itemsPerPage }) {
    let result = ((pageNumber - 1) * itemsPerPage) + 1;
    return result;
  },
  lastItemOnPage (pageNumber, { itemsPerPage }, list) {
    let result = (pageNumber * itemsPerPage);
    return list && result > list.length ? list.length : result;
  },
  nextPageNumber (list, { itemsPerPage, anchorIndex }) {
    let totalPages = PaginationUtils.totalPages(list, { itemsPerPage });
    let currentPage = PaginationUtils.getCurrentPageNumber({ itemsPerPage, anchorIndex });
    let nextPage = currentPage + 1 > totalPages ? 1 : currentPage + 1;
    return nextPage;
  },
  prevPageNumber (list, { itemsPerPage, anchorIndex }) {
    let totalPages = PaginationUtils.totalPages(list, { itemsPerPage });
    let currentPage = PaginationUtils.getCurrentPageNumber({ itemsPerPage, anchorIndex });
    let prevPage = currentPage - 1 < 1 ? totalPages : currentPage - 1;
    return prevPage;
  }
};

export default PaginationUtils;
