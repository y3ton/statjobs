package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.HhSpecialization;

import java.util.List;
import java.util.Map;

public interface HhDictionaryDao {

    List<HhSpecialization> getSpecialization();

    Map<String, String> getCity();

    Map<String, String> getExperience();
}
