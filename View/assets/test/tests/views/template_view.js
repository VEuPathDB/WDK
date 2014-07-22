xdescribe('wdk.views.template_view', function() {

  describe('TemplateView', function() {

    var myView;

    beforeEach(function() {
      var fixture = $("#test-fixture");
      fixture.html('<script id="my/template" type="text/x-handlebars-template">' +
                   '<h1>{{title}}</h1>' +
                   '<div class="body">{{body}}</div>' +
                   '</script>');
      var MyView = wdk.views.TemplateView.extend({
        templateName: 'my/template'
      });

      var myModel = new Backbone.Model({
        title: 'Hello',
        body: 'This is my body'
      });

      myView = new MyView({
        model: myModel
      });

    });

    afterEach(function() {
      $('#test-fixture').html('');
    });

    it('should compile a template', function() {
      expect(myView.$('h1').text()).to.equal(myView.model.get('title'));
      expect(myView.$('.body').text()).to.equal(myView.model.get('body'));
    });

    it('should update the template on model attribute changes', function() {
      myView.model.set('title', 'Howdy');
      expect(myView.$('h1').text()).to.equal('Howdy');
    });

  });

});
