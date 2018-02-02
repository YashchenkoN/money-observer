import java.util.UUID

import com.mohiva.play.silhouette.api.util.Credentials
import controllers.{SignInController, SignUpController}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

/**
  * @author Mykola Yashchenko
  */
@RunWith(classOf[JUnitRunner])
class SignInSpec extends PlaySpecification {

  "signIp method" should {

    "login user" in new WithApplication() {

      val email = UUID.randomUUID().toString
      val password = "123"

      val signUpJson: JsObject = Json.obj(
        "email" -> email,
        "password" -> password,
        "firstName" -> "First",
        "lastName" -> "Last"
      )

      val signUpRequest: FakeRequest[JsObject] = FakeRequest()
        .withBody(signUpJson)
        .withHeaders("Content-Type" -> "application/json")

      val signUpController: SignUpController = app.injector.instanceOf(classOf[SignUpController])
      val result = signUpController.signUp().apply(signUpRequest)

      status(result) must equalTo(OK)

      val signInJson: JsObject = Json.obj(
        "email" -> email,
        "password" -> password
      )

      val signInRequest: FakeRequest[Credentials] = FakeRequest()
        .withMethod("POST")
        .withBody(Credentials(email, password))
        .withHeaders("Content-Type" -> "application/json")

      val signInController: SignInController = app.injector.instanceOf(classOf[SignInController])
      val signInResult = signInController.signIn().apply(signInRequest)

      status(signInResult) must equalTo(OK)

      val responseJson: JsValue = contentAsJson(signInResult)
      val token: String = responseJson.\("token").get.as[String]

      token must not beEmpty

      header("X-Auth-Token", signInResult).get must not beEmpty
    }

    "return error if user doesn't exist" in new WithApplication() {

      val signInRequest: FakeRequest[Credentials] = FakeRequest()
        .withMethod("POST")
        .withBody(Credentials(UUID.randomUUID().toString, UUID.randomUUID().toString))
        .withHeaders("Content-Type" -> "application/json")

      val signInController: SignInController = app.injector.instanceOf(classOf[SignInController])
      val signInResult = signInController.signIn().apply(signInRequest)

      status(signInResult) must equalTo(FORBIDDEN)
    }
  }
}
