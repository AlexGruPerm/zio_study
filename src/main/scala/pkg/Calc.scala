package pkg

import Exceptions.ExcCantConnDb
import zio.IO

class Calc {


  def getResult1(si : Seq[Int]) :IO[Throwable,Option[Int]] = {
    val e = new ExcCantConnDb("message 1")
    //IO(throw e)
    IO(Some(si.sum))
  }

  def getResult2(si : Seq[Int]) :IO[Throwable,Option[Int]] = {
    val e = new Throwable("message 2 ")
    IO(throw e)
    //IO(Some(si.sum))
  }

}
