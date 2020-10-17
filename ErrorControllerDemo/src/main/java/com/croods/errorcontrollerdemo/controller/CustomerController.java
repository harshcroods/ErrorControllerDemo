package com.croods.errorcontrollerdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.croods.errorcontrollerdemo.entity.Customer;
import com.croods.errorcontrollerdemo.repository.CustomerRepository;

@CrossOrigin
@RestController
@RequestMapping("/")
public class CustomerController {

	@Autowired
	private CustomerRepository customerRepository;

	@GetMapping("/")
	public ModelAndView returnCustomerPage() {
		return new ModelAndView("customer");
	}

	@RequestMapping("/add")
	public Customer addCustomer(@ModelAttribute Customer customer) {
		customer = null;
		return customerRepository.save(customer);
	}

}
