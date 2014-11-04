package com.sandinh.couchbase.access

import com.couchbase.client.java.document.Document
import rx.lang.scala.Observable

trait Key2[T, A, B, U, D <: Document[U]] { self: DocBase[T, U, D] =>
  /** Map 2 param of type A, B to a CB key
    * @return CB key */
  protected def key(a: A, b: B): String

  final def get(a: A, b: B): Observable[T] = self.get(key(a, b))
  final def getBulk(aa: Iterable[A], b: B): Observable[Option[T]] = Observable.from(aa).concatMap(get(_, b).singleOption)

  final def set(a: A, b: B, t: T): Observable[T] = self.set(key(a, b), t)
  final def setBulk(aa: Iterable[A], b: B, tt: Iterable[T]): Observable[T] = Observable.from(aa zip tt).flatMap {
    case (a, t) => set(a, b, t)
  }

  final def change(a: A, b: B)(f: Option[T] => T): Observable[T] = get(a, b).singleOption.map(f).flatMap(set(a, b, _))
  final def flatChange(a: A, b: B)(f: Option[T] => Observable[T]): Observable[T] = get(a, b).singleOption.flatMap(f).flatMap(set(a, b, _))

  final def changeBulk(aa: Iterable[A], b: B)(f: Option[T] => T): Observable[T] = Observable.from(aa).concatMap { a =>
    get(a, b).singleOption.map(f).flatMap(set(a, b, _))
  }
  final def flatChangeBulk(aa: Iterable[A], b: B)(f: Option[T] => Observable[T]): Observable[T] = Observable.from(aa).concatMap { a =>
    get(a, b).singleOption.flatMap(f).flatMap(set(a, b, _))
  }

  final def remove(a: A, b: B): Observable[D] = self.remove(key(a, b))
}
