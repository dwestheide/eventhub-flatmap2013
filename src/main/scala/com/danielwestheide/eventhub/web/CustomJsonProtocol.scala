package com.danielwestheide.eventhub.web

object CustomJsonProtocol {

  import spray.json.RootJsonFormat
  import org.joda.time.DateTime

  implicit object dateTimeFormat extends RootJsonFormat[DateTime] {

    import spray.json.{JsString, JsValue}

    def write(dt: DateTime): JsValue = JsString(dt.toString())
    def read(json: JsValue): DateTime = json match {
      case JsString(s) => DateTime.parse(s)
      case _ => throw new IllegalArgumentException("expected a date in milliseconds")
    }
  }

}
