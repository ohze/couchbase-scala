package com.sandinh.rx

import rx.lang.scala.{Subscription, Observable}
import scala.concurrent.Future
import rx.lang.scala.JavaConversions._

object Implicits {
  implicit class RichJObs[T](val underlying: rx.Observable[T]) extends AnyVal {
    def asScala = toScalaObservable(underlying)
  }

  implicit class RichObs[T](val underlying: Observable[T]) extends AnyVal {
    def toFuture: Future[T] = underlying.toBlocking.toFuture
    def subscribeError(e: Throwable => Unit): Subscription = underlying.subscribe(_ => (), e)
  }
}
