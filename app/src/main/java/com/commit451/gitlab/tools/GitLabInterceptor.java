package com.commit451.gitlab.tools;

import retrofit.RequestInterceptor;

public class GitLabInterceptor implements RequestInterceptor {
	
	@Override
	public void intercept(RequestFacade req) {
		req.addHeader("PRIVATE-TOKEN", Repository.getPrivateToken());
	}
}
