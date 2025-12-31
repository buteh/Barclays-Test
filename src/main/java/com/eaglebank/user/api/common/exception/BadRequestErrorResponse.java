package com.eaglebank.user.api.common.exception;

import java.util.List;


public record BadRequestErrorResponse(String message, List<Detail> details)
{
	public record Detail(String field, String message, String type)
	{
	}

}
