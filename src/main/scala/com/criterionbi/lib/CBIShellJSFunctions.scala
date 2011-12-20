package com.criterionbi.lib

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.http.SHtml._
import net.liftweb.http.js._
import net.liftweb.common.{Box, Full,Logger}
import com.criterionbi.lib.CBIShellProperties._

object CBIShellJSFunctions extends Logger {

 def makeMenuJS : net.liftweb.http.js.JsCmd  =  {
    Show("menucol") &
    JsRaw("$('ul.sf-menu').supersubs({ minWidth:    15,  maxWidth:    27, extraWidth:  1 })" + 
      ".superfish({delay:1000, speed:'fast', animation: {height:'show'},})") &
    Hide("cbimenu") & Hide("menucol") &
    JsRaw("$('#menuload').fadeOut(500, function() {$('#menucol').fadeIn(1, function(){ });})") &
    FadeIn("cbimenu", 0, 1500)
  }

  def clearMenuJS: net.liftweb.http.js.JsCmd  = 
    JsRaw("$('#menucol').fadeOut(500, function() { $('#menuload').fadeIn(1000, function(){ });})") 
  

  def addTab(tabIndex: String, name: String) =  
    JsRaw("$('#tabs').tabs( 'add', '#" + tabIndex + "', '" + name + "' )")

  def makeTabRemovable(tabIndex: String) =  
    JsRaw("""$('a[href="#""" + tabIndex + """"]').after("<span class='ui-icon ui-icon-close'>Remove Tab</span>")""")
    
  def selectTab(tabIndex: String) = JsRaw("$('#tabs').tabs( 'select' , '" + tabIndex + "')" )
    
  def closeMenu =  JsRaw("$('ul.sf-menu').hideSuperfishUl()" )

  def freshenLoginer = JsRaw("$(\"#xactionLoginer\").replaceWith('" + loginerFrame + "')")

  def openTabs(url: String, name: String) = JsRaw("tabadderbasic(\"" + url + "\", \"" + name + "\")")

  //TODO make a startup List that is map'ed to this
  //with defaults 
  def openStartUpTabs() = openTabs("static/welcome.html", "Welcome to Criterion BI")

  def startupScript =
    JsCrVar("$tabs", JsRaw("""$("#tabs").tabs({ spinner: 'Retrieving data...' })""" )) &
    JsRaw("""$("#tabs span.ui-icon-close" ).live( "click", function() {  
      var index = $( "li", $tabs).index( $( this ).parent() );  
       $tabs.tabs( "remove", index );  
      });
    $("#mainwindow").data("tabNumber", 1);
    $("#tabs").tabs("remove", "#tabs-0")""") &
    Call("openLoginer") & 
    openStartUpTabs()

  val urlHeader = biProto + "://" + address

  val loginerXactionUrl = urlHeader + "/pentaho/ViewAction?solution=Criterion+Utils&path=&action=loginer.xaction"
  
  val scriptUrl  = urlHeader + "/pentaho/SolutionRepositoryService?component=getSolutionRepositoryDoc"

  val refreshURL =  urlHeader + "/pentaho/Publish?publish=now&class=org.pentaho.platform.engine.services.solution.SolutionPublisher"

  val getStoredMenu =   JsRaw("""$('#menucol').data("xmlMenu")""" )

  val loginerFrame = <div id="xactionLoginer"><iframe onload="getMenu()" id="xactionLoginer" style="display: none" src={loginerXactionUrl} height="0" width="0"></iframe></div>

//------------------------------------------------------------------
// these are the Javascript functions that are called

  def definedOpenLoginer = JsRaw("function openLoginer(){$('#mainwindow').after('" + loginerFrame + "')}")

  //TODO make a generic ajaxcaller that takes the URL and callback function
  def definedGetMenu = JsRaw("""function getMenu(){  
        $.ajax({ url: """" + scriptUrl + """", 
          type: 'get', dataType: 'text', 
          async: true, success: function(rdata) {
            $('#menucol').data("xmlMenu", rdata); 
            ajaxCall() }});}"""
      )

  //this refreshes the repository and then gets menu
  // only admin can call the rereshURL
  def definedRefreshRepoAndGetMenu =  JsRaw(
    """function refreshAndAjax(){  
          $.ajax( {  
            url: """" +  refreshURL + """",  
            type: 'get',  
            dataType: 'text',  
            async: true,  
            success: function(data) { result =  getMenu() }  });  }"""
        )



  def defineTabOpenerBasic = JsRaw(
    """function tabadderbasic(url, tabName){  
       $("#mainwindow").data("tabNumber", $("#mainwindow").data("tabNumber") + 1);  
       var tabNumber = $("#mainwindow").data("tabNumber");  
       var tabIndex  = "tabs-" +  tabNumber;  
       var iframeID  = "iframe-tabs-" + tabNumber;  
       var winHeight = $(window).height() * .87;  
       $('#tabs').tabs( 'add', "#" + tabIndex, tabName );  
       $('a[href=#' + tabIndex + ']').after("<span class='ui-icon ui-icon-close'>Remove Tab</span>");  
       $("#" + tabIndex).html("<iframe class='ui-corner-all tab-content' id=" + iframeID + " src=" + url + "></iframe>");  
       $("#" + iframeID).css('height', winHeight);  
       $('#tabs').tabs( 'select' , tabIndex );   
        }"""
      )
   

  def defineTabOpener = JsRaw(
    """function tabadder(url, tabName){
        tabadderbasic(url, tabName);
        $('ul.sf-menu').hideSuperfishUl();
      }"""
      )


}


