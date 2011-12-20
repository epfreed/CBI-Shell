package com.criterionbi.comet

import scala.xml.{NodeSeq, Text, Elem}

import net.liftweb._
import net.liftweb.util._
import net.liftweb.actor._
import net.liftweb.http._
import net.liftweb.common.{Box, Full,Logger}
import net.liftweb.mapper._
import net.liftweb.http.SHtml._
import net.liftweb.http.S._
import net.liftweb.http.js.JsCmds.{SetHtml, SetValueAndFocus, Replace}
import net.liftweb.http.js.jquery.JqJE._
import net.liftweb.http.js.JE.Str
import Helpers._

import com.criterionbi.lib.CBIShellJSFunctions._
import com.criterionbi._
import com.criterionbi.model._
import com.criterionbi.lib._
import com.criterionbi.lib.CBIShellProperties._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._

case class ReloadMessage(user: Box[Users], xml: String )

class MenuComet extends CometActor with Logger {
 
    override def defaultPrefix = Full("comet")

 // time out the comet actor if it hasn't been on a page for 2 minutes
 // override def lifespan = Full(120 seconds)

  //this is on page load
  def render= {
    myMenuComet.set( Full(this) )
    "cmenu *" #> <span id="cmenu">this is where the menu will go. It should hidden</span>
  }

  override def lowPriority: PartialFunction[Any,Unit] = {
    case ReloadMessage(user, xml) => reloadMenu(user, xml)
  }

  private def reloadMenu(user: Box[Users], xml:String ) = {
    info("----------> Comet Actor asked to do a menu reload. We should refresh Pentaho as well!")
    //partialUpdate( SetHtml("cmenu",  MenuMaker.nodeify(user, xml) ) &  makeMenuJS )
    partialUpdate( SetHtml("cmenu",  MenuMaker.nodeify(user, xml) ) &  makeMenuJS )
  }
   
}
