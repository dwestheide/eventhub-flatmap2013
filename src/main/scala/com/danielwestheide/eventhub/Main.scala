package com.danielwestheide.eventhub

object Main extends App {
  import akka.actor.{Props, ActorRef, ActorSystem}
  import akka.util.Timeout
  import concurrent.duration._
  import iam.application.user.{UserService, UserProcessor, UserRepository}
  import iam.web.IdentityApiService
  import akka.io.IO
  import spray.can.Http
  import org.eligosource.eventsourced.journal.mongodb.reactive.MongodbReactiveJournalProps
  import org.eligosource.eventsourced.core._

  implicit val system = ActorSystem("eventhub-server")
  implicit val timeout = Timeout(5.seconds)

  val journal: ActorRef = Journal(MongodbReactiveJournalProps(List("localhost:27017")))
  val extension = EventsourcingExtension(system, journal)

  val userRepository = new UserRepository
  val userProcessor = extension.processorOf(ProcessorProps(
    1, pid => new UserProcessor(userRepository) with Eventsourced {
      val id = pid
    }))
  val userService = new UserService(userProcessor)

  extension.recover(20.seconds)


  val identityApiService = system.actorOf(Props(
    new IdentityApiService(userService, userRepository)), "iam-service")
  IO(Http) ! Http.Bind(identityApiService, "localhost", port = 8080)

}
