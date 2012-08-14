// =============================================================================
// The js related to the display on question page
// $(document).ready(initializeQuestion);

function initializeQuestion() {
    var question = new WdkQuestion();
    question.registerGroups();
}

function WdkQuestion() {

    this.registerGroups = function() {
        $(".param-group[type='ShowHide']").each(function() {
            // register the click event
            var name = $(this).attr("name");
            var expire = 365;   // in days
            $(this).find(".group-handle").click(function(event) {
                // only call once
                event.stopImmediatePropagation();

                var handle = this;
                var path = handle.src.substr(0, handle.src.lastIndexOf("/"));
                var detail = $(this).parents(".param-group").children(".group-detail");
                detail.toggle();
                if (detail.css("display") == "none") {
                    handle.src = path + "/plus.gif";
                    WDK.createCookie(name, "hide", expire);
                } else {
                    handle.src = path + "/minus.gif";
                    WDK.createCookie(name, "show", expire);
                }
            });
            
            // decide whether need to change the display or not
            var showFlag = WDK.readCookie(name);
            if (showFlag == null) return;
            
            var status = $(this).children(".group-detail").css("display");
            if ((showFlag == "show") && (status == "none")) {   
                // should show, but it is hidden
                $(this).find(".group-handle").trigger("click");
            } else if ((showFlag == "hide") && (status != "none")) {
                // should hide, bit it is shown
                $(this).find(".group-handle").trigger("click");
            }
        });
    };
}
