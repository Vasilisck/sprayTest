package com.sprayTest.config

import akka.util.Timeout
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._


/**
  * Created by vasilisck on 10/17/16.
  */
class SprayTestConfig(implicit val ec: ExecutionContext) {

  implicit val timeout = Timeout(5.seconds)

  //mongo
  private val mongoUri = "mongodb://localhost:27017/mydb?authMode=scram-sha1"
  private val driver = MongoDriver()
  private val parsedUri = MongoConnection.parseURI(mongoUri)
  private val connection = parsedUri.map(driver.connection)
  private def mongoDB: Future[DefaultDB] = Future.fromTry(connection).flatMap(_.database("sprayTest"))
  val userCollection = Await.result(mongoDB.map(_.collection[BSONCollection]("user")), timeout.duration)
  val tokenCollection = Await.result(mongoDB.map(_.collection[BSONCollection]("token")), timeout.duration)
  val noteCollection = Await.result(mongoDB.map(_.collection[BSONCollection]("note")), timeout.duration)

}
