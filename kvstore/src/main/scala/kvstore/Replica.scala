package kvstore

import akka.actor._
import akka.event.LoggingReceive
import kvstore.Arbiter._
import scala.collection.immutable.Queue
import akka.actor.SupervisorStrategy.Restart
import scala.annotation.tailrec
import akka.pattern.{ ask, pipe }
import scala.concurrent.duration._
import akka.util.Timeout

object Replica {
  sealed trait Operation {
    def key: String
    def id: Long
  }
  case class Insert(key: String, value: String, id: Long) extends Operation
  case class Remove(key: String, id: Long) extends Operation
  case class Get(key: String, id: Long) extends Operation

  sealed trait OperationReply
  case class OperationAck(id: Long) extends OperationReply
  case class OperationFailed(id: Long) extends OperationReply
  case class GetResult(key: String, valueOption: Option[String], id: Long) extends OperationReply

  def props(arbiter: ActorRef, persistenceProps: Props): Props = Props(new Replica(arbiter, persistenceProps))
}

class Replica(val arbiter: ActorRef, persistenceProps: Props) extends Actor {
  import Replica._
  import Replicator._
  import Persistence._
  import context.dispatcher

  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */

  arbiter ! Join

  var kv = Map.empty[String, String]
  // a map from secondary replicas to replicators
  var secondaries = Map.empty[ActorRef, ActorRef]
  // the current set of replicators
  var replicators = Set.empty[ActorRef]

  val persistence = context.system.actorOf(persistenceProps)
  var cancellables = Map.empty[Long, Cancellable]

  def receive = {
    case JoinedPrimary   => context.become(leader)
    case JoinedSecondary => context.become(replica)
  }

  /* TODO Behavior for  the leader role. */
  val leader: Receive = LoggingReceive{
    case Get(key, id) => sender ! GetResult(key, kv.get(key), id)
    case Insert(key, value, id) => {
      kv += key -> value
      sender ! OperationAck(id)
    }
    case Remove(key, id) => {
      kv -= key
      sender ! OperationAck(id)
    }
  }

  var version = 0L

  /* TODO Behavior for the replica role. */
  val replica: Receive = LoggingReceive{
    case Get(key, id) => sender ! GetResult(key, kv.get(key), id)
    case Snapshot(key,_,seq) if (seq != version) => {
      println ("Version: "+ version +" seq: "+seq)
      if (seq < version) sender ! SnapshotAck(key, seq)
    }
    case Snapshot(key, Some(value), seq) => {
      println("Inserting value:" + value+" with seq:"+ seq)
      kv += (key -> value)
      val cancellable = context.system.scheduler.schedule(0 milliseconds,
          50 milliseconds,
          persistence,
          Persist(key, Some(value), seq))
      cancellables += seq -> cancellable
      persistence ! Persist(key, Some(value), seq)
      context.become(persisting)
    }
    case Snapshot(key, None, seq) => {
      println("Removing key: "+ key +" with seq: "+seq)
      kv -= key
      version += 1
      sender ! SnapshotAck(key, seq)
    }
  }

  val persisting: Receive = LoggingReceive {
    case Persisted(key, id) => {
      println ("Persisted: Sending ack to: "+sender +" and arbiter: "+arbiter)
      val cancellable = cancellables.get(id)
      cancellable match {case Some(value) => value.cancel()}
      arbiter ! SnapshotAck(key, id)
      version += 1
      context.become(replica)
    }
  }

}

