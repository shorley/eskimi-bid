package com.eskimi.samplebid

case class Targeting(targetedSiteIds: Seq[String], startHourOfDay: Option[Int] = None, endHourOfDay: Option[Int] = None)
