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

import akka.actor.{ Actor, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import de.khive.examples.elevator.model.consoleinterface._
import de.khive.examples.elevator.model.elevator._
import de.khive.examples.elevator.model.elevatordispatcher.{ GetStatus, _ }
import de.khive.examples.elevator.model.timestepper.{ DoStep, StartSteppingAutomation, StopSteppingAutomation }
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Console User Interface Actor
 *
 * Takes user input, parses the given commands and tells it to [[ElevatorDispatcher]]
 *
 * Created by ceth on 09.11.16.
 */
class ConsoleInterface(targetActor: ActorRef) extends Actor {

  val log = LoggerFactory.getLogger(getClass)

  implicit val timeout = Timeout(5 seconds)

  def receive: Receive = {
    case EnableConsoleInput => userInput()
    case ParseInputCommand(cmd) => sender ! parseCommand(cmd)
    case t @ _ => {
      log.info(s"Unprocessable command: ${t}")
      ()
    }
  }

  /**
   * Start to consume user input line by line from STDIN until the user enters
   * the 'exit\ command.
   */
  def userInput(): Unit = {
    printHelp()
    for (ln <- io.Source.stdin.getLines.takeWhile(!_.equals("exit"))) {
      log.info(s"Command given: ${ln}")
      try {
        if (parseCommand(ln)) {
          Console.println("Command accepted!")
        } else {
          Console.println(s"Error with given command: ${ln}")
        }
      } catch {
        case e: Exception => log.error(e.getMessage, e)
      }
    }

    Console.println("Application shutdown (takes about 5 seconds) ... ")
    val result = context.system.terminate()
    Await.result(result, Timeout(5 seconds).duration)

    Console.println("Exiting... bye :-)")
    System.exit(0)
  }

  /**
   * Parse the given cmd string and delegate the successfully parsed command to a further method.
   *
   * @param cmd
   * @return
   */
  def parseCommand(cmd: String): Boolean = {
    val cmdParts: List[String] = cmd.split(" ").toList
    try {
      cmdParts match {
        case "startstep" :: rest => doStartStep()
        case "stopstep" :: rest => doStopStep()
        case "step" :: rest => doStep(rest)
        case "status" :: rest => doStatus()
        case "call" :: rest => doCall(rest)
        case "move" :: rest => doMove(rest)
        case "help" :: rest => printHelp()
        case _ => printUnrecognizedCommandError(cmd)
      }
    } catch {
      case e: Exception => {
        log.error(s"An error on parsing given command occured: ${e.getMessage}", e)
        false
      }
    }
  }

  /**
   * Tell the [[ElevatorDispatcher]] to return a [[Seq]] of [[ElevatorConfig]]s of all available elevators
   *
   * @return
   */
  def doStatus(): Boolean = {
    val rFuture = targetActor ? GetStatus
    try {
      val result = Await.result(rFuture, timeout.duration).asInstanceOf[Seq[ElevatorConfig]]
      if (result.nonEmpty) {
        for (c <- result) Console.println(s"${c}")
        true
      } else {
        false
      }
    } catch {
      case e: Exception =>
        log.error(e.getMessage, e)
        false
    }
  }

  /**
   * Tell the [[ElevatorDispatcher]] to call an [[Elevator]] to a floor with a motion
   *
   * @param params
   * @return
   */
  def doCall(params: List[String]): Boolean = {
    if (params.nonEmpty && params.size == 2) {
      try {
        targetActor ! CallElevator(params(0).toString.toInt, MotionState.fromString(params(1).toString))
        true
      } catch {
        case e: Exception => {
          log.error(e.getMessage, e)
          false
        }
      }
    } else {
      false
    }
  }

  /**
   * Tell the [[ElevatorDispatcher]] to move a specific elevator to the target floor
   *
   * @param params (ele
   * @return
   */
  def doMove(params: List[String]): Boolean = {
    if (params.nonEmpty && params.size == 2) {
      try {
        targetActor ! MoveToFloor(params(0).toString.toInt, params(1).toString.toInt)
        true
      } catch {
        case e: Exception => {
          log.error(e.getMessage, e)
          false
        }
      }
    } else {
      false
    }
  }

  /**
   * Tell the [[TimeStepperService]] to start automated stepping
   *
   * @return
   */
  def doStartStep(): Boolean = {
    targetActor ! StartSteppingAutomation
    true
  }

  /**
   * Tell the [[TimeStepperService]] to stop automated stepping
   *
   * @return
   */
  def doStopStep(): Boolean = {
    targetActor ! StopSteppingAutomation
    true
  }

  /**
   * Tell the [[TimeStepperService]] to one or more steps
   *
   * @param params
   * @return
   */
  def doStep(params: List[String]): Boolean = {
    if (params.nonEmpty && params.size == 1) {
      try {
        val count = params(0).toInt
        targetActor ! DoStep(count)
        true
      } catch {
        case e: Exception => {
          log.error(e.getMessage, e)
          false
        }
      }
    } else {
      targetActor ! DoStep(1)
      true
    }
  }

  /**
   * Print a help text to STDIN
   *
   * @return
   */
  def printHelp(): Boolean = {
    Console.println(
      """Enter command:
        | Possible Motions:
        | -> up
        | -> down
        | Possible Commands:
        | -> status (get all the elevator states)
        | -> call <yourFloorNumber> <motion> (call an elevator to your floor)
        | -> move <elevatorId> <targetFloorNumber> (request to move you to a target floor)
        | -> startstep (start automated time stepping)
        | -> stopstep (stop automated time stepping)
        | -> step [<stepCount>] (do steps, stepCount optional)
        | -> help (print this peace of text :-)
        | -> exit (quit elevator control!)
      """
    )
    true
  }

  /**
   * Print unrecognized command error to STDIN
   *
   * @param cmd
   * @return
   */
  def printUnrecognizedCommandError(cmd: String): Boolean = {
    Console.println(s"Unrecognized command: ${cmd}")
    false
  }

}

object ConsoleInterface {
  val props = Props[ConsoleInterface]
}