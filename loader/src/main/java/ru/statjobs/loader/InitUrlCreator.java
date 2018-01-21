package ru.statjobs.loader;

import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.dto.HhDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InitUrlCreator {

    public List<DownloadableLink> initHhItLink(
            UrlConstructor urlConstructor,
            int sequenceNum,
            Integer perPage,
            Map<String, String> cities,
            List<HhDictionary> specialization,
            Map<String, String> experience,
            List<HhDictionary> industries
    ) {
        List<DownloadableLink> links = new ArrayList<>();
        List<String> listIndustries = industries.stream()
                .map(ind -> ind.getItemCode())
                .distinct()
                .collect(Collectors.toList());
        for (String city: cities.values()) {
            for (String exp: experience.values()) {
                for (HhDictionary spec: specialization) {
                    if (!"Информационные технологии, интернет, телеком".equals(spec.getGroup())) {
                        continue;
                    }
                    List<String> li;
                    if ("Программирование, Разработка".equals(spec.getItem()) || "Инженер".equals(spec.getItem())) {
                        li = listIndustries;
                    } else {
                        li = new ArrayList<>();
                        li.add(null);
                    }
                    for (String ind: li) {
                        String url = urlConstructor.createHhVacancyUrl(
                                spec.getItemCode(),
                                null,
                                city,
                                exp,
                                ind,
                                0,
                                perPage);
                        links.add(new DownloadableLink(url, sequenceNum, UrlHandler.HH_LIST_VACANCIES.name(), null));
                    }
                }
            }
        }
        return links;
    }
}
