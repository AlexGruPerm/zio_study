package pkg

import java.time.LocalDate

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
                     readFrom       :Option[LocalDate]
                     )
object BarFaMeta{
  def apply(bFaSrcMeta :barsFaSourceMeta, cp :ControlParams, readFrom  :Option[LocalDate]) :BarFaMeta ={
    new BarFaMeta(bFaSrcMeta.tickerId, bFaSrcMeta.barWidthSec, cp.formDeepKoeff, cp.intervalNewGroupKoeff,
      cp.percent, cp.resType, readFrom)
  }
}

          case class BarFa(
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


/**
 * result of calculation for each iteration: ticker,bws,....
 * in run method of main application returned as Seq[calcResInfo]
*/
case class calcResInfo(
                        barsMeta     :BarFaMeta,
                        insertedRows :Int
                      )


case class tinyTick(
                     db_tsunx  :Long,
                     ask       :Double,
                     bid       :Double
                   )
