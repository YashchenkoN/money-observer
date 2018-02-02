package services

import models.account.CreateAccountRequest
import models.security.User
import play.api.libs.json.JsObject

import scala.concurrent.Future

/**
  * @author Mykola Yashchenko
  */
trait AccountService {
  def save(createAccountRequest: CreateAccountRequest, identity: User): Future[JsObject]

  def delete(id: String): Future[Unit]
}
