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

import akka.actor.{ActorRef, Props, Actor}
import akka.pattern.ask
import akka.util.Timeout

import de.khive.examples.elevator.ElevatorApplicationConfig
import de.khive.examples.elevator.model.elevator._
import de.khive.examples.elevator.model.elevatordispatcher._
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.util.Random
import scala.concurrent.duration._

/**
  * The [[Elevator]] Dispatcher is responsable for delegating service requests
  * to the registered elevators.
  *
  * Created by ceth on 05.11.16.
  */
class ElevatorDispatcher(config: ElevatorApplicationConfig) extends Actor {

  var log = LoggerFactory.getLogger(getClass)

  implicit val timeout = Timeout(5 seconds)

  // initialize requested amount of elevators
  val elevators = for {
    eId <- 0 until config.elevatorCount
  } yield {
    val elevator = context.actorOf(Props(new Elevator(eId, 0, config.floorCount)))
    elevator ! Initialize(0)
    elevator
  }

  override def receive: Receive = {
    case c@CallElevatorButtonPressed(sourceFloor, motion) => {
      log.info(s"Received ${c} ...")
      enqueueElevatorCall(sourceFloor, motion)
    }
    case m@MoveToFloorButtonPressed(elevatorId, targetFloor) =>
      log.info(s"Received ${m} ...")
      elevators.filter(e => {
        val rFuture = e ? GetConfig
        val result = Await.result(rFuture, timeout.duration).asInstanceOf[ElevatorConfig]
        if(result.elevatorId == elevatorId) true else false
      }).foreach(e => e ! EnqueueFloor(FloorRequest(targetFloor, self)))
    case b@BoardingNotification(elevatorId,floor) =>
      log.info(s"Received ${b} ...")
      log.info(s"(BING) Boarding available for elevator ${elevatorId} on floor ${floor}")
  }

  def enqueueElevatorCall(floor: Int, motion: MotionState): Unit = {
    val availableElevators = elevators.filter(getMotionSelector(floor, motion))
    val targetElevator = if(availableElevators.nonEmpty) Random.shuffle(availableElevators).head else Random.shuffle(elevators).head
    log.info(s"Selected elevator to send to ${floor} with motion ${motion}: ${targetElevator}")
    targetElevator ! EnqueueFloor(FloorRequest(floor, self))
  }

  def getMotionSelector(floor: Int, motion: MotionState)(implicit timeout: Timeout): ActorRef => Boolean =
    motion match {
      case _ =>
        (e) => {
          val config = requestConfig(e)
          log.info(s"Elevator config: ${config}")
          if(config.currentState.motion.eq(Idle) ||
          (config.currentState.motion.eq(MovingUp) && config.currentState.floor < floor) ||
            (config.currentState.motion.eq(MovingDown) && config.currentState.floor > floor)) {
            true
          } else{
            false
          }
      }
    }

  private def requestConfig(ref: ActorRef)(implicit timeout: Timeout): ElevatorConfig = {
    val rFuture = ref ? GetConfig
    Await.result(rFuture, Timeout(5 seconds).duration).asInstanceOf[ElevatorConfig]
  }

}

object ElevatorDispatcher {
  val props = Props[ElevatorDispatcher]
}