import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test.{PlaySpecification, WithApplication}

/**
  * @author Mykola Yashchenko
  */
@RunWith(classOf[JUnitRunner])
class AccountSpec extends PlaySpecification {

  "create method" should {
    "save account to DB" in new WithApplication() {

    }
  }
}
