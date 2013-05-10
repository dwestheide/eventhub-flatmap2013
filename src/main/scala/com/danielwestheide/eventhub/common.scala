package com.danielwestheide.eventhub

object common {
  import org.joda.time.DateTime

  trait Command {
    def issuedAt: DateTime
  }

  trait DomainEvent {
    def occurredAt: DateTime
    def snr: Long
  }

}