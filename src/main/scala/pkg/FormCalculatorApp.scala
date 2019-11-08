package pkg

import cassdb.CassSessionInstance
import zio.console._
import zio._

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

  private val FaMmetaReader: Task[CassSessionInstance.type] => Task[Seq[BarFaMeta]] =
    ses =>
    for{
      s <- ses
      r <- fcInst.readBarsFaMeta(s, controlParams)
    } yield r


  /**
   * todo:
   * 1) app convert into FaDataReader
   * 2) in app change return type from Task[Seq[BarFa]] into Task[Seq[CalcForm]]
   *
   *
  */

  private val app: (Task[CassSessionInstance.type], BarFaMeta) => ZIO[Console, Throwable, Seq[BarFa]] =
    (ses,faMeta) =>
      for {
        s <- ses
        r <- fcInst.readFaBarsData(s, faMeta)
      } yield r


  def run(args: List[String]): ZIO[Console, Nothing, Int]= {
    val ses = Task(CassSessionInstance)
    val seqFaMeta: Seq[BarFaMeta] = rt.unsafeRun(FaMmetaReader(ses))
    seqFaMeta.foreach(println)

    val cni: Seq[ZIO[Console, Nothing, Int]] =
          seqFaMeta.sortBy(f => (f.tickerId,f.barWidthSec,f.percentLogOE)).map(
            faMeta => {
              val t1 = System.currentTimeMillis
              app(ses, faMeta).fold(
                f => {
                  println(s"fail f=$f message=${f.getMessage} cause=${f.getCause}");
                  0
                },
                s => {
                  println(s"success duration=${(System.currentTimeMillis - t1)} ms.");
                  println(s"for faMeta = ${faMeta} s.size=${s.size}")
                  s.size
                }
              )
            }
          )


    val res :Int = cni.map(elm => rt.unsafeRun(elm)).sum

    println(s"Summary duration=${(System.currentTimeMillis - tc1)/1000} sec.");
    IO.succeed(res)
  }


}