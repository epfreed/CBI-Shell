package com.criterionbi.comet

import net.liftweb.util.ActorPing
import net.liftweb.util.Helpers._
import net.liftweb.http.CometActor
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.common.{Box, Full,Logger}
import scala.xml.{Text,NodeSeq,XML}


case object Tick

class Clock extends CometActor with Logger {
  ActorPing.schedule(this, Tick, 5 seconds)

  def render = "#clock_time *" replaceWith timeNow.toString

  override def lowPriority = {
    case Tick => {
      debug("---TICK for Clock!!")
      partialUpdate(  SetHtml("clock_time", Text(timeNow.toString) ))
    }
    ActorPing.schedule(this, Tick, 5 seconds)
  }
}

