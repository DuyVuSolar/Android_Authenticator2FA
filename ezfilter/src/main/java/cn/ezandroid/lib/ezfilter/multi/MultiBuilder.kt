package cn.ezandroid.lib.ezfilter.multi

import cn.ezandroid.lib.ezfilter.EZFilter
import cn.ezandroid.lib.ezfilter.core.FBORender
import cn.ezandroid.lib.ezfilter.core.FilterRender
import cn.ezandroid.lib.ezfilter.core.environment.IFitView
import cn.ezandroid.lib.ezfilter.extra.IAdjustable

/**
 * 多输入源构造器
 *
 *
 * 支持将多个输入源，比如两个视频、一个视频一个图片等，组合到一个界面中进行显示，支持对各输入源分别添加滤镜
 *
 * @author like
 * @date 2018-07-13
 */
class MultiBuilder(
    private val mBuilders: List<EZFilter.Builder>,
    private val mMultiInput: MultiInput
) : EZFilter.Builder() {
    override fun getStartPointRender(view: IFitView?): FBORender? {
        mMultiInput.clearRegisteredFilters()
        for (builder in mBuilders) {
            val render = builder.getStartPointRender(view)
            mMultiInput.registerFilter(render)
        }
        return mMultiInput
    }

    override fun getAspectRatio(view: IFitView?): Float {
        return mMultiInput.width * 1.0f / mMultiInput.height
    }

    public override fun addFilter(filterRender: FilterRender?): MultiBuilder? {
        return super.addFilter(filterRender) as MultiBuilder?
    }

    public override fun <T> addFilter(
        filterRender: T?,
        progress: Float
    ): MultiBuilder? where T : FilterRender?, T : IAdjustable? {
        return super.addFilter<T>(filterRender, progress) as MultiBuilder?
    }

    public override fun enableRecord(
        outputPath: String?,
        recordVideo: Boolean,
        recordAudio: Boolean
    ): MultiBuilder? {
        return super.enableRecord(outputPath, recordVideo, recordAudio) as MultiBuilder?
    }
}
