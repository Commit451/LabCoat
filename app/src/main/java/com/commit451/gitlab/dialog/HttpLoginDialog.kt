package com.commit451.gitlab.dialog

import android.content.Context
import androidx.appcompat.app.AppCompatDialog
import com.commit451.gitlab.R
import kotlinx.android.synthetic.main.dialog_http_login.*

class HttpLoginDialog(context: Context, realm: String, loginListener: LoginListener) : AppCompatDialog(context) {

    init {
        setContentView(R.layout.dialog_http_login)

        textMessage.text = String.format(context.resources.getString(R.string.realm_message), realm)
        buttonOk.setOnClickListener {
            loginListener.onLogin(textUsername.text.toString(), textPassword.text.toString())
            dismiss()
        }
        buttonCancel.setOnClickListener {
            loginListener.onCancel()
            dismiss()
        }
        setTitle(R.string.login_activity)
    }

    interface LoginListener {
        fun onLogin(username: String, password: String)
        fun onCancel()
    }
}
