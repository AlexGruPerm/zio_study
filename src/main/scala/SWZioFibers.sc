import zio._
import zio.console._

/*
import zio._
import zio.clock.Clock
import zio.console._
import zio.duration._
import java.util.concurrent.TimeUnit
*/

/**
 * https://zio.dev/docs/overview/overview_basic_concurrency
 * https://github.com/zio/zio/blob/master/docs/datatypes/fiber.md
*/

  def eff1 : Task[Int] = IO.succeed(123)
  def eff2 : Task[Int] = IO.succeed(456)
  def eff3(v1 :Int, v2 :Int) : Task[Int] = IO.succeed(v1+v2)

  def f1 :Task[Int] = IO.fail(new Exception("f1 fail"))
  def f2 :Task[Int] = IO.succeed(2)
  def f3 :Task[Int] = IO.fail(new Exception("f3 fail"))
  def f4 :Task[Int]= IO.succeed(4)
  def f5 :Task[Int] = IO.fail(new Exception("f5 fail"))

(new zio.DefaultRuntime {}).unsafeRun(
  for {
    f1f <- f1.fork
    f2f <- f2.fork/*
    f3f <- f3.fork
    f4f <- f4.fork
    f5f <- f5.fork*/
    ff = f1f.orElse(f2f)//.orElse(f3f).orElse(f4f).orElse(f5f)
    r <- ff.join
    _ <- putStrLn(s"Result is [$r]")
  } yield ()
)

/*
  (new zio.DefaultRuntime {}).unsafeRun(
    for {
      f1 <- eff1.fork
      f2 <- eff2.fork
      //These methods combine two fibers into a single fiber that produces the results of both.
      fComp = f1.zip(f2)
      fCompTupleRes <- fComp.join
      f3 <- (eff3 _).tupled(fCompTupleRes).fork
      r <- f3.join
      _ <- putStrLn(s"Result is [$r]")
    } yield ()
  )
*/
