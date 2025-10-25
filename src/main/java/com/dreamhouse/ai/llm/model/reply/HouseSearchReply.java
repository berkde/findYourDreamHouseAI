package com.dreamhouse.ai.llm.model.reply;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class HouseSearchReply implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("houses")
    private List<HouseAdDTO> houseAdDTOS;

    @JsonProperty("summary")
    private String summary;

    public HouseSearchReply() {
    }

    public List<HouseAdDTO> getHouseAdDTOS() {
        return houseAdDTOS;
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
