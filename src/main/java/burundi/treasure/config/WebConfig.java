package burundi.treasure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class WebConfig {

	@Bean
	public ViewResolver htmlViewResolver() {
	    ThymeleafViewResolver resolver = new ThymeleafViewResolver();
	    resolver.setTemplateEngine(templateEngine(htmlTemplateResolver()));
	    resolver.setContentType("text/html; charset=UTF-8");
	    resolver.setCharacterEncoding("UTF-8");
	    resolver.setViewNames(new String[] {"*.html"});
	    return resolver;
	}

	private ITemplateResolver htmlTemplateResolver() {
	    SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
	    resolver.setCharacterEncoding("UTF-8");
	    resolver.setPrefix("/templates/");
	    resolver.setCacheable(false);
	    resolver.setTemplateMode(TemplateMode.HTML);
	    return resolver;
	}
	
	public SpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
	    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
	    templateEngine.setTemplateResolver(templateResolver);
	    return templateEngine;
	}
}
