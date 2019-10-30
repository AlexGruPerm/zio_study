package pkg

import zio.console._
import zio.{App, IO, ZIO}

object MyApp extends App {

  val appName = "myapp-app"

  /**
   * final def fold[B](failure: E => B, success: A => B): ZIO[R, Nothing, B] =
   * foldM(new ZIO.MapFn(failure), new ZIO.MapFn(success))
   */
  def run(args: List[String]): ZIO[Console, Nothing, Int] = {
    myCalc.fold(
      f => {putStrLn(s"v = ${f.getMessage}")
        0
      },
      v => {putStrLn(s"v = $v")
        v
      }
    )
    myAppLogic.fold(_ => 1, _ => 0)
 }


  val myCalc :ZIO[Console, Throwable, Int] =
      for {
        _    <- putStrLn("Begin calculation")
        r <-  (new Calc()).getResult(Seq(1, 2, 3, 4, 5))
        res  <- r match {
          case None => IO.fail(new Throwable(s"Calc fails because ... "))
          case Some(p) =>  ZIO.effectTotal(p)
        }
        _    <- putStrLn("End calculation")
      } yield res


  val myAppLogic :ZIO[Console, Exception, Unit] =
    for {
      _    <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _    <- putStrLn(s"Hello, ${name}, welcome to ZIO!")
    } yield ()

}