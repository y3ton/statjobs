package ru.statjobs.loader;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UrlConstructor {

    public String createHhVacancyUrl(
            String specializationCode,
            Integer searchPeriod,
            String areaCode,
            String experienceCode,
            String industryCode,
            Integer page,
            Integer perPage) {
        String url = "https://api.hh.ru/vacancies";
        List<String> param = new ArrayList<>();
        if (searchPeriod != null && !searchPeriod.equals(0)) {
            param.add("search_period=" + searchPeriod);
        }
        if (StringUtils.isNotBlank(specializationCode )) {
            param.add("specialization=" + specializationCode);
        }
        if (StringUtils.isNotBlank(areaCode)) {
            param.add("area=" + areaCode);
        }
        if (StringUtils.isNotBlank(experienceCode)) {
            param.add("experience=" + experienceCode);
        }
        if (StringUtils.isNotBlank(industryCode )) {
            param.add("industry=" + industryCode);
        }
        if (perPage != null && !perPage.equals(0)) {
            param.add("per_page=" + perPage);
        }
        if (page != null && !page.equals(0)) {
            param.add("page=" + page);
        }
        return url + (param.size() == 0 ? "": "?" + StringUtils.join(param, "&"));
    }

    public String hhUrlNextPage(String url) {
        String[] arr = url.split("&page=");
        if (arr.length == 1) {
            return arr[0] + "&page=1";
        }
        String[] arr2 = arr[1].split("&");
        if (arr2.length == 1) {
            return arr[0] + "&page=" + (Integer.valueOf(arr2[0]) + 1);
        } else {
            return arr[0] + "&page=" + (Integer.valueOf(arr2[0]) + 1) + "&" + arr2[1];
        }

    }

    public String createHhResumeListUrl(
            String specializationCode,
            Integer searchPeriod,
            String areaCode,
            Integer page,
            Integer perPage) {
        String url = "https://hh.ru/search/resume?exp_period=all_time&order_by=publication_time&text=&pos=full_text&logic=normal&clusters=true";
        List<String> param = new ArrayList<>();
        if (StringUtils.isNotBlank(specializationCode )) {
            param.add("specialization=" + specializationCode);
        }
        if (searchPeriod != null && !searchPeriod.equals(0)) {
            param.add("search_period=" + searchPeriod);
        }
        if (StringUtils.isNotBlank(areaCode )) {
            param.add("area=" + areaCode);
        }
        if (perPage != null && !perPage.equals(0)) {
            param.add("items_on_page=" + perPage);
        }
        if (page != null && !page.equals(0)) {
            param.add("page=" + page);
        }
        param.add(0, url);
        return StringUtils.join(param, "&");
    }


}
