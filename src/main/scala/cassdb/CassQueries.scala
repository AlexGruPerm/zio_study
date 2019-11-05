package cassdb

trait CassQueries {

  val sqlBarsFAMeta =
    """ select distinct ticker_id,ddate,bar_width_sec
      | from mts_bars.bars_fa """.stripMargin

}
