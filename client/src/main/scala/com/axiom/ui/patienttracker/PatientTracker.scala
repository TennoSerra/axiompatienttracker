package com.axiom.ui.patienttracker

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

  selectedCellVar.signal.foreach{ selOpt => 
    selOpt match
      case Some(sel) => 
        selectedRowVar.set(Some(sel.row))
      case _ => 
        selectedRowVar.set(None)
  } 
  
  val colHeadersVar:Var[List[String]] = Var(ShapelessFieldNameExtractor.fieldNames[Patient].take(10))

  def columns(row:Int,p:Patient) =  
    val c = mutable.IndexedSeq(CellDataConvertor.derived[Patient].celldata(p)*).take(10)
    c(0) = c(0).copy(text = s"*${c(0).text}*", color = "green")
    c.toList

  override def cctoData(row:Int,cc:Patient):List[CellData] = columns(row,cc)


  def renderHtml: L.Element = 
    


    def headerRow(s:List[String]) = 
      List(tr(
          s.map (s => th(s))
        )
      )

    table(
      onKeyDown --> tableKeyboardHandler,
      thead(
        children <-- colHeadersVar.signal.map{headerRow(_) }
      ),
      tbody(
        children <-- gcdVar.signal.map{ 
          (rowList:GCD) => rowList.map(tup => row(tup))
        }
      )
  )
  
  def row(cols:Row)  = tr(
    backgroundColor <-- selectedRowVar.signal.map{ selRow => 
      selRow match
        case Some(row) if row == cols.head._2.row => "green"
        case _ => "black"
    },
    cols.map{c => this.tableCell(c._2)}
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

  def tableKeyboardHandler(e:KeyboardEvent)  =
    e.keyCode match 
      case 40 => e.preventDefault()
      case 38 => e.preventDefault()
      case 37 => e.preventDefault()
      case 39 => e.preventDefault()
      case 32 => e.preventDefault()
      case _  => ()  

    

  def keyboardHandler(e:KeyboardEvent)  =
    val selectedCellOpt = selectedCellVar.now()
    def conditionalUpdate(vector:ColRow):Unit =
      selectedCellOpt.foreach {currentColRow =>
        val newColRow = currentColRow.add(vector)
        inBounds(newColRow) match
          case true => selectedCellVar.set(Some(newColRow))
          case _ => ()
      }
    e.keyCode match
      case 40 =>  //down cursor
        conditionalUpdate(ColRow(0,1))
      case 38 => //up cursor
        conditionalUpdate(ColRow(0,-1))
      case 37 => //left cursor
        conditionalUpdate(ColRow(-1,0))
      case 39 => //right cursor
        conditionalUpdate(ColRow(-1,0))
      case 9 => //tab
        // dom.window.console.log(s"tabbed ${gd.coordinate}tab tab tab ")
      case _ => ()


