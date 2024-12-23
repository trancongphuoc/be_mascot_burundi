package burundi.treasure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class FirebaseInitializer {

    @Value("${firebase.project.credentials}")
    private String mainCredentials;

    @Value("${firebase.project.database}")
    private String mainDatabase;

    @PostConstruct
    public void initializeFirebase() {
        log.warn("initializeFirebase");
        try {
            if(FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(mainCredentials).getInputStream()))
                        .setDatabaseUrl(mainDatabase)
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            log.warn("BUGS", e);
        }
    }
}
