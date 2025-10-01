package pl.kalin.simpledms;

import javafx.application.Application;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringJavaFxLauncher {
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = new SpringApplicationBuilder(SimpleDmsSpringConfig.class).run(args);
        Application.launch(HelloApplication.class, args);
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }
}

