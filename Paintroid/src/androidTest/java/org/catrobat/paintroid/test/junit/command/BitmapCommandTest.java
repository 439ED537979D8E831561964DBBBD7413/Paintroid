/**
 *  Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2015 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.test.junit.command;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

import org.catrobat.paintroid.command.implementation.BaseCommand;
import org.catrobat.paintroid.command.implementation.BitmapCommand;
import org.catrobat.paintroid.test.utils.PaintroidAsserts;
import org.catrobat.paintroid.test.utils.PrivateAccess;
import static org.junit.Assert.*;
import org.junit.*;

import java.io.File;


public class BitmapCommandTest extends CommandTestSetup {
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		mCommandUnderTest = new BitmapCommand(mBitmapUnderTest);
		mCommandUnderTestNull = new BitmapCommand(null);
		mCanvasBitmapUnderTest.eraseColor(BITMAP_BASE_COLOR - 10);
	}

	@Test
	public void testRunInsertNewBitmap() {
		Bitmap hasToBeTransparentBitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888);
		hasToBeTransparentBitmap.eraseColor(Color.DKGRAY);
		Bitmap bitmapToCompare = mBitmapUnderTest.copy(Config.ARGB_8888, false);
		try {

			assertNull("There should not be a file for a bitmap at the beginning.",
					PrivateAccess.getMemberValue(BaseCommand.class, mCommandUnderTest, "mFileToStoredBitmap"));

			mCommandUnderTest.run(mCanvasUnderTest, mLayerUnderTest);

			assertNull("Bitmap is not cleaned up.",
					PrivateAccess.getMemberValue(BaseCommand.class, mCommandUnderTest, "mBitmap"));
			assertTrue("Bitmaps should be the same", bitmapToCompare.sameAs(mLayerUnderTest.getImage()));
			File fileToStoredBitmap = (File) PrivateAccess.getMemberValue(BaseCommand.class, mCommandUnderTest,
					"mFileToStoredBitmap");
			assertNotNull("Bitmap is not stored to filesystem.", fileToStoredBitmap);
			assertTrue("There is nothing in the bitmap file.", fileToStoredBitmap.length() > 0);

			assertTrue(fileToStoredBitmap.delete());

		} catch (Exception e) {
			fail("Failed to replace new bitmap:" + e.toString());
		} finally {
			hasToBeTransparentBitmap.recycle();

			if (bitmapToCompare != null) {
				bitmapToCompare.recycle();
			}
		}
	}

	@Test
	public void testRunReplaceBitmapFromFileSystem() {
		Bitmap bitmapToCompare = mBitmapUnderTest.copy(Config.ARGB_8888, false);
		try {
				assertNull(
					"There should not be a file in the system (hint: check if too many tests crashed and no files were deleted)",
					PrivateAccess.getMemberValue(BaseCommand.class, mCommandUnderTest, "mFileToStoredBitmap"));

			mCommandUnderTest.run(mCanvasUnderTest, mLayerUnderTest);
			assertNotNull("No file - no restore from file system - no test.",
					PrivateAccess.getMemberValue(BaseCommand.class, mCommandUnderTest, "mFileToStoredBitmap"));

			mCanvasBitmapUnderTest.eraseColor(Color.TRANSPARENT);
			mCommandUnderTest.run(mCanvasUnderTest, mLayerUnderTest);// this should load an existing bitmap from file-system

			PaintroidAsserts.assertBitmapEquals(bitmapToCompare, mLayerUnderTest.getImage());

		} catch (Exception e) {
			fail("Failed to restore bitmap from file system" + e.toString());
		} finally {
			if (bitmapToCompare != null) {
				bitmapToCompare.recycle();
			}
		}
	}

	@Test
	public void testBitmapCommand() {
		try {
			PaintroidAsserts.assertBitmapEquals(mBitmapUnderTest,
					(Bitmap) PrivateAccess.getMemberValue(BaseCommand.class, mCommandUnderTest, "mBitmap"));
		} catch (Exception e) {
			fail("Failed with exception:" + e.toString());
		}
	}
}
