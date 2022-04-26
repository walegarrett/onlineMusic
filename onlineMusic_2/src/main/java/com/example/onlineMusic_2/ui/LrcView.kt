package com.example.onlineMusic_2.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.bean.LrcRow
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList


/**
 * liteplayer by loader
 * 显示lrc歌词控件
 */
class LrcView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    var mLrcLines: MutableList<LrcRow> = ArrayList()
    private var mNextTime = 0 // 保存下一句开始的时间
    private var mLrcHeight // lrc界面的高度
            = 0
    private var mRows // 多少行
            = 0
    private var mCurrentLine = 0 // 当前行
    private var mOffsetY // y上的偏移
            = 0
    private var mMaxScroll // 最大滑动距离=一行歌词高度+歌词间距
            = 0
    private var mCurrentXOffset = 0
    private var mDividerHeight // 行间距
            = 0f
    private var mTextBounds: Rect? = null
    private var mNormalPaint // 常规的字体
            : Paint? = null
    private var mCurrentPaint // 当前歌词的大小
            : Paint? = null
    private var mBackground: Bitmap? = null
    private val mScroller: Scroller

    // 初始化操作
    private fun init(attrs: AttributeSet) {
        // <begin>
        // 解析自定义属性
        val ta = context.obtainStyledAttributes(attrs, R.styleable.Lrc)
        val textSize = ta.getDimension(R.styleable.Lrc_android_textSize, 10.0f)
        mRows = ta.getInteger(R.styleable.Lrc_rows, 0)
        mDividerHeight = ta.getDimension(R.styleable.Lrc_dividerHeight, 0.0f)
        val normalTextColor = ta.getColor(R.styleable.Lrc_normalTextColor, -0x1)
        val currentTextColor = ta.getColor(R.styleable.Lrc_currentTextColor, -0xff0022)
        ta.recycle()
        // </end>
        if (mRows != 0) {
            // 计算lrc面板的高度
            mLrcHeight = (textSize + mDividerHeight).toInt() * mRows + 5
        }
        mNormalPaint = Paint()
        mCurrentPaint = Paint()

        // 初始化paint
        mNormalPaint!!.textSize = textSize
        mNormalPaint!!.color = normalTextColor
        mNormalPaint!!.isAntiAlias = true
        mCurrentPaint!!.textSize = textSize
        mCurrentPaint!!.color = currentTextColor
        mCurrentPaint!!.isAntiAlias = true
        mTextBounds = Rect()
        mCurrentPaint!!.getTextBounds(
            DEFAULT_TEXT,
            0,
            DEFAULT_TEXT.length,
            mTextBounds
        )
        computeMaxScroll()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 如果没有设置固定行数， 则默认测量高度，并根据高度计算行数
        var widthMeasureSpec = widthMeasureSpec
        if (mRows == 0) {
            var width = paddingLeft + paddingRight
            var height = paddingTop + paddingBottom
            width = Math.max(width, suggestedMinimumWidth)
            height = Math.max(height, suggestedMinimumHeight)
            widthMeasureSpec =
                MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
            setMeasuredDimension(
                resolveSizeAndState(width, widthMeasureSpec, 0),
                resolveSizeAndState(height, heightMeasureSpec, 0)
            )
            mLrcHeight = measuredHeight
            computeRows()
            return
        }

        // 设置了固定行数，重新设置view的高度
        val measuredHeightSpec = MeasureSpec.makeMeasureSpec(mLrcHeight, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, measuredHeightSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mBackground != null) {
            mBackground =
                Bitmap.createScaledBitmap(mBackground!!, measuredWidth, mLrcHeight, true)
        }
    }

    /**
     * 根据高度计算行数
     */
    private fun computeRows() {
        val lineHeight = mTextBounds!!.height() + mDividerHeight
        mRows = (measuredHeight / lineHeight).toInt()
    }

    /**
     * 计算滚动距离
     */
    private fun computeMaxScroll() {
        mMaxScroll = (mTextBounds!!.height() + mDividerHeight).toInt()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val width = measuredWidth
        if (mBackground != null) {
            canvas.drawBitmap(mBackground!!, Matrix(), null)
        }

//		 float centerY = (getMeasuredHeight() + mTextBounds.height() - mDividerHeight) / 2;
        val centerY = (measuredHeight + mTextBounds!!.height()) / 2.toFloat()
        if (mLrcLines!!.isEmpty()) {
            canvas.drawText(
                DEFAULT_TEXT,
                (width - mCurrentPaint!!.measureText(DEFAULT_TEXT)) / 2,
                centerY, mCurrentPaint!!
            )
            return
        }
        val offsetY = mTextBounds!!.height() + mDividerHeight
        drawCurrentLine(canvas, width, centerY - mOffsetY)
        var firstLine = mCurrentLine - mRows / 2
        firstLine = if (firstLine <= 0) 0 else firstLine
        var lastLine = mCurrentLine + mRows / 2 + 2
        lastLine = if (lastLine >= mLrcLines.size - 1) mLrcLines.size - 1 else lastLine

        // 画当前行上面的
        run {
            var i = mCurrentLine - 1
            var j = 1
            while (i >= firstLine) {
                val lrc = mLrcLines!![i].content
                val x = (width - mNormalPaint!!.measureText(lrc)) / 2
                canvas.drawText(lrc!!, x, centerY - j * offsetY - mOffsetY, mNormalPaint!!)
                i--
                j++
            }
        }

        // 画当前行下面的
        var i = mCurrentLine + 1
        var j = 1
        while (i <= lastLine) {
            val lrc = mLrcLines[i].content
            val x = (width - mNormalPaint!!.measureText(lrc)) / 2
            canvas.drawText(lrc!!, x, centerY + j * offsetY - mOffsetY, mNormalPaint!!)
            i++
            j++
        }
    }

    private fun drawCurrentLine(
        canvas: Canvas,
        width: Int,
        y: Float
    ) {
        mHandler.removeMessages(1)
        val currentLrc = mLrcLines!![mCurrentLine].content
        val contentWidth = mCurrentPaint!!.measureText(currentLrc)
        if (contentWidth > width) {
            canvas.drawText(currentLrc!!, mCurrentXOffset.toFloat(), y, mCurrentPaint!!)
            if (contentWidth - Math.abs(mCurrentXOffset) < width) {
                mCurrentXOffset = 0
            } else {
                mHandler.sendEmptyMessageDelayed(
                    HORIZONTAL_MSG_WHAT,
                    HORIZONTAL_TIME.toLong()
                )
                //				mHandler.sendEmptyMessage(1);
            }
        } else {
            val currentX = (width - mCurrentPaint!!.measureText(currentLrc)) / 2
            // 画当前行
            canvas.drawText(currentLrc!!, currentX, y, mCurrentPaint!!)
        }
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mOffsetY = mScroller.currY
            if (mScroller.isFinished) {
                val cur = mScroller.currX
                mCurrentLine = if (cur <= 1) 0 else cur - 1
                mOffsetY = 0
            }
            postInvalidate()
        }
    }


    /**
     * 在音乐播放回调里调用
     * @param time 当前播放时间
     */
    @Synchronized
    fun onProgress(time: Int?) {
        // 如果当前时间小于下一句开始的时间
        // 直接return
        if (mNextTime > time!!) {
            return
        }

        // 每次进来都遍历存放的时间
        val size = mLrcLines.size
        for (i in 0 until size) {
            // 解决最后一行歌词不能高亮的问题
            if (mNextTime == mLrcLines[size - 1].time) {
                mHandler.removeMessages(1)
                mNextTime += 60 * 1000
                mScroller.abortAnimation()
                mScroller.startScroll(size, 0, 0, mMaxScroll, SCROLL_TIME)
                postInvalidate()
                break
            }

            // 发现这个时间大于传进来的时间
            // 那么现在就应该显示这个时间前面的对应的那一行
            // 每次都重新显示，是不是要判断：现在正在显示就不刷新了
            if (mLrcLines[i].time > time) {
                mNextTime = mLrcLines[i].time
                if (mCurrentLine == 0 && i == 1) {
                    postInvalidate()
                    break
                }
                mScroller.abortAnimation()
                mScroller.startScroll(i, 0, 0, mMaxScroll, SCROLL_TIME)
                postInvalidate()
                break
            }
        }
    }

    /**
     * 拖动进度条时调用，用来改变歌词位置
     * @param progress
     */
    fun onDrag(progress: Int) {
        val lineCount = mLrcLines.size
        for (i in 0 until lineCount) {
            if (mLrcLines[i].time > progress) {
                mNextTime = if (i == 0) 0 else mLrcLines[i - 1].time
                return
            }
        }
        if (!mLrcLines.isEmpty()) {
            mNextTime = mLrcLines[mLrcLines.size - 1].time
        }
    }



    fun reset() {
        mLrcLines.clear()
        mCurrentLine = 0
        mNextTime = 0
        mOffsetY = 0
        postInvalidate()
    }

    /**
     * 是否设置了歌词
     * @return
     */
    fun hasLrc(): Boolean {
        return !mLrcLines.isEmpty()
    }
    /**
     * 设置歌词
     */
    fun setLrc(lrcList:ArrayList<LrcRow>){
        reset()
        mLrcLines=lrcList
    }

    /**
     * 设置背景图片
     * @param bmp
     */
    fun setBackground(bmp: Bitmap?) {
        mBackground = bmp
    }

    private val mHandler = MarqueeHandler(this)

    private class MarqueeHandler internal constructor(view: LrcView?) : Handler() {
        private val mLrcViewRef: WeakReference<LrcView?>
        override fun handleMessage(msg: Message) {
            if (msg.what == HORIZONTAL_MSG_WHAT && mLrcViewRef.get() != null) {
                mLrcViewRef.get()!!.mCurrentXOffset -= HORIZONTAL_OFFSET
                mLrcViewRef.get()!!.invalidate()
                //				sendEmptyMessageDelayed(1, 500);
            }
        }

        init {
            mLrcViewRef = WeakReference(view)
        }
    }

    companion object {
        private const val HORIZONTAL_MSG_WHAT = -125
        private const val HORIZONTAL_TIME = 200
        private const val SCROLL_TIME = 500
        private const val HORIZONTAL_OFFSET = 2
        private const val DEFAULT_TEXT = "暂无歌词"
    }

    init {
        mScroller = Scroller(context, LinearInterpolator())
        init(attrs)
    }
}
