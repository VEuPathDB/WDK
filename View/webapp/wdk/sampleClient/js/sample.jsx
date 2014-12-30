
//**************************************************
// Primary Rendering Component
//**************************************************

var EntryList = React.createClass({
  doRecord: function(event) {
    var op = jQuery(event.target).data("op");
    var id = jQuery(event.target).data("id");
    var div = document.createElement("div");
    var form = document.createElement("form");
    var input = document.createElement("textarea");
    input.cols = "50";
    input.rows = "10";
    input.value = (op == "add" ? "" : JSON.stringify(this.props.data[id], undefined, 2));
    form.appendChild(input);
    div.appendChild(form);
    jQuery(div).dialog({
      title: (op == "add" ? "Add" : "Modify") + " Record",
      modal: true,
      width: "60%",
      buttons: [{
          text: "OK",
          click: function() {
            var value = jQuery(this).find("textarea").val();
            try {
              JSON.parse(value);
              jQuery(this).dialog("close");
              if (op == "add")
                ActionCreator.addRecord(value);
              else
                ActionCreator.modifyRecord(id, value);
            }
            catch (e) {
              alert("JSON not properly formatted.\n\n" + e + "\n\nPlease try again.");
            }
          }
        },{
          text: "Cancel",
          click: function() { jQuery(this).dialog("close"); }
        }]
    });
  },
  deleteRecord: function(event) {
    var id = jQuery(event.target).data("id");
    if (confirm("Are you sure you want to delete record " + id + "?")) {
      ActionCreator.deleteRecord(id);
    }
  },
  render: function() {
    var component = this;
    var data = component.props.data;
    var getRecord = function(key) {
      var displayVal = JSON.stringify(data[key]);
      if (displayVal.length > 40) {
        displayVal = displayVal.substring(0, 37) + "...";
      }
      return (
        <tr key={key}>
          <td>{key}</td>
          <td><pre>{displayVal}</pre></td>
          <td>
            <input type="button" value="Modify" data-id={key} data-op="modify" onClick={component.doRecord}/>
            <input type="button" value="Delete" data-id={key} onClick={component.deleteRecord}/>
          </td>
        </tr>
      );
    };
    return (
      <div>
        <h3>JSON Record Store</h3>
        <div>Each entry must be valid JSON</div>
        <hr/>
        <table>
          <tr>
            <th>ID</th>
            <th>Record Preview</th>
            <th>Operations</th>
          </tr>
          {Object.keys(data).map(getRecord)}
        </table>
        <hr/>
        <input type="button" value="Add Record" data-op="add" onClick={component.doRecord}/>
      </div>
    );
  }
});

//**************************************************
// Dispatcher
//**************************************************

var AppDispatcher = new Dispatcher();

// types of actions sent through the dispatcher
var ActionType = {
  ADD_ACTION: "addAction",
  MODIFY_ACTION: "modifyAction",
  DELETE_ACTION: "deleteAction"
}

//**************************************************
// Model
//**************************************************

var Model = {

  model: {},

  registeredCallbacks: [],

  register: function(callback) {
    registeredCallbacks.push(callback);
  },

  updateRegisteredViews: function() {
    registeredCallbacks.each(function(func) { func(this); });
  },

  handleEvent: function(payload) {
    // update the model here
    switch(payload.actionType) {
      case ActionType.ADD_ACTION:
        alert("Updating model with add action");
        break;
      case ActionType.MODIFY_ACTION:
        alert("Updating model with modify action");
        break;
      case ActionType.DELETE_ACTION:
        alert("Updating model with delete action");
        break;
      default:
        // this model does not support other actions
    }

    // then alert registered views of change
    Model.updateRegisteredViews();
  },

  initialize: function(initialValue) {
    Model.model = initialValue;
    AppDispatcher.register(Model.handleEvent);
  },

  get: function() {
    return Model.model;
  }
}

//**************************************************
// Controller-View (wraps primary page component)
//**************************************************

var ControllerView = React.createClass({
  getInitialState: function() {
    return this.props.model.get();
  },
  componentDidMount: function() {
    this.props.model.register(this.update());
  },
  update: function() {
    alert("Controller-View is being updated");
    this.setState(this.props.model.get());
  },
  render: function() {
    return ( <EntryList data={this.state}/> );
  }
});

//**************************************************
// Action-Creator functions interact with the server
//**************************************************

var ActionCreator = {
  deleteRecord: function(id) {
    alert("Deleting record " + id);
    // do ajaxy stuff
    AppDispatcher.dispatch({ actionType: ActionTypes.DELETE_ACTION });
  },
  modifyRecord: function(id, value) {
    alert("Modifying record " + id + " with new value:\n" + value);
    // do ajaxy stuff
    AppDispatcher.dispatch({ actionType: ActionTypes.MODIFY_ACTION });
  },
  addRecord: function(value) {
    alert("Creating new record with value:\n" + value);
    // do ajaxy stuff
    AppDispatcher.dispatch({ actionType: ActionTypes.ADD_ACTION });
  }
}

//**************************************************
// Page Initialization
//**************************************************

var Sample = {
  serviceUrl: "http://rdoherty.plasmodb.org/plasmo.rdoherty/service/",
  loadPage: function() {
    jQuery.ajax({
      type: "GET",
      url: Sample.serviceUrl + "sample",
      data: { expandRecords: true },
      dataType: "json",
      success: function(data) {
        // initialize the model with the fetched state
        Model.initialize(data);
        // create top-level view-controller
        React.render(<ControllerView model={Model}/>, document.body);
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        alert("Error: Unable to load initial data");
      }
    });
  }
};

Sample.loadPage();

