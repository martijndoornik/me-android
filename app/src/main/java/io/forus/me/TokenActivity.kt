package io.forus.me

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.*
import io.forus.me.entities.Asset
import io.forus.me.entities.Token
import io.forus.me.entities.Voucher
import io.forus.me.entities.base.WalletItem
import io.forus.me.helpers.JsonHelper
import io.forus.me.helpers.QrHelper
import io.forus.me.helpers.ThreadHelper
import io.forus.me.helpers.TransferViewModel
import io.forus.me.services.AssetService
import io.forus.me.services.TokenService
import io.forus.me.services.VoucherService
import java.util.concurrent.Callable

/**
 * Created by martijn.doornik on 30/03/2018.
 */
class TokenActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    lateinit var item: WalletItem
    private lateinit var amountField: TextView
    private lateinit var descriptionField: TextView

    fun goBack(view:View?) {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RETRIEVE_REQUEST -> {
                when (resultCode) {
                    //OK_RESULT -> {}
                    //CANCEL_RESULT -> {}
                }
            }
            SendWalletItemActivity.RequestCode.SEND_REQUEST -> {
                when (resultCode) {
                    SendWalletItemActivity.ResultCode.SUCCESS_RESULT -> {
                        val address = intent!!.extras.getString(SendWalletItemActivity.RequestCode.RECIPIENT)
                        val walletItem = JsonHelper.toWalletItem(intent!!.extras.getString(SendWalletItemActivity.RequestCode.TRANSFER_OBJECT))
                        if (walletItem != null) {
                            // TODO Handle scanned result
                            Toast.makeText(this.baseContext, "//TODO Success!", Toast.LENGTH_LONG).show()
                        }
                    }
                    SendWalletItemActivity.ResultCode.CANCEL_RESULT -> {
                        this.onSendCanceled()
                    }
                }
            }
        }
    }

    fun onChange(walletItem: WalletItem) {
        this.item = walletItem
        refreshToolbar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.loading)
        try {
            if (!intent.hasExtra(RequestCode.TOKEN)) {
                throw NullPointerException()
            }
            item = JsonHelper.toWalletItem(intent.getStringExtra(RequestCode.TOKEN)) ?: throw NullPointerException()
            ThreadHelper.await(Callable{
                when (item) {
                    is Asset -> AssetService().getLiveItem(item.address).observe(this, LiveWalletItemObserver<Asset>(this))
                    is Token -> TokenService().getLiveItem(item.address).observe(this, LiveWalletItemObserver<Token>(this))
                    is Voucher -> VoucherService().getLiveItem(item.address).observe(this, LiveWalletItemObserver<Voucher>(this))
                }
            })
            setContentView(R.layout.activity_token)

            val toolbar: Toolbar = findViewById(R.id.toolbar)
            toolbar.title = item.name

            val optionsButton: ImageView = findViewById(R.id.optionsButton)
            optionsButton.setOnClickListener {
                val menu = PopupMenu(baseContext, optionsButton)
                menu.setOnMenuItemClickListener(this)
                menu.inflate(R.menu.wallet_item_options)
                menu.show()
            }
            val qrView:ImageView = findViewById(R.id.qrView)
            val task = @SuppressLint("StaticFieldLeak")
            object : AsyncTask<Any?, Any?, Bitmap?>() {
                override fun doInBackground(vararg params: Any?): Bitmap? {
                    return QrHelper.getQrBitmap(
                            baseContext,
                            JsonHelper.fromWalletItem(item),
                            QrHelper.Sizes.LARGE,
                            ContextCompat.getColor(baseContext, R.color.black),
                            ContextCompat.getColor(baseContext, R.color.transparent))
                }

                override fun onPostExecute(result: Bitmap?) {
                    super.onPostExecute(result)
                    if (result != null) {
                        qrView.setImageBitmap(result)
                    }
                }

            }
            task.execute()
        } catch (e: NullPointerException) {
            setResult(1)
            finish()
        }
    }
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {
                R.id.sync -> {
                    this.item.sync()
                    return true
                }
                R.id.delete -> {
                    when (this.item) {
                        is Token -> TokenService().delete(this.item as Token)
                    }
                    return true
                }
            }

        }
        return false
    }

    private fun onRequestPressed() {
        if (this::item.isInitialized) {
            val description = descriptionField.text.toString()
            val amount = amountField.text.toString().toFloat()
            val transfer = TransferViewModel(this.item, description, amount)
            val json = transfer.toJson().toString()
            val intent = Intent(this, RequestWalletItemActivity::class.java)
            intent.putExtra("data", json)
            startActivityForResult(intent, TokenActivity.RETRIEVE_REQUEST)
        }
    }

    fun onSendCanceled() {
     //   (this.pager.getChildAt(WalletItemDetailPager.SEND_PAGE) as WalletItemSendFragment).onCancel()

    }

    private fun refreshToolbar() {
        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        if (toolbar != null) {
            toolbar.title = item.name
            toolbar.subtitle = item.amount
        }

    }

    private fun setupDetailFragment() {
        this.amountField = findViewById(R.id.amountField)
        this.descriptionField = findViewById(R.id.descriptionField)
        val nextButton: Button = findViewById(R.id.scanButton)
        nextButton.setOnClickListener({
            onRequestPressed()
        })
    }

    private inner class LiveWalletItemObserver<ITEM: WalletItem>(private val listener: TokenActivity): Observer<ITEM> {
        override fun onChanged(t: ITEM?) {
            if (t != null) {
                listener.onChange(t)
            }
        }

    }

    companion object {
        val RETRIEVE_REQUEST: Int = 2
    }

    class RequestCode {
        companion object {
            const val TOKEN = "walletItem"
        }
    }
}