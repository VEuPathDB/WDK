
/* Basic HTML tooltip functionality.  Tooltip appears during target
 * mousover, then disappears on mouseout.  Only one tooltip at a time.
 * Tooltips appear with "arrow" on the top middle, centered 3 pixels
 * below the "default".  X-coordinate can be changed (positive = to
 * the right, negative to the left).
 */
function assignTooltips(selector, xOffset) {
  doTooltips(selector, xOffset, 'top-center', 'bottom-center');
}
function assignTooltipsLeft(selector, xOffset) {
  doTooltips(selector, xOffset, 'top-right', 'bottom-left');
}

function doTooltips(selector, xOffset, tipPos, targetPos) {
  $(selector).qtip({
    position: {
      adjust: {
        x: (xOffset == undefined ? 0 : xOffset),
        y: 3
      },
      my: tipPos,
      at: targetPos
    },
    show: {
      solo: true,
      event: 'mouseenter'
    },
    hide: {
      event: 'mouseleave'
    }
  });
}

/* "Sticky" HTML tooltip functionality, with a few custom settings
 * for question params.  Behavior:
 *   - Appears when target is clicked
 *   - Appears below and to the left of target (arrow on top right)
 *   - Only one tooltip at a time
 *   - Disappears if target is clicked again
 *   - Disappears if tooltip itself is clicked
 *   - Disappears after 10 seconds if neither of the above happen first
 */
function assignParamTooltips(selector) {
  var expireSecs = 10;

  $(selector).qtip({
    position: {
      adjust: {
        y: 3
      },
      my: 'top-right',
      at: 'bottom-center'
    },
    show: {
      solo: true,
      event: 'click'
    },
    hide: {
      event: 'click'
    },
    events: {
      show: function(event, api) {
        // qtip2 assigns an ID of "ui-tooltip-<id>" to the tooltip div
        var tipSelector = '#ui-tooltip-' + api.get('id');

        // hide the tooltip 'expireSecs' seconds after it appears
        setTimeout("$('" + tipSelector + "').qtip('hide');", (expireSecs*1000));

        // hide the tooltip if the tooltip is clicked
        $(tipSelector).click( function(){ $(tipSelector).qtip('hide'); } );
      }
    }
  });
}
