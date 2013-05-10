package com.danielwestheide.eventhub.eventplanning.web

object EventPlanningJsonProtocol {
  import spray.json._
  import DefaultJsonProtocol._
  import com.danielwestheide.eventhub._
  import eventplanning.query.meetingstats.MeetingStats
  import web.CustomJsonProtocol._
  import eventplanning.domain.attendee._
  import eventplanning.domain.meeting._
  import Confidence._

  implicit object attendeeIdFormat extends RootJsonFormat[AttendeeId] {
    def write(identity: AttendeeId): JsValue = JsString(identity.id)
    def read(json: JsValue): AttendeeId = json match {
      case JsString(id) => AttendeeId(id)
      case _ => throw new IllegalArgumentException("expected a string value representing attendee id")
    }
  }
  implicit object confidenceFormat extends RootJsonFormat[Confidence] {
    def write(confidence: Confidence): JsValue = JsString(confidence.toString)
    def read(json: JsValue): Confidence = json match {
      case JsString(confidence) if Confidence.values.exists(c => c.toString == confidence) =>
        Confidence.withName(confidence)
      case _ => throw new IllegalArgumentException("valid confidence identifier expected")
    }
  }
  implicit object meetingIdFormat extends RootJsonFormat[MeetingId] {
    def write(identity: MeetingId): JsValue = JsString(identity.id)
    def read(json: JsValue): MeetingId = json match {
      case JsString(id) => MeetingId(id)
      case _ => throw new IllegalArgumentException("expected a string value representing meeting id")
    }
  }

  implicit val attendeeFormat = jsonFormat2(Attendee.apply)
  implicit val venueFormat = jsonFormat6(Venue)
  implicit val attendanceFormat = jsonFormat2(Attendance)
  implicit val talkFormat = jsonFormat2(Talk)
  implicit val createMeetingFormat = jsonFormat5(CreateMeeting)
  implicit val addTalkFormat = jsonFormat3(AddTalk)
  implicit val changeVenueFormat = jsonFormat3(ChangeVenue)
  implicit val declareAttendanceFormat = jsonFormat4(DeclareAttendance)
  implicit val cancelAttendanceFormat = jsonFormat3(CancelAttendance)
  implicit val meetingFormat = jsonFormat6(Meeting)
  implicit val meetingStats = jsonFormat7(MeetingStats)

  
}
