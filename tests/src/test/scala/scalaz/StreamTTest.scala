package scalaz

import std.AllInstances._
import scalaz.scalacheck.ScalazProperties._
import scalaz.scalacheck.ScalazArbitrary._

class StreamTTest extends Spec {
  type StreamTOpt[A] = StreamT[Option, A]

  "fromStream / toStream" ! check {
    (ass: Stream[Stream[Int]]) =>
      StreamT.fromStream(ass).toStream must be_===(ass)
  }

  "filter all" ! check {
    (ass: StreamT[Stream, Int]) =>
      ass.filter(_ => true) must be_===(ass)
  }

  "isEmpty" ! check {
    (s: Stream[Int]) =>
      StreamT.fromStream(List(s)).isEmpty.forall(_ == s.isEmpty)
  }

  "filter none" ! check {
    (ass: StreamT[Stream, Int]) =>
      val filtered = ass.filter(_ => false)
      val isEmpty = filtered.isEmpty
      isEmpty.forall(_ == true)
  }
  
  "drop" ! check {
    (ass: Option[Stream[Int]], x: Int) =>
      StreamT.fromStream(ass).drop(x).toStream must be_===(ass.map(_.drop(x)))
  }
  
  "take" ! check {
    (ass: Option[Stream[Int]], x: Int) =>
      StreamT.fromStream(ass).take(x).toStream must be_===(ass.map(_.take(x)))
  }
  
  // Exists to ensure that fromStream and map don't stack overflow.
  "large stream mapped" ! {
    val stream = (0 to 400).toStream.map(_ => (0 to 400).toStream)
    StreamT.fromStream(stream).map(_ * 2).toStream must be_===(stream.map(_.map(_ * 2)))
  }

  checkAll(equal.laws[StreamTOpt[Int]])
  checkAll(monoid.laws[StreamTOpt[Int]])
  checkAll(monad.laws[StreamTOpt])
  
  object instances {
    def semigroup[F[+_]: Monad, A] = Semigroup[StreamT[F, A]]
    def monoid[F[+_]: Monad, A] = Monoid[StreamT[F, A]]
    def functor[F[+_]: Functor, A] = Functor[({type λ[α]=StreamT[F, α]})#λ]
    def monad[F[+_]: Monad, A] = Monad[({type λ[α]=StreamT[F, α]})#λ]
  }
}
