package pkg

import cassdb.CassSessionInstance
import zio.DefaultRuntime
import zio.console._
import zio.{App, IO, Task, ZIO}

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
  //val log = LoggerFactory.getLogger(getClass.getName)
  val appName = "FormsCalc"
  val t1 = System.currentTimeMillis

  private val controlParams: Seq[ControlParams] =
    Seq(0.0012 , 0.0025, 0.0050).flatMap(
      prcnt => Seq("mx", "mn").map(
        resType => ControlParams(6, 2, prcnt, resType)))

  val fcInst = FormCalculator
  val rt = new DefaultRuntime {}


  private val FaMmetaReader: Task[CassSessionInstance.type] => Task[Seq[BarFaMeta]] = ses =>
    Task(CassSessionInstance)
      .map(s => fcInst.readBarsFaMeta(s, controlParams)).flatMap(ttm => ttm)


  private val app: (Task[CassSessionInstance.type], BarFaMeta) => ZIO[Console, Throwable, Seq[BarFa]] =
    (ses, faMeta) =>
      ses.map(s =>
        fcInst.readFaBarsData(s, faMeta) // Task[Seq[BarFa]]
      ).flatMap(sb => sb).flatMap(ls => Task(ls))


  def run(args: List[String]): ZIO[Console, Nothing, Int]= {
    val ses = Task(CassSessionInstance)
    //val faMeta :Task[Seq[BarFaMeta]] = FaMmetaReader(ses)
    val seqFaMeta: Seq[BarFaMeta] = rt.unsafeRun(FaMmetaReader(ses))

    println(s"FaMeta count = ${seqFaMeta.size}")

    val cni: Seq[ZIO[Console, Nothing, Int]] =
      seqFaMeta.map(
        faMeta =>
        app(ses, faMeta).fold(
          f => {
            println(s"fail f=$f message=${f.getMessage} cause=${f.getCause}");
            0
          },
          s => {
            println(s"success duration=${(System.currentTimeMillis - t1)/1000} sec.");
            println(s"BarFa count=${s.size}")
            //s.foreach(elm => /*if (elm.readFrom.isDefined)*/ println(elm))
            s.size
          }
        )
      )

    val res :Int = cni.map(elm => rt.unsafeRun(elm)).sum

    println(s"Summary duration=${(System.currentTimeMillis - t1)/1000} sec.");
    IO.succeed(res)
  }


}


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
*/

//LOGGER: https://stackoverflow.com/questions/58536841/zio-environment-construction
//bottom case.