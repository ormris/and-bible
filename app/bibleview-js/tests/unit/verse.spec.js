/*
 * Copyright (c) 2020 Martin Denham, Tuomas Airaksinen and the And Bible contributors.
 *
 * This file is part of And Bible (http://github.com/AndBible/and-bible).
 *
 * And Bible is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * And Bible is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with And Bible.
 * If not, see http://www.gnu.org/licenses/.
 */


import {shallowMount} from "@vue/test-utils";
import Verse from "@/components/OSIS/Verse";
import {useConfig, useStrings} from "@/composables";
import {ref} from "@vue/reactivity";
import {mapFrom, setFrom} from "@/utils";

describe("Verse.vue", () => {
    let wrapper;
    beforeAll(() => {
        const bookmarkLabels = mapFrom([
            {
                id: 0,
                style: {color: [255, 0, 0, 255]}
            },
            {
                id: 1,
                style: {color: [255, 0, 0, 255]}
            },
            {
                id: 2,
                style: {color: [255, 0, 0, 255]}
            },
        ], v => v.id, v => v.style);

        const bookmarks = {
            bookmarks: [
                {
                    id: 1,
                    ordinalRange: [99, 101],
                    elementRange: [[10, 10], [15, 3]],
                    labels: [0],
                },
                {
                    id: 2,
                    ordinalRange: [99, 101],
                    elementRange: [[10, 10], [15, 5]],
                    labels: [1],
                },
                {
                    id: 3,
                    ordinalRange: [99, 101],
                    elementRange: [[10, 15], [15, 3]],
                    labels: [0, 1, 2],
                },
            ], bookmarkLabels};
        const fragmentInfo = {fragmentKey: "ASDF", elementCount: ref(0)};
        const config = useConfig();
        const strings = useStrings();

        const provide = {
            bookmarks,
            fragmentInfo,
            config,
            strings,
        }
        wrapper = shallowMount(Verse,
            {
                props: {
                    osisID: "Matt.1.1",
                    verseOrdinal: "100",
                },
                global: {
                    provide
                }
            })

    })
    it("Test that leq works", () => {
        expect(wrapper.vm.leq([0, 0], [0, 1])).toBe(true);
        expect(wrapper.vm.leq([0, 0], [1, 1])).toBe(true);
        expect(wrapper.vm.leq([0, 1], [1, 1])).toBe(true);
        expect(wrapper.vm.leq([1, 1], [1, 1])).toBe(true);

        expect(wrapper.vm.leq([1, 1], [1, 0])).toBe(false);
        expect(wrapper.vm.leq([1, 1], [0, 0])).toBe(false);
        expect(wrapper.vm.leq([1, 0], [0, 0])).toBe(false);
        expect(wrapper.vm.leq([1, 0], [0, 0])).toBe(false);
    });
    it("Test styleranges", () => {
        const result = [
            {
                "bookmarks": setFrom(1, 2, 3),
                "elementRange": [[10, 10], [10, 15]],
                "labels": setFrom(0, 1, 2),
            },
            {"bookmarks": setFrom(1, 2, 3), "elementRange": [[10, 15], [15, 3]], "labels": setFrom(0, 1, 2)},
            {"bookmarks": setFrom(1, 2, 3), "elementRange": [[15, 3], [15, 5]], "labels": setFrom(0, 1, 2)}]
        expect(wrapper.vm.styleRanges).toStrictEqual(result);
    });
})
