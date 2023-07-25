/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.layout

import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.extension.half

/**
 * Defines how a chart’s content is positioned horizontally. This affects the [Chart] and the [HorizontalAxis]
 * instances. [startPaddingDp] and [endPaddingDp] control the amount of empty space at the start and end of the [Chart],
 * respectively.
 */
public sealed class HorizontalLayout(public val startPaddingDp: Float, public val endPaddingDp: Float) {
    /**
     * Given a chart’s maximum number of major entries, calculates the number of labels to be displayed by
     * [HorizontalAxis] instances.
     */
    public abstract fun getHorizontalAxisLabelCount(maxMajorEntryCount: Int): Int

    /**
     * Given a [HorizontalAxis]’s tick thickness, calculates the start inset required by the [HorizontalAxis].
     */
    public abstract fun getStartHorizontalAxisInset(
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
    ): Float

    /**
     * Given a [HorizontalAxis]’s tick thickness, calculates the end inset required by the [HorizontalAxis].
     */
    public abstract fun getEndHorizontalAxisInset(
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
    ): Float

    /**
     * When this is applied, the [Chart] centers each major entry in a designated segment. Some empty space is visible
     * at the start and end of the [Chart]. [HorizontalAxis] instances display ticks and guidelines at the edges of the
     * segments.
     */
    public class Segmented : HorizontalLayout(0f, 0f) {
        override fun getHorizontalAxisLabelCount(maxMajorEntryCount: Int): Int = maxMajorEntryCount + 1

        override fun getStartHorizontalAxisInset(
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
        ): Float = tickThickness.half

        override fun getEndHorizontalAxisInset(
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
        ): Float = tickThickness.half
    }

    /**
     * When this is applied, the [Chart]’s content takes up the [Chart]’s entire width (unless padding is added).
     * [HorizontalAxis] instances display a tick and a guideline for each label, with the tick, guideline, and label
     * vertically centered relative to one another.
     */
    public class FullWidth(startPaddingDp: Float = 0f, endPaddingDp: Float = 0f) :
        HorizontalLayout(startPaddingDp, endPaddingDp) {

        override fun getHorizontalAxisLabelCount(maxMajorEntryCount: Int): Int = maxMajorEntryCount

        override fun getStartHorizontalAxisInset(
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
        ): Float = (tickThickness.half - horizontalDimensions.startPadding).coerceAtLeast(0f)

        override fun getEndHorizontalAxisInset(
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
        ): Float = (tickThickness.half - horizontalDimensions.endPadding).coerceAtLeast(0f)
    }

    public companion object
}
