package cassdb

trait CassQueries {

  val sqlBarsFAMeta =
    """ select distinct ticker_id,ddate,bar_width_sec
      | from mts_bars.bars_fa """.stripMargin

  val sqlBarsFormsMaxDdate =
           """  select max(ddate)  as ddate,
             |         max(ts_end) as ts_end
             |    from mts_bars.bars_forms
             |   where ticker_id     = :p_ticker_id and
             |         bar_width_sec = :p_bar_width_sec and
             |         formdeepkoef  = :p_formdeepkoef and
             |         log_oe        = :p_log_oe and
             |         res_type      = :p_res_type
             |   allow filtering """.stripMargin

}
