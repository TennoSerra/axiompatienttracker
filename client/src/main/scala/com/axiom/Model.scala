package com.axiom
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import com.axiom.model.shared.dto.Patient
import com.axiom.TableColProperties

object Model :
  type PatientFields = List[Display]

  val patientListVar = Var(List.empty[Patient])
  val patientListSignal = patientListVar.signal
  val fieldNamesVar = Var(List.empty[String])
  val fieldNamesSignal = fieldNamesVar.signal
  val patientFieldEnums = Var(List[PatientFields]())
  val patientFieldEnumsSignal = patientFieldEnums.signal

  def fetchPatients = ModelFetch.fetchPatients


object ModelFetch :
  import io.laminext.fetch._
  import com.raquo.laminar.api.L._
  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
  import org.scalajs.dom.AbortController
  import com.axiom.ShapelessCaseEnum.EnumMapper.enumCoProduct
  import com.axiom.ShapelessFieldNameExtractor.fieldNames
  import com.raquo.airstream.ownership.OneTimeOwner
  import zio.json._
  given Owner = new OneTimeOwner(()=>()) //implicitly used by FetchStream.foreach
  def fetchPatients = 
    import java.time._ //cross scalajs and jvm compatible
    import com.axiom.model.shared.dto.Patient 
    
    FetchStream.get("http://localhost:8080/patientsjson")
    .map(s => s.fromJson[List[Patient]])
    .map(r => r.toOption.getOrElse(Nil))
    .foreach( patients =>
      Model.patientListVar.set(patients)
      Model.fieldNamesVar.set(fieldNames[Patient])
      //TODO WORKING HERE

      Main.consoleOut("ok here goes...")
      Main.consoleOut(s"count: ${patients.size}")
      val result = TableColProperties.derived[Patient].element(patients(0))
      Model.patientFieldEnums.set(patients.map(x => TableColProperties.derived[Patient].element(x)))
    )


  // val abortController = new AbortController()
  // val resultFuture = (Fetch.get("http://localhost:8080/patientsjson").future.text) (abortController  )
  
  // resultFuture.map(r => r.data.fromJson[List[Patient]]).map(r => r.toOption).foreach{
  //   case Some(patients) => Model.patientListVar.set(patients)
  //   case None => Model.patientListVar.set(List.empty[Patient])
  // }



