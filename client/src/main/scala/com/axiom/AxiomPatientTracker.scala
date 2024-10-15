package com.axiom


import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}
import com.axiom.ui.patienttracker.PatientTracker

object AxiomPatientTracker :
  def consoleOut(msg: String): Unit = {
    dom.console.log(s"%c $msg","background: #222; color: #bada55")
  }
 
  def apply():Element = 

    import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
    val patientTracker = new PatientTracker()
    ModelFetch.fetchPatients.foreach{ p => 
      patientTracker.populate(p)
    }
    patientTracker.renderHtml
    

  def applyx():HtmlElement = 
    Model.fetchPatients
    val numColumnsToShow = 10


    val headerElementsSignal: Signal[List[HtmlElement]] = Model.colHeadersVar.signal.map { fieldNames =>
      fieldNames.take(numColumnsToShow).map { fieldName =>
        th(fieldName)
      }
    }

    val bodyElementsSignal = Model.patientFieldEnums.signal.map { lldatatypes =>
      lldatatypes.map(l => l.take(numColumnsToShow))
        .map(x => x.map(d => td(textAlign := "left",  color:= s"${d.color}",div(   s"${d.text} "))))
        .map(tr(_)) 
    }


    table(
      thead(
        children <-- headerElementsSignal
      ),
      tbody(
        children <--   bodyElementsSignal 
      )
    )  
