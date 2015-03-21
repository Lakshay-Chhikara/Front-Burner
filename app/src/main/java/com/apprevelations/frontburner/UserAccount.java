/*
 * Copyright 2014 IBM Corp. All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apprevelations.frontburner;

public class UserAccount {

	private static String idToken = null;
	private static String userName = null;
	private static String userEmail = null;
	private static String userPicture = null;
	private static String accessToken = null;
	
	public static String getPicture() {
		return userPicture;
	}
	public static void setPicture(String userPicture) {
		UserAccount.userPicture = userPicture;
	}
	public static String getIdToken() {
		return idToken;
	}
	public static void setIdToken(String idToken) {
		UserAccount.idToken = idToken;
	}
	public static String getName() {
		return userName;
	}
	public static void setName(String userName) {
		UserAccount.userName = userName;
	}
	public static String getEmail() {
		return userEmail;
	}
	public static void setEmail(String userEmail) {
		UserAccount.userEmail = userEmail;
	}
	public static String getAccessToken() {
		return accessToken;
	}
	public static void setAccessToken(String accessToken) {
		UserAccount.accessToken = accessToken;
	}
	
	
	

}
