package pkg

import cassdb.CassSessionInstance
import zio.Task

object FormCalculator{

  val readBarsFaMeta :(CassSessionInstance.type, Seq[ControlParams]) => Task[Seq[BarFaMeta]] =
    (ses, setControlParams) =>
      Task(
        ses.dbReadBarsFaMeta(setControlParams)
      )

  val readFaBarsData :(CassSessionInstance.type, BarFaMeta) => Task[Seq[BarFa]] =
    (ses, fm) =>
      Task(
        ses.dbReadBarsFa(fm)
      )

  val filterData : (Seq[BarFa], BarFaMeta) => Task[Seq[(Int,BarFa)]] = (dat, faMeta) => {
    dat.headOption match {
      case Some(ho) => {
        val groupIntervalSec = ho.barWidthSec * faMeta.intNewGrpKoeff
        Task(dat.tail.foldLeft(List((1, dat.head))) {
          (acc, elm) =>
            if ((elm.ts_end - acc.head._2.ts_end) / 1000L < groupIntervalSec)
              (acc.head._1, elm) :: acc
            else
              (acc.head._1 + 1, elm) :: acc
        }.reverse.groupBy(elm => elm._1).map(
          s => (s._1, s._2.filter(
            e => e._2.ts_end == s._2.map(b => b._2.ts_end).max
          ))
        ).toSeq.flatMap(elm => elm._2).sortBy(e => e._2.ts_end))
      }
      case None => Task(Nil)
    }
    /*
      val groupIntervalSec = dat.head.barWidthSec * faMeta.intNewGrpKoeff
      Task(dat.tail.foldLeft(List((1, dat.head))){
        (acc, elm) =>
          if ((elm.ts_end - acc.head._2.ts_end) / 1000L < groupIntervalSec)
            (acc.head._1, elm) :: acc
          else
            (acc.head._1 + 1, elm) :: acc
      }.reverse.groupBy(elm => elm._1).map(
        s => (s._1, s._2.filter(
          e => e._2.ts_end == (s._2.map(
            b => b._2.ts_end).max)
        ))
      ).toSeq.flatMap(elm => elm._2).sortBy(e => e._2.ts_end))
    */
    }

}
