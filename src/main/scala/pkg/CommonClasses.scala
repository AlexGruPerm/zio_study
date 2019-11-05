package pkg

import java.time.LocalDate

import pkg.common.maxFormDateTs

object common {
  type maxFormDateTs = Option[(LocalDate,Long)]
}

case class ControlParams(formDeepKoeff :Int,
                         intervalNewGroupKoeff :Int,
                         percent :Double,
                         resType :String
                        )

case class barsFaSourceMeta(
                            tickerId       :Int,
                            barWidthSec    :Int
                           )

case class BarFaMeta(
                     tickerId       :Int,
                     barWidthSec    :Int,
                     formDeepKoeff  :Int,
                     intNewGrpKoeff :Int,
                     percentLogOE   :Double,
                     resType        :String,
                     readFrom       :maxFormDateTs
                     )
object BarFaMeta{
  def apply(bFaSrcMeta :barsFaSourceMeta, cp :ControlParams, readFrom  :maxFormDateTs) :BarFaMeta ={
    new BarFaMeta(bFaSrcMeta.tickerId, bFaSrcMeta.barWidthSec, cp.formDeepKoeff, cp.intervalNewGroupKoeff,
      cp.percent, cp.resType, readFrom)
  }
}


/**
 * result of calculation for each iteration: ticker,bws,....
 * in run method of main application returned as Seq[calcResInfo]
*/
case class calcResInfo(
                        barsMeta     :BarFaMeta,
                        insertedRows :Int
                      )

case class barsResToSaveDB(
                            tickerId     :Int,
                            dDate        :LocalDate,
                            barWidthSec  :Int,
                            ts_end       :Long,
                            c            :Double,
                            log_oe       :Double,
                            ts_end_res   :Long,
                            dursec_res   :Int,
                            ddate_res    :LocalDate,
                            c_res        :Double,
                            res_type     :String
                          )

case class tinyTick(
                     db_tsunx  :Long,
                     ask       :Double,
                     bid       :Double
                   )