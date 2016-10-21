package com.sprayTest.manager

import com.sprayTest.mappers.{Note, Token, User}
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import reactivemongo.api.ReadPreference
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, _}
import spray.http.{StatusCode, StatusCodes}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * Created by vasilisck on 10/19/16.
  */
class SprayTestManager(userCollection: BSONCollection,
                       tokenCollection: BSONCollection,
                       noteCollection: BSONCollection)(implicit ec: ExecutionContext, formats: Formats) {

  def getToken(pasLog: User): Future[(StatusCode, String)] = {
    userCollection.find(BSONDocument("login" -> pasLog.login)).cursor[User](ReadPreference.primary).collect[List]().flatMap { users =>
      users.find(_.password == pasLog.password) match {
        case None => Future.successful(StatusCodes.Unauthorized ->
          compact(render(("error" -> "AUTH_ERROR") ~ ("message" -> "user or password wrong"))))
        case Some(user) => generateAndWriteToken.map(token => StatusCodes.OK -> compact(render("accessToken" -> token)))
      }
    }
  }

  def putNote(note: Note): Future[(StatusCode, String)] = {
    val data = noteCollection.insert(note)
      .flatMap(_ => noteCollection.find(
        BSONDocument("title" -> note.title, "body" -> note.body)
      ).cursor[Note](ReadPreference.primary).collect[List]().map(_.head))
    data.map(a => StatusCodes.OK -> compact(render("_id" -> a._id)))

  }

  def getNotes: Future[(StatusCode, String)] = {
    noteCollection.find(BSONDocument.empty).cursor[Note](ReadPreference.primary).collect[List]().map { data =>
      StatusCodes.OK -> pretty(render(Extraction.decompose(data)))
    }
  }

  def getNote(id: String) = {
    noteCollection.find(BSONDocument("_id" -> reactivemongo.bson.BSONObjectID.parse(id).get))
      .cursor[Note](ReadPreference.primary).collect[List]().map { data =>
      if(data.isEmpty) {
        StatusCodes.InternalServerError -> pretty(render("message" -> "id not found"))
      } else {
        StatusCodes.OK -> pretty(render(Extraction.decompose(data.head)))
      }
    }
  }

  def deleteNote(id: String) = {
    noteCollection.remove(BSONDocument("_id" -> reactivemongo.bson.BSONObjectID.parse(id).get)).map {
      case a if a.ok => StatusCodes.NoContent
      case a if !a.ok => StatusCodes.InternalServerError
    }
  }

  def updateNode(id: String, note: Note) = {
    noteCollection.update(BSONDocument("_id" -> reactivemongo.bson.BSONObjectID.parse(id).get), note).map {
      case a if a.ok => StatusCodes.NoContent
      case a if !a.ok => StatusCodes.InternalServerError
    }
  }

  def checkToken(token: String): Future[Boolean] = {
    tokenCollection.find(BSONDocument("value" -> token)).cursor[Token](ReadPreference.primary).collect[List]().map(_.nonEmpty)
  }

  private def generateAndWriteToken: Future[String] = {
    val token = Random.alphanumeric.take(30).mkString
    tokenCollection.insert(Token(token)).map(_ => token)
  }
}
