package com.sandinh.rx

import rx.functions.{Func2, Func1}
import rx.{Observer, Observable}
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions

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
      * + emit more than one values then `toFuture` will fail with IllegalArgumentException("Sequence contains too many elements")
      */
    def toFuture: Future[T] = {
      val p = Promise[T]()
      underlying.single.subscribe(new FutureObserver(p))
      p.future
    }

    /** scala map. We can't name `map` because scala compiler will not implicitly pick this method */
    @inline def scMap[R](f: T => R): Observable[R] = underlying.map[R](new SFunc1(f))

    /** scala flatMap. We can't name `flatMap` because scala compiler will not implicitly pick this method.
      * @note result may "out of order". If need in-order then you should use scConcatMap
      */
    @inline def scFlatMap[R](f: T => Observable[R]): Observable[R] = underlying.flatMap[R](new SFunc1(f))

    /** scala concatMap. We can't name `concatMap` because scala compiler will not implicitly pick this method.
      * @note If don't need in-order then you should use scFlatMap
      */
    @inline def scConcatMap[R](f: T => Observable[R]): Observable[R] = underlying.concatMap[R](new SFunc1(f))

    /** we not named `foldLeft` to indicate that Observable may emit items "out of order" (not like Future)
      * Ex: Observable.from(2, 1).flatMap(Observable.timer(_ seconds)).fold("")(_ + _)
      * is Observable of "12" (not "21")
      * @note result may "out of order"
      */
    @inline def fold[R](z: R)(op: (R, T) => R): Observable[R] = underlying.reduce(z, new SFunc2(op))
  }
}
