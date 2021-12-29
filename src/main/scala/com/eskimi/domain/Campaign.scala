package com.eskimi.domain

case class Campaign(id: Int, country: String, targeting: Targeting, banners: List[Banner], bid: Double)
