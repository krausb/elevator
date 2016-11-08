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

import akka.actor.{ Actor, ActorRef, Cancellable }
import de.khive.examples.elevator.model.elevator.NextQueue
import de.khive.examples.elevator.model.timestepper.{ DoStep, StartSteppingAutomation, StopSteppingAutomation }
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
 * Time Stepper Worker Service
 *
 * Sends a [[NextQueue]] to the given delegate in a configurable duration
 *
 * Created by ceth on 08.11.16.
 */
class TimeStepperService(delegate: ActorRef) extends Actor {

  val log = LoggerFactory.getLogger(getClass)

  // finally start queue worker schedule
  import scala.concurrent.ExecutionContext.Implicits.global
  var scheduleRef: Option[Cancellable] = None

  override def receive: Receive = {
    case DoStep(slots) => {
      log.info(s"Doing ${slots} steps...")
      for (c <- 0 until slots) delegate ! NextQueue
    }
    case StartSteppingAutomation => {
      if (scheduleRef.isEmpty) {
        scheduleRef = Option(context.system.scheduler.schedule(10 seconds, 5 seconds, delegate, DoStep)(global))
        log.info("Stepper started!")
      }
    }
    case StopSteppingAutomation => {
      scheduleRef match {
        case Some(ref) => {
          ref.cancel()
          if (ref.isCancelled) {
            scheduleRef = None
          }
          log.info("Stepper stopped!")
        }
        case _ => ()
      }
    }
    case _ => ()
  }

}

