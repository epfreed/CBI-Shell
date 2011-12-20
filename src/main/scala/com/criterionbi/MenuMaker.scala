package com.criterionbi

import net.liftweb.common.{Box,Full,Empty,Loggable,Logger}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.SHtml._
import net.liftweb.http.SessionVar
import net.liftweb.http.S
import net.liftweb.util._
import scala.xml._
import com.criterionbi.model._
import com.criterionbi.lib.CBIShellJSFunctions._
import com.criterionbi.lib.CBIShellProperties._
import scala.xml.transform._

object myMenuComet extends SessionVar[Box[com.criterionbi.comet.MenuComet]](Empty)

object MenuMaker extends Logger {

  def attributeTester(nd: Node, att: String)( f:(NodeSeq => Boolean) ) = { f( nd \ ("@" + att) ) }                 
  
  def attributeEquals(node: Node, att: String, value: String)    = attributeTester(node, att)( _.text ==value )  
  def attributeNotEquals(node: Node, att: String, value: String) = attributeTester(node, att)( _.text != value )  

  def getNodeByAttribute(att:String)(n: Node, value: String)  = (n \\ "_").filter(attributeEquals(_, att, value))
  def getNodeByName(n: Node, value: String) = getNodeByAttribute("name")(n,value)
  def getNodeByLocalizedName(n: Node, value: String) = getNodeByAttribute("localized-name")(n,value)

  def invisable(n: Node)   = attributeEquals(n, "visible", "false")
  def visable(n: Node)     = attributeEquals(n, "visible", "true")
  def isDirectory(n: Node) = attributeEquals(n, "isDirectory", "true")
  def isLink(n: Node)      = attributeEquals(n, "isDirectory", "false") && attributeNotEquals(n, "url", "")

  //def isReport(n: Node)   = attributeTester(n, "url")( _.text.contains("reportviewer/report.html"))
  def isReport(n: Node) = attributeTester(n, "name")( _.text.endsWith(".prpt"))
  def isWcdf(n: Node)   = attributeTester(n, "name")( _.text.endsWith(".wcdf"))
  def isXcdf(n: Node)   = attributeTester(n, "name")( _.text.endsWith(".xcdf"))
  def isCda(n: Node)    = attributeTester(n, "name")( _.text.endsWith(".cda"))
  def isSaiku(n: Node)    = attributeTester(n, "name")( _.text.endsWith(".saiku"))
  def isUrl(n: Node)    = attributeTester(n, "name")( _.text.endsWith(".url"))
  def isXaction(n: Node)    = attributeTester(n, "name")( _.text.endsWith(".xaction"))

  //TODO make sure 1 node it in the NodeSeq instead of assuming
  def saikuUrlNode(xml: Node) = getNodeByLocalizedName( getNodeByName(xml, "Criterion Utils")(0), "saiku.url")(0)

  //TODO add ip address
  def logLinkCreate(user: Box[Users], n: Node) {
    debug("------------> makinglink for " + n )
    CBIShellLog.create.user(user.open_!.username).
              ticket(CasTicket.get openOr null).cbiSession(S.session.open_!.uniqueId).
              url( (n \ "@url").text ).localized_name( (n \ "@localized-name").text ).
              name( (n \ "@name").text ).ip( S.containerRequest.map(_.remoteAddress).openOr("localhost")  ).
              solution_last_modifiedDate( new java.util.Date( ((n \ "@lastModifiedDate").text).toLong ) ).
              saveMe
  }

  def icon(fileName: String) = <img src={ "images/" + fileName } style="width: 16px; height:16px;"></img>

  def getIcon(n: Node)= n match {
    case n: Node if isReport(n) => icon("prptFileType2.png") 
    case n: Node if isWcdf(n)   => icon("wcdfFileType.png")
    case n: Node if isXcdf(n)   => icon("xcdfFileType.png")
    case n: Node if isCda(n)    => icon("cdaFileType.png")
    case n: Node if isSaiku(n)  => icon("saiku_16.png")
    case n: Node if isUrl(n)    => icon("urlFileType.png")
    case n: Node if isXaction(n) => icon("xactionFileType.png")
    case _ => NodeSeq.Empty 
  }

  def nodeify(user: Box[Users], xml:String ): scala.xml.NodeSeq = {
    
    def makeLink(n: Node, name: String, tabName: String, addIcon: Boolean = true ) =  a( () => {  
         logLinkCreate(user, n);
         Call("tabadder", biProto + "://" + address + (n \ "@url").text, tabName)},
        <span>{if (addIcon) getIcon(n)}{name}</span> 
      )

    val fullMenuXml = XML.loadString(xml)
    val clientXml = getNodeByName(fullMenuXml, user.open_!.clientDir.toString)
      
    val nodeIt = new RewriteRule {
      override def transform(n: Node): NodeSeq = n match {
        case n: Node if invisable(n)   => NodeSeq.Empty
        case n: Node if isDirectory(n) => <li><a class="sf-with-ul" href="#">{ n \ "@name" }</a><ul>{transform(n.child)}</ul></li>
        case n: Node if isLink(n)      => <li>{makeLink(n, (n \ "@localized-name").text, (n \ "@localized-name").text ) }</li>
        case n => n
      }
    }

    <div> <ul class="sf-menu sf-vertical">{new RuleTransformer(nodeIt).transform(clientXml \ "file")}</ul>
    <span id="spacer"/> 
    <ul class="sf-menu sf-vertical"><li>{makeLink(saikuUrlNode(fullMenuXml), "Analysis", "Analysis", false)}</li></ul>
    </div>
  }
  
}
