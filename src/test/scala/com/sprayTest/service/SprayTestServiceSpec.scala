package com.sprayTest.service

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.sprayTest.manager.SprayTestManager
import com.sprayTest.mappers.User
import org.json4s.{DefaultFormats, Formats}
import org.specs2.mutable.Specification
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import spray.testkit.Specs2RouteTest
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import spray.http.StatusCodes

import scala.concurrent.{Await, Future}

/**
  * Created by vasilisck on 10/21/16.
  */
class SprayTestServiceSpec extends Specification with Specs2RouteTest with SprayTestService {

  def actorRefFactory = system

  val ec = executor
  override implicit val formats: Formats = DefaultFormats

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  private val mongoUri = "mongodb://localhost:27017/mydb?authMode=scram-sha1"
  private val driver = MongoDriver()
  private val parsedUri = MongoConnection.parseURI(mongoUri)
  private val connection = parsedUri.map(driver.connection)

  def mongoDB: Future[DefaultDB] = Future.fromTry(connection).flatMap(_.database("sprayTestTest"))

  val userCollection = Await.result(mongoDB.map(_.collection[BSONCollection]("user")), timeout.duration)
  val tokenCollection = Await.result(mongoDB.map(_.collection[BSONCollection]("token")), timeout.duration)
  val noteCollection = Await.result(mongoDB.map(_.collection[BSONCollection]("note")), timeout.duration)

  override val sprayTestManager = new SprayTestManager(userCollection, tokenCollection, noteCollection)

  def clearDB = Await.result(mongoDB, timeout.duration).drop()

  "handle login request with valid login/password" in {
    //put user in a base
    clearDB
    val user = new User("xyz", "xyz")
    Await.result(userCollection.insert(user), timeout.duration)

    //check
    Post("/api/login").withEntity("""{"login":"xyz","password":"xyz"}""") ~> route ~> check(
      (parse(response.entity.asString) \ "accessToken").extract[String] must length(30)
    )
  }

  "reject login request with invalid login/password" in {
    //put user in a base
    clearDB
    val user = new User("xyz", "xyz")
    Await.result(userCollection.insert(user), timeout.duration)

    //check
    Post("/api/login").withEntity("""{"login":"foo","password":"foo"}""") ~> route ~> check {
      status === StatusCodes.Unauthorized
      (parse(response.entity.asString) \ "error").extract[String] === "AUTH_ERROR"
      (parse(response.entity.asString) \ "message").extract[String] === "user or password wrong"
    }
  }

  "create notes" in {
    clearDB
    val user = new User("xyz", "xyz")
    Await.result(userCollection.insert(user), timeout.duration)

    Post("/api/login").withEntity("""{"login":"xyz","password":"xyz"}""") ~> route ~> check {
      val token = (parse(response.entity.asString) \ "accessToken").extract[String]
      Put(s"/api/note?access_token=$token").withEntity("""{"title":"foo","body":"foo"}""") ~> route ~> check {
        (parse(response.entity.asString) \ "_id").extract[String] must length(24)
      }
    }
  }

  "don't create notes if params is wrong" in {
    clearDB
    val user = new User("xyz", "xyz")
    Await.result(userCollection.insert(user), timeout.duration)

    Post("/api/login").withEntity("""{"login":"xyz","password":"xyz"}""") ~> route ~> check {
      val token = (parse(response.entity.asString) \ "accessToken").extract[String]
      Put(s"/api/note?access_token=$token").withEntity("""{"foo":"foo","foo":"foo"}""") ~> route ~> check {
        (parse(response.entity.asString) \ "error").extract[String] === "BAD_REQUEST"
        (parse(response.entity.asString) \ "message").extract[String] === "Parameter not found"
      }
    }
  }

  "get note" in {
    clearDB
    val user = new User("xyz", "xyz")
    Await.result(userCollection.insert(user), timeout.duration)

    Post("/api/login").withEntity("""{"login":"xyz","password":"xyz"}""") ~> route ~> check {
      val token = (parse(response.entity.asString) \ "accessToken").extract[String]
      Put(s"/api/note?access_token=$token").withEntity("""{"title":"note​ 1","body":"some text"}""") ~> route ~> check {
        val noteId = (parse(response.entity.asString) \ "_id").extract[String]
        Get(s"/api/note/$noteId?access_token=$token") ~> route ~> check {
          (parse(response.entity.asString) \ "_id").extract[String] === noteId
          (parse(response.entity.asString) \ "title").extract[String] === "note​ 1"
          (parse(response.entity.asString) \ "body").extract[String] === "some text"
        }
      }
    }
  }

  "get notes" in {
    clearDB
    val user = new User("xyz", "xyz")
    Await.result(userCollection.insert(user), timeout.duration)

    Post("/api/login").withEntity("""{"login":"xyz","password":"xyz"}""") ~> route ~> check {
      val token = (parse(response.entity.asString) \ "accessToken").extract[String]
      Put(s"/api/note?access_token=$token").withEntity("""{"title":"note​ 1","body":"some text"}""") ~> route ~> check {
        val noteId = (parse(response.entity.asString) \ "_id").extract[String]
        Get(s"/api/note?access_token=$token") ~> route ~> check {
          (parse(response.entity.asString)(0) \ "_id").extract[String] === noteId
          (parse(response.entity.asString)(0) \ "title").extract[String] === "note​ 1"
          (parse(response.entity.asString)(0) \ "body").extract[String] === "some text"
        }
      }
    }
  }

  "delete notes" in {
    clearDB
    val user = new User("xyz", "xyz")
    Await.result(userCollection.insert(user), timeout.duration)

    Post("/api/login").withEntity("""{"login":"xyz","password":"xyz"}""") ~> route ~> check {
      val token = (parse(response.entity.asString) \ "accessToken").extract[String]
      Put(s"/api/note?access_token=$token").withEntity("""{"title":"note​ 1","body":"some text"}""") ~> route ~> check {
        val noteId = (parse(response.entity.asString) \ "_id").extract[String]
        Delete(s"/api/note/$noteId?access_token=$token") ~> route ~> check {
          status === StatusCodes.NoContent
        }
      }
    }
  }

  "delete notes" in {
    clearDB
    val user = new User("xyz", "xyz")
    Await.result(userCollection.insert(user), timeout.duration)

    Post("/api/login").withEntity("""{"login":"xyz","password":"xyz"}""") ~> route ~> check {
      val token = (parse(response.entity.asString) \ "accessToken").extract[String]
      Put(s"/api/note?access_token=$token").withEntity("""{"title":"note​ 1","body":"some text"}""") ~> route ~> check {
        val noteId = (parse(response.entity.asString) \ "_id").extract[String]
        Post(s"/api/note/$noteId?access_token=$token").withEntity("""{"title":"note​ 2","body":"some other text"}""") ~> route ~> check {
          status === StatusCodes.NoContent
        }
      }
    }
  }
}
