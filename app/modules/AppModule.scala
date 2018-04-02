package modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import services._

class AppModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[AccountService].to[AccountServiceImpl]
    bind[TransactionService].to[TransactionServiceImpl]
  }
}