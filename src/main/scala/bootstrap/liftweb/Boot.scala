package bootstrap.liftweb

import scala.xml.{Text,NodeSeq,XML}
import net.liftweb._
import http.{LiftRules, NotFoundAsTemplate, ParsePath, Req, XHtmlInHtml5OutProperties}
import sitemap.{SiteMap, Menu, Loc}
import util.{ NamedPF }
import net.liftweb.http.{ResourceServer,LiftRules,S,RedirectResponse,SessionVar,PostRequest,GetRequest,TemporaryRedirectResponse,OkResponse}
import net.liftweb.http.rest._
import net.liftweb.common.{Box,Full,Empty,Loggable,Logger}
import net.liftweb.http.auth.{HttpBasicAuthentication,AuthRole,userRoles}
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.db.{DB,ConnectionManager,ConnectionIdentifier,DefaultConnectionIdentifier}
import net.liftweb.mapper.{MapperRules, DBLogEntry,Schemifier,StandardDBVendor}
import com.criterionbi._
import com.criterionbi.model.{CBISession,CBIShellLog}
import java.sql.{Connection,DriverManager}
import net.liftweb.util._

object AuthH2DB extends ConnectionIdentifier {
  def jndiName = "AuthH2DB"
}

class Boot {
  val logger = Logger(classOf[Boot])
  def boot {
   
    // where to search snippet
    LiftRules.addToPackages("com.criterionbi")
    //LiftRules.ajaxPostTimeout = 5000 //m


    val cbiShellDbDriver = Props.get("cbiShell.db.driver") openOr "org.h2.Driver";     
    val cbiShellDbUrl    = Props.get("cbiShell.db.url") openOr "jdbc:h2:mem:cbishell;DB_CLOSE_DELAY=-1";
    val cbiShellDbUserName =  Props.get("cbiShell.db.username") openOr "NOTNEEDED";
    val cbiShellDbPassword = Props.get("cbiShell.db.password") openOr "NOTNEEDED";
    //TODO check for error?
    val cbiShellDbCreateSchema =  (Props.get("cbiShell.db.createSchema") openOr "True").toBoolean


    DB.defineConnectionManager(DefaultConnectionIdentifier,
        new StandardDBVendor(cbiShellDbDriver, cbiShellDbUrl, Full(cbiShellDbUserName), Full(cbiShellDbPassword)))
   
    DB.defineConnectionManager(AuthH2DBConnectionIdentifier,
        new StandardDBVendor(cbiShellDbDriver, cbiShellDbUrl, Full(cbiShellDbUserName), Full(cbiShellDbPassword)))

    // automatically create the tables
    if (cbiShellDbCreateSchema) {
        Schemifier.schemify(true, Schemifier.infoF _,AuthH2DBConnectionIdentifier, CBISession)
        Schemifier.schemify(true, Schemifier.infoF _,AuthH2DBConnectionIdentifier, CBIShellLog)
    }

    S.addAround(DB.buildLoanWrapper(List(DefaultConnectionIdentifier,AuthH2DBConnectionIdentifier)))

    //CAS Users 
    val casDbDriver = Props.get("cas.db.driver") openOr "com.mysql.jdbc.Driver";     
    val casDbUrl    = Props.get("cas.db.url") openOr "jdbc:mysql://localhost:3306/USERS";
    val casDbUserName =  Props.get("cas.db.username") openOr "CASUSER";
    val casDbPassword = Props.get("cas.db.password") openOr "password";

    DB.defineConnectionManager(PentahoUserConnectionIdentifier,
        new StandardDBVendor(casDbDriver, casDbUrl, Full(casDbUserName), Full(casDbPassword))
    )

    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => NotFoundAsTemplate( ParsePath(List("exceptions","404"),"html",false,false))
    })
     
    LiftRules.setSiteMap(SiteMap(   
      Menu("Home") / "index" >> TestAccess( () => AuthControl.testAccess ),
      Menu("login") / "loginTest"   >> TestAccess( () => AuthControl.testAccess ),
      Menu("Static") / "static" / **
    ))

    LiftRules.dispatch.append {
      case request @ Req("casSecurityCheck" :: Nil, _, PostRequest) => AuthControl.logoutRequest(request)
      case request @ Req("casSecurityCheck" :: Nil, _, GetRequest)  => AuthControl.loginUser(request)
      case request @ Req("logout" :: Nil, _, GetRequest)            => AuthControl.logoutUser
    }

    ResourceServer.allow { 
      case "css" :: "images" :: _ => true
      case "css" :: _ => true
      case "js" :: _ => true
    } 
      
    // set character encoding
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    //use html5
    LiftRules.htmlProperties.default.set((r: Req) =>
        new XHtmlInHtml5OutProperties(r.userAgent))

    //is this still needed?
    LiftRules.useXhtmlMimeType = false
   
  }
}



