package com.example.moattravel.form;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReservationRegisterForm {
	
	private Integer userId;
	
	private Integer houseId;
	
	private String checkinDate;
	
	private String checkoutDate;
	
	private Integer numberOfPeople;
	
	private Integer amount;
	
}
