//package burundi.ilucky4g.filter;
//
//import java.io.IOException;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebFilter;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import lombok.extern.log4j.Log4j2;
//
//@Order(value = Ordered.HIGHEST_PRECEDENCE)
//@Component
//@WebFilter(filterName = "RequestCachingFilter", urlPatterns = "/*")
//@Log4j2
//public class RequestFilter extends OncePerRequestFilter {
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//		  logger.info("Request URL::" + request.getRequestURL().toString());
//		  filterChain.doFilter(request, response);
//	}
//
//}
