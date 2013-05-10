package com.danielwestheide.eventhub.web

object SpraySupport {

  import scalaz.{Failure, Success, Validation}
  import spray.util.LoggingContext
  import spray.routing.ExceptionHandler
  import spray.http.StatusCodes
  import spray.json.DefaultJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  case class ResourceNotFoundException(msg: String) extends RuntimeException(msg)
  case class DomainException(msg: String) extends RuntimeException(msg)

  def createCommandResponse[T](validation: Validation[String, T]): T = validation match {
    case Success(response) => response
    case Failure(msg) => throw DomainException(msg)
  }
  def createGetResponse[T](validation: Validation[String, T]): T = validation match {
    case Success(response) => response
    case Failure(msg) => throw ResourceNotFoundException(msg)
  }

  implicit def customExceptionHandler(implicit log: LoggingContext) = ExceptionHandler.fromPF {
    case DomainException(msg) => ctx =>
      log.warning("Request {} could not be handled normally", ctx.request)
      ctx.complete(StatusCodes.PreconditionFailed, Map("error" -> msg))
    case ResourceNotFoundException(msg) => ctx =>
      ctx.complete(StatusCodes.NotFound, Map("error" -> msg))
  }


}
