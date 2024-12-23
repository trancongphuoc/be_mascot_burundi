package burundi.treasure.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.context.webmvc.SpringWebMvcThymeleafRequestContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ExportPdfService {

	@Autowired
    private TemplateEngine templateEngine;

	@Autowired
	private ServletContext servletContext;
	
    public ByteArrayInputStream exportReceiptPdf(String templateName, Map<String, Object> data, HttpServletRequest request, HttpServletResponse response) {
        Context context = new Context();
        context.setVariables(data);
        
        RequestContext requestContext = new RequestContext(request, response, servletContext, data);
        SpringWebMvcThymeleafRequestContext thymeleafRequestContext = new SpringWebMvcThymeleafRequestContext(requestContext, request);
        context.setVariable("thymeleafRequestContext", thymeleafRequestContext);

                
        String htmlContent = templateEngine.process(templateName, context);

        ByteArrayInputStream byteArrayInputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(byteArrayOutputStream, false);
            renderer.finishPDF();
            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (DocumentException e) {
            log.error(e.getMessage(), e);
        }

        return byteArrayInputStream;
    }
}
