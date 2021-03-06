package ru.statjobs.loader.url;

import ru.statjobs.loader.ClientConsts;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.dto.HhDictionary;
import ru.statjobs.loader.common.url.UrlTypes;

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
                    if (!ClientConsts.IT_GROUP_NAME.equals(spec.getGroup())) {
                        continue;
                    }
                    List<String> li;
                    if (ClientConsts.IT_IND_PROGRAMMER.equals(spec.getItem()) || ClientConsts.IT_IND_ENGINEER.equals(spec.getItem())) {
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
                        links.add(new DownloadableLink(url, sequenceNum, UrlTypes.HH_LIST_VACANCIES, null));
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
                if (!ClientConsts.IT_GROUP_NAME.equals(spec.getGroup())) {
                    continue;
                }
                String url = urlConstructor.createHhResumeListUrl(
                        spec.getItemCode(),
                        searchPeriod,
                        city,
                        0,
                        perPage);
                links.add(new DownloadableLink(url, sequenceNum, UrlTypes.HH_LIST_RESUME, null));
            }
        }
        return links;
    }

}
