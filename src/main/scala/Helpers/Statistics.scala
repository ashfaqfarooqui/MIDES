package Helpers

class Statistics {

  private var _membQueries: BigInt = 0

  def membQueries: BigInt = _membQueries

  def membQueries_=(value: BigInt): Unit = {
    _membQueries = value
  }

  private var _eqQueries: BigInt = 0

  def eqQueries: BigInt = _eqQueries

   def eqQueries_=(value: BigInt): Unit = {
    _eqQueries = value
  }

  private var _membcache: BigInt = 0

   def membcache: BigInt = _membcache

   def membcache_=(value: BigInt): Unit = {
    _membcache = value
  }

}
