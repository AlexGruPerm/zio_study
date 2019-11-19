package pkg

import cassdb.CassSessionInstance
import zio._
import zio.console._

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
  val appName = "FormsCalc"
  val tc1 = System.currentTimeMillis

  private val controlParams: Seq[ControlParams] =
    Seq(0.0012 /*, 0.0025, 0.0050*/).flatMap( // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      precent => Seq("mx"/*, "mn"*/).map(     // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        resType => ControlParams(6, 2, precent, resType)))

  val fcInst :FormCalculator.type = FormCalculator
  val rt : Runtime[ZEnv] = new DefaultRuntime {}


  private val FaMmetaReader: Task[CassSessionInstance.type] => Task[Seq[BarFaMeta]] =
    ses =>
    for{
      s <- ses
      r <- fcInst.readBarsFaMeta(s, controlParams)
    } yield r

  /*
  private val calcForm: (Task[CassSessionInstance.type],BarFa, Int, Seq[tinyTick]) => Task[BForm] =
    (ses, bf, formDeepKoef, st) =>
      for {
        s <- ses
        frm <- fcInst.createForm(bf, formDeepKoef, st)
      } yield frm
  */



/*
  private val TicksReader: (Task[CassSessionInstance.type], BarFa, BarFa) => Task[Seq[tinyTick]] =
    (ses, fb, lb) =>
    for {
      s <- ses
      ticks <- fcInst.readAllTicksForForms(s,fb,lb)
    } yield ticks
  */

  private val app: (Task[CassSessionInstance.type], BarFaMeta) => ZIO[Console, Throwable, Seq[Int]] =
    (ses,faMeta) =>
      for {
        s <- ses
        dat <- fcInst.readFaBarsData(s, faMeta)
        lastBarsOfFormsAllTickers <- fcInst.filterData(dat, faMeta)  // filterData : Task[Seq[(Int,BarFa)]]
        fb <- Task(lastBarsOfFormsAllTickers.map(pairGroupBarFa => pairGroupBarFa._2).minBy(_.ts_end)) //common value for all Seq lastBarsOfFormsAllTickers
        lb <- Task(lastBarsOfFormsAllTickers.map(pairGroupBarFa => pairGroupBarFa._2).maxBy(_.ts_end)) //common value for all Seq lastBarsOfFormsAllTickers
        seqTicks <- fcInst.readAllTicksForForms(s, fb, lb) //read all for interval by fb,lb
        formsRows <-  IO.sequence(lastBarsOfFormsAllTickers.map(pairGroupBarFa =>
          fcInst.createForm(pairGroupBarFa._2,faMeta.formDeepKoeff,
            seqTicks.filter(t => t.db_tsunx >= (pairGroupBarFa._2.ts_end - faMeta.formDeepKoeff * pairGroupBarFa._2.barWidthSec * 1000L)
              && t.db_tsunx <= pairGroupBarFa._2.ts_end)
          )))
        rowsCounts <- s.saveForms(formsRows)
      } yield rowsCounts



  def run(args: List[String]): ZIO[Console, Nothing, Int]= {
    val ses = Task(CassSessionInstance)
    val seqFaMeta: Seq[BarFaMeta] = rt.unsafeRun(FaMmetaReader(ses))
    seqFaMeta.foreach(println)

    val cni: Seq[ZIO[Console, Nothing, Int]] =
          seqFaMeta.sortBy(f => (f.tickerId,f.barWidthSec,f.percentLogOE)).map(
            faMeta => {
              //val t1 = System.currentTimeMillis
              app(ses, faMeta).fold(
                f => {
                  println(s"fail f=$f message=${f.getMessage} cause=${f.getCause}");
                  0
                },
                s => {
                  //println(s"success duration=${(System.currentTimeMillis - t1)} ms.");
                  println(s"for Forms = ${faMeta} s.size=${s.size}")
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