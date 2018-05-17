package io.forus.me.helpers

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * A class to aid in drawable functions that have been added in
 * Android Lollipop. This way, you can get coloured icons like
 * the setTint method, but then compatible with Android pre-Lollipop
 */
class DrawableHelper {
    companion object {
        fun getTintedDrawable(context: Context, drawableRes: Int, colorRes: Int): Drawable? {
            var ret = ContextCompat.getDrawable(context, drawableRes)
            if (ret != null) {
                // Wrap, because all drawables are supposed to be linked, thus
                // changing the colour of one will change them all
                ret = DrawableCompat.wrap(ret)
                ret.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN)
            }
            return ret
        }
    }
}