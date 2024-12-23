package burundi.treasure.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
public class HomeController {

    @RequestMapping
    public String home() {
        return "";
    }

    @GetMapping("/log")
    public String logExample() {
        log.info("Info level log example");
        log.debug("Debug level log example");
        log.error("Error level log example", new Exception("Example exception"));
        return "Logging has been demonstrated. Check the console and the log file!";
    }
}
