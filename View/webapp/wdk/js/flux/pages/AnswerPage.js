import React from 'react';
import Router from 'react-router';
import Answer from '../components/Answer';
import AnswerPageStore from '../stores/AnswerPageStore';
import StoreMixin from '../mixins/StoreMixin';
import { loadAnswer } from '../actions/AnswerPageActions';

export default React.createClass({

  displayName: 'AnswerPage',

  propTypes: {
    /** TODO Define object shapes */
    query: React.PropTypes.shape({
      numrecs: React.PropTypes.string,
      offset: React.PropTypes.string
    }),
    params: React.PropTypes.shape({
      questionName: React.PropTypes.string.isRequired
    }).isRequired
  },

  mixins: [ StoreMixin(AnswerPageStore), Router.State ],

  componentWillMount() {
  },

  render() {
    var content;
    var { questionName } = this.getParams();

    return (
      <div>
        <h2>{questionName}</h2>
        <Answer questionName={questionName} {...this.state}/>
      </div>
    );
  },

  statics: {

    willTransitionTo(transition, params, query) {
      /** FIXME Put defaults in loadAnswer */
      var numRecords = Number(query.numrecs) || 100;
      var offset = Number(query.offset) || 0;
      var displayInfo = { pagination: { numRecords, offset } };
      loadAnswer(params.questionName, { displayInfo });
    }

  }

});
