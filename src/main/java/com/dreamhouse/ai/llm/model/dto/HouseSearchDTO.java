package com.dreamhouse.ai.llm.model.dto;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class HouseSearchDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("houses")
    private List<HouseAdDTO> houseAdDTOS;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("agentReply")
    private String agentReply;

    public HouseSearchDTO() {
        /*
            This constructor has been intentionally left empty for
            object marshalling and serialization motives
        */
    }
    public List<HouseAdDTO> getHouseAdDTOS() {
        return houseAdDTOS != null ? houseAdDTOS : Collections.emptyList();
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

    public String getAgentReply() {
        return agentReply;
    }

    public void setAgentReply(String agentReply) {
        this.agentReply = agentReply;
    }
}
