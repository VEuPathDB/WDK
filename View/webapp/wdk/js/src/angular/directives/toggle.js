angular.module('wdk.directives')

.directive('wdkToggle', function() {
  return {
    restrict: 'C',
    //replace: true,
    scope: {
      title: '@'
    },
    transclude: true,
    template: '<div>' +
                '<h3 title="Click to show or hide">' +
                  '<a href="" ng-click="opened = !opened">{{title}}</a></h3>' +
                '<div ng-hide="!opened" ng-transclude></div>' +
              '</div>',

    link: function(scope, element, attrs) {
      scope.opened = attrs.opened === 'true';
    }
  }
})

// .directive('wdkToggleGroup', function() {
//   return {
//     restrict: 'C',
//     link: function(scope, element, attrs) {
//       var container = element.closest(attrs.container),
//           toggles = container.find('.wdk-toggle');
// 
//       element.click(function(e) {
//         e.preventDefault();
//         toggles.simpleToggle('toggle', element.data('show'));
//       });
//     }
//   }
// });
