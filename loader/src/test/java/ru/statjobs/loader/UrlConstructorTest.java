package ru.statjobs.loader;

import org.junit.Assert;
import org.junit.Test;


public class UrlConstructorTest {

    UrlConstructor urlConstructor = new UrlConstructor();

    @Test
    public void hhVacancyUrlNextPageTest() {
        String ur11 = "https://api.hh.ru/vacancies?search_period=17" +
                "&specialization=spec&area=area&experience=exp&industry=industry&per_page=27&page=30";
        String ur12 = "https://api.hh.ru/vacancies?search_period=17" +
                "&specialization=spec&area=area&experience=exp&industry=industry&per_page=27&page=31";
        Assert.assertEquals(ur12, urlConstructor.hhUrlNextPage(ur11));

        ur11 = "https://api.hh.ru/vacancies?search_period=17" +
                "&specialization=spec&area=area&experience=exp&industry=industry&per_page=27&page=1";
        ur12 = "https://api.hh.ru/vacancies?search_period=17" +
                "&specialization=spec&area=area&experience=exp&industry=industry&per_page=27&page=2";
        Assert.assertEquals(ur12, urlConstructor.hhUrlNextPage(ur11));

        ur11 = "https://api.hh.ru/vacancies?search_period=17" +
                "&specialization=spec&area=area&experience=exp&industry=industry&per_page=27";
        ur12 = "https://api.hh.ru/vacancies?search_period=17" +
                "&specialization=spec&area=area&experience=exp&industry=industry&per_page=27&page=1";
        Assert.assertEquals(ur12, urlConstructor.hhUrlNextPage(ur11));

        ur11 = "https://hh.ru/search/resume?exp_period=all_time&items_on_page=100&order_by=publication_time&specialization=1.327&area=1&text=&pos=full_text&logic=normal&clusters=true&search_period=30&page=36";
        ur12 = "https://hh.ru/search/resume?exp_period=all_time&items_on_page=100&order_by=publication_time&specialization=1.327&area=1&text=&pos=full_text&logic=normal&clusters=true&search_period=30&page=37";
        Assert.assertEquals(ur12, urlConstructor.hhUrlNextPage(ur11));
    }

    @Test
    public void hhResumeUrlNextPageTest() {
        String ur11 = "https://hh.ru/search/resume?exp_period=all_time&items_on_page=100&order_by=publication_time&specialization=1.327&area=1&text=&pos=full_text&logic=normal&clusters=true&search_period=30&page=36";
        String ur12 = "https://hh.ru/search/resume?exp_period=all_time&items_on_page=100&order_by=publication_time&specialization=1.327&area=1&text=&pos=full_text&logic=normal&clusters=true&search_period=30&page=37";
        Assert.assertEquals(ur12, urlConstructor.hhUrlNextPage(ur11));
    }


    @Test
    public void hhVacancyUrlNextPageTest2() {
        Assert.assertEquals("url&page=1", urlConstructor.hhUrlNextPage("url"));
        Assert.assertEquals("url&page=2", urlConstructor.hhUrlNextPage(urlConstructor.hhUrlNextPage("url")));
        Assert.assertEquals("url&page=213&param", urlConstructor.hhUrlNextPage("url&page=212&param"));
        Assert.assertEquals("url&page=1&param", urlConstructor.hhUrlNextPage("url&page=0&param"));
        Assert.assertEquals("&page=1", urlConstructor.hhUrlNextPage(""));
        Assert.assertEquals("1&2&url&3&4&page=1", urlConstructor.hhUrlNextPage("1&2&url&3&4"));
    }

    @Test
    public void createHhVacancyUrlSimpleTest() {
        String url = urlConstructor.createHhVacancyUrl(
                "spec",17,"area","exp",
                "industry",30,27);
        Assert.assertEquals(
                "https://api.hh.ru/vacancies?search_period=17&specialization=spec&area=area&experience=exp&industry=industry&per_page=27&page=30",
                url);
    }

    @Test
    public void createHhVacancyUrlEmptyParamTest() {
        Assert.assertEquals(
                "https://api.hh.ru/vacancies?search_period=17&area=area&experience=exp&industry=industry&per_page=27&page=30",
                urlConstructor.createHhVacancyUrl(
                        "",17,"area","exp",
                        "industry",30,27));
        Assert.assertEquals(
                "https://api.hh.ru/vacancies?area=area&experience=exp&industry=industry&per_page=27&page=30",
                urlConstructor.createHhVacancyUrl(
                        "",null,"area","exp",
                        "industry",30,27));
        Assert.assertEquals(
                "https://api.hh.ru/vacancies?experience=exp&industry=industry&per_page=27&page=30",
                urlConstructor.createHhVacancyUrl(
                        null,null,null,"exp",
                        "industry",30,27));
        Assert.assertEquals(
                "https://api.hh.ru/vacancies?industry=industry&per_page=27&page=30",
                urlConstructor.createHhVacancyUrl(
                        null,null,null,null,
                        "industry",30,27));
        Assert.assertEquals(
                "https://api.hh.ru/vacancies?per_page=27&page=30",
                urlConstructor.createHhVacancyUrl(
                        null,null,null,null,
                        null,30,27));
        Assert.assertEquals(
                "https://api.hh.ru/vacancies?per_page=27",
                urlConstructor.createHhVacancyUrl(
                        null,null,null,null,
                        null,null,27));
        Assert.assertEquals(
                "https://api.hh.ru/vacancies",
                urlConstructor.createHhVacancyUrl(
                        null,null,null,null,
                        null,null,null));
        Assert.assertEquals(
                "https://api.hh.ru/vacancies",
                urlConstructor.createHhVacancyUrl(
                        null,0,null,null,
                        null,0,0));
    }

    @Test
    public void createHhResumeListUrlTest() {
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&specialization=1.327&search_period=30&area=1&items_on_page=100&page=7",
                urlConstructor.createHhResumeListUrl("1.327",30,"1",7,100));
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&search_period=30&area=1&items_on_page=100&page=7",
                urlConstructor.createHhResumeListUrl("",30,"1",7,100));
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&area=1&items_on_page=100&page=7",
                urlConstructor.createHhResumeListUrl("",0,"1",7,100));
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&items_on_page=100&page=7",
                urlConstructor.createHhResumeListUrl("",0,"",7,100));
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true&page=7",
                urlConstructor.createHhResumeListUrl("",0,"",7,0));
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true",
                urlConstructor.createHhResumeListUrl("",0,"",0,0));
        Assert.assertEquals(
                "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true",
                urlConstructor.createHhResumeListUrl(null,null,null,null,null));
    }

}
