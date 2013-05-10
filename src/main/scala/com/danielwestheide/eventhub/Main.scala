package com.danielwestheide.eventhub

object Main extends App {
  import akka.actor.{Props, ActorRef, ActorSystem}
  import akka.util.Timeout
  import concurrent.duration._
  import org.eligosource.eventsourced.core._
  import iam.application.user.{UserService, UserProcessor, UserRepository}
  import org.eligosource.eventsourced.journal.mongodb.reactive.MongodbReactiveJournalProps
  import org.eligosource.eventsourced.core.ProcessorProps
  import iam.web.IdentityApiService
  import akka.io.IO
  import spray.can.Http
  import eventplanning.application.attendee.{AttendeeProcessor, AttendeeRepository}
  import eventplanning.application.meeting._
  import eventplanning.web.EventPlanningApiService

  implicit val system = ActorSystem("eventhub-server")
  implicit val timeout = Timeout(5.seconds)

  val journal: ActorRef = Journal(MongodbReactiveJournalProps(List("localhost:27017")))
  val extension = EventsourcingExtension(system, journal)

  val userRepository = new UserRepository
  val userProcessor = extension.processorOf(ProcessorProps(
    1, pid => new UserProcessor(userRepository) with Emitter with Eventsourced {
      val id = pid
    }))
  val userService = new UserService(userProcessor)

  val attendeeRepository = new AttendeeRepository
  val attendeeProcessor = extension.processorOf(ProcessorProps(
    2, pid => new AttendeeProcessor(attendeeRepository) with Receiver with Confirm with Eventsourced {
      val id = pid
    }))

  val meetingRepository = new MeetingRepository
  val meetingProcessor = extension.processorOf(ProcessorProps(
    3, pid => new MeetingProcessor(meetingRepository) with Emitter with Eventsourced {
      val id = pid
    }))
  val meetingService = new MeetingService(meetingProcessor)

  extension.channelOf(DefaultChannelProps(2, attendeeProcessor).withName("identity"))

  extension.recover(20.seconds)

  val identityApiService = system.actorOf(Props(
    new IdentityApiService(userService, userRepository)), "identity-service")
  IO(Http) ! Http.Bind(identityApiService, "localhost", port = 8080)

  val eventPlanningApiService = system.actorOf(Props(
    new EventPlanningApiService(
      attendeeRepository,
      meetingService,
      meetingRepository)), "eventplanning-service")
  IO(Http) ! Http.Bind(eventPlanningApiService, "localhost", port = 8081)


}
