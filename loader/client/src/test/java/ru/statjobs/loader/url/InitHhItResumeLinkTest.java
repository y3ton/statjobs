package ru.statjobs.loader.url;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.statjobs.loader.ClientConsts;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.dto.HhDictionary;
import ru.statjobs.loader.common.url.UrlTypes;
import ru.statjobs.loader.dao.HhDictionaryDaoImpl;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitHhItResumeLinkTest {

    private InitUrlCreator initUrlCreator = new InitUrlCreator();
    private UrlConstructor urlConstructor = new UrlConstructor();

    private Map<String, String> cities;
    private List<HhDictionary> specialization;

    @Before
    public void init() {
        cities = new HashMap<>();
        specialization = new ArrayList<>();
    }

    @Test
    public void createSingleLinkTest() {
        cities.put("msk", "msk");
        specialization.add(new HhDictionary(ClientConsts.IT_GROUP_NAME, "specGC", "spec", "specCode"));
        List<DownloadableLink> list = initUrlCreator.initHhItResumeLink(urlConstructor, 170, 100, cities, specialization, 30);
        Assert.assertEquals(1, list.size());
        DownloadableLink link = list.get(0);
        Assert.assertEquals((Integer) 170, link.getSequenceNum());
        Assert.assertEquals(UrlTypes.HH_LIST_RESUME, link.getHandlerName());
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&specialization=specCode&search_period=30&area=msk&items_on_page=100",
                link.getUrl()
        );
    }

    @Test
    public void createSeveralLinl() {
        cities.put("msk", "msk");
        cities.put("spb", "spb");
        specialization.add(new HhDictionary(ClientConsts.IT_GROUP_NAME, "specGC", "spec", "specCode"));
        specialization.add(new HhDictionary(ClientConsts.IT_GROUP_NAME, "2", "2", "2"));
        List<DownloadableLink> list = initUrlCreator.initHhItResumeLink(urlConstructor, 170, 100, cities, specialization, 30);
        Assert.assertEquals(4, list.size());
        DownloadableLink link = list.get(0);
        Assert.assertEquals((Integer) 170, link.getSequenceNum());
        Assert.assertEquals(UrlTypes.HH_LIST_RESUME, link.getHandlerName());
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&specialization=specCode&search_period=30&area=msk&items_on_page=100",
                link.getUrl()
        );
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&specialization=2&search_period=30&area=msk&items_on_page=100",
                list.get(1).getUrl()
        );
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&specialization=specCode&search_period=30&area=spb&items_on_page=100",
                list.get(2).getUrl()
        );
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&specialization=2&search_period=30&area=spb&items_on_page=100",
                list.get(3).getUrl()
        );
    }

    @Test
    public void checkUrlItTelecomFilter() {
        cities.put("msk", "msk");
        cities.put("spb", "spb");
        specialization = new HhDictionaryDaoImpl(new JsonUtils(), new FileUtils()).getSpecialization();
        List<DownloadableLink> list = initUrlCreator.initHhItResumeLink(urlConstructor, 170, 100, cities, specialization, 30);
        DownloadableLink link = list.get(0);
        Assert.assertEquals((Integer) 170, link.getSequenceNum());
        Assert.assertEquals(UrlTypes.HH_LIST_RESUME, link.getHandlerName());
        Assert.assertTrue(specialization.size() > 150);
        Assert.assertTrue(list.size() < 100);
    }

}
