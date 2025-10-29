package com.dreamhouse.ai.llm.model.reply;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class HouseSearchReply implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("houses")
    private List<HouseAdDTO> houseAdDTOS;

    @JsonProperty("summary")
    private String summary;

    public HouseSearchReply() {
        /*
            This constructor has been intentionally left empty for
            object marshalling and serialization motives
        */
    }

    public List<HouseAdDTO> getHouseAdDTOS() {
        if  (houseAdDTOS == null) return Collections.emptyList();
        return List.copyOf(houseAdDTOS);
    }

    public void setHouseAdDTOS(List<HouseAdDTO> houseAdDTOS) {
        this.houseAdDTOS = houseAdDTOS;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
