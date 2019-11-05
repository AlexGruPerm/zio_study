package pkg

import java.time.LocalDate

case class   BForm(
                    tickerId: Int,
                    barWidthSec: Int,
                    dDate: LocalDate,
                    TsBegin: Long,
                    TsEnd: Long,
                    log_oe: Double,
                    resType: String,
                    ts_end_res: Long,
                    dursec_res: Int,
                    ddate_res: LocalDate,
                    c_res: Double,
                    formDeepKoef: Int,
                    FormProps: Map[String, String]
                  )


object BForm {

  /**
   * Return price where VSA profile has maximum = PEAK
   * calculated by price frequency
   */
  def getMaxRo(seqTicks :Seq[tinyTick]) :BigDecimal = {
    val formCMin :BigDecimal = seqTicks.map(_.ask).min          // total min price
    val formCMax :BigDecimal = seqTicks.map(_.ask).max          // total max price
    val n = 10
    val rngCStep = (formCMax-formCMin)/n

    if (rngCStep != 0) {
      val rngC = formCMin.to(formCMax).by(rngCStep)
      val rangesC = rngC.zip(rngC.tail)

      val rangeFreq: Seq[(BigDecimal, Int)] = rangesC.map(rng =>
        (rng._1, seqTicks.count(t => t.ask >= rng._1 && t.ask <= rng._2)))

      rangeFreq.maxBy(r => r._2)._1

    } else formCMin
  }

  def create(barFa: barsResToSaveDB,
             formDeepKoef: Int,
             seqTicks: Seq[tinyTick]): BForm = {
    val ticksCnt: Int = seqTicks.size
    val tsBegin = seqTicks.size match {
      case 0 => 0L
      case _ => seqTicks.map(t => t.db_tsunx).min
    }

    /**
     * Calculate form configuration.
     * Basic grid:
     * c_begin  peak_pos  c_end
     * 1         1        1
     * 2         2        2
     * 3         3        3
     *
     * If form begins with high c and then peak exists in down part and then c going up.
     * We can get (131)
     * There are possible 27 distinct values.
     * Plus: (000)=0 if form width in seconds less < than 5*bws = (formDeepKoef-1)*bws
     * and Plus 9 values if we can determine peak position.
     * then possible (1,0,1),(1,0,2) etc.
     */

    def simpleRound3Double(valueD: Double) =
      (valueD * 1000).round / 1000.toDouble

    def simpleRound6Double(valueD: Double) =
      (valueD * 1000000).round / 1000000.toDouble

    // println("create1:                       seqTicks.size = "+seqTicks.size)
    // println("create2: seqTicks.maxBy(_.db_tsunx).db_tsunx = "+seqTicks.maxBy(_.db_tsunx).db_tsunx)

    val frmConfPeak: Int = {
      if (seqTicks.isEmpty) 0
      else {
        // Check that real seq width in seconds no less than contol limit.
        if ((seqTicks.maxBy(_.db_tsunx).db_tsunx - seqTicks.minBy(_.db_tsunx).db_tsunx) / 1000L < (formDeepKoef - 1) * barFa.barWidthSec) 0
        else {
          val formBeginC = seqTicks.minBy(_.db_tsunx).ask // begin price of ticker for form
          val formEndC = seqTicks.maxBy(_.db_tsunx).ask // last price of form
          val formCMin = seqTicks.map(_.ask).min // total min price
          val formCMax = seqTicks.map(_.ask).max // total max price
          val deltaC = (formCMax - formCMin) / 3
          val c1 = formCMin
          val c2 = c1 + deltaC
          val c3 = c2 + deltaC
          val c4 = formCMax

          val fc1: Int = if (formBeginC >= c1 && formBeginC <= c2) 3
          else if (formBeginC >= c2 && formBeginC < c3) 2
          else 1

          val fc3: Int = if (formEndC >= c1 && formEndC <= c2) 3
          else if (formEndC >= c2 && formEndC < c3) 2
          else 1

          val cMaxRo: BigDecimal = getMaxRo(seqTicks)
          val fc2: Int = if (cMaxRo >= c1 && cMaxRo <= c2) 3
          else if (cMaxRo >= c2 && cMaxRo < c3) 2
          else 1

          fc1 * 100 + fc2 * 10 + fc3
        }
      }
    }


    //here we can calculate any all forms properties.
    //         (SpS,SmS)
    val SdivS: (Double, Double) = if (seqTicks.toList.size <= 1) {
      (0.0,0.0)
    }
    else {
      val pairsCurrNxt :Seq[(tinyTick,tinyTick)] = seqTicks.zip(seqTicks.tail)

      val filterUp: ((tinyTick,tinyTick)) => Boolean = {
        case (f: tinyTick, s: tinyTick) => if (f.ask < s.ask) true else false
      }

      val filterDown: ((tinyTick,tinyTick)) => Boolean = {
        case (f: tinyTick, s: tinyTick) => if (f.ask > s.ask) true else false
      }

      val filterPairInInterval : ((tinyTick,tinyTick),Double,Double) => Boolean = {
        case ((f: tinyTick, s: tinyTick),beginInterval,endInterval) =>
          if ((s.ask-f.ask) >= beginInterval && (s.ask-f.ask) < endInterval) true
          else false
      }

      val seqPairsUp = pairsCurrNxt.filter(filterUp)
      val seqPairsDown = pairsCurrNxt.filter(filterDown).map(elm => (elm._2,elm._1))

      /*
      println(" seqPairsUp.size="+seqPairsUp.size)
      println(" seqPairsDown.size="+seqPairsDown.size)
      println(" example (ask1 - ask2): "+seqPairsUp.head._1.ask+" - "+seqPairsUp.head._2.ask)
      */

      val n = 10
      val minPairUpStep = seqPairsUp.map(e => e._2.ask-e._1.ask).reduceOption(_ min _).getOrElse(0.0)
      val maxPairUpStep = seqPairsUp.map(e => e._2.ask-e._1.ask).reduceOption(_ max _).getOrElse(0.0)
      val widthPairsUp = simpleRound6Double((maxPairUpStep - minPairUpStep)/n)

      /*
      println(" minPairUpStep="+minPairUpStep)
      println(" maxPairUpStep="+maxPairUpStep)
      println(" widthPairsUp="+widthPairsUp)
      */

      val Sp :Double = if (widthPairsUp == 0) 0.000000
      else (
        for (idx <- Range(1, n + 2)) yield {
          val freqInterval = seqPairsUp.count(elm => filterPairInInterval(
            elm,
            minPairUpStep + (idx - 1) * widthPairsUp,
            minPairUpStep + idx * widthPairsUp
          ))
          simpleRound6Double((minPairUpStep + idx * widthPairsUp) - (minPairUpStep + (idx - 1) * widthPairsUp)
          ) * freqInterval
        }).sum

      val minPairDownStep = seqPairsDown.map(e => e._2.ask-e._1.ask).reduceOption(_ min _).getOrElse(0.0)
      val maxPairDownStep = seqPairsDown.map(e => e._2.ask-e._1.ask).reduceOption(_ max _).getOrElse(0.0)
      val widthPairsDown = simpleRound6Double((maxPairDownStep - minPairDownStep)/n)

      val Sm :Double = if (widthPairsDown == 0) 0.000000
      else (
        for (idx <- Range(1, n + 2)) yield {
          val freqInterval: Int = seqPairsDown.count(elm => filterPairInInterval(
            elm,
            minPairDownStep + (idx - 1) * widthPairsDown,
            minPairDownStep + idx * widthPairsDown
          ))
          simpleRound6Double((minPairDownStep + idx * widthPairsDown) - (minPairDownStep + (idx - 1) * widthPairsDown)
          ) * freqInterval
        }).sum
      /*
      println("ticker_id= "+barFa.tickerId)

      println("barFa.ts_end = "+barFa.ts_end)
      println("barFa.c = "+barFa.c)
      println("barFa.ts_end_res = "+barFa.ts_end_res)
      println("barFa.c_res = "+barFa.c_res)

      println("seqTicks TS begin= "+seqTicks.minBy(e => e.db_tsunx).db_tsunx)
      println("seqTicks TS end= "+seqTicks.maxBy(e => e.db_tsunx).db_tsunx)
      println("seqTicks CNT= "+seqTicks.size)

      println("SpS = " + simpleRound3Double(Sp/(Sp+Sm)))
      println("SmS = " + simpleRound3Double(Sm/(Sp+Sm)))
      println("====================================================================")
      */
      (simpleRound3Double(Sp/(Sp+Sm)),simpleRound3Double(Sm/(Sp+Sm)))
    }


    def analyzePVx(pAnalyze :Double,pv2 :Double,pv3 :Double) :Int ={
      if (pAnalyze > Seq(pv2,pv3).max) 1
      else if (pAnalyze < Seq(pv2,pv3).min) 3
      else 2
    }

    /**
     * ticks volume profile.
     * All interval of ticks diveded on 3 parts, from left (ts_begin) to right (ts_end)
     * equidistant.
     * And we have 3 subproperties: pv1, pv2 , pv3
     * each value take one value from (1,2,3) 1 for maximum, 2 for middle value, and 3 for minimum.
     * For example: Volume profile 120,90,100
     * then p1 = 1(max) pv2 = 3(min) pv3 = 2(mdl)  (1,3,2)
     */
    val pvX : (Int,Int,Int) = {
      val totalTickVolume :Double = if (seqTicks.hasDefiniteSize) seqTicks.size else 0.0
      if (totalTickVolume==0) (0,0,0)
      else {
        //          println("totalTickVolume="+totalTickVolume)
        val tsBegin :Long = seqTicks.minBy(_.db_tsunx).db_tsunx
        val tsEnd   :Long = seqTicks.maxBy(_.db_tsunx).db_tsunx
        val tsStepWidth = (tsEnd - tsBegin)/3
        //          println("tsBegin="+tsBegin+" tsEnd="+tsEnd+" tsStepWidth="+tsStepWidth)
        /*
                  println("count1="+seqTicks.count(tc => tc.db_tsunx >= tsBegin && tc.db_tsunx <= (tsBegin+tsStepWidth)))
                  println("count2="+seqTicks.count(tc => tc.db_tsunx >= (tsBegin+tsStepWidth) && tc.db_tsunx <= (tsBegin+2*tsStepWidth)))
                  println("count3="+seqTicks.count(tc => tc.db_tsunx >= (tsBegin+2*tsStepWidth) && tc.db_tsunx <= tsEnd))
        */
        val pv1 = simpleRound3Double(seqTicks.count(tc => tc.db_tsunx >= tsBegin && tc.db_tsunx <= (tsBegin+tsStepWidth))/totalTickVolume)
        val pv2 = simpleRound3Double(seqTicks.count(tc => tc.db_tsunx >= (tsBegin+tsStepWidth) && tc.db_tsunx <= (tsBegin+2*tsStepWidth))/totalTickVolume)
        val pv3 = simpleRound3Double(seqTicks.count(tc => tc.db_tsunx >= (tsBegin+2*tsStepWidth) && tc.db_tsunx <= tsEnd)/totalTickVolume)
        //         println("barFa.tickerId="+barFa.tickerId+" ts_end="+barFa.ts_end+ " pv1="+pv1+" pv2="+pv2+" pv3="+pv3+"")
        //          println("===================================")
        (
          analyzePVx(pv1,pv2,pv3),
          analyzePVx(pv2,pv1,pv3),
          analyzePVx(pv3,pv1,pv2)
        )
      }
    }


    /*
    Property calculated from previous one.
    As a short form
    Just 5 possible values: 0,1,2,3,4 (0- unknown.)
    */
    val ticksValusFormation :Int = pvX match {
      case (0,0,0) => 0 //not defined
      case (1,2,3) => 1 // volume decreasing
      case (3,2,1) => 2 // volume increasing
      case (2,1,3) | (3,1,2) => 3 // Up peak in middle - maximum
      case (1,3,2) | (2,3,1) => 4 // Down peak in middle - minimum
      case _ => 0 //not defined
    }

    //println("barFa.tickerId="+barFa.tickerId+" ts_end="+barFa.ts_end+ " ticksValusFormation="+ticksValusFormation)


    def getACFKoeff(acLevel :Int) :Double ={
      val seqSrcX = seqTicks.drop(acLevel).map(_.ask)
      val seqSrcY = seqTicks.take(seqTicks.size - acLevel).map(_.ask)

      val n = seqSrcX.size

      val avgX = seqSrcX.sum / n
      val avgY = seqSrcY.sum / n

      val avgXY = seqSrcX.zip(seqSrcY).map(elm => elm._1 * elm._2).sum / n
      val prodAvgXY = avgX * avgY

      val sigma2X = seqSrcX.map(e => Math.pow(e, 2)).sum / n - Math.pow(avgX, 2)
      val sigma2Y = seqSrcY.map(e => Math.pow(e, 2)).sum / n - Math.pow(avgY, 2)

      val r = (avgXY - prodAvgXY) / Math.sqrt(sigma2X * sigma2Y)
      r
    }

    val acfLevel1 = simpleRound3Double(getACFKoeff(barFa.barWidthSec/2))
    val acfLevel2 = simpleRound3Double(getACFKoeff(barFa.barWidthSec))
    val acfLevel3 = simpleRound3Double(getACFKoeff(barFa.barWidthSec*2))

    /**
     * Here calculate any properties for FormProps.
     */
    new BForm(
      barFa.tickerId,
      barFa.barWidthSec,
      barFa.dDate,
      tsBegin,
      barFa.ts_end,
      barFa.log_oe,
      barFa.res_type,
      barFa.ts_end_res,
      barFa.dursec_res,
      barFa.ddate_res,
      barFa.c_res,
      formDeepKoef,
      Map(
        "tickscnt"     -> ticksCnt.toString,
        "frmconfpeak"  -> frmConfPeak.toString,
        "sps"          -> SdivS._1.toString,
        "sms"          -> SdivS._2.toString,
        "tcvolprofile" -> ticksValusFormation.toString,
        "acf_05_bws"  -> acfLevel1.toString,
        "acf_1_bws"   -> acfLevel2.toString,
        "acf_2_bws"   -> acfLevel3.toString
      )
    )
  }

}