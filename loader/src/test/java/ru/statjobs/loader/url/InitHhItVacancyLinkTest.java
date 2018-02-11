package ru.statjobs.loader.url;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.statjobs.loader.Const;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.dto.HhDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class InitHhItVacancyLinkTest {

    private InitUrlCreator initUrlCreator = new InitUrlCreator();
    private UrlConstructor urlConstructor = new UrlConstructor();

    private Map<String, String> cities;
    private List<HhDictionary> specialization;
    private Map<String, String> experience;
    private List<HhDictionary> industries;

    @Before
    public void init() {
        cities = new HashMap<>();
        specialization = new ArrayList<>();
        experience = new HashMap<>();
        industries = new ArrayList<>();
    }

    @Test
    public void createSingleLinkTest() {
        cities.put("msk", "msk");
        specialization.add(new HhDictionary(Const.IT_GROUP_NAME, "specGC", "spec", "specCode"));
        experience.put("ex", "ex");
        //industries.add(new HhDictionary("indG", "indGC", "ind", "indCode"));
        List<DownloadableLink> list =
            initUrlCreator.initHhItVacancyLink(urlConstructor, 170,0, cities, specialization, experience, industries );
        Assert.assertEquals(1, list.size());
        DownloadableLink link = list.get(0);
        Assert.assertEquals((Integer) 170, link.getSequenceNum());
        Assert.assertEquals(UrlHandler.HH_LIST_VACANCIES.name(), link.getHandlerName());
        Assert.assertEquals("https://api.hh.ru/vacancies?specialization=specCode&area=msk&experience=ex", link.getUrl());
    }

    @Test
    public void createSingleLinkWithIndustriesTest() {
        cities.put("msk", "msk");
        specialization.add(new HhDictionary(Const.IT_GROUP_NAME, "specGC", Const.IT_IND_PROGRAMMER, "specCode"));
        experience.put("ex", "ex");
        industries.add(new HhDictionary("indG", "indGC", "ind", "indCode"));
        List<DownloadableLink> list =
                initUrlCreator.initHhItVacancyLink(urlConstructor, 0,0, cities, specialization, experience, industries );
        Assert.assertEquals(1, list.size());
        DownloadableLink link = list.get(0);
        Assert.assertEquals((Integer) 0, link.getSequenceNum());
        Assert.assertEquals(UrlHandler.HH_LIST_VACANCIES.name(), link.getHandlerName());
        Assert.assertEquals("https://api.hh.ru/vacancies?specialization=specCode&area=msk&experience=ex&industry=indCode", link.getUrl());
    }

    @Test
    public void createSeveralLinkWithParamCityExperienceTest() {
        cities.put("msk", "msk");
        cities.put("spb", "spb");
        specialization.add(new HhDictionary(Const.IT_GROUP_NAME, "specGC", "spec", "specCode"));
        experience.put("ex1", "ex1");
        experience.put("ex2", "ex2");
        List<DownloadableLink> list =
                initUrlCreator.initHhItVacancyLink(urlConstructor, 0,0, cities, specialization, experience, industries );
        Assert.assertEquals(4, list.size());

        List<String> l = list.stream().map(DownloadableLink::getUrl).collect(Collectors.toList());
        Assert.assertTrue(l.contains("https://api.hh.ru/vacancies?specialization=specCode&area=msk&experience=ex1"));
        Assert.assertTrue(l.contains("https://api.hh.ru/vacancies?specialization=specCode&area=msk&experience=ex2"));
        Assert.assertTrue(l.contains("https://api.hh.ru/vacancies?specialization=specCode&area=spb&experience=ex1"));
        Assert.assertTrue(l.contains("https://api.hh.ru/vacancies?specialization=specCode&area=spb&experience=ex2"));
    }

    @Test
    public void createSeveralLinkWithParamSpecializationIndustriesTest() {
        cities.put("msk", "msk");
        specialization.add(new HhDictionary("NOT_IT_GROUP_NAME", "", "", ""));
        specialization.add(new HhDictionary(Const.IT_GROUP_NAME, "code1", "spec1", "specCode1"));
        specialization.add(new HhDictionary(Const.IT_GROUP_NAME, "code1", Const.IT_IND_ENGINEER, "specCode2"));
        experience.put("ex1", "ex1");
        industries.add(new HhDictionary("Group1", "G1", "i1", "1"));
        industries.add(new HhDictionary("G2", "2", "i2", "2"));
        industries.add(new HhDictionary("G2", "2", "i3", "3"));
        industries.add(new HhDictionary("G2", "2", "i4", "4"));

        List<DownloadableLink> list =
                initUrlCreator.initHhItVacancyLink(urlConstructor, 0,0, cities, specialization, experience, industries );
        Assert.assertEquals(5, list.size());
        List<String> l = list.stream().map(DownloadableLink::getUrl).collect(Collectors.toList());
        Assert.assertTrue(l.contains("https://api.hh.ru/vacancies?specialization=specCode1&area=msk&experience=ex1"));
        Assert.assertTrue(l.contains("https://api.hh.ru/vacancies?specialization=specCode2&area=msk&experience=ex1&industry=1"));
        Assert.assertTrue(l.contains("https://api.hh.ru/vacancies?specialization=specCode2&area=msk&experience=ex1&industry=2"));
        Assert.assertTrue(l.contains("https://api.hh.ru/vacancies?specialization=specCode2&area=msk&experience=ex1&industry=3"));
        Assert.assertTrue(l.contains("https://api.hh.ru/vacancies?specialization=specCode2&area=msk&experience=ex1&industry=4"));
    }

    @Test
    public void createSeveralLinkCountTest() {
        cities.put("msk", "msk");
        cities.put("spb", "spb");
        specialization.add(new HhDictionary("NOT_IT_GROUP_NAME", "", "", ""));
        specialization.add(new HhDictionary(Const.IT_GROUP_NAME, "code1", "spec1", "specCode1"));
        specialization.add(new HhDictionary(Const.IT_GROUP_NAME, "code1", Const.IT_IND_ENGINEER, "specCode2"));
        specialization.add(new HhDictionary(Const.IT_GROUP_NAME, "code1", Const.IT_IND_PROGRAMMER, "specCode3"));
        experience.put("ex1", "ex1");
        experience.put("ex2", "ex2");
        experience.put("ex3", "ex3");
        industries.add(new HhDictionary("Group1", "G1", "i1", "1"));
        industries.add(new HhDictionary("G2", "2", "i2", "2"));
        industries.add(new HhDictionary("G2", "2", "i3", "3"));
        industries.add(new HhDictionary("G2", "2", "i4", "4"));
        List<DownloadableLink> list =
                initUrlCreator.initHhItVacancyLink(urlConstructor, 80,0, cities, specialization, experience, industries);
        Assert.assertEquals(54, list.size());
        long n = list.stream()
                .filter(dLink -> dLink.getHandlerName().equals(UrlHandler.HH_LIST_VACANCIES.name()))
                .filter(dLink -> dLink.getSequenceNum().equals(80))
                .count();
        Assert.assertEquals(54L, n);
    }

}
