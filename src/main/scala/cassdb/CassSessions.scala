package cassdb

import java.net.InetSocketAddress
import java.time.LocalDate

import scala.language.implicitConversions
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{BoundStatement, Row}
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import pkg.barsFaMeta

import scala.collection.JavaConverters._

trait CassSession extends CassQueries {
  val log = LoggerFactory.getLogger(getClass.getName)

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
        log.error(" prepareSQL - "+e.getMessage)
        throw e
    }
}

object CassSessionInstance extends CassSession{
  private val (node :String, dc :String) = getNodeAddressDc
  log.info("CassSessionInstance DB Address : "+node+" - "+dc)
  private val ses :CqlSession = createSession(node,dc)
  log.info("CassSessionInstance session is connected = " + !ses.isClosed)

  /**
   * This is a potential what we can read and calculate.
  */
  private val prepBarsFAMeta :BoundStatement = prepareSql(ses,sqlBarsFAMeta)

  /**
   * This is what we already have and can continue calculation.
  */
  private val prepBarsFormsMaxDdate :BoundStatement = prepareSql(ses,sqlBarsFormsMaxDdate)

  implicit def LocalDateToOpt(v :LocalDate) :Option[LocalDate] = Option(v)

  private val rowTo

  private val rowToFaMeta : Row => barsFaMeta = {
    (row: Row) =>
       val tickerId :Int = row.getInt("ticker_id")
       val bws :Int =  row.getInt("bar_width_sec")
       val ddate :LocalDate =  row.getLocalDate("ddate")

    barsFaMeta(
      tickerId,
      bws,
      ddate
    )

  }

  def isSesCloses :Boolean = ses.isClosed

  def dbReadTickersDict(formDeepKoeff :Int, intervalNewGroupKoeff :Int, thisPrcnt :Double, thisSw :String) :Seq[barsFaMeta] =
              ses.execute(prepBarsFAMeta).all
                .iterator.asScala.toSeq.map(r => rowToFaMeta(r))

}

