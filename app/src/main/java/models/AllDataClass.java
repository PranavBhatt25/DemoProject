package models;

import java.util.ArrayList;

/**
 * Created by wpa6 on 29/8/16.
 */

public class AllDataClass {

    String id = "";
    String name = "";
    String description = "";
    String created_at = "";
    String is_Updated = "";

    public String getIs_Updated() {
        return is_Updated;
    }

    public void setIs_Updated(String is_Updated) {
        this.is_Updated = is_Updated;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }


}
