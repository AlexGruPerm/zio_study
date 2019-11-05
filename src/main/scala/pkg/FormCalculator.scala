package pkg

import cassdb.CassSessionInstance
import zio.Task
/*
import java.util.concurrent.TimeUnit
import scala.util.Random
import Exceptions.ExcCantConnDb
import zio.duration.Duration
import zio.{Fiber, IO, UIO, ZIO}
*/

/**
 * Task[A]: a description of an effectful program that might fail with a Throwable or produce a value of type A.
 */


object FormCalculator{

  /*
  def readTickersDict(ses: CassSessionInstance.type) :Task[Seq[barsFaMeta]] =
    Task(ses.dbReadTickersDict)
  */

  val readTickersDict :CassSessionInstance.type => Task[Seq[barsFaMeta]] = (ses) =>
    Task(ses.dbReadTickersDict)

 // private def getStartReadDT :Task[readFrom] = ???

  //private def getAllFaBars(seqB :Seq[barsFaMeta], rf :readFrom) :Task[Seq[barsResToSaveDB]] = ???

  //loop by KEYS:  seqWays.flatMap {
  //                  wayType =>
  //                    prcntsDiv
  // in  val lastBarsOfFormsAllTickers: Seq[(Int, barsResToSaveDB)] =
 // private def filterFABars(seqB :Seq[barsResToSaveDB], intervalNewGroupKoeff :Int) : Task[Seq[(Int,barsResToSaveDB)]] = ???

  /**
   * Read seq of tiny ticks from DB
  */
 // private def getAllTicksForForms(tickerID :Int, firstBarOfLastBars :barsResToSaveDB, lastBarOfLastBars :barsResToSaveDB) :Task[Seq[tinyTick]] = ???

  //loop by lastBarsOfFormsAllTickers and create BForm
  /**
   * Save and return numer of inserted rows or Throwable.
  */
    /*
  private def saveForms(seqForms : Seq[BForm]) :Task[Int] = ???

  def runFormsCalculator(implicit ses: CassSessionInstance.type): Task[Seq[calcResInfo]] = {
    readTickersDict
  }
*/

}
