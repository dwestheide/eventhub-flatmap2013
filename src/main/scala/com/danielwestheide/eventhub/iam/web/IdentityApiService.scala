package com.danielwestheide.eventhub.iam.web

import spray.routing.HttpServiceActor
import spray.http.MediaTypes._
import spray.json._
import spray.httpx.SprayJsonSupport._
import com.danielwestheide.eventhub.iam.domain.user.RegisterUser
import DefaultJsonProtocol._
import IdentityJsonProtocol._
import com.danielwestheide.eventhub.web.SpraySupport._
import com.danielwestheide.eventhub.iam.application.user.{UserRepository, UserService}
import com.danielwestheide.eventhub.iam.domain.user.UserInformation

class IdentityApiService(
    userService: UserService,
    userRepository: UserRepository) extends HttpServiceActor {

  implicit val ec = context.dispatcher
  def receive = runRoute(userRoute)

  val userRoute = {
    pathPrefix("api") {
      respondWithMediaType(`application/json`) {
        pathPrefix("users" / "commands") {
          post {
            path("register") {
              entity(as[RegisterUser]) { command =>
                complete {
                  userService.process(command).map(createCommandResponse(_).userInfo)
                }
              }
            }
          }
        } ~
        pathPrefix("users") {
          get {
            path(Segment) { uniqueName =>
              complete {
                createGetResponse[UserInformation](
                  userRepository.fromUniqueName(uniqueName).map(_.userInfo))
              }
            } ~
            path("") {
              complete {
                userRepository.getAll.map(_.userInfo)
              }
            }
          }
        }
      }
    }
  }

}
