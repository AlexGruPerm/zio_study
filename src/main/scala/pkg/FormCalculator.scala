package pkg

import java.time.LocalDate

import cassdb.CassSessionInstance
import zio.Task

object FormCalculator{

  val readBarsFaMeta :(CassSessionInstance.type, Seq[ControlParams]) => Task[Seq[BarFaMeta]] =
    (ses, setControlParams) =>
      Task(
        ses.dbReadBarsFaMeta(setControlParams)
      )

  val readFaBarsData :(CassSessionInstance.type, Int, Int, Option[LocalDate]) => Task[Seq[BarFa]] =
    (ses, tickerId, Bws, dDate) =>
      Task(
        ses.dbReadBarsFa(tickerId, Bws, dDate)
      )

}
