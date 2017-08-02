package haishu.crawler2.selector

/**
 * Created by hldev on 7/21/17.
 */
trait Selector {
  def select(text: String): String

  def selectSeq(text: String): Seq[String]
}
