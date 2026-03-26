package com.vms.vendor.dto.response;

import java.util.UUID;

import com.vms.vendor.entity.VendorStatus;

import lombok.Data;

@Data
public class VendorResponse {

	private UUID id;
	
	private String companyName;
	
	private String contactPerson;
	
	private String email;
	
	private String phone;
	
	private VendorStatus status;
}
