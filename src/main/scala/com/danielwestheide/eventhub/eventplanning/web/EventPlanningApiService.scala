package com.danielwestheide.eventhub.eventplanning.web

import spray.routing.HttpServiceActor
import spray.http.MediaTypes._
import spray.json._
import spray.httpx.SprayJsonSupport._
import DefaultJsonProtocol._
import EventPlanningJsonProtocol._
import com.danielwestheide.eventhub._
import web.SpraySupport._
import eventplanning.application.attendee.AttendeeRepository
import eventplanning.domain.attendee._

class EventPlanningApiService(
    attendeeRepository: AttendeeRepository) extends HttpServiceActor {
  implicit val ec = context.dispatcher
  def receive = runRoute(eventPlanningRoute)

  val eventPlanningRoute = {
    pathPrefix("api") {
      respondWithMediaType(`application/json`) {
        pathPrefix("attendees") {
          get {
            path(Segment) { identity =>
              complete {
                createGetResponse[Attendee](
                  attendeeRepository.fromIdentity(AttendeeId(identity))
                )
              }
            } ~
            path("") {
              complete {
                attendeeRepository.getAll
              }
            }
          }
        }
      }
    }
  }
}
