package com.danielwestheide.eventhub.eventplanning.application

object meeting {
	import org.joda.time.DateTime
  import com.danielwestheide.eventhub._
  import eventplanning.domain.meeting._
  import scalaz.Validation
  import scalaz.syntax.std.option._
  import scalaz.syntax.validation._
  import akka.actor.{Actor, ActorRef}
  import org.eligosource.eventsourced.core.Emitter

  type Result = Validation[String, Meeting]
  type Aggregates = Map[MeetingId, Meeting]

  class MeetingRepository {
    import concurrent.stm.Ref

    private val meetingsRef: Ref[Aggregates] = Ref(Map.empty[MeetingId, Meeting])

    def fromId(identity: MeetingId): Result =
      readMeetings.get(identity).toSuccess(s"meeting $identity cannot be found")

    def contains(identity: MeetingId): Boolean = readMeetings.contains(identity)

    def getAll = readMeetings.values

    private[meeting] def saveOrUpdate(meeting: Meeting): Result = {
      meetingsRef.single.transform(_ + (meeting.id -> meeting))
      meeting.success
    }

    private def readMeetings = meetingsRef.single.get
  }

  class MeetingService(processor: ActorRef) {
    import scala.concurrent.duration._
    import akka.util.Timeout
    import akka.pattern.ask
    import concurrent.Future
    import org.eligosource.eventsourced.core.Message

    implicit val timeout = Timeout(5.seconds)

    def process(command: MeetingCommand): Future[Result] =
      (processor ? Message(command)).mapTo[Result]
  }

  class MeetingProcessor(meetingRepository: MeetingRepository) extends Actor { this: Emitter =>

    type CommandHandler = MeetingCommand => Validation[String, (MeetingEvent, Meeting)]

    override def receive = {
      case command: MeetingCommand => handleCommand(command)
    }

    val handleCommand = process {
      case CreateMeeting(meetingId, name, time, venue, issuedAt) =>
        createMeeting(meetingId, name, time, venue, issuedAt, sequenceNr)
      case AddTalk(meetingId, talk, issuedAt) =>
        update(meetingId, _.addTalk(talk).success) { _ =>
          TalkAdded(meetingId, talk, issuedAt, sequenceNr)
        }
      case ChangeVenue(meetingId, venue, issuedAt) =>
      update(meetingId, _.moveToVenue(venue)) { _ =>
        VenueChanged(meetingId, venue, issuedAt, sequenceNr)
      }
      case DeclareAttendance(meetingId, attendeeId, confidence, issuedAt) =>
      update(meetingId, _.declareAttendance(attendeeId, confidence)) { meeting =>
        AttendanceDeclared(meetingId, meeting.name, meeting.time,
          attendeeId, confidence, issuedAt, sequenceNr)
      }
      case CancelAttendance(meetingId, attendeeId, issuedAt) =>
      update(meetingId, _.cancelAttendance(attendeeId)) { meeting =>
        AttendanceCancelled(meetingId, attendeeId, issuedAt, sequenceNr)
      }
    } _

    def update(meetingId: MeetingId, f: Meeting => Result)(g: Meeting => MeetingEvent) =
      for {
        meeting <- meetingRepository.fromId(meetingId)
        updated <- f(meeting)
      } yield (g(updated), updated)

    def process(commandHandler: CommandHandler)(command: MeetingCommand): Unit = {
      val result = commandHandler(command)
      result.foreach { case (event, meeting) =>
        meetingRepository.saveOrUpdate(meeting)
        emitter("meeting-listeners").sendEvent(event)
      }
      sender ! result.map(_._2)
    }

    def createMeeting(
        meetingId: MeetingId,
        name: String,
        time: DateTime,
        venue: Venue,
        issuedAt: DateTime,
        snr: Long): Validation[String, (MeetingCreated, Meeting)] =
      if (meetingRepository.contains(meetingId)) s"Meeting with id ${meetingId.id} already exists".fail
      else (
        MeetingCreated(meetingId, name, time, venue, issuedAt, snr),
        Meeting(meetingId, name, time, venue, Vector.empty, Vector.empty)).success
  }

}
