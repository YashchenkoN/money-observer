package models.security

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.{Json, _}

case class User(id: Option[String], loginInfo: LoginInfo, username: String, email: String,
                firstName: String, lastName: String, avatarURL: Option[String], activated: Boolean) extends Identity

object User {

  implicit lazy val reader: Reads[User] = Json.reads[User]
  implicit lazy val writer: OWrites[User] = Json.writes[User]

  implicit lazy val loginInfoReader: Reads[LoginInfo] = Json.reads[LoginInfo]
  implicit lazy val loginInfoWriter: OWrites[LoginInfo] = Json.writes[LoginInfo]

}