package haishu.crawler2.selector

/**
 * Created by hldev on 7/21/17.
 */
trait Selectable {

  //def xpath(xpath: String): Selectable

  def css(selector: String): Selectable

  def css(selector: String, attrName: String): Selectable

  //def smartContent(): Selectable

  def links(): Selectable

  def regex(regex: String): Selectable

  def regex(regex: String, group: Int): Selectable

  def replace(regex: String, replacement: String): Selectable

  def get(): Option[String]

  def isMatch: Boolean

  def all(): Seq[String]

  //def jsonPath(jsonPath: String): Selectable

  def select(selector: Selector): Selectable

  def selectSeq(selector: Selector): Selectable
}
