package com.eskimi.samplebid

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.eskimi.domain._
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.slf4j.LoggerFactory

import java.time.LocalDateTime
import scala.collection.mutable.ListBuffer
import scala.io.StdIn
import scala.util.Random

object EskimiBid extends JacksonSupport {

  def constantCampaigns(): Seq[Campaign] = {
    Seq[Campaign](
      Campaign(
        id = 1,
        country = "LT",
        targeting =
          Targeting(Seq("0006a522ce0f4bbbbaa6b3c38cafaa0f"), startHourOfDay = Some(8), endHourOfDay = Some(21)),
        banners = List(
          Banner(1, "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg", 300, 250),
          Banner(2, "https://business.eskimi.com/wp-content/uploads/2020/07/openGraph.jpeg", 300, 250),
        ),
        bid = 4.50,
      ),
      Campaign(
        id = 2,
        country = "LT",
        targeting = Targeting(Seq("0006a522ce0f4bbbbaa6b3c38cafaa0f"), endHourOfDay = Some(21)),
        banners = List(
          Banner(1, "https://home.eskimi.com/wp-content/uploads/2021/01/openGraph.jpeg", 250, 100),
          Banner(2, "https://home.eskimi.com/wp-content/uploads/2021/01/openGraph.jpeg", 275, 110),
        ),
        bid = 5.15,
      ),
      Campaign(id = 3, country = "NG", targeting = Targeting(Seq.empty[String]), banners = Nil, bid = 2.15),
    )
  }

  def validateBid(bid: BidRequest)(implicit campaigns: Seq[Campaign]): Option[BidResponse] = {
    val resolved_responses: ListBuffer[BidResponse] = ListBuffer.empty

    var within_bid_impr: Seq[Impression]            = Seq.empty
    val considered_bid_impr: ListBuffer[Impression] = ListBuffer.empty
    val hour_of_day                                 = LocalDateTime.now().getHour
    val random                                      = new Random()

    campaigns
      .withFilter(c =>
        bid.device.exists(_.geo.flatMap(_.country.map(_ == c.country)).getOrElse(true)) || bid.user.exists(
            _.geo.flatMap(_.country.map(_ == c.country)).getOrElse(true)
          )
      )
      .withFilter(_.targeting.targetedSiteIds.contains(bid.site.id))
      .withFilter(c =>
        c.targeting.startHourOfDay
          .map(hour_of_day >= _)
          .getOrElse(true) && c.targeting.endHourOfDay.map(hour_of_day <= _).getOrElse(true)
      )
      .withFilter(c =>
        bid.imp match {
          case None => false
          case Some(lstimpr) =>
            within_bid_impr = lstimpr.filter(_.bidFloor.exists(_ <= c.bid))
            within_bid_impr.nonEmpty
        }
      )
      .foreach { c =>
        val resolved_banners = c.banners.filter(bnf =>
          within_bid_impr.map { imp =>
            val _exists =
              (imp.w.exists(_ >= bnf.width) || (imp.wmax.exists(_ >= bnf.width) && imp.wmin.exists(_ <= bnf.width))) &&
                (imp.h
                  .exists(_ >= bnf.height) || (imp.hmax.exists(_ >= bnf.height) && imp.hmin.exists(_ <= bnf.height)))
            if (_exists) considered_bid_impr += imp
            _exists
          }.nonEmpty
        )

        if (considered_bid_impr.nonEmpty) {
          var _rand  = random.nextInt(resolved_banners.size)
          val banner = resolved_banners(_rand)

          _rand = random.nextInt(considered_bid_impr.size)
          val impr = considered_bid_impr(_rand)
          resolved_responses += BidResponse(
            1.toString,
            bid.id,
            impr.bidFloor.getOrElse(0.0d),
            Some(s"${c.id}"),
            Some(banner),
          )
        }

        //clear temp
        considered_bid_impr.clear
        within_bid_impr = Seq.empty
      }

    if (resolved_responses.nonEmpty) {
      resolved_responses.zipWithIndex.foreach { case (bid, c) => println(s"$c: $bid") }
      val _rand = random.nextInt(resolved_responses.length)
      Some(resolved_responses(_rand))
    } else {
      None
    }
  }

  implicit val system           = ActorSystem(Behaviors.empty, "EskimiBid")
  implicit val executionContext = system.executionContext
  implicit val campaigns        = DataGenerator.samplecampaigns(5, None, 3.0, 5.5, Some(6), None)

  val route: Route = (path("api" / "bid") & post) {
    entity(as[BidRequest]) { bid =>
      println(s">>>>>>>>>>>>>>>> ----------------------------------------------------<<<<<<<<<<<<<<")
      val response: Option[BidResponse] = validateBid(bid)
      println(s">>>>>>>>> response: $response")
      response match {
        case Some(b) => complete(b)
        case None    => complete(StatusCodes.NoContent)
      }
    }
  }

  implicit def myRejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleNotFound {
        extractUnmatchedPath { p =>
          complete(NotFound, s"The path you requested [${p}] does not exist.")
        }
      }
      .handle {
        case MissingQueryParamRejection(param) =>
          complete(BadRequest, s"Missing query Param error. $param")
        case a @ _ =>
          println(s"some errors occurred here : $a")
          complete(BadRequest, s"Other error. $a")
      }
      .result()

  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    campaigns.zipWithIndex.foreach { case (cam, c) => println(s"${c + 1}: $cam") }
    val httpserver = Http().newServerAt("localhost", 8088).bind(route)

    println(s"Server now online. Please send request to http://localhost:8088/api/bid\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    httpserver
      .flatMap(_.unbind())                 // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}
