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

package com.patrykandpatrick.vico.core.component

import com.patrykandpatrick.vico.core.component.dimension.Margins
import com.patrykandpatrick.vico.core.context.DrawContext

/**
 * [Component] is a generic concept of an object that can be drawn on a canvas at a given pair of coordinates.
 * Its subclasses are used throughout the library.
 */
public interface Component : Margins {

    /**
     * Instructs the [Component] to draw itself at the given coordinates.
     */
    public fun draw(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    )
}
