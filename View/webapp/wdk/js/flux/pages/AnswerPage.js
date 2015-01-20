import React from 'react';
import Router from 'react-router';
import Answer from '../components/Answer';
import AnswerStore from '../stores/AnswerStore';
import StoreMixin from '../mixins/StoreMixin';
import { loadAnswer } from '../actions/AnswerActions';

export default React.createClass({

  displayName: 'AnswerPage',

  mixins: [ Router.State, StoreMixin(AnswerStore) ],

  render() {
    var { questionName } = this.getParams();
    return (
      <div>
        <Answer questionName={questionName} {...this.state}/>
      </div>
    );
  },


  statics: {

    willTransitionTo(transition, params, query) {
      /** FIXME Put defaults in loadAnswer */
      var offset = Number(query.offset) || 1;
      var numRecords = Number(query.numrecs) || 100;
      var displayInfo = { pagination: { numRecords, offset } };
      loadAnswer(params.questionName, { displayInfo });
    }

  }

});
