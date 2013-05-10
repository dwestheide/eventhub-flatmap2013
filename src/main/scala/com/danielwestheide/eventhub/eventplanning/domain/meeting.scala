package com.danielwestheide.eventhub.eventplanning.domain

object meeting {

  import attendee.AttendeeId
  import org.joda.time.DateTime
  import scalaz.Validation
  import scalaz.syntax.validation._
  import com.danielwestheide.eventhub.common.{DomainEvent, Command}

  case class Venue(
      name: String,
      street: String,
      zipCode: String,
      city: String,
      country: String,
      capacity: Int)

  case class Talk(
      title: String,
      speaker: String)

  object Confidence extends Enumeration {
    type Confidence = Value
    val Definitely, Maybe = Value
  }
  import Confidence._
  case class Attendance(attendeeId: AttendeeId, confidence: Confidence)
  case class MeetingId(val id: String) extends AnyVal

  case class Meeting(
      id: MeetingId,
      name: String,
      time: DateTime,
      venue: Venue,
      talks: Vector[Talk],
      attendances: Vector[Attendance]) {

    def addTalk(talk: Talk): Meeting = copy(talks = this.talks :+ talk)

    def moveToVenue(aVenue: Venue): Validation[String, Meeting] =
      if (aVenue.capacity < attendances.size)
        s"Capacity must be at least ${attendances.size}, but was ${aVenue.capacity}".failure
      else copy(venue = aVenue).success

    def declareAttendance(
        attendeeId: AttendeeId,
        confidence: Confidence): Validation[String, Meeting] =
      for {
        _ <- assertAttendanceIsNew(attendeeId)
        _ <- assertCapacityIsSufficient
        attendance = Attendance(attendeeId, confidence)
      } yield copy(attendances = attendances :+ attendance)

    def cancelAttendance(attendeeId: AttendeeId): Validation[String, Meeting] = {
      val (toRemove, remaining) = attendances.partition(_.attendeeId == attendeeId)
      if (toRemove.isEmpty) s"${attendeeId} is not attending $name".failure
      else copy(attendances = remaining).success
    }

    private def assertAttendanceIsNew(attendeeId: AttendeeId) =
      if (attendances.exists(_.attendeeId == attendeeId))
        s"${attendeeId} is already attending $name".failure
      else this.success

    private def assertCapacityIsSufficient =
      if (attendances.size == venue.capacity)
        s"Capacity of ${venue.name} is exhausted with ${attendances.size} registered attendees".failure
      else this.success

  }

  // commands for the Meeting aggregate:
  sealed trait MeetingCommand extends Command
  
  case class CreateMeeting(
      meetingId: MeetingId,
      name: String,
      time: DateTime,
      venue: Venue,
      issuedAt: DateTime) extends MeetingCommand

  case class AddTalk(
      meetingId: MeetingId,
      talk: Talk,
      issuedAt: DateTime) extends MeetingCommand

  case class ChangeVenue(
      meetingId: MeetingId,
      venue: Venue,
      issuedAt: DateTime) extends MeetingCommand

  case class DeclareAttendance(
      meetingId: MeetingId,
      attendeeId: AttendeeId,
      confidence: Confidence,
      issuedAt: DateTime) extends MeetingCommand

  case class CancelAttendance(
      meetingId: MeetingId,
      attendeeId: AttendeeId,
      issuedAt: DateTime) extends MeetingCommand

  // events originating from the Meeting aggregate:
  sealed trait MeetingEvent extends DomainEvent

  case class MeetingCreated(
      meetingId: MeetingId,
      name: String,
      time: DateTime,
      venue: Venue,
      occurredAt: DateTime,
      snr: Long) extends MeetingEvent

  case class TalkAdded(
      meetingId: MeetingId,
      talk: Talk,
      occurredAt: DateTime,
      snr: Long) extends MeetingEvent
    
  case class VenueChanged(
      meetingId: MeetingId,
      venue: Venue,
      occurredAt: DateTime,
      snr: Long) extends MeetingEvent

  case class AttendanceDeclared(
      meetingId: MeetingId,
      meetingName: String,
      meetingTime: DateTime,
      attendeeId: AttendeeId,
      confidence: Confidence,
      occurredAt: DateTime,
      snr: Long) extends MeetingEvent
    
  case class AttendanceCancelled(
      meetingId: MeetingId,
      attendeeId: AttendeeId,
      occurredAt: DateTime,
      snr: Long) extends MeetingEvent

}