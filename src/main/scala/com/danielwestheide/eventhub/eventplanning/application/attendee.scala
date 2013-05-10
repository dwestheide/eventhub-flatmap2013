package com.danielwestheide.eventhub.eventplanning.application

object attendee {

  import scalaz.Validation
  import scalaz.syntax.validation._
  import scalaz.syntax.std.option._
  import akka.actor.Actor
  import org.eligosource.eventsourced.core.Receiver
  import com.danielwestheide.eventhub._
  import eventplanning.domain.attendee._

  type Result = Validation[String, Attendee]
  type Aggregates = Map[AttendeeId, Attendee]

  class AttendeeRepository {

    import concurrent.stm.Ref

    private val attendeesRef: Ref[Aggregates] = Ref(Map.empty[AttendeeId, Attendee])
    def fromIdentity(identity: AttendeeId): Result =
      readAttendees.get(identity).toSuccess(s"attendee $identity cannot be found")
    def getAll: Iterable[Attendee] = readAttendees.values

    private[attendee] def saveOrUpdate(attendee: Attendee): Result = {
      attendeesRef.single.transform(_ + (attendee.identity -> attendee))
      attendee.success
    }
    private def readAttendees: Aggregates = attendeesRef.single.get
  }

  class AttendeeProcessor(attendeesRepository: AttendeeRepository) extends Actor {
    this: Receiver =>

    import com.danielwestheide.eventhub.iam.domain.user.UserRegistered

    override def receive = {
      case UserRegistered(uniqueName, firstName, lastName, occuredAt, snr) =>
        val attendee = Attendee(AttendeeId(uniqueName), firstName, lastName)
        attendeesRepository.saveOrUpdate(attendee)
    }
  }

}
