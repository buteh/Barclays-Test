package com.eaglebank.user.api.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HealthController
{
	/**
	 * Did not add-In/enable actuators to avoid extra dependencies
	 *
	 * @return
	 */
	@GetMapping("/health")
	public String health()
	{
		return "OK";
	}

}
