package ru.statjobs.loader.dao;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ru.statjobs.loader.dto.HhDictionary;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;

import java.util.Map;

public class HhDictionaryDaoImplIT {

    @Test
    public void getCityTest() {
        HhDictionaryDao dao = new HhDictionaryDaoImpl(null, null);
        Map<String, String> map = dao.getCity();
        Assert.assertEquals("1", map.get("Москва"));
    }

    @Test
    public void getExperienceTest() {
        HhDictionaryDao dao = new HhDictionaryDaoImpl(null, null);
        Map<String, String> map = dao.getExperience();
        Assert.assertEquals("noExperience", map.get("Нет опыта"));
    }

    @Test
    public void getIndustriesTest() {
        JsonUtils jsonUtils = Mockito.spy(new JsonUtils());
        FileUtils fileUtils = Mockito.spy(new FileUtils());
        HhDictionaryDao dao = new HhDictionaryDaoImpl(jsonUtils, fileUtils);
        HhDictionary hh = dao.getIndustries().stream().filter(i -> i.getItemCode().equals("34.422")).findFirst().orElse(null);
        Assert.assertEquals("34.422", hh.getItemCode());
        Assert.assertEquals("Неорганическая химия (производство)", hh.getItem());
        Assert.assertEquals("34", hh.getGroupCode());
        Assert.assertEquals("Химическое производство, удобрения", hh.getGroup());
        Mockito.verify(jsonUtils, Mockito.times(1)).readString (Mockito.anyString());
        dao.getIndustries();
        Mockito.verify(jsonUtils, Mockito.times(1)).readString(Mockito.anyString());
    }

    @Test
    public void getSpecializationTest() {
        JsonUtils jsonUtils = Mockito.spy(new JsonUtils());
        FileUtils fileUtils = Mockito.spy(new FileUtils());
        HhDictionaryDao dao = new HhDictionaryDaoImpl(jsonUtils, fileUtils);
        HhDictionary hh = dao.getSpecialization().stream().filter(i -> i.getItemCode().equals("1.359")).findFirst().orElse(null);
        Assert.assertEquals("1.359", hh.getItemCode());
        Assert.assertEquals("Электронная коммерция", hh.getItem());
        Assert.assertEquals("1", hh.getGroupCode());
        Assert.assertEquals("Информационные технологии, интернет, телеком", hh.getGroup());
        Mockito.verify(jsonUtils, Mockito.times(1)).readString(Mockito.anyString());
        dao.getSpecialization();
        Mockito.verify(jsonUtils, Mockito.times(1)).readString(Mockito.anyString());
    }


}
