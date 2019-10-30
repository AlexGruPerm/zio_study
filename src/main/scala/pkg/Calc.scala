package pkg

import zio.Task

class Calc {

  def getResult(si : Seq[Int]) :Task[Option[Int]] =
      Task(Some(si.sum))

}
