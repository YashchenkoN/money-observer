package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.transaction.CreateTransactionRequest
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, Json, OFormat}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.TransactionService
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Mykola Yashchenko
  */
class TransactionController @Inject()(components: ControllerComponents,
                                      transactionService: TransactionService,
                                      silhouette: Silhouette[DefaultEnv],
                                      credentialsProvider: CredentialsProvider)
                                     (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val transactionFormat: OFormat[CreateTransactionRequest] = Json.format[CreateTransactionRequest]

  def create = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[CreateTransactionRequest].map { createTransactionRequest =>
      transactionService.save(createTransactionRequest, request.identity)
        .flatMap(idRef =>
          Future.successful(Ok(idRef))
        )
    }.recoverTotal(error =>
      Future.successful(BadRequest(Json.toJson(Bad(message = JsError.toJson(error))))))
  }

  def read(id: String) = silhouette.SecuredAction.async { implicit request =>
    transactionService.read(id, request.identity).flatMap(res =>
      Future.successful(Ok(res))
    )
  }

  def delete(id: String) = silhouette.SecuredAction.async { implicit request =>
    transactionService.delete(id, request.identity).flatMap(_ =>
      Future.successful(Ok)
    )
  }

  def read = silhouette.SecuredAction.async { implicit request =>
    transactionService.read(request.identity).flatMap(res =>
      Future.successful(Ok(res))
    )
  }
}
