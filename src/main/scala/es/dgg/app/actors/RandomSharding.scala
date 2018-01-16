package es.dgg.app.actors

import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.dispatch.forkjoin.ThreadLocalRandom
import akka.event.Logging
import es.dgg.app.domain.Domain.Message

import scala.concurrent.duration._


object RandomSharding {



  def name = "RandomSharding"

  def props = Props[RandomSharding]
  
  val numberOfShards = 100

  def extractShardId: ExtractShardId = {
    case Message(from,_,_) =>
      (from.hashCode() % numberOfShards).toString
    case ShardRegion.StartEntity(from) ⇒
    (from.hashCode() % numberOfShards).toString   
  }

  def extractEntityId: ExtractEntityId = {
    case Message(from,to,payload) ⇒ (from, Message(from,to,payload))

  }

}


class RandomSharding extends Actor {

  val log = Logging(context.system, this)
  import context.dispatcher
  def scheduler = context.system.scheduler
  def rnd = ThreadLocalRandom.current

  implicit val node = Cluster(context.system)
  val roles = node.getSelfRoles

  //override def preStart(): Unit =
    //scheduler.scheduleOnce(5.seconds, self, Tick)

  // override postRestart so we don't call preStart and schedule a new Tick
  //override def postRestart(reason: Throwable): Unit = ()

  def receive = {

    case Message(from,to,payload) => {
      log.info(s"Mensaje recibido en: $from")
      //sender() ! "hello, tengo estos roles: "+roles
    }
  }

}