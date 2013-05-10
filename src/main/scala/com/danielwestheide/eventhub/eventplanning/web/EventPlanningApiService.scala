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
import eventplanning.application.meeting._
import eventplanning.domain.meeting._

class EventPlanningApiService(
    attendeeRepository: AttendeeRepository,
    meetingService: MeetingService,
    meetingRepository: MeetingRepository) extends HttpServiceActor {

  implicit val ec = context.dispatcher
  def receive = runRoute(eventPlanningRoute)

  val eventPlanningRoute = {
    pathPrefix("api") {
      respondWithMediaType(`application/json`) {
        get {
          pathPrefix("meetings") {
            pathPrefix(Segment) { id =>
              path("") {
                complete {
                  createGetResponse[Meeting](meetingRepository.fromId(MeetingId(id)))
                }
              }
            } ~
            path("") {
              complete {
                meetingRepository.getAll
              }
            }
          } ~
          pathPrefix("attendees") {
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
        } ~
        pathPrefix("meetings" / "commands") {
          post {
            path("create") {
              entity(as[CreateMeeting]) {
                command =>
                  complete {
                    meetingService.process(command).map(createCommandResponse(_))
                  }
              }
            } ~
                path("talk" / "add") {
                  entity(as[AddTalk]) {
                    command =>
                      complete {
                        meetingService.process(command).map(createCommandResponse(_))
                      }
                  }
                } ~
                path("venue" / "change") {
                  entity(as[ChangeVenue]) {
                    command =>
                      complete {
                        meetingService.process(command).map(createCommandResponse(_))
                      }
                  }
                } ~
                path("attendance" / "declare") {
                  entity(as[DeclareAttendance]) {
                    command =>
                      complete {
                        meetingService.process(command).map(createCommandResponse(_))
                      }
                  }
                } ~
                path("attendance" / "cancel") {
                  entity(as[CancelAttendance]) {
                    command =>
                      complete {
                        meetingService.process(command).map(createCommandResponse(_))
                      }
                  }
                }
            }
          }
      }
    }
  }
}