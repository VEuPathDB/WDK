import assert from 'assert';
import * as AnswerActions from 'wdk/client/actions/AnswerActions';

describe('AnswerActions', function() {

  describe('loadAnswer', function() {

    before(function() {
      sinon.stub($, 'ajax');
    });

    after(function() {
      $.ajax.restore();
    })

    it('should trigger a POST request', function() {
      AnswerActions.loadAnswer('MyQuestion');
      assert($.ajax.calledWithMatch({ type: 'POST' }))
    });

  })

});
