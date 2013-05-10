package com.danielwestheide.eventhub.iam.domain

object user {

  import org.joda.time.DateTime
  import com.danielwestheide.eventhub.common.{DomainEvent, Command}

  // the user aggregate:
  case class User(
      userInfo: UserInformation,
      password: String)

  case class UserInformation(
      uniqueName: String,
      firstName: String,
      lastName: String
  )

  // commands for the user aggregate:
  sealed trait UserCommand extends Command
  case class RegisterUser(
      uniqueName: String,
      firstName: String,
      lastName: String,
      password: String,
      issuedAt: DateTime) extends UserCommand

  // domain events originating from the user aggregate:
  sealed trait UserEvent extends DomainEvent
  case class UserRegistered(
    uniqueName: String,
    firstName: String,
    lastName: String,
    occurredAt: DateTime,
    snr: Long
  ) extends UserEvent

}
