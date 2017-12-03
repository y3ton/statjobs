package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.HhDictionary;

import java.util.List;
import java.util.Map;

public interface HhDictionaryDao {

    List<HhDictionary> getSpecialization();

    Map<String, String> getCity();

    Map<String, String> getExperience();

    List<HhDictionary> getIndustries();
}
