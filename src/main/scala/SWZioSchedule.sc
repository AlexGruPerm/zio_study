import zio._
import zio.console._

/*
import zio._
import zio.clock.Clock
import zio.console._
import zio.duration._
import java.util.concurrent.TimeUnit
*/

val r = scala.util.Random

class ExcCantConnDb(message: String) extends Exception(message) {
  def this(message: String, cause: Throwable) {
    this(message)
    initCause(cause)
  }
}

case class User(id: Long, email: String, name: String)
type Error = String with Serializable with Throwable


val searchUserAccount : Long => ZIO[Console, Throwable, User]
/* type Task[+A] = ZIO[Any, Throwable, A]*/
 = ts =>
  for {
    i :Int <- Task(r.nextInt(50))
    sucUpBnd :Int <- Task(25)
    _ <- putStrLn(s" searchUserAccount  ${(System.currentTimeMillis()-ts)/1000L} ms. i=$i")
    _ <- if (i <= sucUpBnd)
           putStrLn("Success")
         else
           putStrLn("--- Failure ---")
    r <- if (i <= sucUpBnd)
           /*ZIO.succeed*/Task(User(1L, "wile@acme.com", "Will"))
         else
           ZIO.fail(new ExcCantConnDb("User not found in database."))
    //r <- ZIO.fail(new ExcCantConnDb("User not in our database."))
    //r <- ZIO.succeed(User(1L, "wile@acme.com", "Will"))
  } yield r



val searchUserAccountFailHandler : (Throwable,Option[Any]) => Task[Any]
 = (throwE,optUser) => for {
    _ <- IO.effect(println(s"searchUserAccountFailHandler"))
   // r <- ZIO.succeed(User(1L, "default@def.com", "DefaultUser"))
    r <- IO.effect(new ExcCantConnDb("User not in default secondary database."))
} yield r

//https://github.com/zio/zio/blob/master/docs/datatypes/schedule.md !!!
/**
 * Retry:
 * There are a number of useful combinators for repeating actions until failure or success:
 *
 * IO.forever — Repeats the action until the first failure.
 * IO.retry — Repeats the action until the first success.
*/

(new zio.DefaultRuntime {}).unsafeRun(
  (for {
        start :Long <- zio.Task(System.currentTimeMillis())
        _ <- zio.IO.effect(println(s"start = $start"))
        user <-
          searchUserAccount(start)
            .repeatOrElse(Schedule.recurs(50),searchUserAccountFailHandler)
      } yield user)
    .catchAll(e => console.putStrLn(s"Application run failed $e").as(1))
)

//Schedule.spaced(1.second) && Schedule.recurs(5)
//https://zio.dev/docs/datatypes/datatypes_schedule


/** ==============================================================================
 * Repeats - выполнять пока не закончится расписание.,
 * или пока не вернется первое исключение.
 * М.б. один вызов и сразу исключение.
 * ===============================================================================
 *
 * Retries with the specified retry policy.
 * Retries are done following the failure of the original `io` (up to a fixed maximum with
 * `once` or `recurs` for example), so that that `io.retry(Schedule.once)` means
 * "execute `io` and in case of failure, try again once".
 *
 *
 * ?????
 *
 *               Schedule.exponential(10.milliseconds) &&
 *                 Schedule.elapsed.whileOutput(_ < 3.seconds)
 *
*/


/*
 execute 6 times and fails if not success
            .repeat(Schedule.spaced(500.milliseconds))
            .retry(Schedule.recurs(5))

             .retry(Schedule.recurs(5) && Schedule.spaced(1.second))
*/

/**
 *
 * Combines two schedules sequentially
 * val sequential = Schedule.recurs(5) andThen Schedule.spaced(1.second)
 *
 * Combines two schedules through intersection,
 * val expUpTo10 = Schedule.spaced(100.milliseconds) && Schedule.recurs(10)
 *
 * * Repeats this effect with the specified schedule until the schedule
 * * completes, or until the first failure.
 *
*/