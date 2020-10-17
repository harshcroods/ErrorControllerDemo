package com.croods.errorcontrollerdemo.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.croods.errorcontrollerdemo.entity.ErrorEntity;
import com.croods.errorcontrollerdemo.repository.ErrorRepository;

@Controller
public class CustomErrorController implements ErrorController {

	@Autowired
	private ErrorRepository errorRepository;

	@GetMapping("/error")
	public String handleError(HttpServletRequest request) {
		String errorPage = "error";
		String errorMessage = "";

		Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		String errorPath = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

		if (status != null) {
			if (status == HttpStatus.NOT_FOUND.value()) {
				// handle HTTP 404 Not Found error
				errorPage = "error-404";
				errorMessage = "404 | NOT FOUND";

			} else if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				// handle HTTP 500 Internal Server Error
				errorPage = "error-500";
				errorMessage = "500 Internal Server Error";

			} else if (status == HttpStatus.FORBIDDEN.value()) {
				// handle HTTP 403 Forbidden
				errorPage = "error-403";
				errorMessage = "403 Forbidden";
			}
		}
		ErrorEntity errorDetails = new ErrorEntity();
		errorDetails.setStatusCode(status);
		errorDetails.setErrorDetails(errorMessage);
		errorDetails.setErrorPath(errorPath);
		errorRepository.save(errorDetails);

		return errorPage;

	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

}
