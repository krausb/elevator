/*
 * This file is part of example-elevator (further: this software).
 *
 * Copyright (C) 2016  Bastian Kraus
 *
 * This software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version)
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.khive.examples.elevator.services

import akka.actor.{ActorRef, FSM}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Finite State Machine: Elevator
  *
  * Created by ceth on 04.11.16.
  */
class Elevator(id: Int, minLevel: Int, maxLevel: Int) extends FSM[MotionState, CurrentState] {

  val logger = LoggerFactory.getLogger(getClass)

  private val upQueue = mutable.Queue[FloorRequest]()
  private val downQueue = mutable.Queue[FloorRequest]()

  // configure the FSM
  startWith(Idle, CurrentState(0, Idle))

  when(Idle) {
    case Event(EnqueueFloor(request),_) => {
      logTransition("Idle")
      log.info(s"Enqueue request: ${request}")
      enqueueFloor(request)
      stay using stateData
    }
    case Event(NextQueue,_) => processQueue(Idle)
  }

  when(MovingUp) {
    case Event(EnqueueFloor(request),_) => {
      logTransition("MovingUp")
      log.info(s"Enqueue request: ${request}")
      enqueueFloor(request)
      stay using stateData
    }
    case Event(NextQueue,_) => processQueue(MovingUp)
  }

  when(MovingDown) {
    case Event(EnqueueFloor(request),_) => {
      logTransition("MovingDown")
      log.info(s"Enqueue request: ${request}")
      enqueueFloor(request)
      stay using stateData
    }
    case Event(NextQueue,_) => processQueue(MovingDown)
  }

  whenUnhandled {
    case Event(Initialize(floor),_) => stay using CurrentState(0, Idle)
    case Event(GetConfig, _) => {
      logger.info("Received GetConfig request...")
      sender ! ElevatorConfig(id, stateData)
      stay using stateData
    }
    case _ => stay using stateData
  }

  onTransition {
    case Idle -> MovingUp => logTransition(s"Transition: Idle -> MovingUp")
    case Idle -> MovingDown => logTransition("Transition: Idle -> MovingDown")
    case MovingUp -> MovingUp => logTransition(s"MovingUp -> MovingUp")
    case MovingUp -> MovingDown => logTransition("MovingUp -> MovingDown")
    case MovingDown -> MovingDown => logTransition("MovingDown -> MovingDown")
    case MovingDown -> MovingUp => logTransition("MovingDown -> MovingUp")
    case MovingDown -> Idle => logTransition("MovingDown -> Idle")
    case Idle -> Idle => logTransition("Idle -> Idle")
  }

  initialize()

  // finally start queue worker schedule
  import scala.concurrent.ExecutionContext.Implicits.global
  context.system.scheduler.schedule(10 seconds, 10 seconds, self, NextQueue)(global)

  private def logTransition(transition: String): Unit = {
    log.info(s"${transition} - current floor: ${stateData.floor} UpQueue: ${upQueue} - DownQueue: ${downQueue}")
  }

  private def enqueueFloor(request: FloorRequest): Unit = {
    if(request.floor == stateData.floor) {
      request.ref ! BoardingNotification(id, stateData.floor)
    } else if(request.floor > stateData.floor) {
      upQueue.enqueue(request)
    } else {
      downQueue.enqueue(request)
    }
  }

  private def processQueue(currentMotion: MotionState): State = {
    log.info("Processing queue...")
    logTransition(s"${currentMotion}")
    currentMotion match {
      case MovingUp => moveUp()
      case MovingDown => moveDown()
      case Idle => isIdle()
    }
  }


  private def moveUp(): State = {
    if(upQueue.nonEmpty && upQueue.head.floor == stateData.floor) {
      upQueue.dequeue().ref ! BoardingNotification(id, stateData.floor)
    }

    if(upQueue.nonEmpty && stateData.floor < maxLevel) {
      goto(MovingUp) using CurrentState(stateData.floor + 1,MovingUp)
    } else if(downQueue.nonEmpty) {
      if(upQueue.nonEmpty) {
        upQueue.foreach(r => enqueueFloor(r))
        upQueue.clear()
      }
      goto(MovingDown) using CurrentState(stateData.floor, MovingDown)
    } else {
      goto(Idle) using CurrentState(stateData.floor, Idle)
    }
  }

  private def moveDown(): State = {
    if(downQueue.nonEmpty && downQueue.head.floor == stateData.floor) {
      downQueue.dequeue().ref ! BoardingNotification(id, stateData.floor)
    }

    if(downQueue.nonEmpty && stateData.floor > 0) {
      goto(MovingDown) using CurrentState(stateData.floor - 1,MovingDown)
    } else if(upQueue.nonEmpty) {
      if(downQueue.nonEmpty) {
        downQueue.foreach(r => enqueueFloor(r))
        downQueue.clear()
      }
      goto(MovingUp) using CurrentState(stateData.floor, MovingUp)
    } else {
      goto(Idle) using CurrentState(stateData.floor, Idle)
    }
  }

  private def isIdle(): State = {
    if(upQueue.nonEmpty) {
      goto(MovingUp) using CurrentState(stateData.floor + 1, MovingUp)
    } else if(downQueue.nonEmpty) {
      goto(MovingDown) using CurrentState(stateData.floor - 1, MovingDown)
    } else {
      stay using stateData
    }
  }

  /**
    * Get the current elevator id
    *
    * @return
    */
  def getId: Int = id

}

sealed trait ElevatorCommand
case class Initialize(floor: Int) extends ElevatorCommand
case class EnqueueFloor(request: FloorRequest) extends ElevatorCommand
case object NextQueue extends ElevatorCommand
case object GetConfig extends ElevatorCommand

sealed trait MotionState
case object Idle extends MotionState
case object MovingUp extends MotionState
case object MovingDown extends MotionState

sealed trait ElevatorData
case class ElevatorConfig(elevatorId: Int, currentState: CurrentState) extends ElevatorData
case class CurrentState(floor: Int, motion: MotionState) extends ElevatorData
case class FloorRequest(floor: Int, ref: ActorRef) extends ElevatorData
case class BoardingNotification(elevatorId: Int, floor: Int) extends ElevatorData
case class FloorRequestError(elevatorId: Int, msg: String) extends ElevatorData