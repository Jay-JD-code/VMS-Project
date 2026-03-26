package com.vms.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

    @Service
	@RequiredArgsConstructor
	public class EmailService {

	    private final JavaMailSender mailSender;

	    public void sendOtpEmail(String toEmail, String otp) {

	        SimpleMailMessage message = new SimpleMailMessage();

	        message.setTo(toEmail);
	        message.setSubject("Password Reset OTP");
	        message.setText(
	                "Your OTP is: " + otp +
	                "\n\nThis OTP is valid for 5 minutes."
	        );

	        mailSender.send(message);

	        System.out.println("OTP email sent to: " + toEmail);
	    }

	    public void sendVendorCredentials(String email, String password) {

	    	 SimpleMailMessage message = new SimpleMailMessage();

		        message.setTo(email);
		        message.setSubject("Login Credentials");
		        message.setText(
		        		"==================================\n"+
		        		"Vendor Account Created\n"+
		        		"Email: " + email+"\n"+
		        		"Temporary Password: " + password+"\n"+
		        		"Please login and change password."+"\n"+
		        		"=================================="
		        );

		        mailSender.send(message);

		        System.out.println("Login Credentials send to: " + email);
	    }
	}

