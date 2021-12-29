package com.eskimi.samplebid

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import com.eskimi.domain.BidRequest
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

object TestSender {
  implicit val system = ActorSystem()
  import system.dispatcher

  private val logger = LoggerFactory.getLogger(this.getClass)

  val constantsource =
    """{"id":"yeyot-porgu-zieos","imp":[{"id":"Ixutzssr","wmin":550,"wmax":600,"w":575,"h":210,"bidFloor":2.97},{"id":"Iqqrdhdr","wmin":700,"wmax":700,"w":700,"hmin":190,"hmax":210,"h":200,"bidFloor":3.0},{"id":"Ighthknu","w":475,"h":190,"bidFloor":2.66},{"id":"Inxzwmkr","wmin":500,"wmax":700,"w":600,"hmin":70,"hmax":270,"h":170,"bidFloor":3.24},{"id":"Izywyswa","wmin":370,"wmax":380,"w":375,"hmin":380,"hmax":420,"h":400,"bidFloor":2.81}],"site":{"id":"WhL62qMWGRe7ZWI","domain":"cnn.com"},"user":{"id":"Uusxby-kdlrtze"},"device":{"id":"Dkhmbfib-kasnird"}}
      |""".stripMargin

  val constantBidrequest = HttpRequest(
    method = HttpMethods.POST,
    uri = "http://localhost:8088/api/bid",
    entity = HttpEntity(ContentTypes.`application/json`, s"$constantsource"),
  )

  implicit val bidRequest = DataGenerator.samplebid(1, None, 2.5, 3.5)

  def request(implicit bidRequest: BidRequest) =
    HttpRequest(
      method = HttpMethods.POST,
      uri = "http://localhost:8088/api/bid",
      entity = HttpEntity(ContentTypes.`application/json`, s"${BidRequest.toJson(bidRequest)}"),
    ) //s"${BidRequest.toJson(bidRequest)}"

  def sendRequest() = {
    logger.info(s"Generated Bid: ${BidRequest.toJson(bidRequest)}")
    Http().singleRequest(request).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.toStrict(1.seconds).map(resp => resp.data.utf8String)
      case empty @ HttpResponse(StatusCodes.NoContent, _, _, _) =>
        empty.discardEntityBytes()
        Future("No Content received!")
      case all @ _ =>
        all.discardEntityBytes()
        Future(s"Something went wrong!!: status code: ${all.status}, message: ${all.toString()}")
    }
  }

  def main(args: Array[String]): Unit = {
    sendRequest().foreach(r => logger.info(s"Matched response: $r"))
  }

}
