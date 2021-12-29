package com.eskimi.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}

@JsonIgnoreProperties(ignoreUnknown = true) case class BidRequest(
    id: String,
    imp: Option[List[Impression]],
    site: Site,
    user: Option[User],
    device: Option[Device],
)
@JsonIgnoreProperties(ignoreUnknown = true) case class Impression(
    id: String,
    wmin: Option[Int],
    wmax: Option[Int],
    w: Option[Int],
    hmin: Option[Int],
    hmax: Option[Int],
    h: Option[Int],
    bidFloor: Option[Double],
)
@JsonIgnoreProperties(ignoreUnknown = true) case class Site(id: String, domain: String)
@JsonIgnoreProperties(ignoreUnknown = true) case class User(id: String, geo: Option[Geo])
@JsonIgnoreProperties(ignoreUnknown = true) case class Device(id: String, geo: Option[Geo])
@JsonIgnoreProperties(ignoreUnknown = true) case class Geo(country: Option[String])

case class BidResponse(id: String, bidRequestId: String, price: Double, adid: Option[String], banner: Option[Banner])
object BidRequest {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.setSerializationInclusion(Include.NON_EMPTY);
  def toJson(value: Any) = mapper.writeValueAsString(value)
}
