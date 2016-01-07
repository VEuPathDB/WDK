
//**************************************************
// Define the depended service
//**************************************************

var ServiceUrl = window.location.href.substring(0,
  window.location.href.indexOf("wdk/simpleClient/search.html")) + "service";

//**************************************************
// Page Initialization
//**************************************************

// singleton
var Page = (function() {

  var exports = {
    loadPage: loadPage
  };

  function wireApplication(serviceUrl, initialData) {

    // create dispatcher
    var dispatcher = new Dispatcher();

    // initialize the store with the fetched state
    var store = new Store(dispatcher, initialData);

    // create action creator container
    var ac = new ActionCreator(serviceUrl, dispatcher);

    // create top-level view-controller
    React.render(<ViewController store={store} ac={ac}/>, document.body);
  }

  function loadPage(serviceUrl) {
    jQuery.ajax({
      type: "GET",
      url: serviceUrl + "/question",
      dataType: "json",
      success: function(data) {
        wireApplication(serviceUrl, data);
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        alert("Error: Unable to load initial data");
      }
    });
  }

  return exports;

})();

Page.loadPage(ServiceUrl);

