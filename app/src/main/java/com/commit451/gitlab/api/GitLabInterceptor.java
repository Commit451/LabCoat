package com.commit451.gitlab.api;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.tools.Prefs;

import retrofit.RequestInterceptor;

public class GitLabInterceptor implements RequestInterceptor {
	
	@Override
	public void intercept(RequestFacade req) {
		req.addHeader("PRIVATE-TOKEN", Prefs.getPrivateToken(GitLabApp.instance()));
	}
}
