// jQuery widget passthrough

angular.module('wdk.directives')
  .directive('widget', function() {
    return {
      restrict: 'C',
      link: function(scope, element, attrs) {
        var widget = attrs.widget,
            options = scope.$eval('[' + attrs.options + ']')[0];
        if (jQuery.fn[widget]) {
          element[widget](options);
        }
      }
    };
  })

  .directive('widgetTwo', function() {
    return {
      restrict: 'C',
      transclude: true,
      replace: true,
      template: '<div class="widget:qtip" options="{position: {viewport: $(\'body\')}}" ng-transclude></div>'
    }
  });
