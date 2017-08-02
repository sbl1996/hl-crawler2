package haishu.crawler2.selector

class RegexResult(private val groups: Array[String]) {
  def get(groupId: Int): String = {
    if (groups == null) null
    else groups(groupId)
  }
}

object RegexResult {

  def apply(groups: Array[String]): RegexResult = new RegexResult(groups)

  def apply() = new RegexResult(null)

  def empty = new RegexResult(null)

}
