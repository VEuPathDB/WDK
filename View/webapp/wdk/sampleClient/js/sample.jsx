
//**************************************************
// Define the depended service
//**************************************************

var ServiceUrl = "http://rdoherty.plasmodb.org/plasmo.rdoherty/service/";

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
      buttons: [
        {
          text: "OK",
          click: function() {
            var value = jQuery(this).find("textarea").val();
            try {
              value = JSON.parse(value);
            }
            catch (e) {
              alert("JSON not properly formatted.\n\n" + e + "\n\nPlease try again.");
              return;
            }
            jQuery(this).dialog("close");
            if (op == "add")
              ActionCreator.addRecord(value);
            else
              ActionCreator.modifyRecord(id, value);
          }
        },{
          text: "Cancel",
          click: function() {
            jQuery(this).dialog("close");
          }
        }]
    });
  },
  deleteRecord: function(event) {
    var id = jQuery(event.target).data("id");
    if (confirm("Are you sure you want to delete record " + id + "?")) {
      ActionCreator.deleteRecord(id);
    }
  },
  resetData: function(event) {
    if (confirm("Are you sure you want to restore the above data to its original state?")) {
      ActionCreator.resetData();
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
        <input type="button" value="Reset Data" onClick={component.resetData}/>
      </div>
    );
  }
});

//**************************************************
// Dispatcher
//**************************************************

var Dispatcher = new Flux.Dispatcher();

// types of actions sent through the dispatcher
var ActionType = {
  ADD_ACTION: "addAction",
  MODIFY_ACTION: "modifyAction",
  DELETE_ACTION: "deleteAction",
  RESET_ACTION: "resetAction"
}

//**************************************************
// Model
//**************************************************

var Model = {

  model: {},

  registeredCallbacks: [],

  register: function(callback) {
    Model.registeredCallbacks.push(callback);
  },

  updateRegisteredViews: function() {
    Model.registeredCallbacks.forEach(function(func) { func(this); });
  },

  handleAction: function(payload) {
    // update the model here
    // data in form { id: int, value: string }
    switch(payload.actionType) {
      case ActionType.ADD_ACTION:
        Model.model[payload.data.id] = payload.data.value;
        break;
      case ActionType.MODIFY_ACTION:
        Model.model[payload.data.id] = payload.data.value;
        break;
      case ActionType.DELETE_ACTION:
        delete Model.model[payload.data.id];
        break;
      case ActionType.RESET_ACTION:
        Model.model = payload.data;
      default:
        // this model does not support other actions
    }
    // then alert registered views of change
    Model.updateRegisteredViews();
  },

  initialize: function(initialValue) {
    Model.model = initialValue;
    Dispatcher.register(Model.handleAction);
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
    this.props.model.register(this.update);
  },
  update: function() {
    var newModel = this.props.model.get();
    alert("Updating view with model: " + JSON.stringify(newModel));
    this.setState(newModel);
  },
  render: function() {
    return ( <EntryList data={this.state}/> );
  }
});

//**************************************************
// Action-Creator functions interact with the server
//**************************************************

var ActionCreator = {
  addRecord: function(value) {
    jQuery.ajax({
      type: "POST",
      url: ServiceUrl + "sample",
      contentType: 'application/json; charset=UTF-8',
      data: JSON.stringify(value),
      dataType: "json",
      success: function(data, textStatus, jqXHR) {
        Dispatcher.dispatch({ actionType: ActionType.ADD_ACTION, data: { id: data.id, value: value }});
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        alert("Error: Unable to create new record");
      }
    });
  },
  modifyRecord: function(id, value) {
    jQuery.ajax({
      type: "PUT",
      url: ServiceUrl + "sample/" + id,
      contentType: 'application/json; charset=UTF-8',
      data: JSON.stringify(value),
      success: function(data, textStatus, jqXHR) {
        Dispatcher.dispatch({ actionType: ActionType.MODIFY_ACTION, data: { id: id, value: value }});
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        alert("Error: Unable to update record with ID " + id);
      }
    });
  },
  deleteRecord: function(id) {
    jQuery.ajax({
      type: "DELETE",
      url: ServiceUrl + "sample/" + id,
      success: function(data, textStatus, jqXHR) {
        Dispatcher.dispatch({ actionType: ActionType.DELETE_ACTION, data: { id: id }});
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        alert("Error: Unable to delete record with ID " + id);
      }
    });
  },
  resetData: function() {
    jQuery.ajax({
      type: "GET",
      url: ServiceUrl + "sample/reset",
      data: { expandRecords: true },
      success: function(data, textStatus, jqXHR) {
        Dispatcher.dispatch({ actionType: ActionType.RESET_ACTION, data: data });
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        alert("Error: Unable to reset record data");
      }
    });
  }
}

//**************************************************
// Page Initialization
//**************************************************

var Sample = {
  loadPage: function() {
    jQuery.ajax({
      type: "GET",
      url: ServiceUrl + "sample",
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

