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

package com.patrykandpatrick.vico.compose.style

import android.graphics.Typeface
import android.text.Layout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.chart.layer.lineSpec
import com.patrykandpatrick.vico.compose.chart.pie.slice.rememberSlice
import com.patrykandpatrick.vico.compose.component.shape.dashedShape
import com.patrykandpatrick.vico.compose.component.shape.shader.color
import com.patrykandpatrick.vico.core.DefaultColors
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer.MergeMode
import com.patrykandpatrick.vico.core.chart.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.chart.layer.LineCartesianLayer.LineSpec
import com.patrykandpatrick.vico.core.chart.pie.Size
import com.patrykandpatrick.vico.core.chart.pie.label.InsideSliceLabel
import com.patrykandpatrick.vico.core.chart.pie.label.SliceLabel
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.formatter.DecimalFormatValueFormatter
import com.patrykandpatrick.vico.core.formatter.ValueFormatter

/**
 * Defines the appearance of charts.
 *
 * @property axis the appearance of chart axes.
 * @property columnLayer the appearance of [ColumnCartesianLayer]s.
 * @property lineLayer the appearance of [LineCartesianLayer]s.
 * @property marker the appearance of chart markers.
 * @property elevationOverlayColor the color used for elevation overlays.
 * @property pieChart the appearance of pie charts.
 */
public data class ChartStyle(
    val axis: Axis,
    val columnLayer: ColumnLayer,
    val lineLayer: LineLayer,
    val marker: Marker,
    val elevationOverlayColor: Color,
    val pieChart: PieChart,
) {
    /**
     * Defines the appearance of chart axes.
     *
     * @property axisLabelBackground an optional [ShapeComponent] to display behind the text of axis labels.
     * @property axisLabelColor the text color for axis labels.
     * @property axisLabelTextSize the text size for axis labels.
     * @property axisLabelLineCount the line count for axis labels.
     * @property axisLabelVerticalPadding the amount of vertical padding between the background and the text of axis
     * labels.
     * @property axisLabelHorizontalPadding the amount of horizontal padding between the background and the text of axis
     * labels.
     * @property axisLabelVerticalMargin the vertical margin around the backgrounds of axis labels.
     * @property axisLabelHorizontalMargin the horizontal margin around the backgrounds of axis labels.
     * @property axisLabelRotationDegrees the number of degrees by which axis labels are rotated.
     * @property axisLabelTypeface the typeface used for axis labels.
     * @property axisLabelTextAlignment the text alignment for axis labels.
     * @property axisGuidelineColor the color of axis guidelines.
     * @property axisGuidelineWidth the width of axis guidelines.
     * @property axisGuidelineShape the [Shape] used for axis guidelines.
     * @property axisLineColor the color of axis lines.
     * @property axisLineWidth the width of axis lines.
     * @property axisLineShape the [Shape] used for axis lines.
     * @property axisTickColor the color of axis ticks.
     * @property axisTickWidth the width of axis ticks.
     * @property axisTickShape the [Shape] used for axis ticks.
     * @property axisTickLength the length of axis ticks.
     * @property axisValueFormatter the [AxisValueFormatter] to use for axis labels.
     */
    public data class Axis(
        val axisLabelBackground: ShapeComponent? = null,
        val axisLabelColor: Color,
        val axisLabelTextSize: TextUnit = DefaultDimens.AXIS_LABEL_SIZE.sp,
        val axisLabelLineCount: Int = DefaultDimens.AXIS_LABEL_MAX_LINES,
        val axisLabelVerticalPadding: Dp = DefaultDimens.AXIS_LABEL_VERTICAL_PADDING.dp,
        val axisLabelHorizontalPadding: Dp = DefaultDimens.AXIS_LABEL_HORIZONTAL_PADDING.dp,
        val axisLabelVerticalMargin: Dp = DefaultDimens.AXIS_LABEL_VERTICAL_MARGIN.dp,
        val axisLabelHorizontalMargin: Dp = DefaultDimens.AXIS_LABEL_HORIZONTAL_MARGIN.dp,
        val axisLabelRotationDegrees: Float = DefaultDimens.AXIS_LABEL_ROTATION_DEGREES,
        val axisLabelTypeface: Typeface = Typeface.MONOSPACE,
        val axisLabelTextAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        val axisGuidelineColor: Color,
        val axisGuidelineWidth: Dp = DefaultDimens.AXIS_GUIDELINE_WIDTH.dp,
        val axisGuidelineShape: Shape =
            Shapes.dashedShape(
                shape = Shapes.rectShape,
                dashLength = DefaultDimens.DASH_LENGTH.dp,
                gapLength = DefaultDimens.DASH_GAP.dp,
            ),
        val axisLineColor: Color,
        val axisLineWidth: Dp = DefaultDimens.AXIS_LINE_WIDTH.dp,
        val axisLineShape: Shape = Shapes.rectShape,
        val axisTickColor: Color = axisLineColor,
        val axisTickWidth: Dp = axisLineWidth,
        val axisTickShape: Shape = Shapes.rectShape,
        val axisTickLength: Dp = DefaultDimens.AXIS_TICK_LENGTH.dp,
        val axisValueFormatter: AxisValueFormatter<AxisPosition> = DecimalFormatAxisValueFormatter(),
    )

    /**
     * Defines the appearance of [ColumnCartesianLayer]s.
     *
     * @property columns the [LineComponent] instances to use for columns. This list is iterated through as many times
     * as necessary for each column collection. If the list contains a single element, all columns have the same
     * appearance.
     * @property outsideSpacing the distance between neighboring column collections.
     * @property innerSpacing the distance between neighboring grouped columns.
     * @property mergeMode defines the way multiple columns are rendered in [ColumnLayer]s.
     * @property dataLabel an optional [TextComponent] to use for data labels.
     * @property dataLabelVerticalPosition the vertical position of data labels relative to the top of their
     * respective columns.
     * @property dataLabelValueFormatter the [ValueFormatter] to use for data labels.
     * @property dataLabelRotationDegrees the rotation of data labels in degrees.
     */
    public data class ColumnLayer(
        val columns: List<LineComponent>,
        val outsideSpacing: Dp = DefaultDimens.COLUMN_OUTSIDE_SPACING.dp,
        val innerSpacing: Dp = DefaultDimens.COLUMN_INSIDE_SPACING.dp,
        val mergeMode: MergeMode = MergeMode.Grouped,
        val dataLabel: TextComponent? = null,
        val dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
        val dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
        val dataLabelRotationDegrees: Float = 0f,
    )

    /**
     * Defines the appearance of [LineCartesianLayer]s.
     *
     * @param lines the [LineSpec]s to use for the lines. This list is iterated through as many times as there are
     * lines.
     * @property spacing the spacing between points.
     */
    public data class LineLayer(
        val lines: List<LineSpec>,
        val spacing: Dp = DefaultDimens.POINT_SPACING.dp,
    )

    /**
     * Defines the appearance of chart markers.
     *
     * @property indicatorSize the size of indicators (these are the components shown at the top of columns and on lines
     * to highlight the value or values that a marker refers to).
     * @property horizontalPadding the horizontal padding for marker bubbles.
     * @property verticalPadding the vertical padding for marker bubbles.
     */
    public data class Marker(
        val indicatorSize: Dp = DefaultDimens.MARKER_INDICATOR_SIZE.dp,
        val horizontalPadding: Dp = DefaultDimens.MARKER_HORIZONTAL_PADDING.dp,
        val verticalPadding: Dp = DefaultDimens.MARKER_VERTICAL_PADDING.dp,
    )

    /**
     * Defines the appearance of pie charts.
     *
     * @property slices the [Slice]s to use for the pie chart.
     * @property spacing the spacing between slices.
     * @property outerSize the size of the pie chart.
     * @property innerSize the size of the hole in the middle of the pie chart.
     * @property startAngle the angle at which the first slice starts.
     * @property sliceLabel the [SliceLabel] used as a default label style in [rememberSlice] function.
     */
    public data class PieChart(
        val slices: List<Slice>,
        val spacing: Dp = DefaultDimens.PIE_CHART_SPACING.dp,
        val outerSize: Size.OuterSize = Size.OuterSize.fill(),
        val innerSize: Size.InnerSize = Size.InnerSize.zero(),
        val startAngle: Float = DefaultDimens.PIE_CHART_START_ANGLE,
        val sliceLabel: SliceLabel = InsideSliceLabel(textComponent = textComponent()),
    )

    public companion object {
        /**
         * Creates a base implementation of [ChartStyle] using the provided colors.
         */
        public fun fromColors(
            axisLabelColor: Color,
            axisGuidelineColor: Color,
            axisLineColor: Color,
            entityColors: List<Color>,
            elevationOverlayColor: Color,
        ): ChartStyle =
            ChartStyle(
                axis =
                    Axis(
                        axisLabelColor = axisLabelColor,
                        axisGuidelineColor = axisGuidelineColor,
                        axisLineColor = axisLineColor,
                    ),
                columnLayer =
                    ColumnLayer(
                        columns =
                            entityColors.map { entityColor ->
                                LineComponent(
                                    color = entityColor.toArgb(),
                                    thicknessDp = DefaultDimens.COLUMN_WIDTH,
                                    shape =
                                        Shapes.roundedCornerShape(
                                            allPercent = DefaultDimens.COLUMN_ROUNDNESS_PERCENT,
                                        ),
                                )
                            },
                    ),
                lineLayer =
                    LineLayer(
                        lines =
                            entityColors.map { entityColor ->
                                lineSpec(
                                    shader = DynamicShaders.color(entityColor),
                                )
                            },
                    ),
                marker = Marker(),
                elevationOverlayColor = elevationOverlayColor,
                pieChart =
                    PieChart(
                        slices =
                            entityColors.map { entityColor ->
                                Slice(
                                    color = entityColor.toArgb(),
                                )
                            },
                    ),
            )

        internal fun fromDefaultColors(defaultColors: DefaultColors) =
            fromColors(
                axisLabelColor = Color(defaultColors.axisLabelColor),
                axisGuidelineColor = Color(defaultColors.axisGuidelineColor),
                axisLineColor = Color(defaultColors.axisLineColor),
                entityColors =
                    listOf(
                        defaultColors.entity1Color,
                        defaultColors.entity2Color,
                        defaultColors.entity3Color,
                    ).map(::Color),
                elevationOverlayColor = Color(defaultColors.elevationOverlayColor),
            )
    }
}

/**
 * Provides a [ChartStyle] instance.
 */
public object LocalChartStyle {
    internal val default: ChartStyle
        @Composable
        get() {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            return remember(isSystemInDarkTheme) {
                ChartStyle.fromDefaultColors(if (isSystemInDarkTheme) DefaultColors.Dark else DefaultColors.Light)
            }
        }

    private val LocalProvidedStyle: ProvidableCompositionLocal<ChartStyle?> =
        compositionLocalOf { null }

    /**
     * The [ChartStyle] instance provided by [LocalChartStyle] at the call site.
     */
    public val current: ChartStyle
        @Composable get() = LocalProvidedStyle.current ?: default

    /**
     * Provides a [ChartStyle] instance via [LocalChartStyle].
     *
     * @param chartStyle the [ChartStyle] instance to provide.
     */
    public infix fun provides(chartStyle: ChartStyle): ProvidedValue<ChartStyle?> =
        LocalProvidedStyle.provides(chartStyle)
}

/**
 * Offers quick access to [LocalChartStyle.current].
 */
public val currentChartStyle: ChartStyle
    @Composable get() = LocalChartStyle.current

/**
 * Provides a [ChartStyle] instance to [content] via [LocalChartStyle].
 *
 * @param chartStyle the [ChartStyle] instance to provide.
 */
@Composable
public fun ProvideChartStyle(
    chartStyle: ChartStyle = LocalChartStyle.default,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalChartStyle provides chartStyle,
        content = content,
    )
}
