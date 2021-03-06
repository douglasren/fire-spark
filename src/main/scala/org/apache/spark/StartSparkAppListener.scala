package org.apache.spark


import org.apache.spark.util.Utils
import org.apache.spark.internal.Logging
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd, SparkListenerApplicationStart}

import scala.util.control.NonFatal

/**
  * Created by cloud on 18/1/19.
  */
class StartSparkAppListener(val sparkConf: SparkConf) extends SparkListener with Logging{

  private val appName = sparkConf.get("spark.app.name","None")
  private val runConf = sparkConf.get("spark.run.main.conf","None")
  private val host = sparkConf.get("spark.application.monitor.host",Utils.localHostName)
  private val port = sparkConf.getInt("spark.application.monitor.port",23456)

  private def sendStartReq(): Unit = {
    try {
      val yarnAppMonitorRef = YarnAppMonitorCli.createYarnAppMonitorRef(sparkConf, host, port)
      yarnAppMonitorRef.send(YarnAppStartRequest(appName, runConf))
      logInfo(s"send start app request to YarnAppMonitorServer $appName $runConf")
    }catch {
      case NonFatal(e) => logError("send start request failed. ",e)
    }
  }

  override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = {
    val appId = applicationStart.appId
    logInfo(appId.toString)
  }

  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
    logInfo("app end time " + applicationEnd.time)
    if(runConf != "None" && appName != "None") sendStartReq()
  }

}
