package com.danielwestheide.eventhub.eventplanning.web

object EventPlanningJsonProtocol {
  import spray.json._
  import DefaultJsonProtocol._
  import com.danielwestheide.eventhub._
  import web.CustomJsonProtocol._
  import eventplanning.domain.attendee._

  implicit object attendeeIdFormat extends RootJsonFormat[AttendeeId] {

    import spray.json.JsString

    def write(identity: AttendeeId): JsValue = JsString(identity.id)
    def read(json: JsValue): AttendeeId = json match {
      case JsString(id) => AttendeeId(id)
      case _ => throw new IllegalArgumentException("expected a string value representing attendee id")
    }
  }

  implicit val attendeeFormat = jsonFormat2(Attendee.apply)
}
