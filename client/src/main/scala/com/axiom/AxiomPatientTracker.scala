package com.axiom


import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}

object AxiomPatientTracker :
  def consoleOut(msg: String): Unit = {
    dom.console.log(s"%c $msg","background: #222; color: #bada55")
  }


  def apply():HtmlElement = 
    Model.fetchPatients
    val numColumnsToShow = 7


    val headerElementsSignal: Signal[List[HtmlElement]] = Model.fieldNamesSignal.map { fieldNames =>
      fieldNames.take(numColumnsToShow).map { fieldName =>
        th(fieldName)
      }
    }

    val bodyElementsSignal = Model.patientFieldEnumsSignal.map { lldatatypes =>
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
