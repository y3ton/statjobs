package ru.statjobs.loader.dao;



import ru.statjobs.loader.dto.HhSpecialization;
import ru.statjobs.loader.utils.JsonUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HhDictionaryDaoImpl implements HhDictionaryDao {

    public final static String FILE_SPECIALIZATIONS = "dictionary/specializations.json";

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

    private List<HhSpecialization> specializations;

    public HhDictionaryDaoImpl(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
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
    public List<HhSpecialization> getSpecialization() {
        if (specializations == null) {
            try {
                specializations = loadSpecialization();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return specializations;
    }

    private List<HhSpecialization> loadSpecialization() throws IOException {
        List<Map<String, Object>> list = jsonUtils.readResource(FILE_SPECIALIZATIONS);
        return list.stream()
                .map(mapGropSpec -> ((List<Map<String, String>>)mapGropSpec.get("specializations")).stream()
                        .map(specMap -> new HhSpecialization((String)mapGropSpec.get("name"), specMap.get("name"), specMap.get("id")))
                        .collect(Collectors.toList())
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }




}
