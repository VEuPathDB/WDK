wdk.util.namespace("window.wdk.dyk", function(ns, $) {
  "use strict";

  var currentTip = 0;
  var tipMax;
  var tips = null;
  var dykHide = 'hide';
  var dykHidePermanent = 'permanent_hide';

  function initHelp() {
    var url = "showXmlDataContent.do?name=XmlQuestions.StrategiesHelp";
    $.ajax({
      url: url,
      type: "POST",
      data: "",
      dataType: "html",
      success: function(data){
        $("div#help div.h2center").after($("div#strategyTips",data).html() + "<hr />");
        // $("div#strategyTips span[id^='tip_']",data).each(function() {
        //   $("#dyk-box div#content").append(this);
        // });
        // if($("div#strategy_results").css("display") == 'none') {
        //   initDYK(false);
        // } else if($("div#Strategies").attr("newstrategy") == 'true') {
        //   initDYK(true);
        // } else {
        //   initDYK(false);
        // }
      }
    });
  }

  function initDYK(o,co) {
    setTipMax();

    if (co == undefined) co = $.cookie("DYK");

    if (!o) {
      tips = $("#dyk-box,#dyk-shadow").hide();
      return;
    } else if (co == dykHide || co == dykHidePermanent) {
      tips = $("#dyk-box,#dyk-shadow").hide();
      if (co == dykHidePermanent) setDYKCookie(true); // Renew the cookie if needed
      return;
    }

    $("#dyk-box,#dyk-shadow").show();
    var randomnumber=Math.floor(Math.random()*tipMax);
    setCurrent(randomnumber);
    $("#dyk-box input#close").click(function(){
      dykClose();
    });
    
    // In some cases, repeated calls to initDYK result in
    // multiple bindings;  to avoid this, unbind any click
    // handler before re-attaching.
    $("#dyk-box input#previous").unbind('click');
    $("#dyk-box input#previous").click(function(){ prevTip(); });
    $("#dyk-box input#next").unbind('click');
    $("#dyk-box input#next").click(function(){ nextTip(); });

    $("div#dyk-box").resizable({
      minWidth: 405,
      minHeight: 87,
      alsoResize: '#dyk-shadow,#dyk-text'
    });

    $("div#dyk-shadow").resizable();

    $("div#dyk-box").draggable({
      handle: ".dragHandle",
      containment: 'window',
  /*    drag: function(e, u){
        var lef = $(this).css('left');
        var to = $(this).css('top');
        lef = parseInt(lef.split("px")[0], 10) - 3;
        to = parseInt(to.split("px")[0], 10) + 3;
        $("div#dyk-shadow").css({
          top: to + "px",
          left: lef + "px"
        });
      }*/
      start:function(e,ui){
        $("#dyk-shadow").hide();
      },
      stop:function(e,ui){
        var lef = $(this).css('left');
        var to = $(this).css('top');
        lef = parseInt(lef.split("px")[0], 10) + 6;
        to = parseInt(to.split("px")[0], 10) + 6;
        $("div#dyk-shadow").css({
          top: to + "px",
          left: lef + "px"
        });
        $("#dyk-shadow").show();
      }
    });
  }

  function setTipMax() {
    tipMax = $("#dyk-box span[id^='tip_']").length;
  }

  function setCount() {
    $("#dyk-count").text((currentTip + 1) + " of " + (tipMax));
  }

  function displayCurrent() {
    if (currentTip >= 0 && currentTip < tipMax ) {
      $("#dyk-box div#dyk-text").html($("#dyk-box span#tip_" + currentTip).html());
    } else {
      if (currentTip < 0) {
        currentTip = tipMax - 1;
      } else {
        currentTip = 0;
      }
      $("#dyk-box div#dyk-text").html($("#dyk-box span#tip_" + currentTip).html());
    }

    $("#dyk-box div#dyk-text a").click(function(){
      wdk.addStepPopup.showPanel('help');
    });

    setCount();
  }

  function setCurrent(tipnum){
    currentTip = tipnum;
    displayCurrent();
  }

  function nextTip(){
    currentTip = parseInt(currentTip, 10) + 1;
    displayCurrent();
  }

  function prevTip(){
    currentTip = parseInt(currentTip, 10) - 1;
    displayCurrent();
  }

  function dykOpen(){
    //  $(tips[0]).find("input#stay-closed-check").attr("disabled",true);
    //  $("div.innertube").append(tips[0]);
    //  $("div.innertube").append(tips[1]);
    initDYK(true, false);
  }

  function dykClose(){
    var ex = $("div#dyk-box input#stay-closed-check").attr("checked");
    setDYKCookie(ex);
    tips = $("#dyk-box,#dyk-shadow").hide();
  }

  function setDYKCookie(expire){
    if (expire) {
      $.cookie("DYK", dykHidePermanent, {
        domain: secondLevelDomain(),
        path: '/',expires: 300
      }); // Use different value if user checked "never show again"
    } else {
      $.cookie("DYK",dykHide,{domain: secondLevelDomain(),path: '/'});
    }
  }

  // return domain.org from www.domain.org
  function secondLevelDomain(){
    var dm = document.domain.split(/\./);
    if(dm.length > 1) {
      return(dm[dm.length-2] + "." +  dm[dm.length-1]) ;
    }else{
      return("");
    }
  }

  ns.initDYK = initDYK;
  //ns.dykOpen = dykOpen;
  ns.dykOpen = function() {
    var s = angular.element(".dyk").scope();
    s.$apply(s.open());
  };
  ns.dykClose = dykClose;
  ns.initHelp = initHelp;

});
