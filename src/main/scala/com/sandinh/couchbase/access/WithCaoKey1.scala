package com.sandinh.couchbase.access

import com.couchbase.client.java.document.Document
import rx.lang.scala.Observable

/** internal */
trait WithCaoKey1[T, A, U, D <: Document[U]] { self: CaoBase[T, U, D] =>
  protected def key(a: A): String

  final def get(a: A): Observable[T] = self.get(key(a))
  final def getBulk(aa: Iterable[A]): Observable[Option[T]] = Observable.from(aa).concatMap(get(_).singleOption)

  final def set(a: A, t: T): Observable[T] = self.set(key(a), t)
  final def setBulk(aa: Iterable[A], tt: Iterable[T]): Observable[T] = Observable.from(aa zip tt).flatMap {
    case (a, t) => set(a, t)
  }

  final def change(a: A)(f: Option[T] => T): Observable[T] = get(a).singleOption.map(f).flatMap(set(a, _))
  final def flatChange(a: A)(f: Option[T] => Observable[T]): Observable[T] = get(a).singleOption.flatMap(f).flatMap(set(a, _))

  final def changeBulk(aa: Iterable[A])(f: Option[T] => T): Observable[T] = Observable.from(aa).concatMap { a =>
    get(a).singleOption.map(f).flatMap(set(a, _))
  }
  final def flatChangeBulk(aa: Iterable[A])(f: Option[T] => Observable[T]): Observable[T] = Observable.from(aa).concatMap { a =>
    get(a).singleOption.flatMap(f).flatMap(set(a, _))
  }

  final def remove(a: A): Observable[D] = self.remove(key(a))
}
