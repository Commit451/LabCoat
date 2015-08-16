package com.commit451.gitlab;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.Session;
import com.commit451.gitlab.tools.Prefs;

import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class LoginActivity extends BaseActivity {
	
	@Bind(R.id.url_input) TextView urlInput;
	@Bind(R.id.user_input) TextView userInput;
	@Bind(R.id.password_input) TextView passwordInput;
	@Bind(R.id.token_input) TextView tokenInput;
	@Bind(R.id.normal_login) View normalLogin;
	@Bind(R.id.token_login) View tokenLogin;
	@Bind(R.id.progress) View progress;
	
	private boolean isNormalLogin = true;

	private final TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			onLoginClick();
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);
		passwordInput.setOnEditorActionListener(onEditorActionListener);
		tokenInput.setOnEditorActionListener(onEditorActionListener);
	}
	
	@OnClick(R.id.show_normal_link)
	public void showNormalLogin() {
		if (normalLogin.getVisibility() == View.VISIBLE) {
			normalLogin.setVisibility(View.GONE);
			tokenLogin.setVisibility(View.VISIBLE);
			isNormalLogin = false;
		} else {
			normalLogin.setVisibility(View.VISIBLE);
			tokenLogin.setVisibility(View.GONE);
			isNormalLogin = true;
		}
	}
	
	@OnClick(R.id.login_button)
	public void onLoginClick() {
		GitLabClient.reset();
		
		String url = urlInput.getText().toString();
		
		if(url.length() == 0) {
			Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_SHORT)
					.show();
			return;
		}
        else if(url.startsWith("http://") && url.endsWith(".git"))
            urlInput.setText(url.substring(0, nthOccurrence(url, '/', 2)));
        else if(url.startsWith("git@") && url.endsWith(".git"))
            urlInput.setText("http://" + url.substring(4, url.indexOf(':')));
        else if(!url.startsWith("http://") && !url.startsWith("https://"))
            urlInput.setText("http://" + urlInput.getText().toString());

		if(isNormalLogin) {
			connect(true);
		}
		else {
			connect(false);
		}
	}

    public static int nthOccurrence(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos + 1);
        return pos;
    }

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
	private void connect(boolean byAuth) {
        progress.setVisibility(View.VISIBLE);
        progress.setAlpha(0.0f);
        progress.animate().alpha(1.0f);
		Prefs.setServerUrl(this, "");
		Prefs.setPrivateToken(this, "");
		Prefs.setLoggedIn(this, false);

		String serverURL = urlInput.getText().toString();
		
		Prefs.setServerUrl(this, serverURL);
		
		if(byAuth)
			connectByAuth();
		else
			connectByToken();
	}
	
	private void connectByAuth() {
		if(userInput.getText().toString().contains("@"))
			GitLabClient.instance().getSessionByEmail(userInput.getText().toString(), passwordInput.getText().toString(), "", sessionCallback);
		else
			GitLabClient.instance().getSessionByUsername(userInput.getText().toString(), passwordInput.getText().toString(), "", sessionCallback);
	}
	
	private Callback<Session> sessionCallback = new Callback<Session>() {
		
		@Override
		public void success(Session session, Response resp) {
			progress.setVisibility(View.GONE);
			
			Prefs.setLoggedIn(LoginActivity.this, true);
			Prefs.setPrivateToken(LoginActivity.this, session.getPrivateToken());
			
			Intent i = new Intent(LoginActivity.this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
		
		@Override
		public void failure(RetrofitError e) {
            handleConnectionError(e, true);
		}
	};
	
	private void connectByToken() {
		Prefs.setPrivateToken(this, tokenInput.getText().toString());
		GitLabClient.instance().getProjects(tokenCallback);
	}
	
	private Callback<List<Project>> tokenCallback = new Callback<List<Project>>() {
		
		@Override
		public void success(List<Project> projects, Response resp) {
            progress.setVisibility(View.GONE);

			Prefs.setLoggedIn(LoginActivity.this, true);
			
			Intent i = new Intent(LoginActivity.this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
		
		@Override
		public void failure(RetrofitError e) {
			handleConnectionError(e, false);
		}
	};

    private void handleConnectionError(RetrofitError e, boolean auth) {
        Timber.e(e.toString());

        progress.setVisibility(View.GONE);

        if(e.getCause() instanceof SSLHandshakeException) {
            Dialog d = new AlertDialog.Builder(this)
                    .setTitle(R.string.certificate_title)
                    .setMessage(R.string.certificate_message)
                    .setNeutralButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

            ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }
        else if(e.getResponse() != null && (e.getResponse().getStatus() == 301 || e.getResponse().getStatus() == 405) && !urlInput.getText().toString().contains("https://")) {
            urlInput.setText(urlInput.getText().toString().replace("http://", "https://"));
            connect(auth);
        }
        else {
			Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_SHORT)
					.show();
		}
    }
}
