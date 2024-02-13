/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.layer

import android.graphics.Color
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.MutableChartValues
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.context.CartesianMeasureContext
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.round
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.put
import com.patrykandpatrick.vico.core.model.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.core.model.CandlestickCartesianLayerModel.TypedEntry.Type
import com.patrykandpatrick.vico.core.model.CandlestickCartesianLayerModel.TypedEntry.Type.Change
import com.patrykandpatrick.vico.core.model.ExtraStore
import com.patrykandpatrick.vico.core.model.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.model.MutableExtraStore
import com.patrykandpatrick.vico.core.model.drawing.CandlestickCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.model.drawing.DefaultDrawingModelInterpolator
import com.patrykandpatrick.vico.core.model.drawing.DrawingModelInterpolator
import com.patrykandpatrick.vico.core.model.forEachInIndexed
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * [CandlestickCartesianLayer] displays data as vertical bars. It can draw multiple columns per segment.
 *
 * @param config TODO
 * @param minRealBodyHeightDp TODO
 * @param spacingDp the horizontal padding between the edges of chart segments and the columns they contain.
 * segments that contain a single column only.
 * @param verticalAxisPosition the position of the [VerticalAxis] with which the [ColumnCartesianLayer] should be
 * associated. Use this for independent [CartesianLayer] scaling.
 */
public open class CandlestickCartesianLayer(
    public var config: Config,
    public var minRealBodyHeightDp: Float = DefaultDimens.REAL_BODY_MIN_HEIGHT_DP,
    axisValueOverrider: AxisValueOverrider<LineCartesianLayerModel>? = null,
    public var spacingDp: Float = DefaultDimens.CANDLESTICK_CHART_DEFAULT_SPACING_DP,
    public var verticalAxisPosition: AxisPosition.Vertical? = null,
    public var drawingModelInterpolator: DrawingModelInterpolator<
        CandlestickCartesianLayerDrawingModel.CandleInfo,
        CandlestickCartesianLayerDrawingModel,
        > = DefaultDrawingModelInterpolator(),
) : BaseCartesianLayer<CandlestickCartesianLayerModel>() {
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
            get() =
                maxOf(
                    realBody.thicknessDp,
                    upperWick.thicknessDp,
                    lowerWick.thicknessDp,
                )

        // Empty companion object is needed for extension functions.
        public companion object
    }

    /**
     * Holds information on the [CandlestickCartesianLayer]’s horizontal dimensions.
     */
    protected val horizontalDimensions: MutableHorizontalDimensions = MutableHorizontalDimensions()

    protected val drawingModelKey: ExtraStore.Key<CandlestickCartesianLayerDrawingModel> = ExtraStore.Key()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()

    private fun getFirstAndLastVisibleIndex(
        itemsCount: Int,
        horizontalScroll: Float,
        maxScroll: Float,
    ): Pair<Int, Int> {

        val oneSegmentWidth =  horizontalDimensions.getContentWidth(1)
        val viewportWidth = bounds.width()
        val scrollPercent = (horizontalScroll / (maxScroll / 100.0f)).roundToInt() / 100.0f

        val visibleItems: Int = (viewportWidth / oneSegmentWidth).roundToInt()
        val firstVisibleIndex = ((scrollPercent * (itemsCount - visibleItems)).toInt()).coerceIn(0, itemsCount - 1)
        val lastVisibleIndex = (firstVisibleIndex + visibleItems).coerceIn(0, itemsCount - 1)
        return firstVisibleIndex to lastVisibleIndex
    }

    private var firstVisibleInx: Int = -1
    private var lastVisibleInx: Int = -1

    override fun drawInternal(
        context: CartesianChartDrawContext,
        model: CandlestickCartesianLayerModel,
    ): Unit =
        with(context) {
            entryLocationMap.clear()
            drawChartInternal(
                chartValues = chartValues,
                model = model,
                drawingModel = model.extraStore.getOrNull(drawingModelKey),
            )
        }

    private fun CartesianChartDrawContext.drawChartInternal(
        chartValues: ChartValues,
        model: CandlestickCartesianLayerModel,
        drawingModel: CandlestickCartesianLayerDrawingModel?,
    ) {
        val yRange = chartValues.getYRange(verticalAxisPosition)
        val heightMultiplier = bounds.height() / yRange.length

        val (firstInx, lastInx) = getFirstAndLastVisibleIndex(
            model.series.size,
            horizontalScroll,
            getMaxScrollDistance(),
        )

        firstVisibleInx = firstInx
        lastVisibleInx = lastInx
        val drawingStart: Float =
            bounds.getStart(isLtr = isLtr) + (
                horizontalDimensions.startPadding -
                    config.maxThicknessDp.half.pixels * zoom
            ) * layoutDirectionMultiplier - horizontalScroll

        var bodyCenterX: Float
        var candle: Candle
        var open: Float
        var close: Float
        var low: Float
        var high: Float
        var openY: Float
        var closeY: Float
        val zeroLineYFraction = drawingModel?.zeroY ?: abs(yRange.minY / yRange.length)
        val zeroLinePosition = (bounds.bottom + zeroLineYFraction * bounds.height()).round
        val minRealBodyHeight = minRealBodyHeightDp.pixels

        model.series.forEachInIndexed(range = chartValues.minX..chartValues.maxX) { index, entry, _ ->
            candle = config.getCandle(entry.type)
            val candleInfo = drawingModel?.entries?.get(entry.x)

            val xSpacingMultiplier = (entry.x - chartValues.minX) / chartValues.xStep
            bodyCenterX = drawingStart + layoutDirectionMultiplier * horizontalDimensions.xSpacing *
                xSpacingMultiplier + candle.thicknessDp.half.pixels * zoom

            open = candleInfo?.open ?: (entry.open / yRange.length)
            close = candleInfo?.close ?: (entry.close / yRange.length)
            low = candleInfo?.low ?: (entry.low / yRange.length)
            high = candleInfo?.high ?: (entry.high / yRange.length)

            openY = (zeroLinePosition - open * bounds.height()).round
            closeY = (zeroLinePosition - close * bounds.height()).round

            if (openY - closeY < minRealBodyHeight) {
                openY = (openY + closeY).half + minRealBodyHeight.half
                closeY = openY - minRealBodyHeight
            }

            if (candle.realBody.intersectsVertical(
                    context = this,
                    top = closeY,
                    bottom = openY,
                    centerX = bodyCenterX,
                    boundingBox = bounds,
                    thicknessScale = zoom,
                )
            ) {
                updateMarkerLocationMap(
                    entry = entry,
                    entryX = bodyCenterX,
                    entryY = zeroLinePosition - (entry.high + (entry.low - entry.high) / 2) * heightMultiplier,
                    realBody = candle.realBody,
                    entryIndex = index,
                )

                candle.realBody.drawVertical(this, closeY, openY, bodyCenterX, zoom)

                candle.upperWick.drawVertical(
                    context = this,
                    top = (zeroLinePosition - high * bounds.height()).round,
                    bottom = closeY,
                    centerX = bodyCenterX,
                    thicknessScale = zoom,
                )

                candle.lowerWick.drawVertical(
                    context = this,
                    top = openY,
                    bottom = (zeroLinePosition - low * bounds.height()).round,
                    centerX = bodyCenterX,
                    thicknessScale = zoom,
                )
            }
        }
    }

    private fun updateMarkerLocationMap(
        entry: CandlestickCartesianLayerModel.TypedEntry,
        entryX: Float,
        entryY: Float,
        entryIndex: Int,
        realBody: LineComponent,
    ) {
        if (entryX in bounds.left..bounds.right) {
            entryLocationMap.put(
                x = entryX,
                y = entryY.coerceIn(bounds.top, bounds.bottom),
                entry = entry,
                color = realBody.solidOrStrokeColor,
                index = entryIndex,
            )
        }
    }

    override fun updateChartValues(
        chartValues: MutableChartValues,
        model: CandlestickCartesianLayerModel,
    ) {
        chartValues.tryUpdate(
            axisPosition = verticalAxisPosition,
            minX = axisValueOverrider?.getMinX(model,firstVisibleInx,lastVisibleInx) ?: model.minX,
            maxX = axisValueOverrider?.getMaxX(model,firstVisibleInx,lastVisibleInx) ?: model.maxX,
            minY = axisValueOverrider?.getMinY(model,firstVisibleInx,lastVisibleInx) ?: model.minY,
            maxY = axisValueOverrider?.getMaxY(model,firstVisibleInx,lastVisibleInx) ?: model.maxY,
        )
    }

    override fun updateHorizontalDimensions(
        context: CartesianMeasureContext,
        horizontalDimensions: MutableHorizontalDimensions,
        model: CandlestickCartesianLayerModel,
    ) {
        with(context) {
            val columnCollectionWidth = config.maxThicknessDp.pixels
            val xSpacing = columnCollectionWidth + spacingDp.pixels
            when (val horizontalLayout = horizontalLayout) {
                is HorizontalLayout.Segmented -> {
                    horizontalDimensions.ensureValuesAtLeast(
                        xSpacing = xSpacing,
                        scalableStartPadding = xSpacing.half,
                        scalableEndPadding = xSpacing.half,
                    )
                }

                is HorizontalLayout.FullWidth -> {
                    horizontalDimensions.ensureValuesAtLeast(
                        xSpacing = xSpacing,
                        scalableStartPadding =
                            columnCollectionWidth.half +
                                horizontalLayout.scalableStartPaddingDp.pixels,
                        scalableEndPadding = columnCollectionWidth.half + horizontalLayout.scalableEndPaddingDp.pixels,
                        unscalableStartPadding = horizontalLayout.unscalableStartPaddingDp.pixels,
                        unscalableEndPadding = horizontalLayout.unscalableEndPaddingDp.pixels,
                    )
                }
            }
        }
    }

    override fun prepareForTransformation(
        model: CandlestickCartesianLayerModel?,
        extraStore: MutableExtraStore,
        chartValues: ChartValues,
    ) {
        drawingModelInterpolator.setModels(
            old = extraStore.getOrNull(drawingModelKey),
            new = model?.toDrawingModel(chartValues),
        )
    }

    override suspend fun transform(
        extraStore: MutableExtraStore,
        fraction: Float,
    ) {
        drawingModelInterpolator
            .transform(fraction)
            ?.let { extraStore[drawingModelKey] = it }
            ?: extraStore.remove(drawingModelKey)
    }

    private fun CandlestickCartesianLayerModel.toDrawingModel(
        chartValues: ChartValues,
    ): CandlestickCartesianLayerDrawingModel {
        val yRange = chartValues.getYRange(verticalAxisPosition)
        return series
            .associate { entry ->
                entry.x to
                    CandlestickCartesianLayerDrawingModel.CandleInfo(
                        low = entry.low / yRange.length,
                        high = entry.high / yRange.length,
                        open = entry.open / yRange.length,
                        close = entry.close / yRange.length,
                    )
            }.let { candleInfo ->
                CandlestickCartesianLayerDrawingModel(
                    entries = candleInfo,
                    zeroY = abs(yRange.minY / yRange.length),
                )
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
            get() =
                maxOf(
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
        public fun getCandle(type: Type): Candle =
            when (type.absoluteChange) {
                Change.Increase ->
                    when (type.relativeChange) {
                        Change.Increase -> absolutelyIncreasingRelativelyIncreasing
                        Change.Decrease -> absolutelyDecreasingRelativelyDecreasing
                        Change.Zero -> absolutelyIncreasingRelativelyZero
                    }

                Change.Decrease ->
                    when (type.relativeChange) {
                        Change.Increase -> absolutelyDecreasingRelativelyIncreasing
                        Change.Decrease -> absolutelyDecreasingRelativelyDecreasing
                        Change.Zero -> absolutelyDecreasingRelativelyZero
                    }

                Change.Zero ->
                    when (type.relativeChange) {
                        Change.Increase -> absolutelyZeroRelativelyIncreasing
                        Change.Decrease -> absolutelyDecreasingRelativelyZero
                        Change.Zero -> absolutelyZeroRelativelyZero
                    }
            }

        public companion object
    }
}

private fun LineComponent.copyAsWick(): LineComponent =
    copy(
        color = if (color == Color.TRANSPARENT) strokeColor else color,
        thicknessDp = DefaultDimens.WICK_DEFAULT_WIDTH_DP,
        strokeWidthDp = 0f,
    )
