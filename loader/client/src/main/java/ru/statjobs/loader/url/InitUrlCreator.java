package ru.statjobs.loader.url;

import ru.statjobs.loader.Const;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.dto.HhDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InitUrlCreator {

    public List<DownloadableLink> initHhItVacancyLink(
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
                    if (!Const.IT_GROUP_NAME.equals(spec.getGroup())) {
                        continue;
                    }
                    List<String> li;
                    if (Const.IT_IND_PROGRAMMER.equals(spec.getItem()) || Const.IT_IND_ENGINEER.equals(spec.getItem())) {
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

    public List<DownloadableLink> initHhItResumeLink(
            UrlConstructor urlConstructor,
            int sequenceNum,
            Integer perPage,
            Map<String, String> cities,
            List<HhDictionary> specialization,
            Integer searchPeriod
    ) {
        List<DownloadableLink> links = new ArrayList<>();
        for (String city: cities.values()) {
            for (HhDictionary spec: specialization) {
                if (!Const.IT_GROUP_NAME.equals(spec.getGroup())) {
                    continue;
                }
                String url = urlConstructor.createHhResumeListUrl(
                        spec.getItemCode(),
                        searchPeriod,
                        city,
                        0,
                        perPage);
                links.add(new DownloadableLink(url, sequenceNum, UrlHandler.HH_LIST_RESUME.name(), null));
            }
        }
        return links;
    }

}
