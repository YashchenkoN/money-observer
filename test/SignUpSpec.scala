import controllers.SignUpController
import models.security.User
import play.api.test.{PlaySpecification, WithApplication}

/**
  * @author Mykola Yashchenko
  */
class SignUpSpec extends PlaySpecification {

  "The `signUp` method" should {
    "return status 200 if sign up is successful" in new WithApplication {
      val controller = app.injector.instanceOf[SignUpController]
      controller.signUp()

      status(result) must equalTo(OK)
    }
  }

}
