package ru.kontur.intern.shortener.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kontur.intern.shortener.logic.Pref.L_PREFIX;
import static ru.kontur.intern.shortener.logic.Pref.STATS_PREFIX;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.kontur.intern.shortener.bootmenu.ShortenerApplication;
import ru.kontur.intern.shortener.logic.Links;
import ru.kontur.intern.shortener.logic.Pref;
import ru.kontur.intern.shortener.models.Link;
import ru.kontur.intern.shortener.models.ShortLink;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ShortenerApplication.class)
@WebAppConfiguration
public class ShortenerControllerTest {

    private final MediaType contentTypeJson = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private Links links;

    private MockMvc mockMvc;

    private final static List<Link> linkList = new ArrayList<>();
    private final static List<Integer> countList = new ArrayList<>();
    private final static List<Integer> rankList = new ArrayList<>();

    @Before
    public void before() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        fillLinks();
        initializeCountList();
        initializeRankList();
    }

    @Test
    public void generateTest() throws Exception {
        final String original = "https://yandex.ru/pogoda/yekaterinburg/maps/nowcast";
        MvcResult result = mockMvc.perform(post("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"original\" : \"" + original + "\" }"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson))
                .andExpect(jsonPath("link", containsString(Pref.L_PREFIX)))
                .andReturn();
        final String content = result.getResponse().getContentAsString();
        final ObjectMapper mapper = new ObjectMapper();
        final ShortLink readLink = mapper.readValue(content, ShortLink.class);
        final Link link = links.getLinkByShortLink(readLink.getLink());
        assertEquals(original, link.getOriginal());
    }

    @Test
    public void generateTestWhenOriginalIsRepeat() throws Exception {
        final String original = "https://yandex.ru/pogoda/yekaterinburg/maps/nowcast";
        final MvcResult result1 = mockMvc.perform(post("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"original\" : \"" + original + "\" }"))
                .andReturn();
        final String content1 = result1.getResponse().getContentAsString();
        final ObjectMapper mapper1 = new ObjectMapper();
        final ShortLink readLink1 = mapper1.readValue(content1, ShortLink.class);
        final MvcResult result2 = mockMvc.perform(post("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"original\" : \"" + original + "\" }"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson))
                .andExpect(jsonPath("link", containsString(Pref.L_PREFIX)))
                .andReturn();
        final String content2 = result2.getResponse().getContentAsString();
        final ObjectMapper mapper2 = new ObjectMapper();
        final ShortLink readLink2 = mapper2.readValue(content2, ShortLink.class);
        assertEquals(readLink1.getLink(), readLink2.getLink());
    }

    @Test
    public void generateTestWhenRequestIsEmpty() throws Exception {
        mockMvc.perform(post("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void generateTestWhenOriginalIsEmpty() throws Exception {
        mockMvc.perform(post("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"original\" : \"\" }"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void generateTestWhenOriginalIsInvalid() throws Exception {
        mockMvc.perform(post("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"original\" : \"Uronili mishku na pol, otorvali mishke lapu.\" }"))
                .andExpect(status().isBadRequest())
                .andReturn();
        links.clear();
    }

    @Test
    public void generateTestWhenJsonIsInvalid() throws Exception {
        mockMvc.perform(post("/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"origina\" : \"https://yandex.ru/pogoda/yekaterinburg/month\" }"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void redirectTest() throws Exception {
        final String original = "https://yandex.ru/pogoda/yekaterinburg/maps/nowcast";
        links.findOrAdd(original);
        final MvcResult result = mockMvc.perform(
                get(links.getShortLinkByOriginal(original).getLink()))
                .andExpect(status().isFound())
                .andReturn();
        final String location = result.getResponse().getHeader("Location");
        assertEquals(original, location);
    }

    @Test
    public void redirectTestWhenOriginalIsInvalid() throws Exception {
        final String original = "Uronili mishku na pol, otorvali mishke lapu.";
        links.findOrAdd(original);
        mockMvc.perform(get(links.getShortLinkByOriginal(original).getLink()))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void redirectTestWhenShortLinkIsEmpty() throws Exception {
        mockMvc.perform(get(L_PREFIX))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void redirectTestWhenShortLinkIsInvalid() throws Exception {
        mockMvc.perform(get(L_PREFIX + "123"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void getLinkStatisticsTest() throws Exception {
        links.clear();
        linkList.clear();
        fillLinks();
        increaseCounts();
        int index = 3;
        final MvcResult result = mockMvc.perform(get(STATS_PREFIX + "/"
                + linkList.get(index).getLink().replace(L_PREFIX, "")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson))
                .andReturn();
        final String content = result.getResponse().getContentAsString();
        final ObjectMapper mapper = new ObjectMapper();
        final Link readLink = mapper.readValue(content, Link.class);
        assertEquals(linkList.get(index).getLink(), readLink.getLink());
        assertEquals(linkList.get(index).getOriginal(), readLink.getOriginal());
        assertEquals(rankList.get(index), Integer.valueOf(readLink.getRank()));
        assertEquals(countList.get(index), Integer.valueOf(readLink.getCount()));
    }

    @Test
    public void getLinkStatisticsTestWhenShortLinkIsInvalid() throws Exception {
        links.clear();
        linkList.clear();
        fillLinks();
        increaseCounts();
        mockMvc.perform(get(STATS_PREFIX + "/123"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void getRatingTest() throws Exception {
        final int page = 1;
        final int count = 3;
        final List<Link> readLinkList = basicMethodForRating(page, count);
        int index = 2;
        int readIndex = 0;
        checkReadLinks(readLinkList, index, readIndex);
        index = 0;
        readIndex++;
        checkReadLinks(readLinkList, index, readIndex);
        index = 3;
        readIndex++;
        checkReadLinks(readLinkList, index, readIndex);
    }

    @Test
    public void getRatingTestWhenPageIsLast() throws Exception {
        final int page = 2;
        final int count = 3;
        final List<Link> readLinkList = basicMethodForRating(page, count);
        int index = 1;
        int readIndex = 0;
        checkReadLinks(readLinkList, index, readIndex);
        index = 4;
        readIndex++;
        checkReadLinks(readLinkList, index, readIndex);
    }

    @Test
    public void getRatingTestWhenPageIsOutOfRating() throws Exception {
        final int page = 5;
        final int count = 2;
        basicMethodForRating(page, count);
    }

    @Test
    public void getRatingTestWhenParamsAreEmpty() throws Exception {
        links.clear();
        linkList.clear();
        fillLinks();
        mockMvc.perform(get(Pref.STATS_PREFIX))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void getRatingTestWhenPageParamIsEmpty() throws Exception {
        links.clear();
        linkList.clear();
        fillLinks();
        mockMvc.perform(get(Pref.STATS_PREFIX + "?count=3"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void getRatingTestWhenPageParamIsString() throws Exception {
        links.clear();
        linkList.clear();
        fillLinks();
        mockMvc.perform(get(Pref.STATS_PREFIX + "?page=one&count=3"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void controlCountsFromDbAndClickMapTest() throws Exception {
        links.clear();
        linkList.clear();
        fillLinks();
        increaseCounts();
        final int index = 3;
        mockMvc.perform(get(STATS_PREFIX + "/"
                + linkList.get(index).getLink().replace(L_PREFIX, "")))
                .andReturn();
        increaseCounts();
        final MvcResult result = mockMvc.perform(get(STATS_PREFIX + "/"
                + linkList.get(index).getLink().replace(L_PREFIX, "")))
                .andReturn();
        final String content = result.getResponse().getContentAsString();
        final ObjectMapper mapper = new ObjectMapper();
        final Link readLink = mapper.readValue(content, Link.class);
        assertEquals(countList.get(index) * 2, readLink.getCount());
    }

    private static void initializeCountList() {
        countList.add(15);
        countList.add(1);
        countList.add(32);
        countList.add(7);
        countList.add(0);
    }

    private static void initializeRankList() {
        rankList.add(2);
        rankList.add(4);
        rankList.add(1);
        rankList.add(3);
        rankList.add(5);
    }

    private void fillLinks() {
        final String[] originals = {
                "https://yandex.ru/pogoda/yekaterinburg/month",
                "https://www.gismeteo.ru/weather-yekaterinburg-4517/",
                "https://pogoda.mail.ru/prognoz/ekaterinburg/",
                "https://weather.com/weather/today/l/56.74,60.65?par=google",
                "https://ru.snow-forecast.com/resorts/Mount-Elbrus/6day/mid"
        };
        for (String original : originals) {
            links.findOrAdd(original);
            final ShortLink shortLink = links.getShortLinkByOriginal(original);
            linkList.add(links.getLinkByShortLink(shortLink.getLink()));
        }
    }

    private void increaseCounts() throws Exception {
        for (int i = 0; i < linkList.size(); i++) {
            for (int j = 0; j < countList.get(i); j++) {
                mockMvc.perform(get(linkList.get(i).getLink()))
                        .andReturn();
            }
        }
    }

    private List<Link> basicMethodForRating(final int page, final  int count) throws Exception {
        links.clear();
        linkList.clear();
        fillLinks();
        increaseCounts();
        final  MvcResult result = mockMvc.perform(get(STATS_PREFIX
                + "?page=" + page + "&count=" + count))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson))
                .andReturn();
        final String content = result.getResponse().getContentAsString();
        final ObjectMapper mapper = new ObjectMapper();
        final List<Link> readLinkList =
                mapper.readValue(content, new TypeReference<List<Link>>() {});
        final int expectedSize;
        if (page * count <= linkList.size()) {
            expectedSize = count;
        } else {
            int difference = linkList.size() - (page - 1) * count;
            expectedSize = difference > 0 ? difference : 0;
        }
        assertEquals(expectedSize, readLinkList.size());
        return readLinkList;
    }

    private void checkReadLinks(final List<Link> readLinkList,
                                final int index, final int readIndex) {
        assertEquals(linkList.get(index).getLink(), readLinkList.get(readIndex).getLink());
        assertEquals(linkList.get(index).getOriginal(), readLinkList.get(readIndex).getOriginal());
        assertEquals(rankList.get(index), Integer.valueOf(readLinkList.get(readIndex).getRank()));
        assertEquals(countList.get(index), Integer.valueOf(readLinkList.get(readIndex).getCount()));
    }
}