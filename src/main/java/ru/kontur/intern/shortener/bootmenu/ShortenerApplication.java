package ru.kontur.intern.shortener.bootmenu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.kontur.intern.shortener.logic.CommandReader;
import ru.kontur.intern.shortener.logic.Links;

@SpringBootApplication(scanBasePackages = "ru.kontur.intern.shortener")
public class ShortenerApplication {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext ctx =
                SpringApplication.run(ShortenerApplication.class, args);
        System.out.println("===== SHORTENER IS STARTED =====");

        final Links links = ctx.getBean(Links.class);

        final CommandReader commandReader = new CommandReader(links);
        commandReader.start();
        commandReader.join();

        ctx.close();
    }
}
