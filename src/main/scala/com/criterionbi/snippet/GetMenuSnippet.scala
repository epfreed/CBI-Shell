package com.criterionbi.snippet

import net.liftweb._
import net.liftweb.http._
import net.liftweb.http.SHtml._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.common.{Box,Full,Empty,Loggable,Logger}
import scala.xml._
import com.criterionbi.lib.CBIShellProperties._
import com.criterionbi.lib.CBIShellJSFunctions._
import com.criterionbi.comet._
import com.criterionbi._

class GetMenuSnippet extends DispatchSnippet with Logger {

  def dispatch = {
    case "startup" => startup _
    case "functions" => functions _
  }


  def startup(xhtml: NodeSeq): NodeSeq = Script(OnLoad(startupScript)) 

  def callComet(s: String) =  myMenuComet.is match {
    case Full(c) => { c ! ReloadMessage(CBIUser.is, s) }
    case _ => info("AAAAAAAAAaAAAA error! No Comet session")
  }
  
  def handler() = (s:String) => {
    info("-------------------------------> Recevied: "+s)
    callComet(s)
    Noop
  }

  //define the JS functions getMenu(), ajaxCall, openLoginer, 
  def functions(xhtml: NodeSeq): NodeSeq = {
    //this is defined here for the myHandler closure
    val myHandler = handler()
    def definedAjaxCall = JsRaw("var ajaxCall = function(){ " + ajaxCall( getStoredMenu, myHandler(_) )._2.toJsCmd + "}")
    Script( definedGetMenu & definedRefreshRepoAndGetMenu & definedAjaxCall  & definedOpenLoginer & defineTabOpenerBasic & defineTabOpener )
  }

}
