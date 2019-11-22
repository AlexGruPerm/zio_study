import zio._
import zio.duration._

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

val searchUserAccount : (String,Long) => Task[User] = (email,ts) =>
  for {
    i :Int <- Task(r.nextInt(50))
    _ <- IO.effect(println(s"searchUserAccount i=$i"))
    _ <- IO.effect(println(s" -------- ${(System.currentTimeMillis()-ts)/1000L} ms."))
    r <- {
      if (i <= 5)
       ZIO.succeed(User(1L, "wile@acme.com", "Will"))
      else
        ZIO.fail(new ExcCantConnDb("User not in our database."))
    }
  } yield r

//https://github.com/zio/zio/blob/master/docs/datatypes/schedule.md
//https://github.com/zio/zio/blob/master/docs/datatypes/schedule.md !!!
/**
 * Retry:
 * There are a number of useful combinators for repeating actions until failure or success:
 *
 * IO.forever — Repeats the action until the first failure.
 * IO.retry — Repeats the action until the first success.
*/
(new zio.DefaultRuntime {}).unsafeRun(
      for {
        start :Long <- Task(System.currentTimeMillis())
        _ <- IO.effect(println(s"start = $start"))
        user <-
          searchUserAccount("wile@acme.com",start)
            .retry(
              Schedule.exponential(10.milliseconds) &&
                Schedule.elapsed.whileOutput(_ < 3.seconds)
            ) //fast 5 calls
            //.repeat(Schedule.spaced(5.seconds))
      } yield user
)

/** ==============================================================================
 * Repeats - выполнять пока не закончится расписание.,
 * или пока не вернется первое исключение.
 * М.б. один вызов и сразу исключение.
 * ===============================================================================
 *
 *  Repeats this effect with the specified schedule until the schedule
 *  completes, or until the first failure.
 *
 * ===============================================================================
 * Repeat :
 * in case of failure, try again
 *
 * ===============================================================================
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