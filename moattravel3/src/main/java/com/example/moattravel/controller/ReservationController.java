package com.example.moattravel.controller;

import java.time.LocalDate;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.moattravel.entity.House;
import com.example.moattravel.entity.Reservation;
import com.example.moattravel.entity.User;
import com.example.moattravel.form.ReservationInputForm;
import com.example.moattravel.form.ReservationRegisterForm;
import com.example.moattravel.repository.HouseRepository;
import com.example.moattravel.repository.ReservationRepository;
import com.example.moattravel.security.UserDetailsImpl;
import com.example.moattravel.service.ReservationService;
import com.example.moattravel.service.StripeService;

@Controller
public class ReservationController {
	
	private final ReservationRepository reservationRepository;
	private final ReservationService reservationService;
	private final HouseRepository houseRepository;
	private final StripeService stripeService;
	
	public ReservationController(ReservationRepository reservationRepository, ReservationService reservationService, HouseRepository houseRepository, StripeService stripeService) {
		this.reservationRepository = reservationRepository;
		this.reservationService = reservationService;
		this.houseRepository = houseRepository;
		this.stripeService = stripeService;
	}
	
	@GetMapping("/reservations")
	public String reservation(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {
		User user = userDetailsImpl.getUser();
		Page<Reservation> reservation = reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
		
		model.addAttribute("reservation", reservation);
		
		return "reservations/index";
	}
	
	@GetMapping("/:houses/{id}/reservations/input")
	public String a(@PathVariable(name = "id") Integer id, @ModelAttribute @Validated ReservationInputForm reservationInputForm, BindingResult bindingResult, RedirectAttributes redirectAttributes,Model model) {
		House house = houseRepository.getReferenceById(id);
		Integer numberOfPeople = reservationInputForm.getNumberOfPeople();
		Integer capacity = house.getCapacity();
		
		if(numberOfPeople != null) {
			if (!reservationService.isWithinCapacity(numberOfPeople, capacity)) {
				FieldError fieldError = new FieldError(bindingResult.getObjectName(), "numberOfPeople", "定員オーバーです。");
				bindingResult.addError(fieldError);
			}
		}
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("house", house);
			model.addAttribute("errorMessage", "予約内容に不備があります。");
			
			return "houses/show";
		}
		
		redirectAttributes.addFlashAttribute("reservationInputForm", reservationInputForm);
		
		return "redirect:/houses/{id}/reservations/confirm";
	}
	
	@GetMapping("/houses/{id}/reservations/confirm")
	public String confirm(@PathVariable(name = "id") Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @ModelAttribute ReservationInputForm reservationInputForm, HttpServletRequest httpServletRequest, Model model) {
		User user = userDetailsImpl.getUser();
		House house = houseRepository.getReferenceById(id);
		Integer numberOfPeople = reservationInputForm.getNumberOfPeople();
		
		//チェックイン日とチェックアウト日を取得
		LocalDate checkinDate = reservationInputForm.getCheckinDate();
		LocalDate checkoutDate = reservationInputForm.getCheckoutDate();
		
		//宿泊料金を計算する
		Integer price = house.getPrice();
		Integer amount = reservationService.calculateAmount(checkinDate, checkoutDate, price);
		
		ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm(user.getId(), house.getId(), checkinDate.toString(), checkoutDate.toString(), numberOfPeople, amount);
		String sessionId = stripeService.createStripeSession(house.getName(), reservationRegisterForm, httpServletRequest);
		
		model.addAttribute("house", house);
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);
		model.addAttribute("sessionId", sessionId);
		
		return "reservations/confirm";
	}
	
//	@PostMapping("/houses/{id}/reservations/create")
//	public String create(@ModelAttribute ReservationRegisterForm reservationRegisterForm) {
//		reservationService.create(reservationRegisterForm);
//		
//		return "redirect:/reservations?reserved";
//	}
}
