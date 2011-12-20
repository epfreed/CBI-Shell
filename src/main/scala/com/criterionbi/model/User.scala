package com.criterionbi.model

import net.liftweb.common.{Box,Full,Empty,Loggable,Logger}
import net.liftweb.mapper._
import com.criterionbi._

abstract trait AuthRecipient {
  def authName : String
}

class Users extends KeyedMapper[String, Users] 
        with ManyToMany with AuthRecipient {
  def authName = this.username.toString
  def getSingleton = Users
  def primaryKeyField = username
  object username     extends MappedStringIndex(this, 50)
  object password     extends MappedString(this, 512)
  object description  extends MappedString(this, 100)
  object enabled      extends MappedBoolean(this)
  object clientDir   extends MappedString(this, 100)
  object authorities   extends MappedManyToMany(
     GrantedAuthorities, GrantedAuthorities.username, GrantedAuthorities.authority, Authorities)
}

object Users extends Users with KeyedMetaMapper[String, Users] {
  override def dbDefaultConnectionIdentifier = PentahoUserConnectionIdentifier
  override def dbTableName = "USERS"
}

class Authorities extends KeyedMapper[String, Authorities]  
        with ManyToMany with AuthRecipient {
  def authName = this.authority.toString
  def getSingleton = Authorities
  def primaryKeyField = authority
  object authority    extends MappedStringIndex(this, 50)
  object description  extends MappedString(this, 100)
  object clientName   extends MappedString(this, 255) 
  object usernames    extends MappedManyToMany(
      GrantedAuthorities, GrantedAuthorities.authority, GrantedAuthorities.username, Users)
}

object Authorities extends Authorities with KeyedMetaMapper[String, Authorities] {
  override def dbDefaultConnectionIdentifier = PentahoUserConnectionIdentifier
  override def dbTableName = "AUTHORITIES"
}

object GrantedAuthorities extends GrantedAuthorities with MetaMapper[GrantedAuthorities]{
  override def dbTableName = "GRANTED_AUTHORITIES"
  override def dbDefaultConnectionIdentifier = PentahoUserConnectionIdentifier
}

class GrantedAuthorities extends Mapper[GrantedAuthorities] {
  def getSingleton = GrantedAuthorities
  object authority extends MappedStringForeignKey(this, Authorities, 50){
    override def foreignMeta = Authorities
  } 
  object username extends  MappedStringForeignKey(this, Users, 50){
     override def foreignMeta =  Users
  }
}


