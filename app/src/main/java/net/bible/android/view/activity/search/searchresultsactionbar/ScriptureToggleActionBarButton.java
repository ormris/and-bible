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

package net.bible.android.view.activity.search.searchresultsactionbar;

import net.bible.android.activity.R;
import net.bible.android.control.ApplicationScope;
import net.bible.android.control.search.SearchControl;
import net.bible.android.view.activity.base.actionbar.ToggleActionBarButton;
import net.bible.service.common.CommonUtils;

import javax.inject.Inject;

/** Toggle between 66 Bible books and deuterocanonical books
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 */
@ApplicationScope
public class ScriptureToggleActionBarButton extends ToggleActionBarButton {

	private final SearchControl searchControl;

	@Inject
	public ScriptureToggleActionBarButton(SearchControl searchControl) {
		super(R.drawable.ic_action_new, R.drawable.ic_baseline_undo_24);

		this.searchControl = searchControl;
	}
	
	@Override
	protected String getTitle() {
		if (isOn()) {
			return CommonUtils.INSTANCE.getResourceString(R.string.deuterocanonical);
		} else {
			return CommonUtils.INSTANCE.getResourceString(R.string.bible);
		}
	}

	@Override
	protected boolean canShow() {
		return searchControl.currentDocumentContainsNonScripture();
	}
}
