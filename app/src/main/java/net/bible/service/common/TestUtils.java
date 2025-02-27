/*
 * Copyright (c) 2020-2022 Martin Denham, Tuomas Airaksinen and the AndBible contributors.
 *
 * This file is part of AndBible: Bible Study (http://github.com/AndBible/and-bible).
 *
 * AndBible is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * AndBible is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AndBible.
 * If not, see http://www.gnu.org/licenses/.
 */

package net.bible.service.common;

/** support junit tests
 * 
 * @author denha1m
 *
 */
public class TestUtils {
	
	private static boolean isAndroid;
	private static boolean isAndroidCheckDone;
	
	/** return true id running in an Android vm
	 * 
	 * @return
	 */
	public static boolean isAndroid() {
		if (!isAndroidCheckDone) {
			try {
				Class.forName("net.bible.test.TestEnvironmentFlag");
				isAndroid = false;
				System.out.println("Running as test");
			} catch (ClassNotFoundException cnfe) {
				isAndroid = true;
				System.out.println("Running on Android");
			}
			isAndroidCheckDone = true;
		}
		return isAndroid;
	}
	
	public static void setTestMode() {
		isAndroid = false;
		isAndroidCheckDone = true;
	}
}
