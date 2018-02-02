package entity

import play.api.libs.json._

import scala.util.{Success, Try}

/**
  * @author Mykola Yashchenko
  */
case class Account(id: String, name: String, `type`: String, user_id: String)

object Account {

  implicit val reader = Json.reads[Account]
  implicit val writer = Json.writes[Account]

  implicit object AccountWrites extends OWrites[Account] {
    override def writes(account: Account): JsObject =
      Json.obj(
        "id" -> account.id,
        "name" -> account.name,
        "type" -> account.`type`,
        "user_id" -> account.user_id
      )
  }

  implicit object AccountReads extends Reads[Account] {
    override def reads(json: JsValue): JsResult[Account] =
      json match {
        case accJson: JsObject =>
          Try {
            val id = (accJson \ "_id" \ "$oid").as[String]
            val name = (accJson \ "name").as[String]
            val `type` = (accJson \ "type").as[String]
            val user_id = (accJson \ "user_id").as[String]

            JsSuccess(
              Account(id, name, `type`, user_id)
            )
          } match {
            case Success(value) => value
          }
      }

  }
}
