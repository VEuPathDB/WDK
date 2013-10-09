angular.module('wdk.directives')
.directive('wdkTooltip', function() {
  return {
    restrict: 'C',
    link: function(scope, element, attrs) {
      element.wdkTooltip();
    }
  }
});
