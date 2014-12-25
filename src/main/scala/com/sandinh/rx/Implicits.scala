package com.sandinh.rx

import com.couchbase.client.java.document.Document
import com.couchbase.client.java.view.{AsyncViewRow, AsyncViewResult}
import play.api.libs.json.{JsArray, JsValue}
import rx.functions.{Func2, Func1}
import rx.{Subscriber, Observable}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.reflect.ClassTag

object Implicits {
  implicit class ScalaObservable[T](val underlying: Observable[T]) extends AnyVal {
    /** @note if `underlying`:
      * + is empty then `toFuture` will fail with NoSuchElementException("Sequence contains no elements")
      * + emit more than one values then `toFuture` will fail with IllegalArgumentException("Sequence contains too many elements") */
    def toFuture: Future[T] = {
      val p = Promise[T]()
      underlying.single.subscribe(new Subscriber[T] {
        def onCompleted() = {}
        def onNext(t: T) = p success t
        def onError(e: Throwable) = p failure e
      })
      p.future
    }

    /** scala map. We can't name `map` because scala compiler will not implicitly pick this method */
    @inline def scMap[R](f: T => R): Observable[R] = underlying.map[R](f.toRx)

    /** scala flatMap. We can't name `flatMap` because scala compiler will not implicitly pick this method */
    @inline def scFlatMap[R](f: T => Observable[R]): Observable[R] = underlying.flatMap[R](f.toRx)

    /** we not named `foldLeft` to indicate that Observable may emit items "out of order" (not like Future)
      * Ex: Observable.from(2, 1).flatMap(Observable.timer(_ seconds)).fold("")(_ + _)
      * is Observable of "12" (not "21") */
    @inline def fold[R](z: R)(op: (R, T) => R): Observable[R] = underlying.reduce(z, op.toRx)
  }

  implicit class RichFunction1[T1, R](val f: T1 => R) extends AnyVal {
    def toRx = new Func1[T1, R] { def call(t1: T1) = f(t1) }
  }

  implicit class RichFunction2[T1, T2, R](val f: (T1, T2) => R) extends AnyVal {
    def toRx = new Func2[T1, T2, R] {
      def call(t1: T1, t2: T2) = f(t1, t2)
    }
  }

  //  @inline implicit def function1ToFunc1[A, R](f: A => R): Func1[A, R] = new Func1[A, R] { def call(a: A): R = f(a) }
  //  @inline implicit def function2ToFunc2[A, B, R](f: (A, B) => R): Func2[A, B, R] = new Func2[A, B, R] { def call(a: A, b: B): R = f(a, b) }

  implicit class RichAsyncViewResult(val underlying: AsyncViewResult) extends AnyVal {
    def foldRows(row2Js: AsyncViewRow => JsValue): Future[JsArray] =
      underlying.rows
        .scMap(row2Js)
        .fold(ListBuffer.empty[JsValue])(_ += _)
        .scMap(JsArray(_))
        .toFuture

    def flatFoldRows(row2Js: AsyncViewRow => Observable[JsValue]): Future[JsArray] = {
      underlying.rows
        .scFlatMap(row2Js)
        .fold(ListBuffer.empty[JsValue])(_ += _)
        .scMap(JsArray(_))
        .toFuture
    }
  }

  implicit class RichAsyncViewRow(val underlying: AsyncViewRow) extends AnyVal {
    def document[D <: Document[_]](implicit tag: ClassTag[D]): Observable[D] =
      underlying.document(tag.runtimeClass.asInstanceOf[Class[D]])
  }
}
