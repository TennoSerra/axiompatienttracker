package com.axiom.ui.patienttracker

import org.scalatest._
import wordspec._
import matchers._

import com.raquo.laminar.api.L._
import io.laminext.fetch._
import org.scalajs.dom.AbortController
import scala.concurrent.Future
import com.raquo.airstream.ownership.OneTimeOwner

import scala.scalajs.js
import com.raquo.laminar.api.L._
import io.laminext.fetch._
import org.scalajs.dom.Response
import scala.scalajs.js
import js.timers
import scala.concurrent.{Future,Promise}
import org.scalajs.dom.AbortController
import com.axiom.ModelFetch

class PatientTrackerTest extends  wordspec.AsyncWordSpec with should.Matchers {
  //TODO[REVIEW] this is how you set the js execution context in WordSpec
  implicit override def executionContext =
    scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  
  val abortController = new AbortController()

  "this" should {
    "work" in {
      given owner:Owner = new OneTimeOwner(()=>())
      import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

       val result = for {
        _     <- Future { info("Fetching data") }
        data  <- ModelFetch.fetch
        pt    <- Future{PatientTracker(data)} 
        elem  <- Future(pt.data(1,1))
        _     <- Future{info(s"elem: $elem")}
      } yield data

      result.map(x => true should be(true)) 




   }
  }
}
