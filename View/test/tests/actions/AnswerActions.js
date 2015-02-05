import assert from 'assert';
import * as AnswerActions from 'wdk/flux/actions/AnswerActions';

describe('AnswerActions', function() {

  describe('loadAnswer', function() {

    before(function() {
      sinon.stub($, 'ajax');
      $.ajax.returns(new Promise(function(resolve) {
        resolve(true);
      }));
    });

    after(function() {
      $.ajax.restore();
    })

    it('should trigger a POST request', function() {
      AnswerActions.loadAnswer('MyQuestion');
      assert($.ajax.calledWithMatch({ type: 'POST' }), `$.ajax called with ${$.ajax.args[0]}`)
    });

  })

});
