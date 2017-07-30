package com.commit451.gitlab.dialog

import android.content.Context
import android.support.v7.app.AppCompatDialog
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R

class HttpLoginDialog(context: Context, realm: String, loginListener: HttpLoginDialog.LoginListener) : AppCompatDialog(context) {

    @BindView(R.id.message_text) lateinit var textMessage: TextView
    @BindView(R.id.login_username) lateinit var textUsername: EditText
    @BindView(R.id.login_password) lateinit var textPassword: EditText
    @BindView(R.id.ok_button) lateinit var buttonOk: Button
    @BindView(R.id.cancel_button) lateinit var buttonCancel: Button

    init {
        setContentView(R.layout.dialog_http_login)
        ButterKnife.bind(this)

        textMessage.text = String.format(context.resources.getString(R.string.realm_message), realm)
        buttonOk.setOnClickListener {
            loginListener.onLogin(textUsername.text.toString(), textPassword.text.toString())
            this@HttpLoginDialog.dismiss()
        }
        buttonCancel.setOnClickListener {
            loginListener.onCancel()
            this@HttpLoginDialog.dismiss()
        }
        setTitle(R.string.login_activity)
    }

    interface LoginListener {
        fun onLogin(username: String, password: String)
        fun onCancel()
    }
}
