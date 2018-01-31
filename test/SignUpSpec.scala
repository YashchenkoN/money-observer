import java.util.UUID

import controllers.SignUpController
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsObject, Json}
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

/**
  * @author Mykola Yashchenko
  */
@RunWith(classOf[JUnitRunner])
class SignUpSpec extends PlaySpecification {

  "signUp method" should {

    "create new user" in new WithApplication() {

      val json: JsObject = Json.obj(
        "email" -> UUID.randomUUID().toString,
        "password" -> "123",
        "firstName" -> "First",
        "lastName" -> "Last"
      )

      val request: FakeRequest[JsObject] = FakeRequest()
        .withBody(json)
        .withHeaders("Content-Type" -> "application/json")

      val controller: SignUpController = app.injector.instanceOf(classOf[SignUpController])
      val result = controller.signUp().apply(request)

      status(result) must equalTo(OK)
    }
  }

  "return conflict if user already exists" in new WithApplication() {
    val json: JsObject = Json.obj(
      "email" -> UUID.randomUUID().toString,
      "password" -> "123",
      "firstName" -> "First",
      "lastName" -> "Last"
    )

    val request: FakeRequest[JsObject] = FakeRequest()
      .withBody(json)
      .withHeaders("Content-Type" -> "application/json")

    val controller: SignUpController = app.injector.instanceOf(classOf[SignUpController])
    val firstResult = controller.signUp().apply(request)

    status(firstResult) must equalTo(OK)

    val secondResult = controller.signUp().apply(request)

    status(secondResult) must equalTo(CONFLICT)
  }
}
