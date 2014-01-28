describe('wdk.core.base_object', function() {

  it('should call a user-defined constructor', function() {
    var A = wdk.core.BaseObject.extend({
      constructor: function(name) {
        this.name = name;
      }
    });

    var a = A.create('Dave');

    expect(a.name).toEqual('Dave');

  })

});
