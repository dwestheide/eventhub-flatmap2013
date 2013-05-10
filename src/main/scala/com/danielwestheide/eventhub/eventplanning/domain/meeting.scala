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


  // events originating from the Meeting aggregate:
  sealed trait MeetingEvent extends DomainEvent

}