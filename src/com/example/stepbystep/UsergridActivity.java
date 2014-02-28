package com.example.stepbystep;

import android.app.Application;

import com.apigee.sdk.ApigeeClient;

	
	public class UsergridActivity  extends Application
	{
	        
	        private ApigeeClient apigeeClient;
	        
	        public UsergridActivity ()
	        {
	                this.apigeeClient = null;
	        }
	        
	        public ApigeeClient getApigeeClient()
	        {
	                return this.apigeeClient;
	        }
	        
	        public void setApigeeClient(ApigeeClient apigeeClient)
	        {
	                this.apigeeClient = apigeeClient;
	        }
	}			

