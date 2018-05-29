package io.forus.me

import android.content.DialogInterface
import android.content.Intent
import android.opengl.Visibility
import android.os.AsyncTask
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import io.forus.me.entities.Identity
import io.forus.me.helpers.DialogHelper
import io.forus.me.helpers.QrHelper
import io.forus.me.helpers.ThreadHelper
import io.forus.me.services.IdentityService
import io.forus.me.services.Web3Service
import kotlinx.android.synthetic.main.activity_identity_view.*
import java.lang.ref.WeakReference

class IdentityViewActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener, View.OnClickListener {

    lateinit var identityView: ConstraintLayout
    lateinit var popupView: ConstraintLayout
    private val isIdentitySelectionVisible: Boolean
            get() = this::popupView.isInitialized && this.popupView.visibility == View.VISIBLE

    fun addIdentitySelected() {
        val intent = Intent(this, CreateIdentityActivity::class.java)
        startActivityForResult(intent, CreateIdentityActivity.RequestCode.REQUEST_IDENTITY)
    }

    fun hideIdentitySelection() {
        if (this::popupView.isInitialized)
            popupView.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CreateIdentityActivity.RequestCode.REQUEST_IDENTITY) {
            if (resultCode == CreateIdentityActivity.ResultCode.CREATED) {
                if (data != null && data.hasExtra(CreateIdentityActivity.Result.NAME)) {
                    setIdentityName(data.getStringExtra(CreateIdentityActivity.Result.NAME))
                    hideIdentitySelection()
                }
            } else {
                // do nothing?
            }
        }
    }

    override fun onBackPressed() {
        if (isIdentitySelectionVisible) {
            hideIdentitySelection()
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id ) {
                    R.id.buttonWrapper -> {
                        // TODO perfect the responsiveness to buttons (like pressing back and such)
                        IdentitySpinnerTask(this, popup).execute()
                    }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setOnMenuItemClickListener(this)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        ThreadHelper.dispense(ThreadHelper.WEB3_THREAD).postTask(Runnable {
            val qrView:ImageView = findViewById(R.id.qrView)
            if (Web3Service.account != null) {
                runOnUiThread({
                    qrView.setImageBitmap(QrHelper.
                            getQrBitmap(
                                    this,
                                    Web3Service.account!!,
                                    QrHelper.Sizes.LARGE,
                                    ContextCompat.getColor(baseContext, R.color.black),
                                    ContextCompat.getColor(baseContext, R.color.white)
                            )
                    )
                })
            }
        })
        popupView = findViewById(R.id.popup)
        val button: ConstraintLayout = findViewById(R.id.buttonWrapper)
        button.setOnClickListener(this)
        identityView = button
        setIdentityName(IdentityService.currentIdentity?.name?:"Loading")

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_identity_view, menu)
        return true
    }

    fun onIdentitySelect(selected:Identity?) {
        if (selected != null) {
            // Select a known identity as current one.
            IdentityService.setCurrentIdentity(this, selected)
            setIdentityName(selected.name)
            this.hideIdentitySelection()
        } else {
            // add a new identity

        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item != null) {
            if (isIdentitySelectionVisible) {
                hideIdentitySelection()
            } else {
                // Currently, there is only one menu option and a back button,
                // which will do the same thing.
                this.finish()
            }
            return true
        }
        return false
    }

    fun showIdentitySelection() {
        if (this::popupView.isInitialized)
            popupView.visibility = View.VISIBLE
    }

    private fun setIdentityName(name: String) {
        identityView.findViewById<TextView>(R.id.nameView)?.text = name
    }

    private class IdentitySpinnerTask(a: IdentityViewActivity, view: View): AsyncTask<Any?, Any?, List<Identity>>() {
        private val activity = WeakReference(a)
        private val view = WeakReference(view)

        override fun doInBackground(vararg params: Any?): List<Identity> {
            return IdentityService().getList()
        }

        override fun onPostExecute(identities: List<Identity>?) {
            super.onPostExecute(identities)
            if (activity.get() != null && view.get() != null && identities != null) {
                val activity = this.activity.get()!!
                val view = this.view.get()!!
                val addButton: ConstraintLayout = view.findViewById(R.id.addButton)
                val list: RecyclerView = view.findViewById(R.id.identityList)
                addButton.setOnClickListener {
                    activity.addIdentitySelected()
                }
                list.layoutManager = LinearLayoutManager(activity)
                list.adapter = IdentityAdapter(identities, object: IdentityAdapter.IdentityClickListener {
                    override fun select(identity: Identity) {
                        activity.onIdentitySelect(identity)
                    }
                })

                activity.showIdentitySelection()
            }
        }

        class IdentityAdapter(private val list: List<Identity>, val listener: IdentityClickListener): RecyclerView.Adapter<IdentityAdapter.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.popup_identity_select_list_item, parent, false)
                return ViewHolder(view)
            }

            override fun getItemCount(): Int {
                return list.size
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                holder.nameView.text = list[holder.adapterPosition].name
                // TODO Bind other values of an identity
                holder.view.setOnClickListener({
                    listener.select(list[holder.adapterPosition])
                })
            }


            class ViewHolder(val view:View): RecyclerView.ViewHolder(view) {
                val imageView: ImageView = view.findViewById(R.id.accountImage)
                val nameView: TextView = view.findViewById(R.id.nameView)
                val typeView: TextView = view.findViewById(R.id.typeView)
            }

            interface IdentityClickListener {
                fun select(identity: Identity)
            }
        }
    }

}
