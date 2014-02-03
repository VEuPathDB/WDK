describe('wdk.core.application', function() {

  describe('Application', function() {
    var Application = wdk.core.Application;
    var BaseObject = wdk.core.BaseObject;

    describe('constructor', function() {
      it('should use body as the default rootElement', function() {
        var app = Application.create();
        expect(app.rootElement).toBe('body');
      });
    });

    var mockDom = $('<div>' +
                    '<div id="mock-view" data-view-name="my-view" data-view-default="default-view"></div>' +
                    '</div>');
    var TestApp = Application.extend({
      rootElement: mockDom
    });

    describe('registerView', function() {
      it('should register views', function(done) {
        var MyView = BaseObject.extend({
          type: 'view'
        });

        var app = TestApp.create({
          ready: function() {
            var View = app.getView('my-view');
            expect(View).toEqual(MyView);
            done();
          }
        });
        app.registerView('my-view', MyView);
      });

      it('should initialize matched views', function(done) {
        var MyView = BaseObject.extend({
          type: 'view',
          constructor: function() {
            done();
          }
        });

        var app = TestApp.create();
        app.registerView('my-view', MyView);
      });

      it('should use default view when unmatched', function(done) {
        var app = TestApp.create();
        var DefaultView = BaseObject.extend({
          type: 'view',
          constructor: function() {
            done();
          }
        });

        app.registerView('default-view', DefaultView);
      });
    });


    describe('initializeDOM', function() {

      it('should initialize the rootElement on app ready', function(done) {
        TestApp.create({
          ready: function() {
            expect(this.rootElement.attr('__initialized')).toBe('true');
            done();
          }
        });
      });

      it('should accept an Element as an argument', function() {
        var element = document.createElement('div');
        var app = TestApp.create();
        app.initializeDOM(element);
        expect(element.getAttribute('__initialized')).toBe('true');
      });

      it('should accept an HTML string as an argument', function() {
        var app = TestApp.create();
        var $el = app.initializeDOM('<div></div>');
        expect($el.attr('__initialized')).toBe('true');
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
            expect(this.rootElement.attr('__initialized')).toBe('true');
            done();
          }
        });
      });

    });

  });
});
