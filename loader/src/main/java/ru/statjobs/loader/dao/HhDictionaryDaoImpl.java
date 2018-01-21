package ru.statjobs.loader.dao;


import ru.statjobs.loader.dto.HhDictionary;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HhDictionaryDaoImpl implements HhDictionaryDao {

    public final static String FILE_SPECIALIZATIONS = "dictionary/specializations.json";
    public final static String FILE_INDUSTRIES = "dictionary/industries.json";

    private final static Map<String, String> EXPERIENCE = Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("Нет опыта", "noExperience"),
            new AbstractMap.SimpleEntry<>("От 1 года до 3 лет", "between1And3"),
            new AbstractMap.SimpleEntry<>("От 3 до 6 лет", "between3And6"),
            new AbstractMap.SimpleEntry<>("Более 6 лет", "moreThan6")
    ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    private final static  Map<String, String> AREAS = Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("Москва",          "1" ),
            new AbstractMap.SimpleEntry<>("Санкт-Петербург", "2"),
            new AbstractMap.SimpleEntry<>("Екатеринбург",    "3"),
            new AbstractMap.SimpleEntry<>("Новосибирск",     "4"),
            new AbstractMap.SimpleEntry<>("Казань",          "88"),
            new AbstractMap.SimpleEntry<>("Челябинск",       "104"),
            new AbstractMap.SimpleEntry<>("Киев",            "115"),
            new AbstractMap.SimpleEntry<>("Минск",           "1002"))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    private final JsonUtils jsonUtils;
    private final FileUtils fileUtils;

    private List<HhDictionary> specializations;
    private List<HhDictionary> industries;

    public HhDictionaryDaoImpl(JsonUtils jsonUtils, FileUtils fileUtils) {
        this.jsonUtils = jsonUtils;
        this.fileUtils = fileUtils;
    }

    @Override
    public Map<String, String> getCity() {
        return AREAS;
    }

    @Override
    public Map<String, String> getExperience() {
        return EXPERIENCE;
    }

    @Override
    public List<HhDictionary> getIndustries() {
        if (industries == null) {
            industries = loadDictionary(FILE_INDUSTRIES, "industries");
        }
        return industries;
    }

    @Override
    public List<HhDictionary> getSpecialization() {
        if (specializations == null) {
            specializations = loadDictionary(FILE_SPECIALIZATIONS, "specializations");
        }
        return specializations;
    }

    private List<HhDictionary> loadDictionary(String fileName, String groupItemName) {
        List<Map<String, Object>> list = jsonUtils.readString(fileUtils.readResourceFile(fileName));
        return list.stream()
                .map(mapGroupSpec -> ((List<Map<String, String>>)mapGroupSpec.get(groupItemName)).stream()
                        .map(specMap -> new HhDictionary(
                                (String)mapGroupSpec.get("name"),
                                (String)mapGroupSpec.get("id"),
                                specMap.get("name"),
                                specMap.get("id"))
                        )
                        .collect(Collectors.toList())
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
