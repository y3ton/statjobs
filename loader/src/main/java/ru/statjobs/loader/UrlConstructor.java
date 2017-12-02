package ru.statjobs.loader;

public class UrlConstructor {

    // https://hh.ru/search/vacancy?search_period=7&specialization=17.256&area=1&experience=noExperience&page=1
    //  https://api.hh.ru/vacancies?search_period=7&specialization=17.256&area=1&experience=noExperience&per_page=100&page=1
    public String createHhVacancyUrl(String specializationCode, Integer searchPeriod, String areaCode, String experienceCode, Integer page, Integer perPage) {
        String url = "https://api.hh.ru/vacancies?search_period=%s&specialization=%s&area=%s&experience=%s&per_page=%s&page=%s";
        return String.format(url, searchPeriod, specializationCode, areaCode, experienceCode, perPage, page);
    }

    public String hhVacancyUrlNextPage(String url) {
        String[] arr = url.split("&page=");
        return arr[0] + "&page=" + (Integer.valueOf(arr[1]) + 1);
    }


}
