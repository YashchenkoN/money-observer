package controllers

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import javax.inject.Inject
import services.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.{CredentialFormat, Token}
import models.security.{SignUp, User}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, JsValue, Json, OFormat}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(components: ControllerComponents,
                                 userService: UserService,
                                 silhouette: Silhouette[DefaultEnv],
                                 authInfoRepository: AuthInfoRepository,
                                 passwordHasherRegistry: PasswordHasherRegistry)
                                (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val credentialFormat: OFormat[Credentials] = CredentialFormat.restFormat
  implicit val signUpFormat: OFormat[SignUp] = Json.format[SignUp]

  def signUp: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUp]
      .map { signUp =>
        val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.email)

        userService.retrieve(loginInfo)
          .flatMap {
            case None => /* user not already exists */
              val user = User(None, loginInfo, loginInfo.providerKey, signUp.email, signUp.firstName, signUp.lastName, None, activated = true)
              val authInfo = passwordHasherRegistry.current.hash(signUp.password)
              for {
                userToSave <- userService.save(user)
                authInfo <- authInfoRepository.add(loginInfo, authInfo)
                authenticator <- silhouette.env.authenticatorService.create(loginInfo)
                token <- silhouette.env.authenticatorService.init(authenticator)
                result <- silhouette.env.authenticatorService.embed(token,
                  Ok(Json.toJson(Token(token = token, expiresOn = authenticator.expirationDateTime)))
                )
              } yield {
                // todo add email confirmation
                silhouette.env.eventBus.publish(SignUpEvent(user, request))
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                result
              }
            case Some(_) => /* user already exists! */
              Future(Conflict(Json.toJson(Bad(message = "user already exists"))))
          }
      }
      .recoverTotal(error =>
        Future.successful(BadRequest(Json.toJson(Bad(message = JsError.toJson(error))))))
  }
}