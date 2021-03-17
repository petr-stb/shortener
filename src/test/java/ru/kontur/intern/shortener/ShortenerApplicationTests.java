package ru.kontur.intern.shortener;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.kontur.intern.shortener.bootmenu.ShortenerApplication;
import ru.kontur.intern.shortener.controller.ShortenerController;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShortenerApplication.class)
public class ShortenerApplicationTests {

    @Autowired
    private ShortenerController controller;

    @Test
    public void contextLoads() {
        assertThat(controller).isNotNull();
    }

}
