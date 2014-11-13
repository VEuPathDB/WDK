// WDK filter layout related scripts

wdk.util.namespace("window.wdk.filter", function(ns, $) {
  "use strict";

  function WdkFilterNew($el) {
    
    this.initialize = function() {
      $el.find(".filters-panel").accordion({
        collapsible: true,
        active: false,
      });

      // initialize current filters
      this.initCurrentFilters();      


      // initialize filter list
      $el.find(".filter").each(function() {
        var filter = $(this);
        filter.qtip({
          content: { text: filter.find(".description") }
        });
        filter.click(function() {
          $el.find(".filter.highlight").removeClass("highlight");
          filter.addClass("highlight");
 
          var detail = $el.find(".filter-detail");
          detail.find("input[name=filter]").val(filter.attr("id"));
          detail.find(".description").html(filter.find(".description"));
          var url = "getFilterSummary.do?step=" + $el.data("step") + "&filter=" + filter.attr("id");
          var summary = detail.find(".filter-summary");
          summary.load(url, function() {
            $el.find(".filters-panel").accordion("refresh");
          });
        });
      });
    };

    this.initCurrentFilters = function() {
      $el.find(".current-filters .option").each(function() {
        var key = $(this).attr("id");
        $(this).find("input").change(function() {
          var disabled = this.checked : "false" : "true";
          var url = $(this).data("url") + "&disabled=" + disabled;
          window.location = url;
        });
      });
    };
  }




  function WdkFilter($el) {

    this.initialize = function() {
      this.addShowHide();
      this.displayFilters();

      $el.on('click', 'a.link-url', function changeFilter(event) {
        var $target = $(event.currentTarget);
        var strategyId = $target.attr('strId');
        var stepId = $target.attr('stpId');
        var url = $target.attr('linkurl');
        var filterElement = $target.closest('.filter-instance')[0];
        wdk.strategy.controller.ChangeFilter(strategyId, stepId, url, filterElement);
      });
    };

    this.addShowHide = function() {
      $el.find(".filter-layout .layout-info .handle").click(function() {
        var content = $(this).parents(".filter-layout").children(".layout-detail");
        content.slideToggle("normal");

        var src = this.src;
        var image = src.substr(src.lastIndexOf("/") + 1);
        src = src.substr(0, src.lastIndexOf("/") + 1);
        if (image == "minus.gif") {
          this.src = src + "plus.gif";
        } else {
          this.src = src + "minus.gif";
        }
      });
    };

    this.displayFilters = function() {
      $el.find(".filter-instance").each(function() {
        // add mouse over to the link
        var detail = $(this).find(".instance-detail");
        //cris: z-index added to show filter popup over column titles in result

        //cris: code added to keep a filter popup from hiding outside the window on the right side
        var popupWidth = 300;
        var winWidth=0;
        if (document.documentElement) {
          winWidth = document.documentElement.offsetWidth;
        } else if (window.innerWidth && window.innerHeight) {
          winWidth = window.innerWidth;
        }
        $(this).hover(function() {
          var position = $(this).position();
          var left = position.left;
          var winMinusLeft = winWidth - left;
          if ( winMinusLeft < (popupWidth + 40) ) {
            left = left - (popupWidth - winMinusLeft + 40);  
            detail.css("left", left + "px");
          }
          detail.css("width", popupWidth + "px");
          detail.css("display", "block");
          detail.css("z-index", "10");
        },
        function() {
          detail.css("display", "none");
        });
      });
    };

    this.loadFilterCount = function() {
      if ($el.data("loaded") == "true") return;

      var countUrl = $el.data("count-url");
      $.getJSON(countUrl, function(data) {
        $el.find(".link-url").each(function() {
          if (!$(this).hasClass("loaded")) {
            var filter = $(this).data("filter");
            var count = data[filter];
            $(this).html((count == -1 || count === undefined) ? "error" : count);
          }
        });
      });

    /*
      var links = $('.filter-instance .link-url[countref]');

      // TODO generalize the following into a reusable function

      // map to $.ajax, which returns a thenable
      // this allows us to download all in parallel
      links.toArray().map(function getCount(element, i) {
        // FIXME Use a Promise library, such as Q
        // jQuery.Deferred deviates form the Promises/A+ spec
        // in that jQuery.Deferred.fail doesn't return a new
        // promise, which breaks composition.
        var countUrl = $(element).attr('countref');
        var deferred = $.Deferred();
        $.get(countUrl).then(deferred.resolve,
            deferred.resolve.bind(null, 'Error'));
        return deferred.promise();
      })
      // Create a Promise sequence such that when $.ajax is resolved,
      // we immediately add the value to the page. This seqeunce effectively
      // interleaves ajax completion and adding the value to the page.
        .reduce(function addToSequence(sequence, countPromise, i) {
          return sequence.then(function() {
            return countPromise;
          }).then(function updateCount(count) {
            var link = links.eq(i);
            if (count.match(/^\d+$/g) != null) {
              if (count == '0') {
                // replace with a non-link
                link.replaceWith('<span class="link-url" id="' +
                    link.attr('id') + '">' + count + '</span>');
              } else {
                link.text(count);
              }
            } else {
              // must be an error
              link.replaceWith('<em style="color:red;">Error</em>');
            }
          })
        }, $.Deferred().resolve());
    */
    };
  }

  ns.WdkFilter = WdkFilter;
  ns.WdkFilterNew = WdkFilterNew;


});
