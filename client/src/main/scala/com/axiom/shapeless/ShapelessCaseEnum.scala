package com.axiom

import com.raquo.laminar.api.L



object ShapelessCaseEnum :
  import shapeless3.deriving.*
  import com.raquo.laminar.api.L.{*, given}
  import java.time._
  import org.scalajs.dom
  

  trait EnumMapper[A]:
    def map(a: A): List[FieldType]

    
  enum FieldType :
    case DateT(value: LocalDate)
    case DateTimeT(value: LocalDateTime) 
    case DateTimeOpt(value: Option[LocalDateTime])
    case LocalDateTimeOpt(value: Option[LocalDateTime])
    case BooleanT(value: Boolean)
    case StringT(value: String)
    case StringOpt(value: Option[String])
    case IntT(value: Int)

  extension (d:FieldType.DateT) 
    def element:Element = div(d.value.toString)
  extension (d:FieldType.DateTimeT)
    def element:Element = div(d.value.toString)    


  
  object EnumMapper:
    given EnumMapper[Int] = FieldType.IntT(_) +: Nil
    given EnumMapper[Boolean] = FieldType.BooleanT(_) +: Nil
    given EnumMapper[LocalDate] = FieldType.DateT(_) +: Nil
    given EnumMapper[LocalDateTime] = FieldType.DateTimeT(_) +: Nil
    given EnumMapper[String] = FieldType.StringT(_) +: Nil
     // Generic given for Option types
    given [A](using ev: EnumMapper[A]): EnumMapper[Option[A]] = 
      (opt: Option[A]) => opt match
        case Some(value) => ev.map(value)
        case None => Nil

    


    def deriveEnumProduct[A](using pInst: K0.ProductInstances[EnumMapper, A], labelling: Labelling[A]  ): EnumMapper[A] =
      (a: A) =>
        labelling.elemLabels.zipWithIndex
          .map { (label, index) =>
            val value = pInst.project(a)(index)([t] => (st: EnumMapper[t], pt: t) => st.map(pt))
            value
          }.foldLeft(List.empty[FieldType])(_ ++ _)
    def deriveEnumSum[A](using inst: K0.CoproductInstances[EnumMapper, A] ): EnumMapper[A] =
      (a: A) => inst.fold(a)([a] => (st: EnumMapper[a], a: a) => st.map(a))

    inline given derived[A](using gen: K0.Generic[A]): EnumMapper[A] =
      gen.derive(deriveEnumProduct, deriveEnumSum)


    def enumCoProduct [T] (v:T)(using  x: EnumMapper[T]) = x.map(v)  
  







