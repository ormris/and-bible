/*
 * Copyright (c) 2022-2022 Martin Denham, Tuomas Airaksinen and the AndBible contributors.
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

package net.bible.android.control.versification;

import net.bible.android.control.navigation.DocumentBibleBooksFactory;

import org.crosswire.jsword.book.Books;
import org.crosswire.jsword.book.basic.AbstractPassageBook;
import org.crosswire.jsword.passage.Verse;
import org.crosswire.jsword.passage.VerseRange;
import org.crosswire.jsword.versification.BibleBook;
import org.crosswire.jsword.versification.Versification;
import org.crosswire.jsword.versification.system.SystemKJV;
import org.crosswire.jsword.versification.system.Versifications;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

/**
 * @author Martin Denham [mjdenham at gmail dot com]
 */
//@Ignore("Until ESV comes back")
public class BibleTraverserTest {

	private BibleTraverser bibleTraverser;

	private AbstractPassageBook testBook;

	@Before
	public void setup() {
		DocumentBibleBooksFactory mockDocumentBibleBooksFactory = mock(DocumentBibleBooksFactory.class);

		bibleTraverser = new BibleTraverser(mockDocumentBibleBooksFactory);
		testBook = (AbstractPassageBook) Books.installed().getBook("ESV2011");
	}

	@Test
	public void testGetNextVerseWrapsToNextChapter() throws Exception {
		assertThat(bibleTraverser.getNextVerse(testBook, TestData.KJV_PS_14_7).getVerse(), equalTo(1));
	}

	@Test
	public void testGetPreviousVerseWrapsToPreviousChapter() throws Exception {
		assertThat(bibleTraverser.getPrevVerse(testBook, TestData.KJV_PS_14_1).getVerse(), equalTo(6));
	}

	@Test
	public void testGetNextVerseRangeWrapsToNextChapter() throws Exception {
		assertThat(bibleTraverser.getNextVerseRange(testBook, TestData.KJV_PS_14).getStart(), equalTo(new Verse(TestData.KJV, BibleBook.PS, 15, 1)));
		assertThat(bibleTraverser.getNextVerseRange(testBook, TestData.KJV_PS_14).getEnd(), equalTo(new Verse(TestData.KJV, BibleBook.PS, 16, 2)));
	}

	@Test
	public void testGetPreviousVerseRangeWrapsToPreviousChapter() throws Exception {
		assertThat(bibleTraverser.getPreviousVerseRange(testBook, TestData.KJV_PS_14).getStart(), equalTo(new Verse(TestData.KJV, BibleBook.PS, 12, 8)));
		assertThat(bibleTraverser.getPreviousVerseRange(testBook, TestData.KJV_PS_14).getEnd(), equalTo(new Verse(TestData.KJV, BibleBook.PS, 13, 6)));
	}

	@Test
	public void testGetNextVerseRangeCanStopBeforeNextChapter() throws Exception {
		assertThat(bibleTraverser.getNextVerseRange(testBook, TestData.KJV_PS_14, false), equalTo(TestData.KJV_PS_14));
	}

	interface TestData {
		Versification KJV = Versifications.instance().getVersification(SystemKJV.V11N_NAME);

		Verse KJV_PS_14_7 = new Verse(KJV, BibleBook.PS, 14, 7);
		Verse KJV_PS_14_1 = new Verse(KJV, BibleBook.PS, 14, 1);

		VerseRange KJV_PS_14 = new VerseRange(KJV, KJV_PS_14_1, KJV_PS_14_7);
	}

}
