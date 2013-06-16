package com.danielwestheide.eventhub

object Main extends App {
  import akka.actor.{Props, ActorRef, ActorSystem}
  import akka.util.Timeout
  import concurrent.duration._
  import iam.application.user.{UserService, UserProcessor, UserRepository}
  import iam.web.IdentityApiService
  import akka.io.IO
  import spray.can.Http

  implicit val system = ActorSystem("eventhub-server")
  implicit val timeout = Timeout(5.seconds)

  val userRepository = new UserRepository
  val userProcessor = system.actorOf(Props(new UserProcessor(userRepository)))
  val userService = new UserService(userProcessor)

  val identityApiService = system.actorOf(Props(
    new IdentityApiService(userService, userRepository)), "iam-service")
  IO(Http) ! Http.Bind(identityApiService, "localhost", port = 8080)

}
