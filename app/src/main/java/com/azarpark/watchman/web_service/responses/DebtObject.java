package com.azarpark.watchman.web_service.responses;

/**
 * {
 *       "key": "freeway_debt",
 *       "value": 0,
 *       "id": -1,
 *       "name": "عوارض آزادراهی"
 *     }
 */
public class DebtObject {
    public String key;
    public int value;
    public int id;
    public String name;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getId() {return id;}

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
