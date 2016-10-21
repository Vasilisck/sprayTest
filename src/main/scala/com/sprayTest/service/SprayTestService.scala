package com.sprayTest.service

import java.util.concurrent.Executors

import akka.actor.Actor
import com.sprayTest.config.SprayTestConfig
import com.sprayTest.manager.SprayTestManager
import com.sprayTest.mappers.{Note, User}
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.ExecutionContext

/**
  * Created by vasilisck on 10/17/16.
  */


class SprayTestServiceActor extends Actor with SprayTestService {

  val threadNumber = 8
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadNumber))
  implicit val formats : Formats = DefaultFormats
  val sprayTestConfig: SprayTestConfig  = new SprayTestConfig()
  val sprayTestManager = new SprayTestManager(
    sprayTestConfig.userCollection,
    sprayTestConfig.tokenCollection,
    sprayTestConfig.noteCollection
  )
  def receive = runRoute(route)

  def actorRefFactory = context
}


trait SprayTestService extends HttpService {


  implicit val ec: ExecutionContext

  implicit val formats : Formats

  val sprayTestManager: SprayTestManager

  //QxcQ7NkGjFpFBcnMVN8JPIjhnbiIvP

  val route =
    pathPrefix("api") {
      pathPrefix("login") {
        pathEnd {
          post {
            extract(_.request.entity.data.asString) { data =>
              complete {
                try {
                  val user = parse(data).extract[User]
                  sprayTestManager.getToken(user)
                } catch {
                  case e: Exception =>
                    StatusCodes.BadRequest -> compact(render("error" -> "BAD_REQUEST") merge render("message" -> "user or password not found"))
                }
              }
            }
          }
        }
      } ~
        pathPrefix("note") {
          parameters('access_token) { token =>
            val loginStatus = sprayTestManager.checkToken(token)
            onSuccess(loginStatus) {
              case true =>
                pathEnd {
                  put {
                    extract(_.request.entity.data.asString) { data =>
                      complete {
                        try {
                          val note = parse(data).as[Note]
                          sprayTestManager.putNote(note)
                        } catch {
                          case e: Exception =>
                            StatusCodes.BadRequest -> compact(render("error" -> "BAD_REQUEST") merge render("message" -> "Parameter not found"))
                        }
                      }

                    }
                  } ~ get {
                    complete {
                      sprayTestManager.getNotes
                    }

                  }
                } ~ (get & path(Segment)) { segm =>
                  complete {
                    sprayTestManager.getNote(segm)
                  }
                } ~ (post & path(Segment)) { segm =>
                  extract(_.request.entity.data.asString) { data =>
                    complete {
                      try {
                        val note = parse(data).as[Note]
                        sprayTestManager.updateNode(segm, note)
                      } catch {
                        case e: Exception => StatusCodes.BadRequest -> "can't find title or body"
                      }
                    }

                  }
                } ~ (delete & path(Segment)) { segm =>
                  complete {
                    sprayTestManager.deleteNote(segm)
                  }
                }
              case false => complete(StatusCodes.Unauthorized -> "invalid token")
            }
          }
        }
    }
}
