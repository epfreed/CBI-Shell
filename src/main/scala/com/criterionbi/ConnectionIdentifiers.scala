package com.criterionbi

import net.liftweb.mapper._
import net.liftweb.actor._

case object PentahoUserConnectionIdentifier extends ConnectionIdentifier {
  def jndiName: String = "PentahoUserDB"
}

object PentahoHibernateConnectionIdentifier extends ConnectionIdentifier {
  def jndiName: String = "PentahoHibernateDB"
}