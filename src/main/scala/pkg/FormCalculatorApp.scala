package pkg

import java.time.LocalDate

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
  val tc1 = System.currentTimeMillis

  private val controlParams: Seq[ControlParams] =
    Seq(0.0012 , 0.0025, 0.0050).flatMap(
      precent => Seq("mx", "mn").map(
        resType => ControlParams(6, 2, precent, resType)))

  val fcInst = FormCalculator
  val rt = new DefaultRuntime {}

  private val FaMmetaReader: Task[CassSessionInstance.type] => Task[Seq[BarFaMeta]] = ses =>
    Task(CassSessionInstance)
      .map(s => fcInst.readBarsFaMeta(s, controlParams)).flatMap(ttm => ttm)

  private val readFullDataByTickerBwsDate :(Task[CassSessionInstance.type], Int, Int , Option[LocalDate])
    => Task[Seq[BarFa]] =
    (ses, tickerId, Bws, dDate) =>
      ses.map(s =>
        fcInst.readFaBarsData(s, tickerId, Bws, dDate) // Task[Seq[BarFa]]
      ).flatMap(sb => sb).flatMap(ls => Task(ls))

  private val app: (BarFaMeta, Task[Seq[BarFa]]) => ZIO[Console, Throwable, Seq[BarFa]] =
    (faMeta, fullDs) =>
      fullDs.flatMap(sB =>
        Task(sB.filter(bfm =>
          bfm.tickerId == faMeta.tickerId &&
            bfm.barWidthSec == faMeta.barWidthSec &&
            bfm.log_oe == faMeta.percentLogOE &&
            bfm.res_type == faMeta.resType
        ))
      )

  def run(args: List[String]): ZIO[Console, Nothing, Int]= {
    val ses = Task(CassSessionInstance)
    //val faMeta :Task[Seq[BarFaMeta]] = FaMmetaReader(ses)
    val seqFaMeta: Seq[BarFaMeta] = rt.unsafeRun(FaMmetaReader(ses))

    println(s"FaMeta count = ${seqFaMeta.size} ---------------------")
    seqFaMeta.foreach(println)
    println("-------------------------------")

    val cni: Seq[ZIO[Console, Nothing, Int]] =
      seqFaMeta.map(fm => (fm.tickerId, fm.barWidthSec, fm.readFrom)).distinct.flatMap {
        k =>
          val fullDs = readFullDataByTickerBwsDate(ses, k._1, k._2, k._3)
          println(s"Read full Ds for k=$k  sec.")
          seqFaMeta.sortBy(f => (f.tickerId,f.barWidthSec,f.percentLogOE)).map(
            faMeta => {
              app(faMeta, fullDs).fold(
                f => {
                  println(s"fail f=$f message=${f.getMessage} cause=${f.getCause}");
                  0
                },
                s => {
                  val t1 = System.currentTimeMillis
                  println(s"success duration=${(System.currentTimeMillis - t1) / 1000} sec.");
                  println(s"for faMeta = ${faMeta} s.size=${s.size}")
                  //println(s"BarFa tickerId=${s.head.tickerId} bws=${s.head.barWidthSec} log_oe=${s.head.log_oe} res_type=${s.head.res_type} count=${s.size}")
                  //s.map(bfa => (bfa.tickerId,bfa.barWidthSec,bfa.log_oe,bfa.res_type)).distinct.foreach(println)
                  //s.foreach(elm => /*if (elm.readFrom.isDefined)*/ println(elm))
                  s.size
                }
              )
            }
          )
      }

    val res :Int = cni.map(elm => rt.unsafeRun(elm)).sum

    println(s"Summary duration=${(System.currentTimeMillis - tc1)/1000} sec.");
    IO.succeed(res)
  }


}