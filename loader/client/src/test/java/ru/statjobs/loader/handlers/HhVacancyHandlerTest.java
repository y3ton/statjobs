package ru.statjobs.loader.handlers;

import org.junit.Assert;
import org.junit.Test;
import ru.statjobs.loader.utils.JsonUtils;

public class HhVacancyHandlerTest {

    HhVacancyHandler handler = new HhVacancyHandler(null, null, null, new JsonUtils());

    private final String JSON1 = "{  \"alternate_url\": \"https://hh.ru/vacancy/666\",  \"code\": null,  \"premium\": false,  \"description\": \"что ТЫ-НАШ ЧЕЛОВЕК</strong></em></li> </ul>\",  \"test\": null,  \"schedule\": {    \"id\": \"fullDay\",    \"name\": \"Полный день\"  },  \"driver_license_types\": [],  \"suitable_resumes_url\": null,  \"site\": {    \"id\": \"hh\",    \"name\": \"hh.ru\"  },  \"billing_type\": {    \"id\": \"standard\",    \"name\": \"Стандарт\"  },  \"published_at\": \"2018-08-20T12:06:03+0300\",  \"accept_incomplete_resumes\": false,  \"accept_handicapped\": false,  \"experience\": {    \"id\": \"between1And3\",    \"name\": \"От 1 года до 3 лет\"  },  \"address\": {    \"building\": \"55/1\",    \"city\": \"Новосибирск\",    \"description\": null,    \"metro\": {      \"line_name\": \"Дзержинская\",      \"station_id\": \".304\",      \"line_id\": \"53\",      \"lat\": 45.043634,      \"station_name\": \"Маршала Покрышкина\",      \"lng\": 45.935566    },    \"metro_stations\": [      {        \"line_name\": \"Дзержинская\",        \"station_id\": \"53.304\",        \"line_id\": \"53\",        \"lat\": 45.043634,        \"station_name\": \"Маршала Покрышкина\",        \"lng\": 45.935566      }    ],    \"raw\": null,    \"street\": \"улица Ломоносова\",    \"lat\": 45.03944,    \"lng\": 45.934937  },  \"key_skills\": [    {      \"name\": \"Python\"    },    {      \"name\": \"Django Framework\"    },    {      \"name\": \"MySQL\"    },    {      \"name\": \"JavaScript\"    },    {      \"name\": \"jQuery\"    }  ],  \"allow_messages\": true,  \"employment\": {    \"id\": \"full\",    \"name\": \"Полная занятость\"  },  \"id\": \"000000\",  \"response_url\": null,  \"salary\": {    \"to\": null,    \"gross\": false,    \"from\": 80000,    \"currency\": \"RUR\"  },  \"archived\": false,  \"name\": \"Программист Python Developer\",  \"contacts\": null,  \"employer\": {    \"logo_urls\": {      \"90\": \"https://hhcdn.ru/employer-logo/.jpeg\",      \"240\": \"https://hhcdn.ru/employer-logo/.jpeg\",      \"original\": \"https://hhcdn.ru/employer-logo-original/.jpg\"    },    \"id\": \"706928\",    \"trusted\": true  },  \"created_at\": \"2018-08-20T12:06:03+0300\",  \"area\": {    \"url\": \"https://api.hh.ru/areas/4?host=hh.ru\",    \"id\": \"4\",    \"name\": \"Новосибирск\"  },  \"relations\": [], " +
            " \"branded_description\": \"branded_description_long\", " +
            " \"hidden\": false,  \"type\": {    \"id\": \"open\",    \"name\": \"Открытая\"  },  \"specializations\": [    {      \"profarea_id\": \"1\",      \"profarea_name\": \"Информационные технологии, интернет, телеком\",      \"id\": \"1.221\",      \"name\": \"Программирование, Разработка\"    },    {      \"profarea_id\": \"1\",      \"profarea_name\": \"Информационные технологии, интернет, телеком\",      \"id\": \"1.9\",      \"name\": \"Web инженер\"    }  ]}";

    private final String JSON2 = "{\"branded_description\":\"branded_description_long\",\"a\":{\"a1\":\"a11\",\"b1\":\"b11\"},\"arr\":[1,2,3],\"arr2\":[{\"c1\":\"c2\"},{\"c3\":\"c4\"}]}";

    @Test
    public void reduceMessageLengthTest() {
        check(JSON1);
        check(JSON2);
    }

    private void check(String json) {
        String newJson = handler.reduceMessageLength(json).replace(" ", "");
        String replacedJson = json.replace("branded_description_long","DEL").replace(" ", "");
        Assert.assertEquals(replacedJson, newJson);
    }


}
