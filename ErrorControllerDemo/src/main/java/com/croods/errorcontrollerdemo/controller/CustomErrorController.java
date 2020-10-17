package com.croods.errorcontrollerdemo.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.croods.errorcontrollerdemo.entity.ErrorEntity;
import com.croods.errorcontrollerdemo.repository.ErrorRepository;

@Controller
public class CustomErrorController implements ErrorController {

	@Autowired
	private ErrorRepository errorRepository;

	@GetMapping("/error")
	public ModelAndView handleError(HttpServletRequest request) {
		String errorPage = "error";

		Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		String errorPath = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
		Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		String errorMessage = ex.getMessage();
		if (status != null) {
			if (status == HttpStatus.NOT_FOUND.value()) {
				// handle HTTP 404 Not Found error
				errorPage = "error-404";

			} else if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				// handle HTTP 500 Internal Server Error
				errorPage = "error-500";

			} else if (status == HttpStatus.FORBIDDEN.value()) {
				// handle HTTP 403 Forbidden
				errorPage = "error-403";
			}
		}
		
		// save error
		ErrorEntity errorEntity = new ErrorEntity();
		errorEntity.setStatusCode(status);
		errorEntity.setErrorDetails(errorMessage);
		errorEntity.setErrorPath(errorPath);
		errorRepository.save(errorEntity);
		
		// errorModel
		ModelAndView errorModel = new ModelAndView(errorPage);
		errorModel.addObject("errorEntity", errorEntity);
		return errorModel;

	}
	
	

	@Override
	public String getErrorPath() {
		return "/error";
	}

}
