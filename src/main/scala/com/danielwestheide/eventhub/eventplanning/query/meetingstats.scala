package com.danielwestheide.eventhub.eventplanning.query

object meetingstats {

  import org.joda.time.DateTime
  import scalaz.syntax.std.option._
  import akka.actor.Actor
  import com.danielwestheide.eventhub._
  import eventplanning.domain.meeting._

  case class MeetingStats(
      meetingId: MeetingId,
      meetingName: String,
      created: DateTime,
      meetingTime: DateTime,
      registrationsByDay: Map[DateTime, Int],
      cancellationsByDay: Map[DateTime, Int],
      talkAnnouncements: Map[String, DateTime])

  class MeetingStatsStore {

    import scalaz.Validation
    import concurrent.stm.Ref

    private val meetingStatsRef = Ref(Map.empty[MeetingId, MeetingStats])
    def forId(meetingId: MeetingId): Validation[String, MeetingStats] =
      meetingStatsRef.single.get.get(meetingId).toSuccess(s"meeting stats $meetingId cannot be found")
    def saveOrUpdate(stats: MeetingStats): Unit = {
      meetingStatsRef.single.transform(statistics => statistics + (stats.meetingId -> stats))
    }
  }

  class MeetingStatsEventHandler(store: MeetingStatsStore) extends Actor {

    private var lastSequenceNr: Long = 0

    def receive = {
      case MeetingCreated(meetingId, meetingName, time, _, issuedAt, snr) if snr > lastSequenceNr =>
        store.saveOrUpdate(
          MeetingStats(meetingId, meetingName, issuedAt, time, Map.empty, Map.empty, Map.empty))
        lastSequenceNr = snr
      case TalkAdded(meetingId, talk, issuedAt, snr) if (snr > lastSequenceNr) =>
        store.forId(meetingId).foreach { stats =>
            store.saveOrUpdate(addTalkAnnouncement(stats, talk, issuedAt))
        }
        lastSequenceNr = snr
      case AttendanceDeclared(meetingId, _, _, _, _, issuedAt, snr) if (snr > lastSequenceNr) =>
        store.forId(meetingId).foreach { stats =>
            store.saveOrUpdate(addRegistration(stats, issuedAt))
        }
        lastSequenceNr = snr
      case AttendanceCancelled(meetingId, _, issuedAt, snr) if (snr > lastSequenceNr) =>
        store.forId(meetingId).foreach { stats =>
            store.saveOrUpdate(addCancellation(stats, issuedAt))
        }
        lastSequenceNr = snr
    }

    def addTalkAnnouncement(stats: MeetingStats, talk: Talk, time: DateTime): MeetingStats =
      stats.copy(talkAnnouncements = stats.talkAnnouncements + (talk.speaker -> time.withTimeAtStartOfDay()))

    def addRegistration(stats: MeetingStats, time: DateTime): MeetingStats = {
      val day = time.withTimeAtStartOfDay()
      val count = stats.registrationsByDay.getOrElse(day, 0)
      stats.copy(registrationsByDay = stats.registrationsByDay + (day -> (count + 1)))
    }

    def addCancellation(stats: MeetingStats, time: DateTime): MeetingStats = {
      val day = time.withTimeAtStartOfDay()
      val count = stats.cancellationsByDay.getOrElse(day, 0)
      stats.copy(cancellationsByDay = stats.cancellationsByDay + (day -> (count + 1)))
    }

  }


}