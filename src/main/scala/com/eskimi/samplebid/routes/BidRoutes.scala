package com.eskimi.samplebid.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.eskimi.domain.{BidRequest, BidResponse}
import com.eskimi.samplebid.DataGenerator
import com.eskimi.samplebid.EskimiBid.validateBid
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.slf4j.LoggerFactory

class BidRoutes extends JacksonSupport {

  private implicit val campaigns = DataGenerator.samplecampaigns(5, None, 3.0, 5.5, Some(6), None)
  private val logger             = LoggerFactory.getLogger(this.getClass)

  val route: Route = (path("api" / "bid") & post) {
    entity(as[BidRequest]) { bid =>
      logger.info(s">>>>>>>>>>>>>>>> ----------------------------------------------------<<<<<<<<<<<<<<")
      val response: Option[BidResponse] = validateBid(bid)
      logger.info(s">>>>>>>>> response: $response")
      response match {
        case Some(b) => complete(b)
        case None    => complete(StatusCodes.NoContent)
      }
    }
  }

}
