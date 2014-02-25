package com.example.stepbystep;

import android.app.Application;

import com.apigee.sdk.ApigeeClient;

public class ApigeeActivity extends Application{
	
	private ApigeeClient apigeeClient;
	
	 public ApigeeActivity()
     {
             this.apigeeClient = null;
     }

	public ApigeeClient getApigeeClient() {
		return apigeeClient;
	}

	public void setApigeeClient(ApigeeClient apigeeClient) {
		this.apigeeClient = apigeeClient;
	}

}
