package burundi.treasure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import burundi.treasure.model.dto.DateRangeDTO;

@Service
public class ThymeleafRenderer {

	@Autowired
    private TemplateEngine templateEngine;
    
    public String renderPdf(DateRangeDTO dateRangeDTO) {
        Context context = new Context();
        context.setVariable("dateRangeDTO", dateRangeDTO);
        return templateEngine.process("view/cms", context);
    }
}
