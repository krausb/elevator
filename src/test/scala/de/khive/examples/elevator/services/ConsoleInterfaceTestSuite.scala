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

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestActorRef, TestKit, TestProbe }
import akka.util.Timeout
import org.scalactic.source.Position
import org.scalatest.{ BeforeAndAfter, FlatSpecLike, MustMatchers }

import scala.concurrent.duration._

/**
 * Unit Test Suite for [[ConsoleInterface]]
 *
 * Created by ceth on 08.11.16.
 */
class ConsoleInterfaceTestSuite extends TestKit(ActorSystem("ConsoleInterfaceTestSuite")) with ImplicitSender with FlatSpecLike
    with MustMatchers with BeforeAndAfter {

  implicit val timeout = Timeout(5 seconds)
  val probe = TestProbe()
  val consoleInterface = TestActorRef[ConsoleInterface](new ConsoleInterface(probe.ref))

  val testScenario: Map[String, Boolean] = Map(
    ("startstep", true),
    ("stopstep", true),
    ("step", true),
    ("step 3", true),
    ("call 3 up", true),
    ("call 6 down", true),
    ("move 0 4", true),
    ("help", true),
    ("call 3", false),
    ("call down", false),
    ("move 0", false)
  )

  override protected def after(fun: => Any)(implicit pos: Position): Unit = {
    TestKit.shutdownActorSystem(system)
    super.after(fun)
  }

  "The ConsoleInterface" should "parse the command list properly and return the correct results" in {

    for (testCase <- testScenario) {
      assert(consoleInterface.underlyingActor.parseCommand(testCase._1) == testCase._2)
    }

  }

}
