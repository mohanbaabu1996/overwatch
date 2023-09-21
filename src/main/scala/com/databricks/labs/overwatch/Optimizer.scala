package com.databricks.labs.overwatch

import com.databricks.labs.overwatch.env.Workspace
import com.databricks.labs.overwatch.pipeline._
import com.databricks.labs.overwatch.utils.{BadConfigException, JsonUtils, OverwatchParams, SparkSessionWrapper}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

object Optimizer extends SparkSessionWrapper{

  private val logger: Logger = Logger.getLogger(this.getClass)

  import spark.implicits._

  /**
   * Identify the latest successful runs by module
   * @param overwatchETLDB name of the Overwtch ETL database
   * @return
   */
  private[overwatch] def getLatestSuccessState(overwatchETLDB: String,orgId: String): DataFrame = {
//    val orgId = Initializer.getOrgId
    val lastSuccessByModuleW = Window.partitionBy('moduleID).orderBy('Pipeline_SnapTS.desc)
    spark.table(s"${overwatchETLDB}.pipeline_report")
      .filter('organization_id === orgId)
      .filter('status === "SUCCESS" || 'status.like("EMPTY:%"))
      .withColumn("rnk", rank.over(lastSuccessByModuleW))
      .withColumn("rn", row_number.over(lastSuccessByModuleW))
      .filter('rnk === 1 && 'rn === 1).drop("rnk", "rn")
      .orderBy('Pipeline_SnapTS.desc)
  }

  /**
   * Derive the workspace from the config supplied in the last run
   * @param overwatchETLDB name of the Overwtch ETL database
   * @return workspace object
   */
  private def getLatestWorkspace(overwatchETLDB: String,orgId: String): Workspace = {
    val params = getLatestSuccessState(overwatchETLDB,orgId)
      .selectExpr("inputConfig.*")
      .as[OverwatchParams]
      .first

    val args = JsonUtils.objToJson(params).compactString
    val prettyArgs = JsonUtils.objToJson(params).prettyString
    logger.log(Level.INFO, s"ARGS: Identified config string is: \n$prettyArgs")
    Initializer(args, debugFlag = true)
  }

  /**
   * pass in the overwatch ETL database name to optimize overwatch in parallel
   * @param args(0)   Overwatch ETL Database.
   * @param args(1)   Organization_ID for which optmization need to be performed.(Optional). By default optimizer will run for all
   *                  the organization ID in pipeline_report.
   */
  def main(args: Array[String]): Unit = {


    val (overwatchETLDB, orgIDs) = if (args.length == 1) {
      val orgIdList = spark.table(s"${args(0)}.pipeline_report").select("organization_id").distinct().collect().map(x => x(0).toString)
      (args(0), orgIdList)
    } else if (args.length == 2) {
      val cntOrgID = spark.table(s"${args(0)}.pipeline_report").select("organization_id").filter('organization_id === args(1)).distinct.count()
      if (cntOrgID > 0) {
        (args(0), Array(args(1)))
      } else {
        throw new BadConfigException("Input Organization_ID is not part of the Overwatch Deployment for which you want to run the optimizer")
      }
    } else {
      throw new BadConfigException(s"Main class requires at least 1 but less than 3 arguments. Received ${args.length} " +
        s"arguments. Please review the docs to compose the input arguments appropriately.")
    }

    orgIDs.foreach { orgId =>
      val workspace = getLatestWorkspace(overwatchETLDB,orgId)
      val config = workspace.getConfig
      if (config.debugFlag) println(JsonUtils.objToJson(config.inputConfig).compactString)
      val bronze = Bronze(workspace, suppressReport = true, suppressStaticDatasets = true)
      val silver = Silver(workspace, suppressReport = true, suppressStaticDatasets = true)
      val gold = Gold(workspace, suppressReport = true, suppressStaticDatasets = true)

      val optimizationCandidates = bronze.getAllTargets ++ silver.getAllTargets ++ gold.getAllTargets :+ bronze.pipelineStateTarget
      val postProcessor = new PostProcessor(config)
      postProcessor.optimizeOverwatch(spark, optimizationCandidates,orgId)
    }

  }

}
