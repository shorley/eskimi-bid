package com.eskimi.samplebid

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object DataGenerator {
  val country: Seq[String] = Seq("NG", "IN", "RU", "US", "UK", "SA", "GB", "IT", "BZ", "KY", "LT")
  val sites_domains: Seq[(String, String)] = Seq(
    ("FZNd99HW7rcpNzb", "apple.com"),
    ("TXRIgMS4GivcwcC", "bbc.com"),
    ("WhL62qMWGRe7ZWI", "cnn.com"),
    ("MUVG4TqGGFThw4P", "eskimi.com"),
    ("VkyZev7PUyB0Y5q", "facebook.com"),
    ("XQDkODE92T2KPFt", "google.com"),
    ("PJ4YCW7bNCVB3cN", "paypal.com"),
    ("lDwsYTPvjPnntJx", "alibaba.com"),
    ("JMb73LblxbfrPQ2", "vodafone.com"),
    ("p653UNliISPWlyu", "netflix.com"),
  )
  val (banner_min_w: Int, banner_max_w: Int) = (350, 750)
  val (banner_min_h: Int, banner_max_h: Int) = (150, 400)
  val wh_ranges: Seq[Int]                    = Seq(0, 5, 10, 20, 25, 50, 100)

  val random = new Random()

  def randomPrice(min: Double, max: Double) =
    math.rint(Random.between(min, max) * 100) / 100.0

  private def randomImage() =
    s"https://business.eskimi.com/wp-content/uploads/${random.alphanumeric.take(15).mkString("")}.jpeg"

  private def randomIntWithSteps(min: Int, max: Int, step: Int) =
    min + (random.nextInt(((max - min) / step) + 1) * step)

  def targettingsiteids() = {
    val _size                          = random.nextInt(sites_domains.size)
    val targetids: ArrayBuffer[String] = ArrayBuffer()
    do {
      val siteid = sites_domains(random.nextInt(sites_domains.size))._1
      if (!targetids.contains(siteid)) targetids += siteid
    } while (_size > targetids.size)
    targetids.toSeq
  }

  def samplecampaigns(
      n: Int,
      fixedcountry: Option[String] = None,
      minprice: Double,
      maxprice: Double,
      startHour: Option[Int] = None,
      endHour: Option[Int] = None,
  ): Seq[Campaign] =
    Seq.tabulate(n)(idx => {
      val bidprice  = randomPrice(minprice, maxprice)
      val targeting = targettingsiteids
      val banners = 0
        .to(random.nextInt(10))
        .map(idx2 => {
          val w   = randomIntWithSteps(banner_min_w, banner_max_w, 25)
          val h   = randomIntWithSteps(banner_min_h, banner_max_h, 10)
          val img = randomImage
          Banner(idx2 + 1, img, w, h)
        })
        .toList
      val ctry = fixedcountry.getOrElse(country(random.nextInt(country.size)))
      Campaign(idx + 1, ctry, Targeting(targeting, startHour, endHour), banners, bidprice)
    })

  def randomOption[A](a: A): Option[A] =
    random.nextBoolean() match {
      case true => Some(a)
      case _    => None
    }

  def samplebid(nImpr: Int, fixedcountry: Option[String] = None, pricemin: Double, pricemax: Double): BidRequest = {
    val _site = sites_domains(random.nextInt(sites_domains.size))
    val udgeo: (Geo, Geo) = fixedcountry match {
      case None =>
        val user_geo   = Geo(randomOption(country(random.nextInt(country.size))))
        val device_geo = Geo(randomOption(country(random.nextInt(country.size))))
        (user_geo, device_geo)
      case a @ _ =>
        val user_geo   = Geo(a)
        val device_geo = Geo(a)
        (user_geo, device_geo)
    }

    val user                = User(s"U${randomString(4)}-${randomString(6)}", randomOption(udgeo._1))
    val device              = Device(s"D${randomString(6)}-${randomString(6)}", randomOption(udgeo._2))
    val user_device_options = Seq((None, Some(device)), (Some(user), None), (Some(user), Some(device)))
    val user_device         = user_device_options(random.nextInt(user_device_options.size))

    val impressions: List[Impression] = Seq
      .tabulate(nImpr)(idx => {
        val bid         = randomPrice(pricemin, pricemax)
        val w           = randomIntWithSteps(banner_min_w, banner_max_w, 25)
        val wstepfactor = wh_ranges(random.nextInt(wh_ranges.size))
        val min_max_w_opt = Seq(
          (Some(w - wstepfactor), Some(w + wstepfactor), None),
          (None, None, Some(w)),
          (Some(w - wstepfactor), Some(w + wstepfactor), Some(w)),
        )
        val min_max_w   = min_max_w_opt(random.nextInt(min_max_w_opt.size))
        val h           = randomIntWithSteps(banner_min_h, banner_max_h, 10)
        val hstepfactor = wh_ranges(random.nextInt(wh_ranges.size))
        val min_max_h_opt = Seq(
          (Some(h - hstepfactor), Some(h + hstepfactor), None),
          (None, None, Some(h)),
          (Some(h - hstepfactor), Some(h + hstepfactor), Some(h)),
        )
        val min_max_h = min_max_h_opt(random.nextInt(min_max_h_opt.size))
        Impression(
          s"I${randomString(6)}",
          min_max_w._1,
          min_max_w._2,
          min_max_w._3,
          min_max_h._1,
          min_max_h._2,
          min_max_h._3,
          Some(bid),
        )
      })
      .toList

    BidRequest(
      s"${randomString(4)}-${randomString(4)}-${randomString(4)}",
      Some(impressions),
      Site(_site._1, _site._2),
      user_device._1,
      user_device._2,
    )
  }

  def randomString(n: Int) =
    new String((0 to n).map(_ => ('a' + random.nextInt(26)).toChar).toArray)

}
