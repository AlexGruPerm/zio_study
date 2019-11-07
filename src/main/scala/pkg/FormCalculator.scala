package pkg

import cassdb.CassSessionInstance
import zio.Task

object FormCalculator{

  val readBarsFaMeta :(CassSessionInstance.type, Seq[ControlParams]) => Task[Seq[BarFaMeta]] =
    (ses, setControlParams) =>
      Task(
        setControlParams.flatMap(cp => ses.dbReadBarsFaMeta(cp))
      )

  val readFaBarsData :(CassSessionInstance.type, BarFaMeta) => Task[Seq[BarFa]] =
    (ses, sfm) =>
      Task(
        ses.dbReadBarsFa(sfm)
      )

}
