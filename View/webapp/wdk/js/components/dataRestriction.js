wdk.namespace('wdk.dataRestriction', (ns, $) => {

  function getIdFromRecordClass (recordClass) {
    if (typeof recordClass !== 'string') return null;
    if (recordClass.length > 13) recordClass = recordClass.slice(0, 13);
    const result = recordClass.match(/^DS_[^_]+/g);
    return result === null
      ? null
      : result[0];
  };

  function emit (action, details) {
    const detail = Object.assign({}, details, { action });
    const event = new CustomEvent('DataRestricted', { detail });
    document.dispatchEvent(event);
  };

  // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

  ns.pagingController = (element) => {
    const { recordClass } = element.data();
    const studyId = getIdFromRecordClass(recordClass);
    const handler = (event) => emit('paginate', { studyId, event });
    element.find('input.paging-button').on('click', handler);
    element.on('click', 'a', handler);
  };

  ns.advancedPagingButtonController = (element) => {
    element.on('click', function (event) {
      // Original 'onclick' behavior, moved  here to be downstream
      // from fn above incase event propagation is cancelled
      // due to data restrictions
      wdk.resultsPage.openAdvancedPaging(element);
    });
  };

  ns.downloadLinkController = (element) => {
    const { recordClass } = element.data();
    const studyId = getIdFromRecordClass(recordClass);
    element.on('click', (event) => {
      emit('download', { studyId, event });
    });
  };

});
