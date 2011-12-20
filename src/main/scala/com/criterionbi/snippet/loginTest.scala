package com.criterionbi.snippet

import scala.xml.{NodeSeq}
import net.liftweb.util.Helpers._
import com.criterionbi.{LoggedIn,AuthControl}

class loginTest {
  def render = "*" #> LoggedIn.openOr("noOne")
}
