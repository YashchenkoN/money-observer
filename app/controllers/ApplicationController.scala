package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future

@Singleton
class ApplicationController @Inject()(components: ControllerComponents,
                                      silhouette: Silhouette[DefaultEnv]) extends AbstractController(components) {

  def badPassword = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.obj("result" -> "qwerty1234")))
  }

  def colors = Action.async {
    Future.successful(Ok(Json.arr("black", "blue", "green", "red", "white")))
  }
}