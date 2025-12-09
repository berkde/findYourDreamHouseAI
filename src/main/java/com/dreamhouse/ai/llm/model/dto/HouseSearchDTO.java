package com.dreamhouse.ai.llm.model.dto;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class HouseSearchDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("houseAdDTOs")
    @JsonAlias({"houseAds"})
    private List<HouseAdDTO> houseAdDTOs = Collections.emptyList();


    @JsonProperty("agentReply")
    private String agentReply;

    public HouseSearchDTO() {
        /*
            This constructor has been intentionally left empty for
            object marshalling and serialization motives
        */
    }
    public List<HouseAdDTO> getHouseAdDTOs() {
        return houseAdDTOs;
    }

    public void setHouseAdDTOs(List<HouseAdDTO> houseAdDTOS) {
        this.houseAdDTOs = houseAdDTOS;
    }

    public String getAgentReply() {
        return agentReply;
    }

    public void setAgentReply(String agentReply) {
        this.agentReply = agentReply;
    }
}
