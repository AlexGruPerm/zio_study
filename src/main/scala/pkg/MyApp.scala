package pkg

import zio.console._
import zio.{App, IO, ZIO}

/**
 * Medium articles related with ZIO.
 *
 * https://medium.com/@wiemzin/zio-with-http4s-and-doobie-952fba51d089
 *
 *
 * type RIO[-R, +A]  = ZIO[R, Throwable, A]
 * type URIO[-R, +A] = ZIO[R, Nothing, A]
 * type IO[+E, +A]   = ZIO[Any, E, A]
 * type UIO[+A]      = ZIO[Any, Nothing, A]
 * type Task[+A]     = ZIO[Any, Throwable, A]
 *
*/

object MyApp extends App {

  val appName = "myapp-app"

  def run(args: List[String]): ZIO[Console, Nothing, Int] =
    myCalc(Seq(-1,-2,1,2)).fold(
      f => {println(s"fail message  =  ${f.getMessage}")
        0},
      s => {println(s"success result  =  $s")
        s})

  val myCalc :(Seq[Int] => ZIO[Console, Throwable, Int]) = (inpSeq) =>
      for {
        _    <- putStrLn("Begin calculation")
        r <-  (new Calc()).getResult1(inpSeq)
        res  <- r match {//we are here only if r contains Option[Int] and not raise exception
          case None => IO.fail(new Throwable(s"Empty result"))
          case Some(p) =>  ZIO.effectTotal(p)
        }
        _    <- putStrLn("End calculation")
      } yield res

}