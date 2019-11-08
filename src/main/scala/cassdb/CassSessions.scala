package cassdb

import java.net.InetSocketAddress

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{BoundStatement, Row}
import com.typesafe.config.{Config, ConfigFactory}
import pkg.{BarFa, BarFaMeta, ControlParams, barsFaSourceMeta}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

trait CassSession extends CassQueries {
  //val log = LoggerFactory.getLogger(getClass.getName)

  val config :Config = ConfigFactory.load()
  val confConnectPath :String = "cassandra.connection."

  def getNodeAddressDc :(String,String) =
    (config.getString(confConnectPath+"ip"),
      config.getString(confConnectPath+"dc"))

  def createSession(node :String,dc :String,port :Int = 9042) :CqlSession =
    CqlSession.builder()
      .addContactPoint(new InetSocketAddress(node, port))
      .withLocalDatacenter(dc).build()

  def prepareSql(sess :CqlSession,sqlText :String) :BoundStatement =
    try {
      sess.prepare(sqlText).bind()
    }
     catch {
      case e: com.datastax.oss.driver.api.core.servererrors.SyntaxError =>
        //log.error(" prepareSQL - "+e.getMessage)
        throw e
    }
}

object CassSessionInstance extends CassSession{
  private val (node :String, dc :String) = getNodeAddressDc
  //log.info("CassSessionInstance DB Address : "+node+" - "+dc)
  private val ses :CqlSession = createSession(node,dc)
  //log.info("CassSessionInstance session is connected = " + !ses.isClosed)

  def isSesCloses :Boolean = ses.isClosed

  //This is a potential what we can read and calculate.
  private val prepBarsSourceFAMeta :BoundStatement = prepareSql(ses,sqlBarsFAMeta)
  //This is what we already have and can continue calculation.
  private val prepBarsFormsMaxDdate :BoundStatement = prepareSql(ses,sqlBarsFormsMaxDdate)
  //Return bars_fa for ticker+bws or >= ddate
  private val prepBarsFaDataMtDate :BoundStatement = prepareSql(ses,sqlBarsFaDataMtDate)
  private val prepBarsFaData :BoundStatement = prepareSql(ses,sqlBarsFaData)

  private val rowToFaSourceMeta : Row => barsFaSourceMeta =
    row => barsFaSourceMeta(row.getInt("ticker_id"),
                            row.getInt("bar_width_sec"))

  private val rowToBarFAData = (row: Row, tickerID: Int, barWidthSec: Int) => {
    BarFa(
      tickerID,
      row.getLocalDate("ddate"),
      barWidthSec,
      row.getLong("ts_end"),
      row.getDouble("c"),
      row.getDouble("log_oe"),
      row.getLong("ts_end_res"),
      row.getInt("dursec_res"),
      row.getLocalDate("ddate_res"),
      row.getDouble("c_res"),
      row.getString("res_type")
    )
  }

  private def readFormsMaxDateTs(bFaSrcMeta :barsFaSourceMeta, cp: ControlParams) :BarFaMeta = {
    //println(s"Call readFormsMaxDateTs for cp.percent = ${cp.percent} cp.resType = ${cp.resType}")
    val row = ses.execute(prepBarsFormsMaxDdate
      .setInt("p_ticker_id", bFaSrcMeta.tickerId)
      .setInt("p_bar_width_sec", bFaSrcMeta.barWidthSec)
      .setInt("p_formdeepkoef", cp.formDeepKoeff)
      .setDouble("p_log_oe", cp.percent)
      .setString("p_res_type", cp.resType)
    ).one()
    if (row.getLocalDate("ddate")==null)
      BarFaMeta(bFaSrcMeta, cp, None)
    else
      BarFaMeta(bFaSrcMeta, cp, Some(row.getLocalDate("ddate")))
  }

  def dbReadBarsFaMeta(cp: Seq[ControlParams]): Seq[BarFaMeta] = {
    val s :Seq[barsFaSourceMeta] = ses.execute(prepBarsSourceFAMeta).all
      .iterator.asScala.toSeq.map(r => rowToFaSourceMeta(r))
      .filter(elm => elm.tickerId==1 && Seq(30,300).contains(elm.barWidthSec))
      .distinct
     println(s"Distinct keys(tickerId,Bws) s=${s.size}")
    cp.flatMap(thisCp =>
      s.map(thisFaMeta => readFormsMaxDateTs(thisFaMeta, thisCp))
    )
  }

  def dbReadBarsFa(fm :BarFaMeta) :Seq[BarFa] =
    /*
    ses.execute(
      prepBarsFaData
        .setInt("p_ticker_id", fm.tickerId)
        .setInt("p_bar_width_sec", fm.barWidthSec)
        .setDouble("p_log_oe", fm.percentLogOE)
        .setString("p_res_type", fm.resType)
    ).all.iterator.asScala.toSeq
      .map(r => rowToBarFAData(r, fm.tickerId, fm.barWidthSec)).sortBy(_.ts_end)
  */
    fm.readFrom match {
      case Some(readFrom) => ses.execute(
        prepBarsFaDataMtDate
          .setInt("p_ticker_id", fm.tickerId)
          .setInt("p_bar_width_sec",fm.barWidthSec)
          .setDouble("p_log_oe", fm.percentLogOE)
          .setString("p_res_type", fm.resType)
          .setLocalDate("p_ddate",readFrom)).all.iterator.asScala.toSeq
        .map(r => rowToBarFAData(r,fm.tickerId,fm.barWidthSec)).sortBy(_.ts_end)
      case None => ses.execute(
        prepBarsFaData
          .setInt("p_ticker_id", fm.tickerId)
          .setDouble("p_log_oe", fm.percentLogOE)
          .setString("p_res_type", fm.resType)
          .setInt("p_bar_width_sec",fm.barWidthSec)).all.iterator.asScala.toSeq
        .map(r => rowToBarFAData(r,fm.tickerId,fm.barWidthSec)).sortBy(_.ts_end)
    }

}

