package com.sandinh.rx

import com.couchbase.client.java.document.Document
import com.couchbase.client.java.view.{AsyncViewRow, AsyncViewResult}
import play.api.libs.json.{JsArray, JsValue}
import rx.functions.{Func2, Func1}
import rx.{Observer, Observable}
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.reflect.ClassTag

object Implicits {
  private final class FutureObserver[T](p: Promise[T]) extends Observer[T] {
    def onCompleted(): Unit = {}
    def onNext(t: T): Unit = p success t
    def onError(e: Throwable): Unit = p failure e
  }

  private final class SFunc1[T1, R](f: T1 => R) extends Func1[T1, R] {
    def call(t1: T1): R = f(t1)
  }
  private final class SFunc2[T1, T2, R](f: (T1, T2) => R) extends Func2[T1, T2, R] {
    def call(t1: T1, t2: T2): R = f(t1, t2)
  }

  implicit class ScalaObservable[T](val underlying: Observable[T]) extends AnyVal {
    /** @note if `underlying`:
      * + is empty then `toFuture` will fail with NoSuchElementException("Sequence contains no elements")
      * + emit more than one values then `toFuture` will fail with IllegalArgumentException("Sequence contains too many elements") */
    def toFuture: Future[T] = {
      val p = Promise[T]()
      underlying.single.subscribe(new FutureObserver(p))
      p.future
    }

    /** scala map. We can't name `map` because scala compiler will not implicitly pick this method */
    @inline def scMap[R](f: T => R): Observable[R] = underlying.map[R](new SFunc1(f))

    /** scala flatMap. We can't name `flatMap` because scala compiler will not implicitly pick this method */
    @inline def scFlatMap[R](f: T => Observable[R]): Observable[R] = underlying.flatMap[R](new SFunc1(f))

    /** we not named `foldLeft` to indicate that Observable may emit items "out of order" (not like Future)
      * Ex: Observable.from(2, 1).flatMap(Observable.timer(_ seconds)).fold("")(_ + _)
      * is Observable of "12" (not "21") */
    @inline def fold[R](z: R)(op: (R, T) => R): Observable[R] = underlying.reduce(z, new SFunc2(op))
  }

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
    def doc[D <: Document[_]](implicit tag: ClassTag[D]): Observable[D] =
      underlying.document(tag.runtimeClass.asInstanceOf[Class[D]])
  }
}
