package com.danielwestheide.eventhub.eventplanning.domain

object attendee {
  case class AttendeeId(id: String) extends AnyVal
  case class Attendee(identity: AttendeeId, name: String)
  object Attendee {
    def apply(identity: AttendeeId, firstName: String, lastName: String): Attendee =
      Attendee(identity, s"$firstName $lastName")
  }
}
