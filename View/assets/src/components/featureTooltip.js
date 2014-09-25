wdk.namespace('wdk.components', function(ns, $) {
  'use strict';

  // imports
  var preventEvent = wdk.fn.preventEvent,
      assetsUrl = wdk.assetsUrl;

  // Creates a feature tooltip.
  //
  // opts:
  //   * el is the tooltip target
  //   * featureType is the type of feature
  //   * title is the title of the tooltip (see http://qtip2.com/options#content.title)
  //   * text is the content of the tooltip (see http://qtip2.com/options#content.text)
  //   * container is optional and delegates to position.container
  //     (see http://qtip2.com/options#position.container)
  ns.createFeatureTooltip = function createFeatureTooltip(opts) {
    var $el = opts.el;
    var key = opts.key;
    var title = opts.title;
    var text = opts.text;
    var container = opts.container;
    var dismissedStorageKey = 'featureTooltip::dismissed::' + key;
    var sessionId = $.cookie('JSESSIONID');
    var dismissValue = localStorage.getItem(dismissedStorageKey);

    $el = $el instanceof $ ? $el : $($el);

    if (dismissValue == '1' || dismissValue == sessionId) return;

    return $el
      .wdkTooltip({
        content: {
          text: text,
          // button: 'Got it!',
          title: '<img title="This is a new search!" alt="New feature icon" ' +
                 'src="' + assetsUrl('/wdk/images/new-feature.png') + '"> ' +
                 title
        },
        style: {
          classes: 'qtip-bootstrap wdk-feature-tooltip',
          tip: {
            width: 48,
            height: 64
          }
        },
        position: {
          my: 'left center',
          at: 'right center',
          viewport: true,
          container: container
        },
        hide: {
          event: false
        },
        show: {
          event: false,
          ready: true,
          effect: false
        },
        events: {
          hide: function(e, api) {
            if (e.originalEvent.type === 'tooltipsolo') {
              e.preventDefault();
            } else {
              if (api.cache.remember) {
                localStorage.setItem(dismissedStorageKey, 1);
              }
              else {
                localStorage.setItem(dismissedStorageKey, sessionId);
              }
              api.destroy();
            }
          },
          show: function(e, api) {
            var anchor = $('<div class="dismiss-wrapper">' +
                           '  <a href="#dismiss">Got it!</a>' +
                           '  <label><input type="checkbox" name="remember"/>' +
                           ' Don\'t bother me anymore.</label>' +
                           '</div>')
              .on('click', 'a', preventEvent(api.hide.bind(api)))
              .on('change', 'input', function(e) {
                api.cache.remember = e.target.checked;
              });

            api.elements.content.append(anchor);
          }
        }
      });
  };

});
