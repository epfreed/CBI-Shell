package com.criterionbi

import net.liftweb.common.{Box,Full,Empty,Loggable,Logger}
import net.liftweb.http.{UnauthorizedResponse,Req,ResourceServer,
        LiftRules,S,RedirectResponse,SessionVar,PostRequest,
        GetRequest,TemporaryRedirectResponse,OkResponse}
import net.liftweb.mapper._
import net.liftweb.util._
import com.criterionbi.model._

case object AuthH2DBConnectionIdentifier extends ConnectionIdentifier {
  def jndiName: String = "AuthH2DB"
}

object LoggedIn extends SessionVar[Box[String]](Empty){
  registerCleanupFunc(session => {
      CBIShellLog.create.user( LoggedIn.openOr("noOne") ).cbiSession(session.uniqueId).name("--Session expired--").url(S.hostAndPath).ip( S.containerRequest.map(_.remoteAddress).openOr("localhost")  ).saveMe
      CBISession.findAll(By(CBISession.sessionID, session.uniqueId)).map(_.delete_!)
  });
}

object CBIUser  extends SessionVar[Box[Users]](Empty)
//object CBIUserRootDir  extends SessionVar[Box[SolutionFiles]](Empty)
object CasTicket extends SessionVar[Box[String]](Empty)

object AuthControl extends Logger {
  
  import com.criterionbi.lib.CBIShellProperties._

  def testAccess = {
    //can there ever be more than 1 session?
    val session = S.session.open_!
    val r =  CBISession.findAll(By(CBISession.sessionID, session.uniqueId))
    debug("testing access " + r.size + " for session " + session.uniqueId)

    //we need our URL
    //     S.hostAndPath   http://secure.criterionbi.com:8080/cbishell

    debug("called " + S.hostAndPath)

    //TODO this should be checked, but can it fail? It probably should be stores in the Session
    val myUrl =  S.hostAndPath.split(":", 2)(1)

    if (r.size == 0)  { 
      debug("Session not found. Redirecting to Login page")
      Full(RedirectResponse("https://" + casAddress + "/cas/login?service=" + 
        cbiShellProto + ":" + myUrl + "/casSecurityCheck"))
    } else {
      debug("access OK! for " + CBISession.sessionID + ":" + session.uniqueId)
      Empty
    }
  }


  def logoutUser = {
    //this should redirect to the CAS logout, which should then send a logout request
    // but just in case...
    val session = S.session.open_!.uniqueId
    info("Logging out session: " + session)
    val cbiSessions =  CBISession.findAll(By(CBISession.sessionID, session))
    cbiSessions.map(_.delete_!)
    CBIShellLog.create.user( LoggedIn.openOr("noOne") ).cbiSession(session).name("--Logged out from logout page--").url(S.hostAndPath).saveMe
    CBIUser.remove
    LoggedIn.remove
    //() => (Full(RedirectResponse("https://" + casAddress + "/cas/logout")))
    //just in case lets go to pentaho to log it out as well
    debug("------------------------------> sending to logout of Pentaho  <" +  biProto + "://" + address + "/pentaho/Logout>")
    () => (Full(RedirectResponse(  biProto + "://" + address + "/pentaho/Logout" )))
  }

  def logoutRequest(request: Req) = {
    import scala.xml._
    //this is a reqest from CAS--no real session
    val logoutRequest = S.param("logoutRequest").openOr("None")

    //should check that logoutRequest can be parsed as XML
    logoutRequest match {
      case "None" => () => Full( UnauthorizedResponse("casSecurityCheck") )
      case _   => {
        val logoutRequestXML = XML.loadString( logoutRequest)
        val ticket = (logoutRequestXML \\ "LogoutRequest" \ "SessionIndex" ).text    
        info("   got request to logout this ticket: " + ticket)
        val sessions =  CBISession.findAll(By(CBISession.ticket, ticket))
        sessions.map(_.delete_!)
        CBIShellLog.create.user( LoggedIn.openOr("noOne") ).ticket(ticket).name("--Logged out from CAS--").url(S.hostAndPath).ip( S.containerRequest.map(_.remoteAddress).openOr("localhost")  ).saveMe
        () => Full( OkResponse() )
      }
    }
  }


  def loginUser(request: Req)  = {
    LoggedIn.remove
    val ticket = S.param("ticket").openOr("none")
    debug("ticket is " + ticket )
    ticket match {
      case "none" => debug("Got no Ticket ")
      case _      => loginFromTicket(ticket)
    }
    () => Full(TemporaryRedirectResponse("/index", request))
  }

  private def loginFromTicket(ticket: String) = {
    import dispatch._
    import dispatch.HttpsLeniency
    debug("Logging in from Ticket: " + ticket)

    //TODO this should be checked, but can it fail?
    val myUrl =  S.hostAndPath.split(":", 2)(1)

    val serviceValidator  = url("https://" + casAddress + "/cas/serviceValidate?ticket=" + 
    ticket + "&service=" + cbiShellProto + ":" + myUrl + "/casSecurityCheck")

    // TODO this can be removed once we have a good SSL cert
    // and http below replaced with Http
    def http[A](handler: dispatch.Handler[A]): A = {
        val h: Http = new Http with HttpsLeniency /* with NoLogging */
        try { h(handler) } finally { h.shutdown() }
    }

    val userName = http(serviceValidator </> {nodes => ( nodes \\ "authenticationSuccess" \ "user" ).text })
    debug("got Username: " + userName + "<--")

    //can there ever be no session?
    val session = S.session.open_!

    if (userName != "") loginSession(userName, ticket, session.uniqueId) 
        
    info("logged in <" + userName + "> with ticket " + ticket + " session: "  + session.uniqueId)
  }

  private def loginSession(username:String, ticket:String, sessionID: String) {
    CBISession.create.userName(username).ticket(ticket).sessionID(sessionID).saveMe
    val l = CBIShellLog.create.user(username).ticket(ticket).cbiSession(sessionID).name("--Logged In--").url(S.hostAndPath).ip( S.containerRequest.map(_.remoteAddress).openOr("localhost")  ).saveMe
    info("logged " + l)
    LoggedIn(Full(username))
    CasTicket(Full(ticket))
    
    val user = Users.find(username)
    CBIUser(user)
    val rootDirName =  if (multiTenant) {
      user match {case Full(u) => u.clientDir.toString ; case _ => "__ERROR__"} 
    } else  solutionsLoc.split("/").last

    //TODO fixthis
    // CBIUserRootDir( SolutionFiles.find(By(SolutionFiles.fileName, rootDirName)) )
  }

}
