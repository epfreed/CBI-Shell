package com.criterionbi.lib

import net.liftweb.common.{Box,Full,Empty,Loggable,Logger}
import net.liftweb.util._

object CBIShellProperties {
  val solutionsLoc    = Props.get("cbiShell.solutionsLoc") openOr "/opt/criterion/solutions"
  val address         = Props.get("bi.address") openOr "www.criterionbi.com"
  val biProto         = Props.get("bi.proto") openOr "https"
  val casAddress      = Props.get("cas.address") openOr "www.criterionbi.com"   
  val cbiShellAddress = Props.get("cbiShell.address") openOr "www.criterionbi.com" 
  val cbiShellProto   = Props.get("cbiShell.proto") openOr "https"
  val multiTenant     = (Props.get("cbiShell.multitenant") openOr "false").toBoolean
}
  
