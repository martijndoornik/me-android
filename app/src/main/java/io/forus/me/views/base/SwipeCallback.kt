package io.forus.me.views.base

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import io.forus.me.R
import io.forus.me.helpers.DrawableHelper

abstract class SwipeCallback(context: Context,
                             direction: Int,
                             backgroundColor: Int,
                             iconRes: Int,
                             iconColor: Int = R.color.white) : ItemTouchHelper.SimpleCallback(0, direction) {
    private val background = ColorDrawable(ContextCompat.getColor(context, backgroundColor))
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private val icon = DrawableHelper.getTintedDrawable(context, iconRes, iconColor)!!

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun onChildDraw(
            c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder,
            dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the red delete background
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon

        val deleteIconTop = itemView.top + (itemHeight - icon.intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - icon.intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - icon.intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + icon.intrinsicHeight

        // Draw the delete icon
        icon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        icon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}