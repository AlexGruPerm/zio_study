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

}
