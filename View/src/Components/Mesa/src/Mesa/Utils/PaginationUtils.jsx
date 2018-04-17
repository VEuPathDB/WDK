const PaginationUtils = {
  getCurrentPage (list, { itemsPerPage, activeItem }) {
    let pages = PaginationUtils.splitIntoPages(list, { itemsPerPage });
    let pageIndex = PaginationUtils.getCurrentPageIndex({ itemsPerPage, activeItem });
    return pages[pageIndex];
  },
  getCurrentPageNumber ({ itemsPerPage, activeItem }) {
    return Math.ceil(activeItem / itemsPerPage);
  },
  getCurrentPageIndex ({ itemsPerPage, activeItem }) {
    let pageNumber = PaginationUtils.getCurrentPageNumber({ itemsPerPage, activeItem });
    return pageNumber - 1;
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
  nextPageNumber (list, { itemsPerPage, activeItem }) {
    let totalPages = PaginationUtils.totalPages(list, { itemsPerPage });
    let currentPage = PaginationUtils.getCurrentPageNumber({ itemsPerPage, activeItem });
    let nextPage = currentPage + 1 > totalPages ? 1 : currentPage + 1;
    return nextPage;
  },
  prevPageNumber (list, { itemsPerPage, activeItem }) {
    let totalPages = PaginationUtils.totalPages(list, { itemsPerPage });
    let currentPage = PaginationUtils.getCurrentPageNumber({ itemsPerPage, activeItem });
    let prevPage = currentPage - 1 < 1 ? totalPages : currentPage - 1;
    return prevPage;
  }
};

export default PaginationUtils;
