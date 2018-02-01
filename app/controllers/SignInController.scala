package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.{CredentialFormat, Token}
import models.security.SignUp
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.UserService
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Mykola Yashchenko
  */
class SignInController @Inject()(components: ControllerComponents,
                                 userService: UserService,
                                 silhouette: Silhouette[DefaultEnv],
                                 credentialsProvider: CredentialsProvider)
                                (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val credentialFormat: OFormat[Credentials] = CredentialFormat.restFormat
  implicit val signUpFormat: OFormat[SignUp] = Json.format[SignUp]

  def signIn = Action.async(parse.json[Credentials]) { implicit request =>
    val credentials = Credentials(request.body.identifier, request.body.password)

    credentialsProvider
      .authenticate(credentials)
      .flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) if !user.activated =>
            Future.failed(new IdentityNotFoundException("Couldn't find user"))
          case Some(user) =>
            silhouette.env.authenticatorService
              .create(loginInfo)
              .flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService
                  .init(authenticator)
                  .flatMap { token =>
                    silhouette.env.authenticatorService
                      .embed(
                        token,
                        Ok(
                          Json.toJson(
                            Token(
                              token,
                              expiresOn = authenticator.expirationDateTime
                            )
                          )
                        )
                      )
                  }
              }
          case None =>
            Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
      .recover {
        case _: ProviderException =>
          Forbidden
      }
  }
}