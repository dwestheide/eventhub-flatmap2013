package com.danielwestheide.eventhub.eventplanning.domain

object meeting {

  import attendee.AttendeeId
  import org.joda.time.DateTime
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
      attendances: Vector[Attendance])

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