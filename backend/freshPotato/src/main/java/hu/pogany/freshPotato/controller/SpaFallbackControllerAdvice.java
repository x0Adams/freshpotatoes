package hu.pogany.freshPotato.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaFallbackControllerAdvice {

	@ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
	public ModelAndView fallbackToIndex(Exception ex, HttpServletRequest request) throws Exception {
		String path = request.getRequestURI();

		if (!"GET".equalsIgnoreCase(request.getMethod()) || isExcluded(path) || hasFileExtension(path)) {
			throw ex;
		}

		ModelAndView modelAndView = new ModelAndView("forward:/index.html");
		modelAndView.setStatus(HttpStatus.OK);
		return modelAndView;
	}

	private boolean isExcluded(String path) {
		return path.startsWith("/api")
				|| path.startsWith("/error");
	}

	private boolean hasFileExtension(String path) {
		int lastSlash = path.lastIndexOf('/');
		int lastDot = path.lastIndexOf('.');
		return lastDot > lastSlash;
	}
}

