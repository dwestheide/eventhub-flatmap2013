package com.danielwestheide.eventhub.iam.web

object IdentityJsonProtocol {
  import spray.json._
  import DefaultJsonProtocol._
  import com.danielwestheide.eventhub.web.CustomJsonProtocol._
  import com.danielwestheide.eventhub.iam.domain.user.{UserInformation, RegisterUser}

  implicit val registerUserFormat = jsonFormat5(RegisterUser)
  implicit val userFormat = jsonFormat3(UserInformation)

}
