package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import services.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import formatters.{CredentialFormat, Token}
import models.security.{SignUp, User}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(components: ControllerComponents,
                                 userService: UserService,
                                 configuration: Configuration,
                                 silhouette: Silhouette[DefaultEnv],
                                 clock: Clock,
                                 credentialsProvider: CredentialsProvider,
                                 authInfoRepository: AuthInfoRepository,
                                 passwordHasherRegistry: PasswordHasherRegistry,
                                 avatarService: AvatarService,
                                 messagesApi: MessagesApi)
                                (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val credentialFormat = CredentialFormat.restFormat

  implicit val signUpFormat = Json.format[SignUp]

  def signUp = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUp].map { signUp =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.identifier)
      userService.retrieve(loginInfo).flatMap {
        case None => /* user not already exists */
          val user = User(None, loginInfo, loginInfo.providerKey, signUp.email, signUp.firstName, signUp.lastName, None, true)
          val authInfo = passwordHasherRegistry.current.hash(signUp.password)
          for {
            avatar <- avatarService.retrieveURL(signUp.email)
            userToSave <- userService.save(user.copy(avatarURL = avatar))
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
    }.recoverTotal(error =>
      Future.successful(BadRequest(Json.toJson(Bad(message = JsError.toJson(error))))))
  }
}