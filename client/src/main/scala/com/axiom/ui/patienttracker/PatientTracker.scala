package com.axiom.ui.patienttracker

import scala.scalajs.js
import com.axiom.ui.tableutils.*
import com.axiom.model.shared.dto.Patient
import scala.collection.mutable
import com.axiom.ShapelessFieldNameExtractor
import java.time.*
import com.axiom.ui.patienttracker.TypeClass.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import com.axiom.ModelFetch
import com.raquo.laminar.api.L
import com.axiom.ModelFetch.headers
import com.raquo.airstream.ownership.OneTimeOwner
import org.scalajs.dom.KeyboardEvent
import io.bullet.borer.derivation.key

type PatientList = CCRowList[Patient]


trait RenderHtml :
  def renderHtml:Element

case class CellData(text:String,color:String) 

//TODO[populate] PatientGridData
case class PatientGridData(grid: PatientTracker,colrow:ColRow, data:CellData) 
    extends GridDataT[PatientTracker,Patient,CellData](grid,colrow,data) with RenderHtml :
  def renderHtml = td(data.text,backgroundColor:=data.color)




class PatientTracker() extends GridT [Patient,CellData] with RenderHtml:

  given owner:Owner = new OneTimeOwner(()=>())
  val selectedCellVar:Var[Option[ColRow]] = Var(None)
  val selectedRowVar:Var[Option[Int]] = Var(None)
  val searchQueryVar: Var[String] = Var("")
  
  
  selectedCellVar.signal.map {
    case Some(sel) => Some(sel.row)
    case None => None
  }.foreach(selectedRowVar.set)
  
  selectedRowVar.signal.foreach {
    case Some(rowIdx) =>
      Option(dom.document.getElementById(s"row-$rowIdx")).foreach { element =>
        val rect = element.getBoundingClientRect()
        val isInView = rect.top >= 0 && rect.bottom <= dom.window.innerHeight
      
        if (!isInView) {
          element.asInstanceOf[js.Dynamic].scrollIntoView(
            js.Dynamic.literal(
              behavior = "smooth",
              block = "center" // Optional: centers the row in view
            )
          )
        }
      }     
    case None => // Do nothing if no row is selected
  }
  
  
  val colHeadersVar:Var[List[String]] = Var(ShapelessFieldNameExtractor.fieldNames[Patient].take(10))

  def columns(row:Int,p:Patient) =  
    val c = mutable.IndexedSeq(CellDataConvertor.derived[Patient].celldata(p)*).take(10)
    c(0) = c(0).copy(text = s"*${c(0).text}*", color = "green")
    c.toList

  override def cctoData(row:Int,cc:Patient):List[CellData] = columns(row,cc)


  // Rudimentary serach filter function, could be made column/data agnostic to be able to use for all columns of the patient data.
  def searchFilterFunction(patient: Patient): Boolean = {
    val query = searchQueryVar.now().toLowerCase
    val patientName = patient.firstName +" "+patient.lastName
    patientName.toLowerCase.contains(query) 
  }
  
  def renderHtml: L.Element = 
    def headerRow(s: List[String]) = 
      List(tr(s.map(s => th(s))))
  
    div(
      // Search bar
      div(
        label("Search: "),
        input(
          typ := "text",
          placeholder := "Search patients...",
          inContext { thisNode =>
            onInput.mapTo(thisNode.ref.value) --> searchQueryVar
          }
        )
      ),
      
      // Render table with filtered rows
      table(
        onKeyDown --> tableKeyboardHandler,
        thead(
          children <-- colHeadersVar.signal.map(headerRow)
        ),
        tbody(
          children <-- gcdVar.signal.map{ 
            (rowList:GCD) => rowList.map(tup => row(tup))
          }
        )
      )
    )
  

  def row(cols: Row) = tr(
    idAttr := s"row-${cols.head._2.row}",
    backgroundColor <-- selectedRowVar.signal.map {
      case Some(row) if row == cols.head._2.row => "green"
      case _ => "black"
    },
    cols.map { c => this.tableCell(c._2) }
  )
  

  def tableCell(colRow:ColRow) : HtmlElement  =
    td(
      tabIndex := colRow.row*9000 + colRow.col, //apparently I need this capture keyboard events
      onKeyDown --> keyboardHandler,
      onMouseUp.mapTo(colRow).map(Some(_)) --> selectedCellVar.writer,
      data(colRow).map{ gcdTuple => 
        s"${gcdTuple._3.text}"
      }.getOrElse("---")
    )

  /**
    * event handler at the table later to prevent default behaviour from key actions
    * that can cause the web page to scroll
    *
    * @param e
    */
  def tableKeyboardHandler(e:KeyboardEvent)  =
    e.keyCode match 
      case 40 => e.preventDefault()
      case 38 => e.preventDefault()
      case 37 => e.preventDefault()
      case 39 => e.preventDefault()
      case 32 => e.preventDefault()
      case _  => ()  

    

  def keyboardHandler(e: KeyboardEvent): Unit = {
    val selectedCellOpt = selectedCellVar.now()
    


    println(selectedCellOpt.get.row)

    def conditionalUpdate(vector: ColRow): Unit = {
      selectedCellOpt.foreach { currentColRow =>
        val newColRow = currentColRow.add(vector)
        if (inBounds(newColRow)) {
          selectedCellVar.set(Some(newColRow))
        }
      }
    }

    e.keyCode match {
      case 40 => // down cursor
        println(selectedCellOpt)
        conditionalUpdate(ColRow(0, 1))
      case 38 => // up cursor
        conditionalUpdate(ColRow(0, -1))
      case 37 => // left cursor
        conditionalUpdate(ColRow(-1, 0))
      case 39 => // right cursor
        conditionalUpdate(ColRow(1, 0))
      case _ => ()
    }
  }



