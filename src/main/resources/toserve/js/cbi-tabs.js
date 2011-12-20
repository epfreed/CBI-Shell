$(function() {
    var $tabs = $( "#tabs").tabs()
    // close icon: removing the tab on click
    // note: closable tabs gonna be an option in the future - see http://dev.jqueryui.com/ticket/3924
    $( "#tabs span.ui-icon-close" ).live( "click", function() {
        var index = $( "li", $tabs ).index( $( this ).parent() );
        $tabs.tabs( "remove", index );
    });
});


