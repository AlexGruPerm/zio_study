package pkg

import cassdb.CassSessionInstance
import org.slf4j.LoggerFactory
import zio.console._
import zio.{App, Task, ZIO}

/**
 * Medium articles related with ZIO.
 *
 * https://medium.com/@wiemzin/zio-with-http4s-and-doobie-952fba51d089
 *
 *
 * The ZIO[R, E, A] data type has three type parameters:
 *
 * R - Environment Type. The effect requires an environment of type R. If this type parameter is Any, it means the effect has no requirements, because you can run the effect with any value (for example, the unit value ()).
 * E - Failure Type. The effect may fail with a value of type E. Some applications will use Throwable. If this type parameter is Nothing, it means the effect cannot fail, because there are no values of type Nothing.
 * A - Success Type. The effect may succeed with a value of type A. If this type parameter is Unit, it means the effect produces no useful information, while if it is Nothing, it means the effect runs forever (or until failure).
 *
 * type RIO[-R, +A]  = ZIO[R, Throwable, A]
 * type URIO[-R, +A] = ZIO[R, Nothing, A]
 * type IO[+E, +A]   = ZIO[Any, E, A]
 * type UIO[+A]      = ZIO[Any, Nothing, A]
 * type Task[+A]     = ZIO[Any, Throwable, A]
 *
*/

object FormCalculatorApp extends App {
  val log = LoggerFactory.getLogger(getClass.getName)
  val appName = "FormsCalc"
  val t1 = System.currentTimeMillis

  private val controlParams :Seq[ControlParams] =
    Seq(0.0012, 0.0025, 0.0050).flatMap(
      prcnt => Seq("mx", "mn").map(
        resType => ControlParams(6, 2, prcnt, resType)))

  val fcInst = FormCalculator


  /*
  trait WithDatabase { val database: Database }
trait WithEventBus { val eventbus: EventBus }

val createUser: ZIO[WithDatabase, AppError, User]
def userCreated(user: User): ZIO[WithEventBus, AppError, Unit]

val program: ZIO[WithDatabase with WithEventBus, AppError, User] =
  for {
    user <- createUser
    _    <- userCreated(user)
  } yield user

val runtime = new WithDatabase with WithEventBus {
  val database: Database = ...
  val eventbus: EventBus = ...
}

// type IO[E, A] = ZIO[Any, E, A]
program.provide(runtime): IO[AppError, User]
  */

  /*
  private val app: ZIO[Console, Throwable, Seq[BarFa]] =
    for {
      _ <- putStrLn("=========== begin calculation ===========")
      ses <- Task(CassSessionInstance)
      _ <- putStrLn(s"Is cassandra session opened : ${!ses.isSesCloses}")
      faFullMeta <- fcInst.readBarsFaMeta(ses, controlParams)
      thisFaMeta <- faFullMeta
      faBars <- for {thisBarData <- fcInst.readFaBarsData(ses,thisFaMeta)} yield thisBarData
      _ <- putStrLn("=========== end calculation ===========")
    } yield faBars
*/

  private val app: ZIO[Console, Throwable, Seq[BarFa]] = {
    val r :ZIO[Console,Nothing,Unit] = putStrLn("=========== begin calculation ===========").map(m => m)
    val ses = Task(CassSessionInstance)
    putStrLn(s"Is cassandra session opened : ${ses.map(s => s.isSesCloses)}")
    val faFullMeta :Task[Task[Seq[BarFaMeta]]] = ses.map(s =>  fcInst.readBarsFaMeta(s, controlParams))
    putStrLn("=========== end calculation ===========")

    val faBarsData :Task[Seq[Task[Task[Seq[BarFa]]]]] =
      faFullMeta.flatMap(                     // faFullMeta :Task[Task[Seq[BarFaMeta]]]
        setFMeta => setFMeta.map(             // seqFMeta :Task[Seq[BarFa]] after first flatMap we have internal Task
          thisFaMeta => thisFaMeta.map(       // thisFaMeta :Seq[BarFaMeta]
            thFaM => ses.map(s =>             // thFaM :BarFaMeta
              fcInst.readFaBarsData(s, thFaM) // Task[Seq[BarFa]]
            )
          )                                   // Seq[Task[Seq[BarFa]]]
        )
      )

    // Task[Seq[Task[Task[Seq[BarFa]]]]]
    val expFaBarData : Task[Seq[BarFa]] = faBarsData.flatMap(
      extTask => // extTask : Seq[Task[Task[Seq[BarFa]]]]
        extTask
    )

    //val t :Set[ZIO[Any, Throwable, zio.Task[Seq[BarFa]]]] = faBarsData.flatMap(setZioSeqFaBars => setZioSeqFaBars)

    /*
    val faFullMetaSet :ZIO[Any,Throwable,Set[BarFaMeta]] = faFullMeta.flatMap(ffm => ffm)
    faFullMetaSet
    */
      expFaBarData
  }


  /*
    private val app: ZIO[Console, Throwable, Set[BarFaMeta]] =
    for {
    _ <- putStrLn("=========== begin calculation ===========")
    ses <- Task(CassSessionInstance)
    _ <- putStrLn(s"Is cassandra session opened : ${!ses.isSesCloses}")
    faFullMeta <- fcInst.readBarsFaMeta(ses, controlParams)
    _ <- putStrLn("=========== end calculation ===========")
    } yield faFullMeta
  */


  def run(args: List[String]): ZIO[Console, Nothing, Int] = {
    app.fold(
      f => {
        println(s"fail f=$f message=${f.getMessage} cause=${f.getCause}"); 0
      },
      s => {
        println(s"success duration=${System.currentTimeMillis - t1} ms.");
        s.foreach(elm => /*if (elm.readFrom.isDefined)*/ println(elm))
        1
      }
    )
    }
  }