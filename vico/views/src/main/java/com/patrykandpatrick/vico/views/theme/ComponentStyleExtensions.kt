/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.theme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.OverlayingComponent
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.extension.copyColor
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.views.extension.defaultColors

internal fun TypedArray.getLineComponent(
    context: Context,
    defaultColor: Int = context.defaultColors.axisLineColor.toInt(),
    defaultThickness: Float = DefaultDimens.AXIS_LINE_WIDTH,
    defaultShape: Shape = Shapes.rectShape,
): LineComponent = use { array ->
    LineComponent(
        color = array.getColorExtended(
            index = R.styleable.LineComponent_color,
            defaultColor = defaultColor,
        ),
        thicknessDp = array.getRawDimension(
            context = context,
            index = R.styleable.LineComponent_thickness,
            defaultValue = defaultThickness,
        ),
        shape = if (hasValue(R.styleable.LineComponent_shapeStyle)) {
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.LineComponent_shapeStyle,
                styleableResourceId = R.styleable.Shape,
            ).getShape(
                context = context,
            )
        } else {
            defaultShape
        },
        strokeColor = array.getColorExtended(
            index = R.styleable.LineComponent_strokeColor,
            defaultColor = Color.TRANSPARENT,
        ),
        strokeWidthDp = array.getRawDimension(
            context = context,
            index = R.styleable.LineComponent_strokeWidth,
            defaultValue = 0f,
        ),
    )
}

internal fun TypedArray.getComponent(
    context: Context,
): Component? = use { array ->

    if (!hasValue(R.styleable.ComponentStyle_color)) {
        return@use null
    }

    val overlayingComponent = if (hasValue(R.styleable.ComponentStyle_overlayingComponentStyle)) {
        getNestedTypedArray(
            context = context,
            resourceId = R.styleable.ComponentStyle_overlayingComponentStyle,
            styleableResourceId = R.styleable.ComponentStyle,
        ).getComponent(context)
    } else {
        null
    }

    val baseComponent = ShapeComponent(
        color = array.getColorExtended(index = R.styleable.ComponentStyle_color),
        shape = getNestedTypedArray(
            context = context,
            resourceId = R.styleable.ComponentStyle_shapeStyle,
            styleableResourceId = R.styleable.Shape,
        ).getShape(context),
        strokeColor = array.getColorExtended(
            index = R.styleable.ComponentStyle_strokeColor,
            defaultColor = Color.TRANSPARENT,
        ),
        strokeWidthDp = array.getRawDimension(
            context = context,
            index = R.styleable.ComponentStyle_strokeWidth,
            defaultValue = 0f,
        ),
    )

    if (overlayingComponent != null) {
        OverlayingComponent(
            outer = baseComponent,
            inner = overlayingComponent,
            innerPaddingAllDp = getRawDimension(
                context = context,
                index = R.styleable.ComponentStyle_overlayingComponentPadding,
                defaultValue = 0f,
            ),
        )
    } else {
        baseComponent
    }
}

internal fun TypedArray.getLineSpec(
    context: Context,
    defaultColor: Int = context.defaultColors.entity1Color.toInt(),
): LineChart.LineSpec {
    val lineColor = getColorExtended(
        index = R.styleable.LineSpec_color,
        defaultColor = defaultColor,
    )

    val shader = if (
        hasValue(R.styleable.LineSpec_gradientTopColor) ||
        hasValue(R.styleable.LineSpec_gradientBottomColor)
    ) {
        val gradientTopColor = getColorExtended(R.styleable.LineSpec_gradientTopColor)
        val gradientBottomColor = getColorExtended(R.styleable.LineSpec_gradientBottomColor)

        DynamicShaders.verticalGradient(gradientTopColor, gradientBottomColor)
    } else {
        DynamicShaders.verticalGradient(
            lineColor.copyColor(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_START),
            lineColor.copyColor(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_END),
        )
    }

    return LineChart.LineSpec(
        lineColor = lineColor,
        point = getNestedTypedArray(
            context = context,
            resourceId = R.styleable.LineSpec_pointStyle,
            styleableResourceId = R.styleable.ComponentStyle,
        ).getComponent(context),
        pointSizeDp = getRawDimension(
            context = context,
            index = R.styleable.LineSpec_pointSize,
            defaultValue = DefaultDimens.POINT_SIZE,
        ),
        lineThicknessDp = getRawDimension(
            context = context,
            index = R.styleable.LineSpec_lineThickness,
            defaultValue = DefaultDimens.LINE_THICKNESS,
        ),
        lineBackgroundShader = shader,
        dataLabel = if (getBoolean(R.styleable.LineSpec_showDataLabels, false)) {
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.LineSpec_dataLabelStyle,
                styleableResourceId = R.styleable.TextComponentStyle,
            ).getTextComponent(context = context)
        } else {
            null
        },
        dataLabelVerticalPosition = getInteger(R.styleable.LineSpec_dataLabelVerticalPosition, 0).let { value ->
            val values = VerticalPosition.values()
            values[value % values.size]
        },
        dataLabelRotationDegrees = getFloat(
            R.styleable.LineSpec_dataLabelRotationDegrees,
            0f,
        ),
        pointConnector = DefaultPointConnector(
            cubicStrength = getFraction(
                index = R.styleable.LineSpec_cubicStrength,
                defaultValue = DefaultDimens.CUBIC_STRENGTH,
            ),
        ),
    )
}
