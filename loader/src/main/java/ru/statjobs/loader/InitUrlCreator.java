package ru.statjobs.loader;

import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.dto.HhDictionary;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InitUrlCreator {

    public List<DownloadableLink> initHhLink(
            Integer perPage,
            Map<String, String> cities,
            List<HhDictionary> specialization,
            Map<String, String> experience,
            List<HhDictionary> industries,
            UrlConstructor urlConstructor,
            int sequenceNum
    ) {
        return cities.values().stream()
                .map(city ->  specialization.stream()
                        .map(spec -> experience.values().stream()
                                .map(exp -> industries.stream()
                                        .map(ind -> urlConstructor.createHhVacancyUrl(
                                                spec.getCode(),
                                                null,
                                                city,
                                                exp,
                                                ind.getCode(),
                                                0,
                                                perPage))
                                        .map(url -> new DownloadableLink(url, sequenceNum, UrlHandler.HH_LIST_VACANCIES.name()))
                                )
                                .flatMap(l -> l))
                        .flatMap(l -> l))
                .flatMap(l -> l)
                .collect(Collectors.toList());
    }
}
