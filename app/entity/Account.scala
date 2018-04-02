package entity

import play.api.libs.json._

/**
  * @author Mykola Yashchenko
  */
case class Account(id: String, name: String, `type`: String, userId: String)

object Account {

  implicit lazy val reader: Reads[Account] = Json.reads[Account]
  implicit lazy val writer: OWrites[Account] = Json.writes[Account]

}
