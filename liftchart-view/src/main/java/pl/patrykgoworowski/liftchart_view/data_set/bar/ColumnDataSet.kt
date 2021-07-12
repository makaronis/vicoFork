package pl.patrykgoworowski.liftchart_view.data_set.bar

import android.graphics.Canvas
import android.graphics.PointF
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.constants.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.data_set.bar.ColumnDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.emptyMultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel

class ColumnDataSet(
    columns: List<RectComponent>,
    spacing: Float = DEF_BAR_SPACING.dp,
    innerSpacing: Float = DEF_MERGED_BAR_INNER_SPACING.dp,
    mergeMode: MergeMode = MergeMode.Stack,
) : ColumnDataSetRenderer(columns, spacing, innerSpacing, mergeMode),
    DataSetRendererWithModel<MultiEntriesModel> {

    constructor(
        column: RectComponent,
        spacing: Float = DEF_BAR_SPACING.dp
    ) : this(listOf(column), spacing)

    private val listeners = ArrayList<UpdateRequestListener>()

    var model: MultiEntriesModel = emptyMultiEntriesModel()
        set(value) {
            field = value
            listeners.forEach { it() }
        }

    override fun draw(
        canvas: Canvas,
        pointF: PointF?,
        marker: Marker?,
    ) = draw(canvas, model, pointF, marker)

    override fun addListener(listener: UpdateRequestListener) {
        listeners += listener
    }

    override fun removeListener(listener: UpdateRequestListener) {
        listeners -= listener
    }

    override fun getMeasuredWidth(): Int = getMeasuredWidth(model)

    override fun getEntriesModel(): MultiEntriesModel = model

    override fun getSegmentProperties(): SegmentProperties = getSegmentProperties(model)

}