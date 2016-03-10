package com.favre.ppe.twitter.authentification.java;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.*;
import org.apache.commons.lang3.StringEscapeUtils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.sun.org.apache.xml.internal.utils.URI;

import java.util.Date;


public class TwitterAuthHeader {
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	
	//Declaration ours token
	static String oauthConsumerKey = "DoZ2bSvA5pAniS8kDxJJ8KGsu";
	static String oauthConsumerSecret = "fDQon1kUoffYBzls52iOGeeSUKY6USBcSN0CMbLjCk4CGc7djs";
	static String oauthToken = "706818055054233600-axPLau20pgEjl752BmJRsVx0Fzaa6ON";
	static String oauthTokenSecret = "TLQ5uK4KK2ZTWnpMA1I8lA6m5IyuoPdCiJWBiB16wNKpz";
	static String oauthVersion = "1.0";
	static String oauthSignatureMethod = "HMAC-SHA1";
	
	public String getTwitterAuthHeader() throws URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, IOException {
			Locale local = new Locale("fr", "FR");
		
		// Creation oauth_nonce
		DateFormat df = new SimpleDateFormat(DATE_FORMAT_NOW);
		Date today = Calendar.getInstance().getTime();		
		String reportDate = df.format(today);
		byte[] reportDateByte = reportDate.getBytes(StandardCharsets.UTF_8);

		String oauthNonce = Base64.encode(reportDateByte);
		oauthNonce = oauthNonce.toString();
		Random ran = new Random();
		long n = ran.nextLong();
		Long no = Math.abs(new Long(n));
		
		// Creation oauth_timestamp
		long timestamp = System.currentTimeMillis();
		timestamp = timestamp/1000;		
		String oauthTimeStamp = Long.toString(timestamp);
		
		//Creation url user_timeline
		String resourceUrl = "https://api.twitter.com/1.1/statuses/user_timeline.json";

		// Generation an Encypted oauth_signature
		String baseFormat = "oauth_consumer_key=%s&oauth_nonce=%s&oauth_signature_method=%s"
							+"&oauth_timestamp=%s&oauth_token=%s&oauth_version=%s";		
		String tempBaseString = String.format(baseFormat, oauthConsumerKey, oauthNonce, 
											oauthSignatureMethod, oauthTimeStamp, oauthToken, oauthVersion);
		
		String baseStringTemp = "";
		String baseString = baseStringTemp.concat("POST&")
				.concat(StringEscapeUtils.escapeJava(resourceUrl))
				.concat("&")
				.concat(StringEscapeUtils.escapeJava(tempBaseString));
		
		//
		String finalConsumerSecret = StringEscapeUtils.escapeJava(oauthConsumerSecret);

		String finalTokenSecret = StringEscapeUtils.escapeJava(oauthTokenSecret);
		String finalConsumer = StringEscapeUtils.escapeJava(oauthConsumerKey);
		
		String compositeKeyTemp = "";
		String compositeKey = compositeKeyTemp.concat(finalConsumerSecret).concat(finalTokenSecret);
		
		String oauthSignature;
		
		SecretKeySpec signinKey = new SecretKeySpec(compositeKey.getBytes(), "HmacSHA1");
		Mac m = Mac.getInstance(signinKey.getAlgorithm());
        m.init(signinKey);
		byte[] signature = m.doFinal(baseString.getBytes());
		oauthSignature = Base64.encode(signature).toString();
		
		// Generate Authentification header
        String finalNonce = StringEscapeUtils.escapeJava(oauthNonce);
        String finalSignatureMethod = StringEscapeUtils.escapeJava(oauthSignatureMethod);
        String finalSignature = StringEscapeUtils.escapeJava(oauthSignature);
        String finalTimestamp = StringEscapeUtils.escapeJava(oauthTimeStamp);
        String finalToken = StringEscapeUtils.escapeJava(oauthToken);
        String finalVersion = StringEscapeUtils.escapeJava(oauthVersion);
        
		/*String authHeader = "oauth_consumer_key=%s&oauth_nonce=%s&oauth_signature_method=%s" +
                "&oauth_timestamp=%s&oauth_token=%s&oauth_version=%s";*/
        String authHeader = "OAuth oauth_nonce=\"%s\", oauth_signature_method=\"%s\", " +
                "oauth_timestamp=\"%s\", oauth_consumer_key=\"%s\", " +
                "oauth_token=\"%s\", oauth_signature=\"%s\", " +
                "oauth_version=\"%s\"";
		String tempHeaderFormat = String.format(authHeader, finalNonce, finalSignatureMethod, finalTimestamp, finalConsumer, finalToken, finalSignature, finalVersion);
		
		HttpsURLConnection request = (HttpsURLConnection)new URL(resourceUrl).openConnection();
		request.setDoOutput(true);
		request.addRequestProperty("Authorization", tempHeaderFormat);;
		request.setRequestMethod("POST");
		request.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		DataOutputStream dos = new DataOutputStream(request.getOutputStream());

		System.out.println(request.getResponseMessage());
		System.out.println(request.getResponseCode());
		
		return oauthNonce;
	}	
}	