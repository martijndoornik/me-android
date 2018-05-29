package io.forus.me

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import io.forus.me.entities.Voucher
import io.forus.me.helpers.JsonHelper
import io.forus.me.helpers.QrHelper

import kotlinx.android.synthetic.main.activity_voucher.*

class VoucherActivity : AppCompatActivity() {
    private lateinit var voucher: Voucher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!intent.hasExtra(RequestCode.VOUCHER)) {
            throw NullPointerException("Voucher cannot be null in VoucherActivity.onCreate")
        }
        val walletItem = JsonHelper.toWalletItem(intent.getStringExtra(RequestCode.VOUCHER))
        if (walletItem != null && walletItem is Voucher) this.voucher = walletItem
        else throw NullPointerException("Invalid voucher in VoucherActivity.onCreate")
        setContentView(R.layout.activity_voucher)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.title = voucher.name
        }
        val nameView: TextView = findViewById(R.id.nameView)
        val valueView: TextView = findViewById(R.id.valueView)
        val expirationView: TextView = findViewById(R.id.expirationView)
        val qrView = findViewById<ImageView>(R.id.qrView)

        nameView.text = voucher.name
        val task = @SuppressLint("StaticFieldLeak")
        object: AsyncTask<Any?, Any?, Bitmap?>() {
            override fun doInBackground(vararg params: Any?): Bitmap? {
                return QrHelper.getQrBitmap(baseContext,
                        voucher.toJson(),
                        QrHelper.Sizes.LARGE,
                        ContextCompat.getColor(baseContext,R.color.black),
                        ContextCompat.getColor(baseContext,R.color.transparent)
                        )
            }

            override fun onPostExecute(result: Bitmap?) {
                super.onPostExecute(result)
                if (result != null) {
                    qrView.setImageBitmap(result)
                }
            }
        }
        task.execute()
    }

    interface RequestCode {
        companion object {
            const val VOUCHER = "voucher"
        }
    }

}
