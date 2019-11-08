package cassdb

trait CassQueries {

  protected lazy val sqlBarsFAMeta =
    """ select distinct ticker_id,ddate,bar_width_sec
      | from mts_bars.bars_fa """.stripMargin

  protected lazy val sqlBarsFormsMaxDdate =
    """  select max(ddate)  as ddate
      |    from mts_bars.bars_forms
      |   where ticker_id     = :p_ticker_id and
      |         bar_width_sec = :p_bar_width_sec and
      |         formdeepkoef  = :p_formdeepkoef and
      |         log_oe        = :p_log_oe and
      |         res_type      = :p_res_type
      |   allow filtering """.stripMargin

  protected lazy val sqlBarsFaData =
    """ select   ddate,
      |          ts_end,
      |          c,
      |          log_oe,
      |          ts_end_res,
      |          dursec_res,
      |          ddate_res,
      |          c_res,
      |          res_type
      |     from mts_bars.bars_fa
      |    where ticker_id     = :p_ticker_id and
      |          bar_width_sec = :p_bar_width_sec and
      |          log_oe        = :p_log_oe and
      |          res_type      = :p_res_type
      |    allow filtering """.stripMargin

  protected lazy val sqlBarsFaDataMtDate =
    """ select   ddate,
      |          ts_end,
      |          c,
      |          log_oe,
      |          ts_end_res,
      |          dursec_res,
      |          ddate_res,
      |          c_res,
      |          res_type
      |     from mts_bars.bars_fa
      |    where ticker_id     = :p_ticker_id and
      |          bar_width_sec = :p_bar_width_sec and
      |          ddate         >= :p_ddate and
      |          log_oe        =  :p_log_oe and
      |          res_type      =  :p_res_type
      |    allow filtering """.stripMargin

}
