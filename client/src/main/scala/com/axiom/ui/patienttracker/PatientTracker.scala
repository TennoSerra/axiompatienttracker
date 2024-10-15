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

type PatientList = CCRowList[Patient]


trait RenderHtml :
  def renderHtml:Element

case class CellData(text:String,color:String) 

//TODO[populate] PatientGridData
case class PatientGridData(grid: PatientTracker,colrow:ColRow, data:CellData) 
    extends GridDataT[PatientTracker,Patient,CellData](grid,colrow,data) with RenderHtml :
  def renderHtml = td(data.text,backgroundColor:=data.color)




class PatientTracker() extends GridT [Patient,CellData] with RenderHtml:
  
  val colHeadersVar:Var[List[String]] = Var(ShapelessFieldNameExtractor.fieldNames[Patient].take(10))

  def columns(row:Int,p:Patient) =  
    val c = mutable.IndexedSeq(CellDataConvertor.derived[Patient].celldata(p)*).take(10)
    c(0) = c(0).copy(text = s"*${c(0).text}*", color = "green")
    c.toList

  override def cctoData(row:Int,cc:Patient):List[CellData] = columns(row,cc)


  def renderHtml: L.Element = 
    type Row =mutable.IndexedSeq[GCDTuple]

    def row(cols:Row)  = tr(
      cols.map{c => td(this.tableCell(c._2))}
    )

    def headerRow(s:List[String]) = 
      List(tr(
          s.map (s => th(s))
        )
      )

    table(
      thead(
        children <-- colHeadersVar.signal.map{headerRow(_) }
      ),
      tbody(
        children <-- gcdVar.signal.map{ 
          (rowList:GCD) => rowList.map(tup => row(tup))
        }
      )
  )
  
  def tableCell(colRow:ColRow) : HtmlElement  =
    div(
      data(colRow).map{ gcdTuple => 
        s"${gcdTuple._3.text}"
      }.getOrElse("---")
    )
    

 

  