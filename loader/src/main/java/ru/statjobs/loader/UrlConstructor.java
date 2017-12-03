package ru.statjobs.loader;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UrlConstructor {

    // https://hh.ru/search/vacancy?search_period=7&specialization=17.256&area=1&experience=noExperience&page=1
    //  https://api.hh.ru/vacancies?search_period=7&specialization=17.256&area=1&experience=noExperience&per_page=100&page=1
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
        if (perPage != null) {
            param.add("per_page=" + perPage);
        }
        if (page != null && !page.equals(0)) {
            param.add("page=" + page);
        }
        return url + (param.size() == 0 ? "": "?" + StringUtils.join(param, "&"));
    }

    public String hhVacancyUrlNextPage(String url) {
        String[] arr = url.split("&page=");
        if (arr.length == 1) {
            return arr[0] + "&page=1";
        }
        return arr[0] + "&page=" + (Integer.valueOf(arr[1]) + 1);
    }


}
