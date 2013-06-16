package com.danielwestheide.eventhub.iam.application

object user {

  import scalaz.Validation
  import scalaz.syntax.std.option._
  import scalaz.syntax.validation._
  import com.danielwestheide.eventhub.iam.domain.user.User
  import akka.actor.{Actor, ActorRef}
  import org.eligosource.eventsourced.core.Message

  type Result = Validation[String, User]
  type Aggregates = Map[String, User]

  class UserRepository {

    import concurrent.stm.Ref

    private val usersRef: Ref[Aggregates] = Ref(Map.empty[String, User])

    def fromUniqueName(uniqueName: String): Result =
      readUsers.get(uniqueName).toSuccess(s"user $uniqueName cannot be found")

    def contains(uniqueName: String): Boolean =
      readUsers.contains(uniqueName)

    def getAll: Iterable[User] = readUsers.values

    private[user] def saveOrUpdate(user: User): Result = {
      usersRef.single.transform(_ + (user.userInfo.uniqueName -> user))
      user.success
    }

    private def readUsers = usersRef.single.get
  }

  class UserService(processor: ActorRef) {

    import com.danielwestheide.eventhub.iam.domain.user.UserCommand
    import concurrent.Future
    import akka.pattern.ask
    import akka.util.Timeout
    import scala.concurrent.duration._

    implicit val timeout = Timeout(5.seconds)
    def process(command: UserCommand): Future[Result] =
      (processor ? Message(command)).mapTo[Result]
  }

  class UserProcessor(userRepository: UserRepository) extends Actor {
    import com.danielwestheide.eventhub.iam.domain.user.RegisterUser
    import com.danielwestheide.eventhub.iam.domain.user.UserRegistered
    override def receive = {
      case RegisterUser(uniqueName, firstName, lastName, password, issuedAt) =>
        if (userRepository.contains(uniqueName))
          sender ! s"User name $uniqueName is already taken".fail
        else {
          import com.danielwestheide.eventhub.iam.domain.user.UserInformation
          val result = userRepository.saveOrUpdate(
            User(UserInformation(uniqueName, firstName, lastName), password))
          println("repo: " + userRepository.fromUniqueName(uniqueName))
          sender ! result
        }
    }
  }

}
