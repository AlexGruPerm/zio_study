package pkg

import java.time.LocalDate

object common {
  type readFrom = (Option[LocalDate], Option[Long])
}

case class barsFaMeta(
                       tickerId       :Int,
                       barWidthSec    :Int,
                       dDate          :LocalDate,
                       formDeepKoeff  :Int,
                       intNewGrpKoeff :Int,
                       percentLogOE   :Double,
                       formWayType    :String
                     )

/**
 * result of calculation for each iteration: ticker,bws,....
 * in run method of main application returned as Seq[calcResInfo]
*/
case class calcResInfo(
                        barsMeta     :barsFaMeta,
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