package com.criterionbi.model

import com.criterionbi.AuthH2DBConnectionIdentifier
import net.liftweb.mapper._

class CBISession extends LongKeyedMapper[CBISession]
  with CreatedUpdated with IdPK {
  def getSingleton = CBISession
  object sessionID    extends MappedString(this, 255)
  object userName     extends MappedString(this, 255)
  object ticket      extends MappedString(this, 255)
}

object CBISession extends CBISession with LongKeyedMetaMapper[CBISession]{
 /*   CBISession.create.userName("joe").ticket("ST-12345").sessionID(session.ID)
      CBISession.find(By(CBISession.ticket, "ST-12345")) */
  override def dbDefaultConnectionIdentifier = AuthH2DBConnectionIdentifier
  override def dbTableName = "cbi_sessions"
}


class CBIShellLog extends LongKeyedMapper[CBIShellLog] with IdPK with CreatedUpdated {
  def getSingleton = CBIShellLog
  object user         extends MappedString(this, 50)  
  object cbiSession   extends MappedString(this, 255)
  object ticket       extends MappedString(this, 255)
  object url          extends MappedString(this, 1000)
  object localized_name extends MappedString(this, 500)
  object name         extends MappedString(this, 500)
  object ip           extends MappedString(this,16)
  object solution_last_modifiedDate extends MappedDateTime(this)
}

object CBIShellLog extends CBIShellLog  with LongKeyedMetaMapper[CBIShellLog] {
  override def dbDefaultConnectionIdentifier = AuthH2DBConnectionIdentifier
  override def dbTableName = "CBIShellLog"
}
