import assert from 'assert';
import Application from 'wdk/core/application';


// polyfill Class.create
Function.prototype.create = function(...args) {
  return new this(...args);
}


describe('wdk.core.application', function() {

  var mockDom, TestApp;

  class DefaultView {
    constructor(done) {
      this.type = 'view';
      if (done) done();
    }
  }

  class MyView {
    constructor() {
      this.type = 'view';
    }
  }

  beforeEach(function() {
    var htmlString = '<div>' +
                       '<div id="mock-view" data-view-name="my-view" data-view-default="default-view"></div>' +
                     '</div>'
    mockDom = $(htmlString);
    TestApp = Application.extend({
      rootElement: mockDom
    });
  });

  describe('Application', function() {

    describe('constructor', function() {
      it('should use body as the default rootElement', function() {
        var app = Application.create();
        assert(app.rootElement === 'body')
      });
    });

    describe('registerView', function() {
      it('should register views', function(done) {
        var app = TestApp.create({
          ready: function() {
            var View = app.getView('my-view');
            assert(View === MyView);
            done();
          }
        });
        app.registerView('my-view', MyView);
      });

      it('should initialize matched views', function(done) {
        var app = TestApp.create({
          ready: done
        });
        app.registerView('my-view', MyView);
      });

      // not sure we should provide this -- seems like an anti-pattern
      xit('should use default view when unmatched', function(done) {
        var app = TestApp.create({
          ready: done
        });
        app.registerView('default-view', DefaultView);
      });
    });


    describe('initializeDOM', function() {

      it('should initialize the rootElement on app ready', function(done) {
        TestApp.create({
          ready: function() {
            assert(this.rootElement.attr('__initialized'));
            done();
          }
        });
      });

      it('should accept an Element as an argument', function() {
        var element = document.createElement('div');
        var app = TestApp.create();
        app.initializeDOM(element);
        assert(element.getAttribute('__initialized'));
      });

      it('should accept an HTML string as an argument', function() {
        var app = TestApp.create();
        var $el = app.initializeDOM('<div></div>');
        assert($el.attr('__initialized'));
      });

    });

    describe('ready callback', function() {

      it('should be called when the app is created', function(done) {
        TestApp.create({
          ready: function() {
            done();
          }
        });
      })

      it('should be called after views are initialized', function(done) {
        TestApp.create({
          ready: function() {
            assert(this.rootElement.attr('__initialized'));
            done();
          }
        });
      });

    });

  });
});
