package com.criterionbi.snippet

import net.liftweb.util.Helpers._
import net.liftweb.mapper._
import net.liftweb.http.DispatchSnippet
import com.criterionbi._
import net.liftweb.common.{Box,Full,Empty,Loggable,Logger}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JE._
import net.liftweb.http.SHtml._
import net.liftweb.util._
import scala.xml._
import com.criterionbi.lib.CBIShellJSFunctions._
import com.criterionbi.model._
import com.criterionbi.comet._

class CBIMenu extends DispatchSnippet with Logger {

  def dispatch = {
    case "reload" => reload 
    case "label" => label _
  }
    
  //TODO this should be css matching
  def label(xhtml: NodeSeq): NodeSeq = {
    val user = CBIUser.is 
    user match {
      case Full(u) => {    
        val clientNames = u.authorities.map(_.clientName).toList.groupBy(_.toString).keys
        //what if there is more than 1 clientName?
        <div>
          <div id="menuLabelComp">Solutions for <br/><b>{clientNames.toList(0)}</b></div> 
          <div id="menuLabelName">User: {u.username.toString}</div>
        </div> 
      }
      case _ =>  <div id="menuLabelComp">Solutions</div>
    }
  }

  def reload = "#reloadLink" #> a(<span>Reload</span>, clearMenuJS & freshenLoginer )
  
}
