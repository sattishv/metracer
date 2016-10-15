/*
 * Copyright 2015-2016 Michael Kocherov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.develorium.metracer;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import org.junit.*;
import org.junit.Assert;

public class AutocloseIT extends BaseClass {
	public static class SystemExitException extends RuntimeException {
	}

	private static class WaitForSystemExitScenario extends Scenario {
		public WaitForSystemExitScenario(String thePid) {
			pid = thePid;
		}

		@Override
		public String[] getLaunchArguments() {
			return new String[] { "-v",  pid, "com.develorium.metracertest.Main", "doSomething" };
		}

		@Override	
		public int process() {
			if(exitCode != null)
				throw new SystemExitException();

			printNewStderr();

			for(String line : stderr.toString().split("\n")) {
				if(line.contains("classes instrumented")) {
					if(testProgramProcess != null) {
						testProgramProcess.destroy();
						testProgramProcess = null;
						System.out.println("Test program destroyed");
					}
				}
			}

			return 0;
		}
	}

	@Test(timeout = 5000, expected = SystemExitException.class)
	public void testMetracerExitsOnVictimDeath() throws Throwable {
		Scenario scenario = new WaitForSystemExitScenario(pid);
		runMetracerScenario(scenario);
	}
}
