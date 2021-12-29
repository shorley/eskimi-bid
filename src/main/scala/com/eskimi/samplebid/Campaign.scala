package com.eskimi.samplebid

case class Campaign(id: Int, country: String, targeting: Targeting, banners: List[Banner], bid: Double)
