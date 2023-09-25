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

package com.patrykandpatrick.vico.core.chart.candlestick

import android.graphics.Color
import com.patrykandpatrick.vico.core.DefaultDimens.REAL_BODY_MIN_HEIGHT_DP
import com.patrykandpatrick.vico.core.DefaultDimens.WICK_DEFAULT_WIDTH_DP
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntryModel
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntryType
import com.patrykandpatrick.vico.core.chart.BaseChart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.chart.forEachIn
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.put
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.context.CartesianMeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.round
import com.patrykandpatrick.vico.core.marker.Marker
import kotlin.math.roundToInt

/**
 * [CandlestickChart] displays data as vertical bars. It can draw multiple columns per segment.
 *
 * @param config TODO
 * @param minRealBodyHeightDp TODO
 * @param spacingDp the horizontal padding between the edges of chart segments and the columns they contain.
 * segments that contain a single column only.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 */
public open class CandlestickChart(
    public var config: Config,
    public var minRealBodyHeightDp: Float = REAL_BODY_MIN_HEIGHT_DP,
    public var spacingDp: Float = 16f,
    public var targetVerticalAxisPosition: AxisPosition.Vertical? = null,
) : BaseChart<CandlestickEntryModel>() {

    /**
     * TODO
     *
     * @param realBody TODO
     * @param upperWick TODO
     * @param lowerWick TODO
     */
    public data class Candle(
        public val realBody: LineComponent,
        public val upperWick: LineComponent = realBody.copyAsWick(),
        public val lowerWick: LineComponent = upperWick,
    ) {

        /**
         * Returns the maximum thickness among all of the candle components.
         */
        val thicknessDp: Float
            get() = maxOf(
                realBody.thicknessDp,
                upperWick.thicknessDp,
                lowerWick.thicknessDp,
            )

        // Empty companion object is needed for extension functions.
        public companion object
    }

    private val heightMap = HashMap<Float, Pair<Float, Float>>()

    /**
     * Holds information on the [CandlestickChart]’s horizontal dimensions.
     */
    protected val horizontalDimensions: MutableHorizontalDimensions = MutableHorizontalDimensions()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()

    private fun getFirstAndLastVisibleIndex(
        itemsCount: Int,
        horizontalScroll: Float,
        maxScroll: Float,
    ): Pair<Int, Int> {
        val oneSegmentWidth = segmentProperties.cellWidth
        val viewportWidth = bounds.width()
        val scrollPercent = (horizontalScroll / (maxScroll / 100.0f)).roundToInt() / 100.0f

        val visibleItems: Int = (viewportWidth / oneSegmentWidth).roundToInt()
        val firstVisibleIndex = ((scrollPercent * (itemsCount - visibleItems)).toInt()).coerceIn(0, itemsCount - 1)
        val lastVisibleIndex = (firstVisibleIndex + visibleItems).coerceIn(0, itemsCount - 1)
        return firstVisibleIndex to lastVisibleIndex
    }

    private var firstVisibleInx: Int = -1
    private var lastVisibleInx: Int = -1


    override fun drawChart(
        context: CartesianChartDrawContext,
        model: CandlestickEntryModel,
    ): Unit = with(context) {
        entryLocationMap.clear()
        drawChartInternal(
            chartValues = chartValuesManager.getChartValues(axisPosition = targetVerticalAxisPosition),
            model = model,
        )
        heightMap.clear()
    }

    private fun CartesianChartDrawContext.drawChartInternal(
        chartValues: ChartValues,
        model: CandlestickEntryModel,
    ) {
        val (firstInx, lastInx) = getFirstAndLastVisibleIndex(
            model.entries.size,
            horizontalScroll,
            getMaxScrollDistance(),
        )
        firstVisibleInx = firstInx
        lastVisibleInx = lastInx

        val yRange = (chartValues.maxY - chartValues.minY).takeIf { it != 0f } ?: return
        val heightMultiplier = bounds.height() / yRange

        val drawingStart: Float = bounds.getStart(isLtr = isLtr) +
            horizontalDimensions.scaled(chartScale).startPadding -
            config.maxThicknessDp.half.pixels * layoutDirectionMultiplier * chartScale - horizontalScroll

        var bodyCenterX: Float
        var candle: Candle
        var bodyTop: Float
        var bodyBottom: Float
        val zeroLinePosition = (bounds.bottom + chartValues.minY / yRange * bounds.height()).round

        val minRealBodyHeight = minRealBodyHeightDp.pixels

        model.entries.forEachIn(range = chartValues.minX..chartValues.maxX) { entry ->

            candle = config.getCandle(entry.type)

            val xSpacingMultiplier = (entry.x - chartValues.minX) / chartValues.xStep
            bodyCenterX = drawingStart + layoutDirectionMultiplier * horizontalDimensions.xSpacing *
                xSpacingMultiplier + candle.thicknessDp.half.pixels * chartScale

            bodyBottom = (zeroLinePosition - minOf(entry.close, entry.open) * heightMultiplier).round
            bodyTop = (zeroLinePosition - maxOf(entry.close, entry.open) * heightMultiplier).round

            if (bodyBottom - bodyTop < minRealBodyHeight) {
                bodyBottom = (bodyBottom + bodyTop).half + minRealBodyHeight.half
                bodyTop = bodyBottom - minRealBodyHeight
            }

            if (candle.realBody.intersectsVertical(
                    context = this,
                    top = bodyTop,
                    bottom = bodyBottom,
                    centerX = bodyCenterX,
                    boundingBox = bounds,
                    thicknessScale = chartScale,
                )
            ) {
                listOf(entry.low, entry.open, entry.close, entry.high)
                    .forEach { entryY ->
                        updateMarkerLocationMap(
                            entry = entryOf(x = entry.x, y = entryY),
                            columnTop = zeroLinePosition - entryY * heightMultiplier,
                            columnCenterX = bodyCenterX,
                            realBody = candle.realBody,
                        )
                    }

                candle.realBody.drawVertical(this, bodyTop, bodyBottom, bodyCenterX, chartScale)

                candle.upperWick.drawVertical(
                    context = this,
                    top = zeroLinePosition - entry.high * heightMultiplier,
                    bottom = bodyTop,
                    centerX = bodyCenterX,
                    thicknessScale = chartScale,
                )

                candle.lowerWick.drawVertical(
                    context = this,
                    top = bodyBottom,
                    bottom = zeroLinePosition - entry.low * heightMultiplier,
                    centerX = bodyCenterX,
                    thicknessScale = chartScale,
                )
            }
        }
    }

    private fun updateMarkerLocationMap(
        entry: ChartEntry,
        columnTop: Float,
        columnCenterX: Float,
        realBody: LineComponent,
    ) {
        if (columnCenterX in bounds.left..bounds.right) {
            entryLocationMap.put(
                x = columnCenterX,
                y = columnTop.coerceIn(bounds.top, bounds.bottom),
                entry = entry,
                color = realBody.solidOrStrokeColor,
                index = 0,
            )
        }
    }

    override fun updateChartValues(
        chartValuesManager: ChartValuesManager,
        model: CandlestickEntryModel,
        xStep: Float?,
    ) {
        chartValuesManager.tryUpdate(
            minX = axisValuesOverrider?.getMinX(model) ?: model.minX,
            maxX = axisValuesOverrider?.getMaxX(model) ?: model.maxX,
            minY = axisValuesOverrider?.getMinY(model) ?: model.minY,
            maxY = axisValuesOverrider?.getMaxY(model) ?: model.maxY,
            xStep = xStep ?: model.xGcd,
            model = model,
            axisPosition = targetVerticalAxisPosition,
        )
    }

    override fun getHorizontalDimensions(
        context: CartesianMeasureContext,
        model: CandlestickEntryModel,
    ): HorizontalDimensions = with(context) {
        val columnCollectionWidth = config.maxThicknessDp.pixels
        horizontalDimensions.apply {
            xSpacing = columnCollectionWidth + spacingDp.pixels
            when (horizontalLayout) {
                is HorizontalLayout.Segmented -> {
                    scalableStartPadding = xSpacing.half
                    scalableEndPadding = scalableStartPadding
                }

                is HorizontalLayout.FullWidth -> {
                    scalableStartPadding = columnCollectionWidth.half + horizontalLayout.startPaddingDp.pixels
                    scalableEndPadding = columnCollectionWidth.half + horizontalLayout.endPaddingDp.pixels
                }
            }
        }
    }

    /**
     * TODO
     *
     * @param absolutelyIncreasingRelativelyIncreasing TODO
     * @param absolutelyIncreasingRelativelyZero TODO
     * @param absolutelyIncreasingRelativelyDecreasing TODO
     * @param absolutelyZeroRelativelyIncreasing TODO
     * @param absolutelyZeroRelativelyZero TODO
     * @param absolutelyZeroRelativelyDecreasing TODO
     * @param absolutelyDecreasingRelativelyIncreasing TODO
     * @param absolutelyDecreasingRelativelyZero TODO
     * @param absolutelyDecreasingRelativelyDecreasing TODO
     */
    @Suppress("LongParameterList")
    public class Config(
        public val absolutelyIncreasingRelativelyIncreasing: Candle,
        public val absolutelyIncreasingRelativelyZero: Candle,
        public val absolutelyIncreasingRelativelyDecreasing: Candle,
        public val absolutelyZeroRelativelyIncreasing: Candle,
        public val absolutelyZeroRelativelyZero: Candle,
        public val absolutelyZeroRelativelyDecreasing: Candle,
        public val absolutelyDecreasingRelativelyIncreasing: Candle,
        public val absolutelyDecreasingRelativelyZero: Candle,
        public val absolutelyDecreasingRelativelyDecreasing: Candle,
    ) {

        /**
         * TODO
         */
        public val maxThicknessDp: Float
            get() = maxOf(
                absolutelyIncreasingRelativelyIncreasing.realBody.thicknessDp,
                absolutelyIncreasingRelativelyZero.realBody.thicknessDp,
                absolutelyIncreasingRelativelyDecreasing.realBody.thicknessDp,
                absolutelyZeroRelativelyIncreasing.realBody.thicknessDp,
                absolutelyZeroRelativelyZero.realBody.thicknessDp,
                absolutelyZeroRelativelyDecreasing.realBody.thicknessDp,
                absolutelyDecreasingRelativelyIncreasing.realBody.thicknessDp,
                absolutelyDecreasingRelativelyZero.realBody.thicknessDp,
                absolutelyDecreasingRelativelyDecreasing.realBody.thicknessDp,
            )

        /**
         * TODO
         */
        public fun getCandle(type: CandlestickEntryType): Candle =
            when(type.absoluteChange) {
                CandlestickEntryType.Change.Increase -> when (type.relativeChange) {
                    CandlestickEntryType.Change.Increase -> absolutelyIncreasingRelativelyIncreasing
                    CandlestickEntryType.Change.Decrease -> absolutelyDecreasingRelativelyDecreasing
                    CandlestickEntryType.Change.Zero -> absolutelyIncreasingRelativelyZero
                }
                CandlestickEntryType.Change.Decrease -> when (type.relativeChange) {
                    CandlestickEntryType.Change.Increase -> absolutelyDecreasingRelativelyIncreasing
                    CandlestickEntryType.Change.Decrease -> absolutelyDecreasingRelativelyDecreasing
                    CandlestickEntryType.Change.Zero -> absolutelyDecreasingRelativelyZero
                }
                CandlestickEntryType.Change.Zero -> when (type.relativeChange) {
                    CandlestickEntryType.Change.Increase -> absolutelyZeroRelativelyIncreasing
                    CandlestickEntryType.Change.Decrease -> absolutelyDecreasingRelativelyZero
                    CandlestickEntryType.Change.Zero -> absolutelyZeroRelativelyZero
                }
            }

        public companion object
    }
}

private fun LineComponent.copyAsWick(): LineComponent =
    copy(
        color = if (color == Color.TRANSPARENT) strokeColor else color,
        thicknessDp = WICK_DEFAULT_WIDTH_DP,
        strokeWidthDp = 0f,
    )
